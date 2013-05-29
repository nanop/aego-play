package controllers

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import reactivemongo.core.commands._
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent._
import scala.concurrent.duration._
import models._

class ApplicationSpec extends Specification {

  implicit val ec = ExecutionContext.Implicits.global
  val timeout: Duration = Duration(2, "seconds")

  def db = Application.db

  def stories = db.collection[JSONCollection]("stories")

  def dropStories = Await.ready(stories.drop, timeout)

  "The Application controller" should {

    "not create new story without data" in new WithApplication(FakeApplication()) {
      dropStories
      val result = Application.newStory()(FakeRequest())
      status(result) mustEqual 400
      Await.result(db.command(Count("stories")), timeout) mustEqual 0
      dropStories
    }

    "create a new story with some valid test data" in new WithApplication(FakeApplication()) {
      dropStories
      val formValues = Map("title" -> "test story őűŐŰ",
      "public" -> true.toString,
      "adult" -> false.toString,
      "tags" -> "apple,pear,shadow of darkness,árvíztűrő tükörfúrógép",
      "master" -> "GM")
      val result = Application.newStory()(FakeRequest().withFormUrlEncodedBody(formValues.toSeq: _*))
      status(result) mustEqual 200
      Await.result(db.command(Count("stories")), timeout) mustEqual 1
      val story = Await.result(stories.find(Json.obj()).one[Story], timeout).get
      story.adult mustEqual formValues("adult").toBoolean
      story.public mustEqual formValues("public").toBoolean
      story.id must not be empty
      story.master.alias mustEqual formValues("master")
      story.readerIds must beEmpty
      story.storyTellers must beEmpty
      story.title mustEqual formValues("title")
      story.tags mustEqual formValues("tags").split(",").toSeq
      println(story.id)
      dropStories
    }

  }
}

