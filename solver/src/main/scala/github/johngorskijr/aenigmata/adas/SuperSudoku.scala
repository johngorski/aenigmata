package github.johngorskijr.aenigmata.adas

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

    def row(index: Int) = state(index)

    def col(index: Int) = indices.map(state(_)(index))

    def withCell(location: Location, cell: Cell): Puzzle = location match {
      case Location(row, col) =>
        Puzzle(state.updated(row, state(row).updated(col, cell)),
          numericConstraints, ltConstraints)
    }

    def withCell(location: Location, cellTransform: Cell => Cell): Puzzle =
      withCell(location, cellTransform(cellAt(location)))

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
}
