package github.johngorskijr.aenigmata.adas

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import github.johngorskijr.aenigmata.adas.Nonogram._

/**
 * User: jgorski
 * Date: 1/31/2015
 * Time: 6:44 PM
 */
@RunWith(classOf[JUnitRunner])
class NonogramTest extends FunSuite {
  test("Solve!") {
    val solution = solve
    solution.cells
  }

  test("Vector equality") {
    val a: Vector[Cell] = Vector(UNKNOWN, FILLED, OPEN)
    assert(Vector(UNKNOWN, FILLED, OPEN) == a === true)
    assert(Vector(OPEN, UNKNOWN, FILLED) == a === false)
  }

  test("Vector.with equality") {
    val a: Vector[Cell] = Vector(UNKNOWN, FILLED, OPEN)
    assert(a.updated(1, FILLED) == a === true)
    assert(a.updated(1, OPEN) == a === false)
  }

  test("Equal puzzles are valid with == also") {
    val s: State = Vector(Vector(UNKNOWN))
    val c: Constraints = (Vector(List(1)), Vector(List(1)))
    val p1: Puzzle = new Puzzle(s, c)
    val p2: Puzzle = new Puzzle(s, c)
    assert(p1 == p2 === true)
  }

  test("withRow/withCol") {
    val unk: Vector[Cell] = Vector(UNKNOWN, UNKNOWN, UNKNOWN)
    val middle: Vector[Cell] = Vector(UNKNOWN, FILLED, UNKNOWN)
    val base = Puzzle(Vector(unk, unk, unk), (Vector(List()), Vector(List())))
    val expected = Puzzle(Vector(unk, middle, unk), (Vector(List()), Vector(List())))

    assert(base.withRow(1, middle) == expected)
    assert(base.withCol(1, middle) == expected)
  }
}
