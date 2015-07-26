package github.johngorskijr.aenigmata.adas

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import github.johngorskijr.aenigmata.adas.Util._

@RunWith(classOf[JUnitRunner])
class SolverTest extends FunSuite {
  type CountingPuzzle = Int

  test("count with heuristics") {
    val solver = new Solver[CountingPuzzle](
      1, List((cp: CountingPuzzle) => if (cp < 10) cp + 1 else cp))
    assert(solver.solution === 10)
  }

  test("num to char translation from puzzle 17") {
    val values = List(
      2 + 5 + 3 + 4,
      5 + 1 + 2 + 7,
      6 + 2 + 7 + 4 + 1,
      1 + 4,
      4 + 2 + 1,
      2 + 1,
      4 + 6 + 1 + 4,
      4 + 6 + 3 + 7 + 2,
      2 + 3,
      4 + 2 + 3 + 6 + 2 + 1,
      1 + 4,
      4,
      5 + 1 + 4 + 5 + 1 + 3,
      6 + 2 + 5 + 1 + 7,
      1 + 2,
      3 + 5,
      2 + 5 + 6 + 1,
      5 + 3 + 6 + 2 + 5,
      3 + 2 + 7 + 1,
      2,
      2 + 3,
      5 + 1 + 6 + 4 + 2,
      4 + 6 + 2 + 7
    )

    assert((values map charOf).mkString === "notegcoveredsuchnumbers")
  }

  test("letter translation") {
    assert(charOf(1) === 'a')
    assert(charOf(15) === 'o')
    assert(charOf(26) === 'z')
  }

  test("char + num from puzzle 18 final clue") {
    assert(("APPCICQWQCYLEFBGDSHNK" zip
      List(22, 11, 22, 9, 11, 12, 20, 8, 14, 13, 16, 9, 15, 14, 6, 24, 15, 10, 7, 6, 8)).map {
      case (c: Char, n: Int) => advanceChar(c, n)
    }.mkString === "walltokeepoutthescots")
  }

  test("puzzle 21 path") {
    def translate21(letters: String, numbers: List[Int]): String = letters
      .zip(runningTotal(numbers).map(_ % 10))
      .map { case (c: Char, i: Int) => advanceChar(c, i) }
      .mkString

    val allLetters = "rcxmgifsvnrogryaqapxgnzzavbytyqczqite"
    val allNumbers = "2323234242342323243232323232323243232".toCharArray.toList.map(_.toInt - '0'.toInt)

    assert(translate21(allLetters, allNumbers) === "theminotaursmazewasconceivedbythisman")
  }
}
