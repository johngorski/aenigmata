package github.johngorskijr.aenigmata.adas

import scala.util.{Failure, Success, Try}

object SuperSudoku {
  type Cell = Set[Int]

  def printable: Cell => String = cell => if (cell.size != 1) "?" else cell.last.toString

  type PuzzleState = Vector[Vector[Cell]]

  case class Location(row: Int, col: Int) {
    override def toString = s"($row, $col)"

    def + (that: Location) = Location(this.row + that.row, this.col + that.col)
  }

  case class NumConstraint(cells: (Location, Location), requiredDelta: Int)
  case class LtConstraint(lesserCell: Location, greaterCell: Location)

  case class Puzzle(state: PuzzleState, numericConstraints: Set[NumConstraint], ltConstraints: Set[LtConstraint]) {
    def cellAt(location: Location): Cell = location match {
      case Location(row, col) => state(row)(col)
    }
    override def toString = tinyBoard + "\n" + qContents

    def tinyBoard = (
      for {
        row <- state
      } yield (row map printable).mkString
      ).mkString("\n")

    def cellToString: Cell => String = c =>
      if (c.size == 1) c.last.toString
      else (values map (v => if (c(v)) "." else " ")).mkString("")

    def qContents: String = (for {
      (row, rowIndex) <- state.zipWithIndex
      (cell, colIndex) <- row.zipWithIndex
      if cellAt(Location(rowIndex, colIndex)).size != 1
    } yield Location(rowIndex, colIndex) + ": " + cellToString(cellAt(Location(rowIndex, colIndex)))
      ).mkString("\n")

    def row(index: Int): Vector[Cell] = state(index)

    def col(index: Int): Vector[Cell] = indices.map(state(_)(index)).toVector

    def withCell(location: Location, cell: Cell): Puzzle = location match {
      case Location(row, col) =>
        Puzzle(state.updated(row, state(row).updated(col, cell)),
          numericConstraints, ltConstraints)
    }

    def withCell(location: Location, cellTransform: Cell => Cell): Puzzle =
      withCell(location, cellTransform(cellAt(location)))
    
    def withRow(rowIndex: Int)(row: Vector[Cell]): Puzzle = Puzzle(state.updated(rowIndex, row), numericConstraints, ltConstraints)
    
    def withCol(colIndex: Int)(col: Vector[Cell]): Puzzle =
      indices.foldLeft(this)((p, rowIndex) => p.withRow(rowIndex)(p.row(rowIndex).updated(colIndex, col(rowIndex))))

    lazy val size = state.length
    lazy val indices = 0 until size
    lazy val values = 1 to size

    def set(location: Location, value: Int) = withCell(location, Set(value))

    def clear(location: Location, value: Int) = withCell(location, _ filter(v => v != value))

    def clearCross(origin: Location, value: Int): Puzzle = origin match {
      case Location(oRow, oCol) =>
        val horizontals = for {
          col <- indices
          if col != oCol
          if cellAt(Location(oRow, col)) contains value
        } yield Location(oRow, col)

        val verticals = for {
          row <- indices
          if row != oRow
          if cellAt(Location(row, oCol)) contains value
        } yield Location(row, oCol)

        (horizontals ++ verticals).foldLeft(this)((p: Puzzle, l: Location) => p.clear(l, value))
    }
  }

  def freshPuzzle(size: Int): PuzzleState = {
    val values = 1 to size
    val freshCell: Cell = values.toSet
    val freshRow: Vector[Cell] = (values map (_ => freshCell)).toVector
    (values map (_ => freshRow)).toVector
  }

  // heuristic based on a numeric constraint
  def numHeuristic(constraint: NumConstraint): Puzzle => Puzzle = constraint match {
    case NumConstraint((locationA, locationB), delta) => (p: Puzzle) => {
      val cellA = p.cellAt(locationA)
      val cellB = p.cellAt(locationB)
      val remainingA = cellA.filter(a => cellB.exists(b => (a - b).abs == delta))
      val remainingB = cellB.filter(b => remainingA.exists(a => (a - b).abs == delta))
      p.withCell(locationA, remainingA).withCell(locationB, remainingB)
    }
  }

