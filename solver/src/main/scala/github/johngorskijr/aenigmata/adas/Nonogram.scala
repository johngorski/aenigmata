package github.johngorskijr.aenigmata.adas

import github.johngorskijr.aenigmata.adas.Solver

/**
 * http://www.goobix.com/games/nonograms/
 *
 * User: jgorski
 * Date: 1/31/2015
 * Time: 6:06 PM
 */

/**
 * A Nonogram cell has a solved state of either Filled or Open. Its default state is Unknown.
 */
object Nonogram {

  // object Cell extends Enumeration {
    // type State = Value
    // val Unknown, Open, Filled = Value
  // }

  // Until I figure out Enum use in Scala, I'll just use Int constants
  type Cell = Int
  val UNKNOWN: Cell = 0
  val OPEN: Cell = 1
  val FILLED: Cell = 2

  type State = Vector[Vector[Cell]]

  type Constraints = (Vector[List[Int]], Vector[List[Int]])

  /**
   * e.g.
   *
   *         3
   *     3 3 1 3 2
   * 3 1 x x x o x
   *   5 x x x x x
   *   4 x x x x o
   *   1 o o o x o
   *   1 o o x o o
   *
   * for our purposes would be represented as the string
   * ((3,1),(5),(4),(1),(1)),((3),(3),(3,1),(3),(2))
   *
   * So row constraints before column constraints
   * Left constraint before right constraint
   * Top constraint before bottom constraint
   *
   * @param raw
   * @return
   */
  def load(raw: String): Constraints = (
    Vector(3 :: 1 :: Nil, 5 :: Nil, 4 :: Nil, 1 :: Nil, 1 :: Nil),
    Vector(3 :: Nil, 3 :: Nil, 3 :: 1 :: Nil, 3 :: Nil, 2 :: Nil)
  )

  class Puzzle(c: State, constr: Constraints) {
    val cells = c
    val constraints = constr

    def withCells(cells: State): Puzzle =
      if (cells == this.cells) this
      else new Puzzle(cells, constraints)
  }

  /**
   * All right. So. We sum up the constraints.
   * The constraint groups are spaced by at least one open cell.
   * So if the sum of the constraints with one extra for the "at least one more"
   * open cell is equal to the available cells, we should be able to fill everything.
   *
   * We could also fiddle with Constraints based on the filled state of our puzzle,
   * but that might be more in the realm of other heuristics, possibly with transformations.
   *
   * @param row
   * @param constraint
   * @return
   */
  def resolveRow(row: Vector[Cell], constraint: List[Int]): Vector[Cell] = {
    if (constraint.sum + constraint.length - 1 != row.length) row
    else fullyConstrainedRow(constraint)
  }

  def fullyConstrainedRow(constraint: List[Int]): Vector[Cell] = {
    def fill(cnstr: List[Int], acc: List[Cell]): List[Cell] = cnstr match {
      case Nil => acc.reverse
      case head :: tail =>
    }

    fill(constraint, Nil).to[Vector]
  }

  /**
   * Look at each row in p.
   *
   * @param p
   * @return
   */
  def checkRows(p: Puzzle): Puzzle = {
    val rows = p.cells
    val rowConstraints = p.constraints match {
      case (forRows, _) => forRows
    }

    val range = Range(0, rows(0).length)
    val newRows = range map (r => resolveRow(rows(r), rowConstraints(r)))

    p.withCells(newRows)
  }

  def checkCols(p: Puzzle): Puzzle = {
    p
  }

  def solve: Puzzle = {
    val constraints = load("anything")
    val emptyRow = Vector(UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN)
    val cells = Vector(emptyRow, emptyRow, emptyRow, emptyRow, emptyRow)
    val p: Puzzle = new Puzzle(cells, constraints)
    val solver: Solver[Puzzle] = new Solver(p, checkRows :: Nil)
    solver.solution
  }
}
