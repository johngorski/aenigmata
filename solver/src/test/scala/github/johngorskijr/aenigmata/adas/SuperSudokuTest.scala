package github.johngorskijr.aenigmata.adas

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import github.johngorskijr.aenigmata.adas.SuperSudoku._

@RunWith(classOf[JUnitRunner])
class SuperSudokuTest extends FunSuite {
  test("solve sample") {

    val sampleNumConstraint = NumConstraint((Location(2, 3), Location(2, 4)), 3)
    val sampleLtConstraints = Set(
        LtConstraint(Location(0, 1), Location(0, 0)),
        LtConstraint(Location(0, 3), Location(0, 2)),
        LtConstraint(Location(1, 1), Location(0, 1)),
        LtConstraint(Location(2, 0), Location(1, 0)),
        LtConstraint(Location(2, 2), Location(1, 2)),
        LtConstraint(Location(2, 1), Location(2, 0)),
        LtConstraint(Location(3, 3), Location(3, 2)),
        LtConstraint(Location(4, 1), Location(4, 2))
      )

    val sample = Puzzle(
      freshPuzzle(5),
      Set(sampleNumConstraint),
      sampleLtConstraints
    )

    val solver = new Solver[Puzzle](
      sample,
      List(
        crossHeuristic,
        pigeonholeHeuristic,
        numHeuristic(sampleNumConstraint)
      ) ::: (sampleLtConstraints map ltHeuristic).toList
    )

    // println(solver.solution)
    solver.steps.foreach(p => println("-----\n" + p))
  }

  test("solve main") {
    ???
  }
}
