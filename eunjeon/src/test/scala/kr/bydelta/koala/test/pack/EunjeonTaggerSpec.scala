package kr.bydelta.koala.test.pack

import kr.bydelta.koala.eunjeon.{Dictionary, Tagger}
import kr.bydelta.koala.test.core.TaggerSpec
import kr.bydelta.koala.traits.{CanCompileDict, CanTag}
import org.bitbucket.eunjeon.seunjeon.Analyzer
import org.specs2.execute.Result

/**
  * Created by bydelta on 16. 7. 26.
  */
class EunjeonTaggerSpec extends TaggerSpec {

  override def isSentenceSplitterImplemented: Boolean = true

  override def tagSentByOrig(str: String): (String, String) = {
    val original = Analyzer.parseEojeol(str)
    val tag = original.map(_.nodes.map {
      e =>
        val arr = e.morpheme.feature.last
        if (arr == "*") e.morpheme.surface
        else arr.replaceAll("([^/]+)/[^\\+]+", "$1")
    }.mkString("+")).mkString(" ")
    val surface = original.map(_.surface).mkString(" ")
    surface -> tag
  }

  override def tagParaByOrig(str: String): Seq[String] = Seq.empty

  override def getTagger: CanTag[_] =
    new Tagger()

  override def getDict: CanCompileDict =
    Dictionary

  override def expectEmptyDict: Result =
    Dictionary.userDict.termDict must beEmpty

  override def expectNonEmptyDict: Result =
    Dictionary.nonEmpty must beTrue
}
