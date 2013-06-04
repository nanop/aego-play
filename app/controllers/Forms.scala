package controllers

import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import models.{User, Story}

object Forms {

  /**
   * New story form with mapping and constraints.
   */
  val newStory = Form(
    mapping(
      "title" -> nonEmptyText,
      "master" -> nonEmptyText,
      "public" -> boolean,
      //"adult" -> boolean,
      "tags" -> text)
      ((title, master, public, tags) =>
        Story.create(title, public, false, toTags(tags), master))
      (story => Some(story.title, story.master.alias, story.public, story.tags.mkString(",")))
  )

  /**
   * Sign in form, the user can enter only her OpenID.
   */
  val signIn = Form(
    single("openId" -> text)
  )

  /**
   * Create/edit profile form.
   */
  val profileForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "email" -> nonEmptyText,
      "birthday" -> date)
      (User.create)(u => Some(u.username, u.email, u.birthday))
  )

  val tellerSettingsForm = Form(
    tuple("alias" -> nonEmptyText, "color" -> nonEmptyText)
  )

  /**
   * Convert a string to sequence of tags. A tag should not be null or empty and cannot begin or end with whitespaces.
   *
   * @param s A string which contains all the tags separated by commas (,) or optionally spaces around commas.
   * @return Sequence of tags.
   */
  private def toTags(s: String) =
    for (shard <- s.split(","); tag = shard.trim() if !tag.isEmpty()) yield tag

}
