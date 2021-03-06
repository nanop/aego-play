package controllers

import play.api.mvc._
import play.api.cache.Cache
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection
import models._
import play.api.libs.json._
import play.api.libs.json.Json._
import reactivemongo.api.Cursor
import play.api.libs.Codecs
import java.util.Date
import play.api.i18n.Lang
import play.api.Play.current
import play.api.libs.openid.OpenID
import scala.concurrent.Future
import play.api.Logger
import hu.jupi.play.authentication._

object Application extends Controller with MongoController with Authentication[User] {

  private final val GOOGLE_OPEN_ID_URL = "https://www.google.com/accounts/o8/id"
  private final val OPEN_ID_SESSION_KEY = "user"
  private final val LOG = Logger.logger

  /**
   * Persistent collection of stories.
   */
  private def stories: JSONCollection = db.collection[JSONCollection]("stories")

  /**
   * Persistent collection of users.
   */
  private def users: JSONCollection = db.collection[JSONCollection]("users")


  /**
   * Overriden method of the Authentication trait.
   * Fetch principal by OpenID of the session.
   *
   * @param rh Request header to get the session.
   * @return A Future of an optional user.
   */
  override def principal(rh: RequestHeader): Future[Option[User]] = {
    val idOpt = rh.session.get(OPEN_ID_SESSION_KEY)
    idOpt match {
      case Some(id) => Cache.getAs[User](id) match {
        case s: Some[User] => Future(s)
        case None => {
          users.find(Json.obj("openId" -> id)).one[User] map { user =>
            Cache.set(id, user, 1800)
            user
          }
        } recover {
          case t: Throwable => {
            LOG.warn("DB error on getting principal: {}", t)
            None
          }
        }
      }
      case _ => Future(None)
    }
  }

  def generateStoryId(title: String): Future[String] = {
    val generated = Codecs.md5((title + new Date().getTime) getBytes).substring(0, 10)
    for {
      story <- stories.find(Json.obj("id" -> generated)).one[Story]
      id <- if (story.isEmpty) Future(generated) else generateStoryId(title)
    } yield id
  }

  /**
   * Shows the index page.
   * @return Index action.
   */
  def indexAction = WithAuthentication {
    implicit request =>
      Ok(views.html.index())
  }

  def storiesAction = WithAuthentication {
    implicit request =>
      Async {
		for {
		  public <- stories.find(Json.obj("public" -> true)).cursor[Story].toList
		  own <- ownStories(request)
		} yield Ok(views.html.stories(public, own, Forms.storyFilter))
      }
  }

  /**
   * @return Action to show new story form.
   */
  def newStoryFormAction = WithAuthentication(
    implicit request => request match {
      case Authenticated(_) => Ok(views.html.newStoryForm(Forms.newStory))
      case _ => Unauthorized(views.html.signIn(Forms.signIn))
    })

  /**
   * Creates a new story.
   *
   * @return The action.
   */
  def newStoryAction = WithAuthentication {
    implicit request =>
      request match {
        case Authenticated(user: User) =>
          Forms.newStory.bindFromRequest.fold(
            errors => BadRequest(views.html.newStoryForm(errors)),
            data => Async {
              for {
                id <- generateStoryId(data.title)
                story = data.copy(id = id)
                insertStory <- stories.insert(story)
                modifier = Json.obj("$addToSet" -> Json.obj("masterOfStories" -> id))
                updateUser <- users.update(Json.obj("openId" -> user.openId), modifier)
              } yield Redirect(routes.Application.storiesAction)
            }
          )
        case _ => Unauthorized(views.html.signIn(Forms.signIn))
      }

  }

  /**
   * Show last 100 posts of a story.
   *
   * @param storyId ID of the story.
   * @return HTML page action result.
   */
  def postsAction(storyId: String) = TODO

  /**
   * Get the last 100 post of a story after the specified timestamp.
   *
   * @param after Timestamp.
   * @return JSON list of posts.
   */
  def postsAction(storyId: String, after: Long) = TODO

  /**
   * Submit a new post of a story.
   * The request body must contain the ID of the story, the data of the post in JSON format,
   * and the story teller must be an authorized user.
   *
   * @param storyId ID ot the story to add the post to.
   * @return A response with empty body.
   */
  def submitPostAction(storyId: String) = TODO

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
  def storyBoardAction(storyId: String) = WithAuthentication(
    implicit request => Async {
      for(story <- stories.find(Json.obj("id" -> storyId)).one[Story])
      yield Ok(views.html.storyBoard(story.get, Forms.tellerSettings))
    }
  )

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
  def addTellerSettingsAction(storyId: String) = TODO

  /**
   * Cleans all stories from the DB. For testing purposes only.
   * @return Index page.
   */
  def cleanDbAction = WithAuthentication(implicit request => Async {
    for {
      dropStories <- stories.drop
      dropUsers <- users.drop
    } yield Ok(views.html.index())
  })

