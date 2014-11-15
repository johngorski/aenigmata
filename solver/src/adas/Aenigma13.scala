package adas

class Vertex(row: Int, col: Int) {
  def <(that: Vertex) = {
    if (row == that.row) col < that.col
    else row < that.row
  }
}

class Edge(pathPossible: Boolean, gapPossible: Boolean)
class Square {
  case class Key(letter: Char)
  case class Constraint(number: Int)
}

class Aenigma13(grid: Map[(Vertex, Vertex), Edge],
                constraints: Map[(Int, Int), Int],
                keys: Map[(Int, Int), Char]) {
  def this(chars: Vector[Vector[Char]]) = {
    // Build up a grid from just the characters in the squares.
    // Each vertex should be represented exactly once.
    // Each edge should be represented exactly once.
    // Each square should be represented exactly once.
    // Edges should be queryable by vertex.
    // Squares should be queryable by edge or vertex.
    val rows = chars.length
    val cols = chars(0).length

    val constraints = (for {
      r <- 0 until (rows + 1)
      c <- 0 until (cols + 1)
      if chars(r)(c).isDigit
    } yield (r, c) -> chars(r)(c).toInt).toMap

    val keys = (for {
      r <- 0 until (rows + 1)
      c <- 0 until (cols + 1)
      if !chars(r)(c).isDigit
    } yield (r, c) -> chars(r)(c)).toMap

    val origin = new Vertex(0, 0)
    val oneRight = new Vertex(0, 1)
    val oneDown = new Vertex(1, 0)
    val gridStart: List[((Vertex, Vertex), Edge)] = List((origin, oneRight) -> new Edge(true, true), (origin, oneDown) -> new Edge(true, true))

    val gridVerticals: List[((Vertex, Vertex), Edge)] = for {
      r <- 0 until rows
      c <- 0 until cols
    } yield (new Vertex(r, c + 1), new Vertex(r + 1, c + 1) -> new Edge(true, true))

    val gridHorizontals: List[((Vertex, Vertex), Edge)] = for {
      r <- 0 until rows
      c <- 0 until cols
    } yield (new Vertex(r + 1, c), new Vertex(r + 1, c + 1)) -> new Edge(true, true)

    val grid: List[((Vertex, Vertex), Edge)] = gridStart ::: gridVerticals ::: gridHorizontals

    this(grid, constraints, keys)
  }
}
