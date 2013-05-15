package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import reactivemongo.api._
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json._
import models._
import models.JsonFormats._


object Application extends Controller with MongoController {

  /**
   * New story form with mapping and constraints.
   */
  val newStoryForm = Form(
    mapping("Story title" -> nonEmptyText, "Avatar name" -> nonEmptyText)
      ((title, name) => Story(title, StoryTeller(name)))
      (story => Some(story.name, story.master.alias)))

  /*
* Get a JSONCollection (a Collection implementation that is designed to work
* with JsObject, Reads and Writes.)
* Note that the `stories` is not a `val`, but a `def`. We do _not_ store
* the stories reference to avoid potential problems in development with
* Play hot-reloading.
*/
  def stories: JSONCollection = db.collection[JSONCollection]("stories")

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
            insertFuture map (_ => Redirect(routes.Application.index()))
          }
        }
      )
  }

}