  def ltHeuristic(constraint: LtConstraint): Puzzle => Puzzle = constraint match {
    case LtConstraint(ltLocation, gtLocation) => (p: Puzzle) => {
      val ltCell = p.cellAt(ltLocation)
      val gtCell = p.cellAt(gtLocation)
      val ltRemaining = ltCell.filter(lt => gtCell.exists(gt => lt < gt))
      val gtRemaining = gtCell.filter(gt => ltCell.exists(lt => gt > lt))
      p.withCell(ltLocation, ltRemaining).withCell(gtLocation, gtRemaining)
    }
  }

  // there can be only one copy of a number per row or column
  def crossHeuristic: Puzzle => Puzzle = p => {
    val singletonLocations = for {
      row <- p.indices
      col <- p.indices
      if p.cellAt(Location(row, col)).size == 1
    } yield Location(row, col)

    singletonLocations.foldLeft(p)((puz: Puzzle, loc: Location) => puz.clearCross(loc, puz.cellAt(loc).last))
  }

  // each number must appear in each row and column
  def pigeonholeHeuristic: Puzzle => Puzzle = p => {
    val pigeons = for {
      row <- p.indices
      col <- p.indices
      if p.cellAt(Location(row, col)).size > 1
      value <- p.cellAt(Location(row, col))
      if !p.indices.exists(rowIndex => rowIndex != row && p.cellAt(Location(rowIndex, col)).contains(value)) ||
         !p.indices.exists(colIndex => colIndex != col && p.cellAt(Location(row, colIndex)).contains(value))
    } yield (Location(row, col), value)

    pigeons.foldLeft(p)((puz: Puzzle, pigeon: (Location, Int)) => pigeon match {
      case (l, v) => puz.set(l, v)
    })
  }

  // if there are two cells in a row or column that are each limited to one of two values, remove those values from
  // consideration in the other rows or columns
  def rayHeuristic(extractRay: Puzzle => Vector[Cell], replaceRay: (Puzzle, Vector[Cell]) => Puzzle): Puzzle => Puzzle = p => {
    val ray = extractRay(p)
    replaceRay(p, updateRay(ray))
  }

  // if there are two cells in a ray that are each limited to one of two values, remove those values from
  // consideration in the other rays
  def updateRay(ray: Vector[Cell]): Vector[Cell] = {
    val pairs = for {
      i <- 0 until (ray.size - 1)
      if ray(i).size == 2
      j <- (i + 1) until ray.size
      if ray(i) == ray(j)
    } yield (i, j, ray(i))

    pairs.foldLeft(ray)((cells: Vector[Cell], pair: (Int, Int, Cell)) => pair match {
      case (n, m, c) => cells.map(cell => cell -- c).updated(n, c).updated(m, c)
    })
  }

  def rayHeuristics(puzzle: Puzzle): List[Puzzle => Puzzle] = {
    val rowUpdaters: List[Puzzle => Puzzle] = puzzle.indices.map(i => {
      val rowExtractor: Puzzle => Vector[Cell] = (p: Puzzle) => p.row(i)
      val rowUpdater: (Puzzle, Vector[Cell]) => Puzzle = (p: Puzzle, row: Vector[Cell]) => p.withRow(i)(row)
      rayHeuristic(rowExtractor, rowUpdater)
    }).toList
    val colUpdaters: List[Puzzle => Puzzle] = puzzle.indices.map(i => {
      val colExtractor: Puzzle => Vector[Cell] = (p: Puzzle) => p.col(i)
      val colUpdater: (Puzzle, Vector[Cell]) => Puzzle = (p: Puzzle, col: Vector[Cell]) => p.withCol(i)(col)
      rayHeuristic(colExtractor, colUpdater)
    }).toList
    rowUpdaters ::: colUpdaters
  }

  case class IdentityConstraint(puzzle1Coordinate: Coordinate, puzzle2Coordinate: Coordinate)

