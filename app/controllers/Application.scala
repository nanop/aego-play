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
import play.api.i18n.Lang
import play.api.Play.current
import play.api.libs.openid.OpenID

object Application extends Controller with MongoController {

  /**
   * Persistent collection of stories.
   */
  private def stories: JSONCollection = db.collection[JSONCollection]("stories")

  private final val TIMEOUT = Duration("2 seconds")
  private final val GOOGLE_OPEN_ID_URL = "https://www.google.com/accounts/o8/id"

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
        Story.create(generateStoryId(title), title, public, adult, toTags(tags), master))
      (story => Some(story.title, story.master.alias, story.public, story.adult, story.tags.mkString(",")))
  )

  private val signInForm = Form(
    single("openId" -> text)
  )

  /**
   * Convert a string to sequence of tags. A tag should not be null or empty and cannot begin or end with whitespaces.
   *
   * @param s A string which contains all the tags separated by commas (,) or optionally spaces around commas.
   * @return Sequence of tags.
   */
  private def toTags(s: String) =
    for (shard <- s.split(","); tag = shard.trim() if !tag.isEmpty()) yield tag

  private val tellerSettingsForm = Form(
    tuple("alias" -> nonEmptyText, "color" -> nonEmptyText)
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
    implicit request =>
      Ok(views.html.index())
  }

  def showStories = Action {
    implicit request =>
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
   *
   * @return The action.
   */
  def newStory = Action {
    implicit request =>
      newStoryForm.bindFromRequest.fold(
        errors => BadRequest(views.html.newStoryForm(errors)),
        data => {
          val insertFuture = stories.insert(data)
          Async {
            insertFuture map (_ => Ok(views.html.index()))
          }
        }
      )
  }

  /**
   * Show last 100 posts of a story.
   *
   * @param storyId ID of the story.
   * @return HTML page action result.
   */
  def showPosts(storyId: String) = TODO

  /**
   * Get the last 100 post of a story after the specified timestamp.
   *
   * @param after Timestamp.
   * @return JSON list of posts.
   */
  def getPosts(storyId: String, after: Long) = TODO

  /**
   * Submit a new post of a story.
   * The request body must contain the ID of the story, the data of the post in JSON format,
   * and the story teller must be an authorized user.
   *
   * @param storyId ID ot the story to add the post to.
   * @return A response with empty body.
   */
  def submitPost(storyId: String) = TODO

  /**
   * Show authentication form and some information about the story.
   *
   * @param storyId ID of the story.
   * @return HTML page with a form.
   */
  /*
   TODO get previous settings by the settings id of the cookie and prefill the form
   TODO show the form and login panel if required
   */
  def showStoryBoard(storyId: String) = TODO

  /**
   * Change a storytellers settings or create a new one.
   * Settings cookie is also created if the teller has not already got one.
   *
   * @param storyId ID of the story
   * @return HTML page with last 100 posts of the story
   */
  /*
   TODO check user rights (may not be a storyteller)
   TODO create setting id cookie if needed
   TODO create a new storyteller or modify its color (or even its alias in no posts case)
   Action {
     request =>
       tellerSettingsForm.bindFromRequest.fold(
       errors =>
       data =>
       )
   }                                             */
  def addTellerSettings(storyId: String) = TODO

  /**
   * Cleans all stories from the DB. For testing purposes only.
   * @return Index page.
   */
  def cleanDb = Action {
    Async {
      stories.drop() map (_ => Ok(views.html.index()))
    }
  }

  /**
   * Shows the sign-in form.
   * @return HTML page with the form.
   */
  def loginPage = Action {
    implicit request =>
      Ok(views.html.signIn(signInForm))
  }

  /**
   * Sign-in action after OpenId was picked.
   * @return
   */
  def signInOpenId = Action {
    implicit request =>
      signInForm.bindFromRequest.get match {
        case empty if empty.isEmpty => Redirect(routes.Application.loginPage)
        case url => AsyncResult(OpenID.redirectURL(url, routes.Application.openIdCallback.absoluteURL())
          .map(url => Redirect(url))
          .recover { case t => Redirect(routes.Application.loginPage) }
        )
      }
  }

  def signInGoogle = Action {
    implicit request =>
      AsyncResult(OpenID.redirectURL(GOOGLE_OPEN_ID_URL, routes.Application.openIdCallback.absoluteURL())
        .map(url => Redirect(url))
        .recover { case t => Redirect(routes.Application.loginPage) }
      )
  }

  def openIdCallback = Action { implicit request =>
    AsyncResult(
      OpenID.verifiedId
        .map(info => Ok(info.toString))
        //.map(info => Ok("(?<=id=).*".r findFirstIn info.id get))
        .recover { case t =>
          // Here you should look at the error, and give feedback to the user
          Redirect(routes.Application.loginPage)
        }
    )
  }

  def changeLanguage(lang: String) = Action {
    implicit request =>
      val referrer = request.headers.get(REFERER).getOrElse("/")
      Redirect(referrer).withLang(Lang(lang))
  }
}