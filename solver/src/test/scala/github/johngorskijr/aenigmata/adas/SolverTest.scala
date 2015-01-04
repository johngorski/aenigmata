package github.johngorskijr.aenigmata.adas

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SolverTest extends FunSuite {
  type CountingPuzzle = Int

  test("count with heuristics") {
    val solver = new Solver[CountingPuzzle](
      1, List((cp: CountingPuzzle) => if (cp < 10) cp + 1 else cp))
    assert(solver.solution === 10)
  }
}