  case class CompositePuzzle(puzzles: Vector[Puzzle], identityConstraints: Set[IdentityConstraint]) {
    def cellAt(coordinate: Coordinate): Cell = puzzles(coordinate.puzzleIndex).cellAt(coordinate.location)

    def withCell(c: Coordinate, cell: Cell): CompositePuzzle =
      withPuzzle(c.puzzleIndex, puzzles(c.puzzleIndex).withCell(c.location, cell))

    def withPuzzle(index: Int, puzzle: Puzzle) =
      CompositePuzzle(puzzles.updated(index, puzzle), identityConstraints)
  }

  def identityHeuristic(constraint: IdentityConstraint): CompositePuzzle => CompositePuzzle = {
    val i1 = constraint.puzzle1Coordinate.puzzleIndex
    val i2 = constraint.puzzle2Coordinate.puzzleIndex
    val loc1 = constraint.puzzle1Coordinate.location
    val loc2 = constraint.puzzle2Coordinate.location

    cp: CompositePuzzle => {
      val puzzles = cp.puzzles
      val cell1 = puzzles(i1).cellAt(loc1)
      val cell2 = puzzles(i2).cellAt(loc2)
      val cell: Cell = cell1 intersect cell2
      val puzzle1 = puzzles(i1).withCell(loc1, cell)
      val puzzle2 = puzzles(i2).withCell(loc2, cell)
      val updatedPuzzles = puzzles.updated(i1, puzzle1).updated(i2, puzzle2)
      CompositePuzzle(updatedPuzzles, cp.identityConstraints)
    }
  }

  def subPuzzleHeuristic: CompositePuzzle => CompositePuzzle = c => {
    val solutions: Vector[Puzzle] = c.puzzles.map(p => {
      val heuristics = crossHeuristic :: pigeonholeHeuristic :: rayHeuristics(p) :::
        (p.numericConstraints map numHeuristic).toList :::
        (p.ltConstraints map ltHeuristic).toList
      new Solver[Puzzle](p,
        heuristics ::: List(contradictionHeuristic(heuristics))
      ).solution
    })
    CompositePuzzle(solutions, c.identityConstraints)
  }

  /**
   * @param speculate gives the speculative form of a puzzle
   * @param reject gives the form of the puzzle if the speculation must be rejected
   */
  case class Speculator(speculate: Puzzle => Puzzle, reject: Puzzle => Puzzle)

  def contradiction(p: Puzzle): Boolean = p.state.flatten.exists(cell => cell.size < 1)

  def solve(p: Puzzle, hs: List[Puzzle => Puzzle]): Puzzle = new Solver(p, hs).solution

  def hypotheticalFails(hypothetical: Puzzle, hs: List[Puzzle => Puzzle]): Boolean = {
    Try(contradiction(solve(hypothetical, hs))) match {
      case Success(b) => b
      case Failure(_) => true
    }
  }

  // TODO: Contradiction heuristic. Take a puzzle, assume a direction as an answer, solve, and if the solution contains
  // a contradiction (cell with an empty set), remove the hypothesized value from consideration. Otherwise, don't.
  // IMPORTANT: Don't include the contradiction heuristic when checking the feasibility of a contradiction heuristic!
  def contradictionHeuristic(hs: List[Puzzle => Puzzle]): Puzzle => Puzzle = p => {
    // Out of the possible pairs of speculation duals and resulting solved puzzle states based on given heuristics,
    val singlePlySpeculators = for {
      rowIndex <- p.indices
      colIndex <- p.indices
      if p.state(rowIndex)(colIndex).size > 1
      value <- p.state(rowIndex)(colIndex)
    } yield Speculator(z => z.set(Location(rowIndex, colIndex), value), z => z.clear(Location(rowIndex, colIndex), value))

    // for the resulting states which are contradictory,
    val rejectors = for {
      dual <- singlePlySpeculators
      hypothetical = dual.speculate(p)
      if hypotheticalFails(hypothetical, hs)
    } yield dual.reject

    // the resulting puzzle should not consider those values
    rejectors.foldLeft(p)((z: Puzzle, reject: Puzzle => Puzzle) => reject(z))
  }

