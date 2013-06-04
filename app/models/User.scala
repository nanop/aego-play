package models

import play.api.libs.json.Json
import java.util.Date

/**
 * Domain class for storing the properties of a user.
 *
 * @param openId OpenID of the user.
 * @param username Public, general user name.
 * @param email Email of log in.
 * @param online Online state.
 * @param readerOfStories The user has read access of these stories, with other words, the user is a reader of the specified stories.
 * @param tellerOfStories The user has r/w access of these stories, with other words, the user is a story teller of the specified stories.
 * @param masterOfStories The user is the master of the specified stories (r/w access, more rights than a story teller).
 *
 * @author jupi
 */
case class User(openId: String,
                username: String,
                email: String,
                birthday: Date,
                online: Boolean = true,
                readerOfStories: Seq[Story] = Nil,
                tellerOfStories: Seq[Story] = Nil,
                masterOfStories: Seq[Story] = Nil)

object User {
  implicit val format = Json.format[User]

  def create(username: String, email: String, birthday: Date) =
    User("", username, email, birthday)
}