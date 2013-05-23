package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection
import models._
import play.api.libs.json.Json
import play.api.libs.json.Json._
import reactivemongo.api.Cursor
import play.api.libs.Codecs
import java.util.Date
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Application extends Controller with MongoController {

  /**
   * Persistent collection of stories.
   */
  private def stories: JSONCollection = db.collection[JSONCollection]("stories")

  private final val TIMEOUT = Duration("2 seconds")

  /**
   * New story form with mapping and constraints.
   */
  private val newStoryForm = Form(
    mapping(
      "title" -> nonEmptyText,
      "master" -> nonEmptyText,
      "public" -> boolean,
      "adult" -> boolean,
      "tags" -> text)
      ((title, master, public, adult, tags) =>
        Story.create(generateStoryId(title), title, public, adult, tags.split(","), master))
      (story => Some(story.title, story.master.alias, story.public, story.adult, story.tags.mkString(",")))
  )

  def generateStoryId(title: String): String = {
    def unique(id: String): Boolean = Await.result(stories.find(Json.obj("id" -> id)).one[Story], TIMEOUT).isEmpty
    val storyId = Codecs.md5((title + new Date().getTime) getBytes).substring(0, 10)
    if (unique(storyId)) storyId else generateStoryId(title)
  }

  /**
   * Shows the index page.
   * @return Index action.
   */
  def index = Action {
    Ok(views.html.index())
  }

  def showStories = Action {
    Async {
      val cursor: Cursor[Story] = stories.find(Json.obj()).cursor[Story]
      val storiesFuture = cursor.toList
      storiesFuture map (s => Ok(views.html.stories(s)))
    }
  }

  /**
   * @return Action to show new story form.
   */
  def showNewStoryForm = Action(Ok(views.html.newStoryForm(newStoryForm)))

  /**
   * Creates a new story.
   * @return The action.
   */
  def newStory = Action {
    implicit request =>
      newStoryForm.bindFromRequest.fold(
        errors => BadRequest(views.html.index()),
        data => {
          val insertFuture = stories.insert(data)
          Async {
            insertFuture map (_ => Ok(views.html.index()))
          }
        }
      )
  }



}