  case class SharedSpace(left: Vector[Cell], shared: Vector[Cell], right: Vector[Cell]) {
    def leftPuzzleRow: Vector[Cell] = left ++: shared
    def rightPuzzleRow: Vector[Cell] = shared ++: right
  }

  // TODO: Massive cleanup of numerous shoddy assumptions
  def extractShared(constraints: Set[IdentityConstraint]): CompositePuzzle => SharedSpace = {
    val leftPuzzleIndex = constraints.last.puzzle1Coordinate.puzzleIndex
    val rightPuzzleIndex = constraints.last.puzzle2Coordinate.puzzleIndex
    // sloppy hard-coding of 3 = rows, 2 = columns
    if (constraints.size == 3) {
      val leftRowIndex = constraints.last.puzzle1Coordinate.location.row
      val leftCols = constraints.map(c => c.puzzle1Coordinate.location).map(l => l.col)

      val rightRowIndex = constraints.last.puzzle2Coordinate.location.row
      val rightCols = constraints.map(c => c.puzzle2Coordinate.location).map(l => l.col)

      cp: CompositePuzzle => {
        val leftPuzzle = cp.puzzles(leftPuzzleIndex)
        val rightPuzzle = cp.puzzles(rightPuzzleIndex)

        val leftPuzzleRow = leftPuzzle.row(leftRowIndex)
        val leftRow: Vector[Cell] = leftPuzzle.indices.filter(i => i < leftCols.min || i > leftCols.max).map(i => leftPuzzleRow(i)).toVector
        val leftSharedRow: Vector[Cell] = leftPuzzle.indices.filter(i => i >= leftCols.min && i <= leftCols.max).map(i => leftPuzzleRow(i)).toVector

        val rightPuzzleRow = rightPuzzle.row(rightRowIndex)
        val rightRow: Vector[Cell] = rightPuzzle.indices.filter(i => i < rightCols.min || i > rightCols.max).map(i => rightPuzzleRow(i)).toVector

        SharedSpace(leftRow, leftSharedRow, rightRow)
      }
    } else if (constraints.size == 2) {
      val leftColIndex = constraints.last.puzzle1Coordinate.location.col
      val leftRows = constraints.map(c => c.puzzle1Coordinate.location).map(l => l.row)

      val rightColIndex = constraints.last.puzzle2Coordinate.location.col
      val rightRows = constraints.map(c => c.puzzle2Coordinate.location).map(l => l.row)

      cp: CompositePuzzle => {
        val leftPuzzle = cp.puzzles(leftPuzzleIndex)
        val rightPuzzle = cp.puzzles(rightPuzzleIndex)

        val leftPuzzleCol = leftPuzzle.col(leftColIndex)
        val leftCol: Vector[Cell] = leftPuzzle.indices.filter(i => i < leftRows.min || i > leftRows.max).map(i => leftPuzzleCol(i)).toVector
        val leftSharedCol: Vector[Cell] = leftPuzzle.indices.filter(i => i >= leftRows.min && i <= leftRows.max).map(i => leftPuzzleCol(i)).toVector

        val rightPuzzleCol = rightPuzzle.col(rightColIndex)
        val rightCol: Vector[Cell] = rightPuzzle.indices.filter(i => i < rightRows.min || i > rightRows.max).map(i => rightPuzzleCol(i)).toVector

        SharedSpace(leftCol, leftSharedCol, rightCol)
      }
    } else ??? // A sure sign that this code sucks. TODO
  }

