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

    println(solver.solution)
    // solver.steps.foreach(p => println("-----\n" + p))
  }

  test("updateRay normalizes double pair") {
    val pair    = Set(   2,          6)
    val removed = Set(1,    3, 4, 5,    7)
    val cell = pair union removed

    val ray      = Vector(cell,    pair, cell,    pair, cell,    cell,    cell)
    val expected = Vector(removed, pair, removed, pair, removed, removed, removed)

    assert(updateRay(ray) === expected)
  }

  // TODO: triple triplets heuristic/n n-tuples if needed

  test("solve main") {
    val p7 = freshPuzzle(7)

    val aNumConstraints = Set(
      NumConstraint((Location(0, 2), Location(1, 2)), 4),
      NumConstraint((Location(1, 0), Location(2, 0)), 2),
      NumConstraint((Location(2, 2), Location(3, 2)), 4),
      NumConstraint((Location(2, 4), Location(3, 4)), 3),
      NumConstraint((Location(3, 4), Location(4, 4)), 3)
    )

    val aLtConstraints = Set(
      LtConstraint(Location(0, 0), Location(0, 1)),
      LtConstraint(Location(0, 1), Location(1, 1)),
      LtConstraint(Location(1, 1), Location(1, 0)),
      LtConstraint(Location(1, 2), Location(1, 3)),
      LtConstraint(Location(1, 3), Location(1, 4)),
      LtConstraint(Location(2, 6), Location(2, 5)),
      LtConstraint(Location(2, 5), Location(3, 5)),
      LtConstraint(Location(3, 5), Location(4, 5)),
      LtConstraint(Location(5, 2), Location(4, 2)),
      LtConstraint(Location(5, 0), Location(6, 0)),
      LtConstraint(Location(6, 2), Location(6, 1)),
      LtConstraint(Location(5, 3), Location(6, 3)),
      LtConstraint(Location(6, 6), Location(5, 6))
    )

    val puzzleA = Puzzle(p7, aNumConstraints, aLtConstraints)

    val bNumConstraints = Set(
      NumConstraint((Location(0, 5), Location(0, 6)), 4),
      NumConstraint((Location(0, 4), Location(1, 4)), 6),
      NumConstraint((Location(2, 5), Location(3, 5)), 4),
      NumConstraint((Location(3, 3), Location(4, 3)), 6),
      NumConstraint((Location(4, 1), Location(4, 2)), 2),
      NumConstraint((Location(5, 1), Location(5, 2)), 5),
      NumConstraint((Location(5, 5), Location(5, 6)), 2)
    )

    val bLtConstraints = Set(
      LtConstraint(Location(0, 2), Location(0, 3)),
      LtConstraint(Location(1, 2), Location(0, 2)),
      LtConstraint(Location(2, 2), Location(1, 2)),
      LtConstraint(Location(2, 4), Location(2, 3)),
      LtConstraint(Location(6, 2), Location(6, 3))
    )

    val puzzleB = Puzzle(p7, bNumConstraints, bLtConstraints)

    val cNumConstraints = Set(
      NumConstraint((Location(1, 0), Location(2, 0)), 4),
      NumConstraint((Location(1, 6), Location(2, 6)), 3),
      NumConstraint((Location(2, 5), Location(3, 5)), 2),
      NumConstraint((Location(3, 5), Location(4, 5)), 3),
      NumConstraint((Location(4, 5), Location(5, 5)), 4),
      NumConstraint((Location(5, 1), Location(5, 2)), 4),
      NumConstraint((Location(5, 0), Location(6, 0)), 6)
    )

    val cLtConstraints = Set(
      LtConstraint(Location(0, 1), Location(0, 0)),
      LtConstraint(Location(0, 2), Location(0, 1)),
      LtConstraint(Location(0, 3), Location(0, 2)),
      LtConstraint(Location(0, 3), Location(0, 4)),
      LtConstraint(Location(0, 4), Location(0, 5)),
      LtConstraint(Location(0, 5), Location(0, 6)),
      LtConstraint(Location(2, 3), Location(2, 2)),
      LtConstraint(Location(2, 2), Location(3, 2)),
      LtConstraint(Location(3, 2), Location(3, 3)),
      LtConstraint(Location(5, 2), Location(5, 3))
    )

    val puzzleC = Puzzle(p7, cNumConstraints, cLtConstraints)

    val dNumConstraints = Set(
      NumConstraint((Location(1, 5), Location(2, 5)), 4),
      NumConstraint((Location(3, 2), Location(4, 2)), 2),
      NumConstraint((Location(4, 6), Location(5, 6)), 2),
      NumConstraint((Location(5, 3), Location(5, 4)), 1),
      NumConstraint((Location(5, 0), Location(6, 0)), 3),
      NumConstraint((Location(6, 5), Location(6, 6)), 6)
    )

    val dLtConstraints = Set(
      LtConstraint(Location(1, 3), Location(0, 3)),
      LtConstraint(Location(2, 1), Location(2, 0)),
      LtConstraint(Location(2, 2), Location(2, 1)),
      LtConstraint(Location(2, 3), Location(3, 3)),
      LtConstraint(Location(3, 4), Location(2, 4)),
      LtConstraint(Location(3, 3), Location(3, 4)),
      LtConstraint(Location(4, 0), Location(3, 0)),
      LtConstraint(Location(3, 5), Location(4, 5)),
      LtConstraint(Location(5, 5), Location(4, 5)),
      LtConstraint(Location(5, 2), Location(6, 2))
    )

    val puzzleD = Puzzle(p7, dNumConstraints, dLtConstraints)

    val eNumConstraints = Set(
      NumConstraint((Location(1, 5), Location(1, 6)), 3),
      NumConstraint((Location(2, 3), Location(3, 3)), 1),
      NumConstraint((Location(3, 6), Location(4, 6)), 3),
      NumConstraint((Location(4, 2), Location(4, 3)), 4),
      NumConstraint((Location(4, 3), Location(4, 4)), 2),
      NumConstraint((Location(4, 4), Location(4, 5)), 3),
      NumConstraint((Location(4, 1), Location(5, 1)), 6),
      NumConstraint((Location(6, 3), Location(6, 4)), 4),
      NumConstraint((Location(6, 5), Location(6, 6)), 5)
    )

    val eLtConstraints = Set(
      LtConstraint(Location(1, 1), Location(0, 1)),
      LtConstraint(Location(0, 4), Location(1, 4)),
      LtConstraint(Location(1, 0), Location(1, 1)),
      LtConstraint(Location(1, 2), Location(1, 1)),
      LtConstraint(Location(1, 1), Location(2, 1)),
      LtConstraint(Location(2, 4), Location(2, 3)),
      LtConstraint(Location(2, 0), Location(3, 0))
    )

    val puzzleE = Puzzle(p7, eNumConstraints, eLtConstraints)

    val identityConstraints = Set(
      IdentityConstraint(0, Location(5, 4), 1, Location(0, 0)),
      IdentityConstraint(0, Location(5, 5), 1, Location(0, 1)),
      IdentityConstraint(0, Location(5, 6), 1, Location(0, 2)),
      IdentityConstraint(0, Location(6, 4), 1, Location(1, 0)),
      IdentityConstraint(0, Location(6, 5), 1, Location(1, 1)),
      IdentityConstraint(0, Location(6, 6), 1, Location(1, 2)),
      IdentityConstraint(1, Location(0, 4), 2, Location(5, 0)),
      IdentityConstraint(1, Location(0, 5), 2, Location(5, 1)),
      IdentityConstraint(1, Location(0, 6), 2, Location(5, 2)),
      IdentityConstraint(1, Location(1, 4), 2, Location(6, 0)),
      IdentityConstraint(1, Location(1, 5), 2, Location(6, 1)),
      IdentityConstraint(1, Location(1, 6), 2, Location(6, 2)),
      IdentityConstraint(2, Location(5, 4), 3, Location(0, 0)),
      IdentityConstraint(2, Location(5, 5), 3, Location(0, 1)),
      IdentityConstraint(2, Location(5, 6), 3, Location(0, 2)),
      IdentityConstraint(2, Location(6, 4), 3, Location(1, 0)),
      IdentityConstraint(2, Location(6, 5), 3, Location(1, 1)),
      IdentityConstraint(2, Location(6, 6), 3, Location(1, 2)),
      IdentityConstraint(3, Location(0, 4), 4, Location(5, 0)),
      IdentityConstraint(3, Location(0, 5), 4, Location(5, 1)),
      IdentityConstraint(3, Location(0, 6), 4, Location(5, 2)),
      IdentityConstraint(3, Location(1, 4), 4, Location(6, 0)),
      IdentityConstraint(3, Location(1, 5), 4, Location(6, 1)),
      IdentityConstraint(3, Location(1, 6), 4, Location(6, 2))
    )

    val puzzle = CompositePuzzle(Vector(puzzleA, puzzleB, puzzleC, puzzleD, puzzleE), identityConstraints)

    val solver = new Solver[CompositePuzzle](puzzle, subPuzzleHeuristic :: (identityConstraints map identityHeuristic).toList)

    println(solver.solution)
  }
}
