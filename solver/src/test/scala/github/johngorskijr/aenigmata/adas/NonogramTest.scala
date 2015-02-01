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
  test("load 5x5") {
    val actual = load("((3,1),(5),(4),(1),(1)),((3),(3),(3,1),(3),(2))")
  }
}
