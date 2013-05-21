package controllers

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import play.api.libs.json.util._
import play.api.libs.json.Reads._
import play.api.libs.json.Writes._
import play.modules.reactivemongo.json.collection.JSONCollection
import scala.concurrent._
import scala.concurrent.duration._
import models.JsonFormats._
import reactivemongo.core.commands.Count
import models.Story

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
      status(result) must equalTo(400)
      Await.result(db.command(Count("stories")), timeout) mustEqual 0
      dropStories
    }

    "create new story with some valid test data" in new WithApplication(FakeApplication()) {
      dropStories
      val formValues = ("title" -> "test story",
      "public" -> true.toString,
      "adult" -> false.toString,
      "tags" -> "apple,pear,shadow of darkness,árvíztűrő tükörfúrógép",
      "master" -> "Shinobi")
      val result = Application.newStory()(FakeRequest().withFormUrlEncodedBody(formValues: _*))
      status(result) must equalTo(200)
      Await.result(db.command(Count("stories")), timeout) mustEqual 1
      val story = Await.result(stories.find(Json.obj()).one[Story], timeout)
      dropStories
    }

  }
}

