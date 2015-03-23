package github.johngorskijr.aenigmata.adas

import scala.util.{Failure, Success, Try}

object SuperSudoku {
  type Cell = Set[Int]

  def printable: Cell => String = cell => if (cell.size != 1) "?" else cell.last.toString

  type PuzzleState = Vector[Vector[Cell]]

  case class Location(row: Int, col: Int) {
    override def toString = s"($row, $col)"
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

  case class IdentityConstraint(puzzle1Index: Int, puzzle1Location: Location, puzzle2Index: Int, puzzle2Location: Location)

  case class CompositePuzzle(puzzles: Vector[Puzzle], identityConstraints: Set[IdentityConstraint])

  def identityHeuristic(constraint: IdentityConstraint): CompositePuzzle => CompositePuzzle = {
    val i1 = constraint.puzzle1Index
    val i2 = constraint.puzzle2Index
    val loc1 = constraint.puzzle1Location
    val loc2 = constraint.puzzle2Location

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
  case class Dual(speculate: Puzzle => Puzzle, reject: Puzzle => Puzzle)

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
    val onePlyDuals = for {
      rowIndex <- p.indices
      colIndex <- p.indices
      // if p.state(rowIndex)(colIndex).size == 2
      if p.state(rowIndex)(colIndex).size > 1
      value <- p.state(rowIndex)(colIndex)
    } yield Dual(z => z.set(Location(rowIndex, colIndex), value), z => z.clear(Location(rowIndex, colIndex), value))

    // for the resulting states which are contradictory,
    val rejectors = for {
      dual <- onePlyDuals
      hypothetical = dual.speculate(p)
      if hypotheticalFails(hypothetical, hs)
    } yield dual.reject

    // the resulting puzzle should not consider those values
    rejectors.foldLeft(p)((z: Puzzle, reject: Puzzle => Puzzle) => reject(z))
  }
}
