package models

import play.api.libs.json.Json
import java.util.Date

/**
 * Domain class for storing the properties of a user.
 *
 * @param openId OpenID of the user.
 * @param username Public, general user name.
 * @param email Email of log in.
 * @param readerOfStories Contains IDs of stories. The user has read access of these stories, with other words, the user is a reader of the specified stories.
 * @param tellerOfStories Contains IDs of stories. The user has r/w access of these stories, with other words, the user is a story teller of the specified stories.
 * @param masterOfStories Contains IDs of stories. The user is the master of the specified stories (r/w access, more rights than a story teller).
 *
 * @author jupi
 */
case class User(openId: String,
                username: String,
                email: String,
                birthday: Date,
                readerOfStories: Seq[String] = Nil,
                tellerOfStories: Seq[String] = Nil,
                masterOfStories: Seq[String] = Nil)

object User {
  implicit val format = Json.format[User]

  def create(username: String, email: String, birthday: Date) =
    User("", username, email, birthday)
}