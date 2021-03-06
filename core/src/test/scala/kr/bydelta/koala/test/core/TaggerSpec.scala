package kr.bydelta.koala.test.core

import kr.bydelta.koala.traits.{CanCompileDict, CanTag}
import org.specs2.execute.Result
import org.specs2.mutable.Specification

/**
  * Created by bydelta on 17. 4. 22.
  */
trait TaggerSpec extends Specification with Examples {
  sequential

  def tagSentByOrig(str: String): (String, String)

  def tagParaByOrig(str: String): Seq[String]

  def getTagger: CanTag[_]

  def getDict: CanCompileDict

  def expectCorrectParse(str: String) = {
    val tagger = getTagger
    val (oSurface, oTag) = tagSentByOrig(str)
    val (tSurface, tTag) = tagSentByKoala(str, tagger)

    if (oTag.nonEmpty)
      tTag must_== oTag
    if (oSurface.nonEmpty)
      tSurface must_== oSurface

    tSurface.replaceAll("\\s+", "") must_== str.replaceAll("\\s+", "")
  }

  def tagSentByKoala(str: String, tagger: CanTag[_]): (String, String) = {
    val tagged = tagger.tagSentence(str)
    val tag = tagged.map(_.map(_.surface).mkString("+")).mkString(" ")
    val surface = tagged.surfaceString()
    surface -> tag
  }

  def expectCorrectParses(str: Seq[String]) = {
    val tagger = getTagger
    val single = str.map(s => tagSentByKoala(s, tagger))
    val multi = str.par.map {
      s =>
        val t = getTagger
        tagSentByKoala(s, t)
    }

    val matched = single.zip(multi)

    Result.unit {
      matched.foreach {
        case ((sS, sT), (tS, tT)) =>
          tS must_== sS
          tT must_== sT
      }
    }
  }

  def expectEmptyDict: Result

  def expectNonEmptyDict: Result

  def isSentenceSplitterImplemented: Boolean

  "Tagger" should {
    "handle empty sentence" in {
      val sent = getTagger.tagSentence("")
      sent.words must beEmpty
    }

    "tag a sentence" in {
      Result.foreach(exampleSequence().filter(_._1 == 1)) {
        case (_, sent) =>
          expectCorrectParse(sent)
      }
    }

    "be thread-safe" in {
      val sents = exampleSequence().filter(_._1 == 1).map(_._2)
      expectCorrectParses(sents)
    }

    if (isSentenceSplitterImplemented) {
      "match sentence split spec" in {
        val tagger = getTagger
        Result.foreach(exampleSequence(requireMultiLine = true)) {
          case (n, sent) =>
            val splits = tagger.tagParagraph(sent)
            splits.length must_== n
        }
      }

      "tag paragraph" in {
        val sent = "포털의 '속초' 연관 검색어로 '포켓몬 고'가 올랐다. 속초시청이 관광객의 편의를 위해 예전에 만들었던 무료 와이파이존 지도는 순식간에 인기 게시물이 됐다."
        val sents = Seq("포털의 '속초' 연관 검색어로 '포켓몬 고'가 올랐다.",
          "속초시청이 관광객의 편의를 위해 예전에 만들었던 무료 와이파이존 지도는 순식간에 인기 게시물이 됐다.")
        val tagger = getTagger
        val splits = tagger.tagParagraph(sent)
        val sentMap = sents.map(tagger.tagSentence)

        splits.length must_== 2
        splits.head mustEqual sentMap.head
        splits.last mustEqual sentMap.last
      }
    } else {
      "tag paragraph" in {
        val tagger = getTagger
        Result.foreach(exampleSequence()) {
          case (_, sent) =>
            val splits = tagger.tagParagraph(sent)
            val orig = tagParaByOrig(sent)

            splits.length must_== orig.length
            splits.map(_.map(_.map(_.surface).mkString("+"))
              .mkString(" ")).mkString("\n") must_== orig.mkString("\n")
        }
      }
    }
  }
}

