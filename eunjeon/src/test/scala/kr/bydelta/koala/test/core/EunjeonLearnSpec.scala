package kr.bydelta.koala.test.core

import kr.bydelta.koala.eunjeon.{Dictionary, Tagger}
import kr.bydelta.koala.util.SimpleWordLearner
import org.bitbucket.eunjeon.seunjeon.Eojeol
import org.specs2.execute.Result
import org.specs2.mutable.Specification

import scala.collection.JavaConverters._

/**
  * Created by bydelta on 16. 7. 30.
  */
class EunjeonLearnSpec extends Specification with BasicWordLearnerSpecs[scala.Seq[Eojeol]] {
  sequential

  "SimpleWordLearner" should {
    lazy val learner = {
      Dictionary.rawDict = Set()
      Dictionary.isDicChanged = true
      Dictionary.reloadDic()
      new SimpleWordLearner(Dictionary)
    }

    "extract all nouns" in {
      val level0 = learner.extractNouns(text.toIterator, minOccurrence = 2, minVariations = 2)
      val level2 = learner.extractNouns(text.toIterator, minOccurrence = 5, minVariations = 3)

      level0.size must be_>=(level2.size)
      level0 must not(containAnyOf(EXCLUDED_SET))
      level0 must containAllOf(INCLUDED_SET)
      level2 must containAllOf(INCLUDED_SET_2)
    }

    "learn all nouns" in {
      val tagger1 = getTagger
      val beforeLearn = text.map(s => tagger1.tagSentence(s).singleLineString).mkString("\n")

      learner.jLearn(text.toIterator.asJava, minOccurrence = 1, minVariations = 1)

      val tagger2 = getTagger
      val afterLearn = text.map(s => tagger2.tagSentence(s).singleLineString).mkString("\n")

      beforeLearn must_!= afterLearn
    }
  }

  override def getTagger = new Tagger
  override def getDict = Dictionary

  "BasicWordLearner" should {
    var lv0Empty = false

    "extract all nouns" in {
      val level0 = learner.extractNouns(text.toIterator, minOccurrence = 2, minVariations = 2)
      val level2 = learner.extractNouns(text.toIterator, minOccurrence = 5, minVariations = 3)

      lv0Empty = level0.isEmpty
      level0.size must be_>=(level2.size)
      level0 must not(containAnyOf(EXCLUDED_SET))
      level0 must containAllOf(INC_1._1)
      level0 must not(containAnyOf(INC_1._2))
      level2 must containAllOf(INC_2._1)
      level2 must not(containAnyOf(INC_2._2))
    }

    "learn all nouns" in {
      Result.unit {
        if (!lv0Empty) {
          val tagger1 = getTagger
          val beforeLearn = text.map(s => tagger1.tagSentence(s).singleLineString).mkString("\n")

          learner.jLearn(text.toIterator.asJava, minOccurrence = 1, minVariations = 1)

          val tagger2 = getTagger
          val afterLearn = text.map(s => tagger2.tagSentence(s).singleLineString).mkString("\n")

          beforeLearn must_!= afterLearn
        }
      }
    }
  }
}
