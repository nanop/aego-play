package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection
import java.awt.Color
import models.{StoryTeller, Story}
import models.JsonFormats._
import play.api.libs.Codecs
import java.util.Date
import play.api.libs.json.Json
import reactivemongo.api.Cursor

object Application extends Controller with MongoController {

  /**
   * Persistent collection of stories.
   */
  private def stories: JSONCollection = db.collection[JSONCollection]("stories")

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
      ((title, master, public, adult, tags) => Story.create(title, public, adult, tags.split(","), master))
      (story => Some(story.title, story.master.alias, story.public, story.adult, story.tags.mkString(",")))
  )

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