  /**
   * Shows the sign-in form.
   * @return HTML page with the form.
   */
  def loginPageAction = WithAuthentication {
    implicit request =>
      Ok(views.html.signIn(Forms.signIn))
  }

  /**
   * Sign-in action after OpenId was picked.
   * @return OpenID redirect if everything is fine. Login page otherwise.
   */
  def signInOpenIdAction = WithAuthentication {
    implicit request =>
      Forms.signIn.bindFromRequest.get match {
        case empty if empty.isEmpty => Redirect(routes.Application.loginPageAction)
        case url => AsyncResult(OpenID.redirectURL(url, routes.Application.openIdCallbackAction.absoluteURL())
          .map(url => Redirect(url))
          .recover {
          case t => Redirect(routes.Application.loginPageAction)
        }
        )
      }
  }

  /**
   * Start an OpenId authentication process towards Google.
   * @return OpenID redirect if everything is fine. Login page otherwise.
   */
  def signInGoogleAction = WithAuthentication {
    implicit request =>
      AsyncResult(OpenID.redirectURL(GOOGLE_OPEN_ID_URL, routes.Application.openIdCallbackAction.absoluteURL())
        .map(url => Redirect(url))
        .recover {
        case t => Redirect(routes.Application.loginPageAction)
      }
      )
  }

  /**
   * Clears the session.
   *
   * @return Index page.
   */
  def signOutAction = WithAuthentication {
    implicit request =>
      request match {
        case Authenticated(user: User) =>
          Cache.remove(user.openId)
        case _ =>
          LOG.warn("A GUEST user has been signed out.")
      }
      Redirect(routes.Application.indexAction).withNewSession
  }

  /**
   * Callback for OpenID provider.
   * @return Profile or create profile pages, depending on the existence of the user profile.
   */
  def openIdCallbackAction = WithAuthentication {
    implicit request => AsyncResult {
      OpenID.verifiedId flatMap {
        info =>
          users.find(Json.obj("openId" -> info.id)).one[User] map (_ match {
            case Some(user: User) =>
              Redirect(routes.Application.indexAction()).withSession(OPEN_ID_SESSION_KEY -> user.openId)
            case _ => Ok(views.html.createProfile(Forms.profile)).withSession(OPEN_ID_SESSION_KEY -> info.id)
          })
      }
    }
  }

  /**
   * Sets or changes the value of the language cookie.
   * @param lang Language code.
   * @return Referer or index page.
   */
  def changeLanguageAction(lang: String) = WithAuthentication {
    implicit request =>
      val referrer = request.headers.get(REFERER).getOrElse("/")
      Redirect(referrer).withLang(Lang(lang))
  }

  def createProfileAction = WithAuthentication {
    implicit request =>
      Forms.profile.bindFromRequest() fold(errors => BadRequest(views.html.createProfile(errors)),
        data => Async {
          for (lastError <- users.insert(data.copy(openId = request.session.get(OPEN_ID_SESSION_KEY).get)))
          yield Redirect(routes.Application.storiesAction())
        })
  }

  def editProfileAction = WithAuthentication( implicit request =>
    request match {
      case Authenticated(user: User) =>
        Forms.profile.bindFromRequest() fold (
          error => BadRequest(views.html.profile(error)),
          data => Async {
            users.update(Json.obj("openId" -> user.openId), Json.obj("$set" -> Json.obj(
              "username" -> data.username, "email" -> data.email, "birthday" -> data.birthday))) map (lastError =>
              Ok(views.html.profile(Forms.profile.fill(data))))
          }
        )
      case _ => Unauthorized(views.html.signIn(Forms.signIn))
    }
  )

  def profileAction = WithAuthentication {
    implicit request => request match { case Authenticated(user: User) =>
      Ok(views.html.profile(Forms.profile.fill(user)))
    }
  }

  def ownStories(auth: WithAuthentication): Future[List[Story]] = {
	auth match {
	  case Authenticated(user: User) => stories.find(
		  Json.obj("id" -> Json.obj("$in" -> user.masterOfStories))).cursor[Story].toList
	  case _ => Future(Nil)
	}
  }
  
  def filterStoriesAction = WithAuthentication {
    implicit request => Forms.storyFilter.bindFromRequest() fold (
      errors => Redirect(routes.Application.storiesAction),
      data => Async {
        val criteria = for {
		  c <- List("title" -> data.title, "tags" -> data.tag) if !c._2.isEmpty
		} yield (c._1, Json.obj("$regex" -> c._2.get))
       for {
		 public <- stories.find(JsObject(("public" -> JsBoolean(true) :: criteria))).cursor[Story].toList
		 own <- ownStories(request)
	   } yield Ok(views.html.stories(public, own, Forms.storyFilter.fill(data)))
      })
  }
  
  def aboutAction = WithAuthentication {
	implicit request => Ok(views.html.about())
  }

}