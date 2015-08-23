package github.johngorskijr.aenigmata.adas

import org.scalatest.FunSuite

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import github.johngorskijr.aenigmata.adas.Politics._

@RunWith(classOf[JUnitRunner])
class PoliticsTest extends FunSuite {
  test("sample") {
    val text: String =
      """FJ222NA
        |1RZ122O
        |PH2TDQT
        |LB2SE23
        |E3ERHOO
        |OZR2D1C
        |RUD1112
      """.stripMargin

    val arrows: String =
      """  ><<..
        |^  >vv.
        |  <....
        |  v  v<
        | >.....
        |   ^ <.
        |   >>><
      """.stripMargin

    val puzzle = puzzleFrom(text, arrows)
    println(puzzle)
  }

  test("solve") {
    val text: String =
      """T7HIOH4AM12A43ARS55ND
        |TLQ4REULTC44GSC4HEDHQ
        |6ZSYGQZH4JCPA3NGYBVEE
        |OMEENAOLE0ETLW75QNGXS
        |EWI6TRHLTTSSDMS3REITO
        |QMPTC3RZL3HE2BRATPE1H
        |H2HI55LEC3VDMCORVAORR
        |ED4RZEB1UOTCELE6HIFDE
        |M182DS332R32FYWDISTNO
        |9LO2KALCDEEIQSGT4RHHO
        |DQALDGFYSSSYDYTJOLEK5
        |HOSUWRBOSTOALU632LE2T
      """.stripMargin

    val arrows: String =
      """ >    v  << >>   vv..
        |   >      vv   <.....
        |>       v    >.......
        |         v    <v.....
        |   >           >.....
        |     v   ^  ^      ^.
        | v  >>   >...........
        |  ^    v       <.....
        | ^>^  >>< ><.........
        |>  <            ^....
        |                    ^
        |              <>>  ^.
      """.stripMargin

    val puzzle = puzzleFrom(text, arrows)
    println(puzzle)
  }
}
