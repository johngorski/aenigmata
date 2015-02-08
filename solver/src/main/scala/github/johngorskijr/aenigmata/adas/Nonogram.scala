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

  type Cell = Int
  val UNKNOWN: Cell = 0
  val OPEN: Cell = 1
  val FILLED: Cell = 2

  type State = Vector[Vector[Cell]]
  type Constraint = List[Int]

  type Constraints = (Vector[Constraint], Vector[Constraint])

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
   */
  def load(raw: String): Constraints = (
    Vector(3 :: 1 :: Nil, 5 :: Nil, 4 :: Nil, 1 :: Nil, 1 :: Nil),
    Vector(3 :: Nil, 3 :: Nil, 3 :: 1 :: Nil, 3 :: Nil, 2 :: Nil)
    )

  case class Puzzle(cells: State, constraints: Constraints) {

    def row(r: Int): (Vector[Cell], Constraint) = (cells(r),
      constraints match { case (rowConstraints, _) => rowConstraints(r) }
    )

    def col(c: Int): (Vector[Cell], Constraint) = (
      (for { i <- Range(0, cells.length) } yield cells(i)(c)).to[Vector],
      constraints match { case (_, colConstraints) => colConstraints(c) }
    )

    def withRow(r: Int, row: Vector[Cell]): Puzzle = Puzzle(cells.updated(r, row), constraints)

    def withCol(c: Int, col: Vector[Cell]): Puzzle = Puzzle(
      (for {
        i <- Range(0, col.length)
      } yield cells(i).updated(c, col(i))).to[Vector]
      ,
      constraints)
  }

  /**
   * All right. So. We sum up the constraints.
   * The constraint groups are spaced by at least one open cell.
   * So if the sum of the constraints with one extra for the "at least one more"
   * open cell is equal to the available cells, we should be able to fill everything.
   *
   * We could also fiddle with Constraints based on the filled state of our puzzle,
   * but that might be more in the realm of other heuristics, possibly with transformations.
   */
  def resolveBaseSegment(segment: Vector[Cell], constraint: Constraint): Vector[Cell] = {
    if (segment.contains(UNKNOWN) && constraint.sum + constraint.length - 1 != segment.length) segment
    else {
      def fenceposts(blocks: Constraint, index: Int, acc: Vector[Cell]): Vector[Cell] = blocks match {
        case Nil => acc
        case block :: tail =>
          val fp = index + block
          if (fp >= acc.length) acc
          else fenceposts(tail, fp + 1, acc.updated(fp, OPEN))
      }

      fenceposts(constraint, 0, segment) map (cell => if (cell == UNKNOWN) FILLED else cell)
    }
  }

  def checkBaseRow(r: Int): Puzzle => Puzzle = {
    p: Puzzle => {
      p.withRow(r, p.row(r) match { case (segment, constraint) => resolveBaseSegment(segment, constraint)})
    }
  }

  def checkBaseCol(c: Int): Puzzle => Puzzle = {
    p: Puzzle => {
      p.withCol(c, p.col(c) match { case (segment, constraint) => resolveBaseSegment(segment, constraint)})
    }
  }

  def fromConstraints(constraints: Constraints): Puzzle = constraints match {
    case (rowConstraints, colConstraints) =>
      def fill[Element](i: Int, stop: Int, element: Element, acc: => Vector[Element]): Vector[Element] =
        if (i >= stop) acc
        else fill(i + 1, stop, element, acc :+ element)

      val rowPrototype = fill(0, colConstraints.length, UNKNOWN, Vector())
      val cells = fill(0, rowConstraints.length, rowPrototype, Vector())

      Puzzle(cells, constraints)
  }

  def solverFor(puzzle: Puzzle): Solver[Puzzle] = {
    val baseRowChecks = Range(0, puzzle.cells.length) map checkBaseRow
    val baseColChecks = Range(0, puzzle.cells(0).length) map checkBaseCol
    val heuristics = baseRowChecks ++ baseColChecks

    new Solver(puzzle, heuristics.to[List])
  }
}