  def replaceShared(constraints: Set[IdentityConstraint]): (CompositePuzzle, SharedSpace) => CompositePuzzle = {
    val leftPuzzleIndex = constraints.last.puzzle1Coordinate.puzzleIndex
    val rightPuzzleIndex = constraints.last.puzzle2Coordinate.puzzleIndex

    (cp: CompositePuzzle, space: SharedSpace) => {
      val leftPuzzle = cp.puzzles(leftPuzzleIndex)
      val rightPuzzle = cp.puzzles(rightPuzzleIndex)

      // sloppy hard-coding of 3 = rows, 2 = columns
      if (constraints.size == 3) {
        val leftRowIndex = constraints.last.puzzle1Coordinate.location.row
        val updatedLeftPuzzle: Puzzle = leftPuzzle.withRow(leftRowIndex)(space.left ++: space.shared)

        val rightRowIndex = constraints.last.puzzle2Coordinate.location.row
        val updatedRightPuzzle: Puzzle = rightPuzzle.withRow(rightRowIndex)(space.shared ++: space.right)

        CompositePuzzle(cp.puzzles.updated(leftPuzzleIndex, updatedLeftPuzzle).updated(rightPuzzleIndex, updatedRightPuzzle),
          cp.identityConstraints
        )
      } else if (constraints.size == 2) {
        val leftColIndex = constraints.last.puzzle1Coordinate.location.col
        val updatedLeftPuzzle: Puzzle = leftPuzzle.withCol(leftColIndex)(space.left ++: space.shared)

        val rightColIndex = constraints.last.puzzle2Coordinate.location.col
        val updatedRightPuzzle: Puzzle = rightPuzzle.withCol(rightColIndex)(space.shared ++: space.right)

        CompositePuzzle(cp.puzzles.updated(leftPuzzleIndex, updatedLeftPuzzle).updated(rightPuzzleIndex, updatedRightPuzzle),
          cp.identityConstraints
        )
      } else ??? // A sure sign that this code sucks. TODO
    }
  }

  // Hint: If a value must appear within a certain row or column of shared space,
  //       it must not appear in the linked non-shared rows/columns
  def sharedSpaceHeuristic(extract: CompositePuzzle => SharedSpace, replace: (CompositePuzzle, SharedSpace) => CompositePuzzle): CompositePuzzle => CompositePuzzle = cp => {
    val space: SharedSpace = extract(cp)
    val propagation: SharedSpace = shareSpace(space)
    if (space == propagation) cp
    else replace(cp, propagation)
  }

  def shareSpace(original: SharedSpace): SharedSpace = {
    val flatLeft = original.left.flatten.toSet
    val flatRight = original.right.flatten.toSet
    val sharedOnly = original.shared.flatten.filter(v => !flatLeft(v) || !flatRight(v))

    def resolveLinked(linked: Vector[Cell], toRemove: Int): Vector[Cell] = {
      linked.map(cell => cell - toRemove)
    }

    val left = sharedOnly.foldLeft(original.left)(resolveLinked)

    val right = sharedOnly.foldLeft(original.right)(resolveLinked)

    SharedSpace(left, original.shared, right)
  }

  case class Coordinate(puzzleIndex: Int, location: Location) {
    def + (delta: Location) = Coordinate(puzzleIndex, location + delta)
  }

  def overlapForSize(rows: Int, cols: Int): (Coordinate, Coordinate) => Overlap =
    (c1: Coordinate, c2: Coordinate) => Overlap(rows, cols, c1, c2)

