package github.johngorskijr.aenigmata.adas

/**
 * Functional programming approximation of my general puzzle-solving approach.
 *
 * Created by jgorski on 11/6/14.
 */
class Solver[Puzzle](puzzle: Puzzle, heuristics: List[(Puzzle => Puzzle)]) {
  type Heuristic = Puzzle => Puzzle

  /**
   * This is better as a def, not a val. If it were a val, the github.johngorskijr.aenigmata.adas.Solver object
   * would hold references to all steps when all that may have been requested
   * was the solution. This would be unfortunate.
   *
   * @return A stream of changed Puzzle states as heuristics succeed in making progress.
   */
  def steps: Stream[Puzzle] = puzzle #:: nextStep(puzzle, heuristics, progress = false)

  def nextStep(p: Puzzle, hs: List[Heuristic], progress: Boolean): Stream[Puzzle] = hs match {
    case List() => if (!progress || heuristics.isEmpty) Stream()
      else nextStep(p, heuristics, progress = false)
    case h :: hTail =>
      val next = h(p)
      if (next == p) nextStep(p, hTail, progress)
      else next #:: nextStep(next, hs, progress = true)
  }

  /**
   * Implementing solution as a lazy evaluation of the last step in our stream is convenient,
   * but we can't parallelize our work this way since Streams are sequential.
   *
   * In order to parallelize the work, we need to use my original approach (the Stream approach
   * was invented so that I could easily show work).
   * 
   * I feel as though there's some repetition between the stream construction and the original
   * approach. Let's see if I can avoid repeating myself.
   */
  // lazy val solution = steps.last

  /**
   * @return The result of milking every last drop of Heuristic h from Puzzle p.
   */
  def apply(p: Puzzle, h: Heuristic): Puzzle = {
    val updated = h(p)
    if (updated == p) updated
    else apply(updated, h)
  }
  
  def applyAll(p: Puzzle, hs: List[Heuristic]): Puzzle = hs match {
    case List() => p
    case h :: hTail => applyAll(apply(p, h), hTail)
  }

  /**
   * We should be able to apply mapReduce instead:
   * map all heuristics to the result of applying them to puzzle,
   * then combine the answers.
   *
   * Repeat as long as the result of the mapReduce is different from the Puzzle fed into the mapReduce.
   *
   * This assumes:
   *  0. Puzzles have a partial ordering O from their initial state to their solved state.
   *  1. There exists an associative, commutative operator + for combining Puzzles.
   *     - TODO: Enforce via bounded generics.
   *  2. All heuristics yield puzzle states that are monotonically increasing given partial ordering O.
   *     - TODO: Enforce via automatically included tests run against every github.johngorskijr.aenigmata.adas.Solver/Puzzle implementation.
   *  3. No Heuristic makes a mistake or guesses. Deductions only!
   *  4. + is a monotonically increasing operation w.r.t. the ordering referenced as O.
   */
  lazy val solution = apply(puzzle, p => applyAll(p, heuristics))
}

object Util {
  lazy val charOf: Map[Int, Char] = ((1 to 26) zip ('a' to 'z')).toMap.updated(0, 'z')
  def numOf(c: Char): Int = c.toLower - 'a' + 1

  def advanceChar(c: Char, n: Int): Char = charOf((c.toLower - 'a' + n) % 26 + numOf('a'))

  def runningTotal(input: List[Int]): List[Int] = {
    def runningTotal(input: List[Int], total: Int, accumulator: List[Int]): List[Int] = input match {
      case List() => accumulator.reverse
      case head :: tail => {
        val sum = head + total
        runningTotal(tail, sum, sum :: accumulator)
      }
    }

    runningTotal(input, 0, List())
  }

  case class Location(row: Int, col: Int) {
    override def toString = s"($row, $col)"

    def + (that: Location) = Location(this.row + that.row, this.col + that.col)
  }
}
