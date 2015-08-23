package github.johngorskijr.aenigmata.adas

import github.johngorskijr.aenigmata.adas.Util.Location

object Politics {
  object Direction extends Enumeration {
    type Direction = Value
    val Up, Left, Down, Right = Value

    def from(c: Char): Direction = c match {
      case '^' => Up
      case '<' => Left
      case 'v' => Down
      case '>' => Right
    }

    def to(d: Direction): Char = d match {
      case Up => '^'
      case Left => '<'
      case Down => 'v'
      case Right => '>'
    }
  }
  import Direction._

  case class Constraint(location: Location, cellsToDarken: Int, direction: Direction) {
    override def toString: String = {
      val dirString = Vector.fill(cellsToDarken) { to(direction) }.mkString
      s"$location$dirString"
    }
  }

  case class Cell(location: Location, darkened: Option[Boolean])

  case class PoliticalPuzzle(cells: Vector[Vector[Cell]], constraints: Set[Constraint]) {
    lazy val numRows = cells.size
    lazy val numCols = cells.maxBy(_.size).size

    override def toString: String = {
      val updateFunctions: IndexedSeq[(Vector[Vector[String]]) => Vector[Vector[String]]] = for {
        i <- Range(0, numRows)
        j <- Range(0, numCols)
        d <- cells(i)(j).darkened
      } yield (grid: Vector[Vector[String]]) => grid.updated(i, grid(i).updated(j, gridMark(d)))

      val gridString: String = updateFunctions.foldLeft(emptyGrid) { (acc: Vector[Vector[String]], updater) =>
        updater.apply(acc)
      }.map(_.mkString("")).mkString("\n")

      List(gridString, constraintsString).mkString("\n")
    }
    private def constraintsString: String = constraints.toArray.sortBy(c => (c.location.row, c.location.col)).mkString("\n")
    private val cellString = "   "
    private val emptyGrid: Vector[Vector[String]] = Vector.tabulate(numRows, numCols) { (r, c) => cellString }
    private def gridMark(darkened: Boolean): String = {
      val mark = if (darkened) '*' else '_'
      cellString.updated(1, mark)
    }
  }

  def puzzleFrom(text: String, arrows: String): PoliticalPuzzle = {
    val textRows = text.split('\n')
    val numRows = textRows.length
    val numCols = textRows.maxBy(_.length).length

    val cells: Vector[Vector[Cell]] = Vector.tabulate(numRows, numCols) { (r, c) => Cell(Location(r, c), None) }

    val arrowRows = arrows.split('\n')

    val constraints = for {
      i <- Range(0, numRows)
      j <- Range(0, textRows(i).length)
      if textRows(i)(j).isDigit
    } yield Constraint(Location(i, j), textRows(i)(j).toString.toInt, Direction.from(arrowRows(i)(j)))

    PoliticalPuzzle(cells, constraints.toSet)
  }
}