  case class Overlap(rows: Int, cols: Int, topLeftPuzzle1: Coordinate, topLeftPuzzle2: Coordinate) {
    def identityConstraints: Set[IdentityConstraint] = (for {
      rowCursor <- 0 until rows
      colCursor <- 0 until cols
      cursor = Location(rowCursor, colCursor)
    } yield IdentityConstraint(topLeftPuzzle1 + cursor, topLeftPuzzle2 + cursor)).toSet

    /** Propagators are (extract, replace) pairs of functions for extracting SharedSpaces from CompositePuzzles and
      * replacing them.
      *
      * We can get propagators by choosing puzzles as Left and Right, identifying the Shared overlap,
      * and then computing based on their intersection and disjunction.
      * This is still makes the inelegant assumption that the Overlaps make sense given the starting
      * locations in each puzzle.
      *
      * There is a further inelegant assumption that row shared spaces have left = less than puzzle index and
      * column shared spaces have left (top) = even puzzle index.
      *
      * TODO: This inelegance would probably disappear if we give each subpuzzle a global grid origin.
      */
    def propagators: Set[Propagator] = rowPropagators ++: colPropagators

    def rowPropagators: Set[Propagator] = {
      val propagators = for {
        rowCursor <- 0 until rows
      } yield Propagator((cp: CompositePuzzle) => {
          // Assumption is that lower puzzle index comes first and that this maps to puzzles left of
          // the other.
          // TODO: Encode this explicitly rather than torturing brain cells.
          val leftPuzzle: Puzzle = cp.puzzles(topLeftPuzzle1.puzzleIndex)
          val leftRow: Vector[Cell] = leftPuzzle.row(topLeftPuzzle1.location.row + rowCursor)

          val left: Vector[Cell] = leftRow.take(leftRow.size - cols)
          val shared: Vector[Cell] = leftRow.takeRight(cols)

          val rightPuzzle: Puzzle = cp.puzzles(topLeftPuzzle2.puzzleIndex)
          val rightRow: Vector[Cell] = rightPuzzle.row(topLeftPuzzle2.location.row + rowCursor)

          val right: Vector[Cell] = rightRow.slice(cols, rightRow.size)

          SharedSpace(left, shared, right)
        }, (cp: CompositePuzzle, space: SharedSpace) => {
          val leftPuzzle: Puzzle = cp.puzzles(topLeftPuzzle1.puzzleIndex).withRow(topLeftPuzzle1.location.row + rowCursor)(space.leftPuzzleRow)
          val rightPuzzle: Puzzle = cp.puzzles(topLeftPuzzle2.puzzleIndex).withRow(topLeftPuzzle2.location.row + rowCursor)(space.rightPuzzleRow)
          cp.withPuzzle(topLeftPuzzle1.puzzleIndex, leftPuzzle).withPuzzle(topLeftPuzzle2.puzzleIndex, rightPuzzle)
        })
      propagators.toSet
    }

    def colPropagators: Set[Propagator] = {
      val propagators = for {
        colCursor <- 0 until cols
      } yield Propagator((cp: CompositePuzzle) => {
          // Assumption is that even puzzle indices are "left" (above) odd puzzle indices.
          val leftCoordinate: Coordinate =
            if (topLeftPuzzle1.puzzleIndex % 2 == 0) topLeftPuzzle1
            else topLeftPuzzle2

          val rightCoordinate: Coordinate =
            if (topLeftPuzzle1.puzzleIndex % 2 == 0) topLeftPuzzle2
            else topLeftPuzzle1

          val leftPuzzle: Puzzle = cp.puzzles(leftCoordinate.puzzleIndex)
          val leftCol: Vector[Cell] = leftPuzzle.col(leftCoordinate.location.col + colCursor)

          val left: Vector[Cell] = leftCol.take(leftCol.size - rows)
          val shared: Vector[Cell] = leftCol.takeRight(rows)

          val rightPuzzle: Puzzle = cp.puzzles(rightCoordinate.puzzleIndex)
          val rightCol: Vector[Cell] = rightPuzzle.col(rightCoordinate.location.col + colCursor)

          val right: Vector[Cell] = rightCol.slice(rows, rightCol.size)

          SharedSpace(left, shared, right)
        }, (cp: CompositePuzzle, space: SharedSpace) => {
          // Assumption is that even puzzle indices are "left" (above) odd puzzle indices.
          val leftCoordinate: Coordinate =
            if (topLeftPuzzle1.puzzleIndex % 2 == 0) topLeftPuzzle1
            else topLeftPuzzle2

          val rightCoordinate: Coordinate =
            if (topLeftPuzzle1.puzzleIndex % 2 == 0) topLeftPuzzle2
            else topLeftPuzzle1

          val leftPuzzle: Puzzle = cp.puzzles(leftCoordinate.puzzleIndex).withCol(leftCoordinate.location.col + colCursor)(space.leftPuzzleRow)
          val rightPuzzle: Puzzle = cp.puzzles(rightCoordinate.puzzleIndex).withCol(rightCoordinate.location.col + colCursor)(space.rightPuzzleRow)
          cp.withPuzzle(topLeftPuzzle1.puzzleIndex, leftPuzzle).withPuzzle(topLeftPuzzle2.puzzleIndex, rightPuzzle)
      })

      propagators.toSet
    }
  }

  case class Propagator(extract: CompositePuzzle => SharedSpace, replace: (CompositePuzzle, SharedSpace) => CompositePuzzle)
}
