package github.johngorskijr.aenigmata.adas

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
   * 3 1
   *   5
   *   4
   *   1
   *   1
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
  def load(raw: String): Option[Constraints] = {
    Some()
  }

  class Puzzle(cells: State, rowConstraints: Vector[List[Int]], colConstraints: Vector[List[Int]]) {
  }
}
