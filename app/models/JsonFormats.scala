package models

import play.api.libs.json._
import java.awt.Color



object JsonFormats {
  implicit val context = Json.format[Context]
  implicit val storyTeller = Json.format[StoryTeller]
  implicit val story = Json.format[Story]
  implicit val phrase = Json.format[Phrase]
  implicit val user = Json.format[User]
  implicit val comment = Json.format[Post]
}
