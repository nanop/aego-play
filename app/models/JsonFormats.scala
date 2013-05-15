package models

import play.api.libs.json.Json

object JsonFormats {
  implicit val contextJson = Json.format[Context]
  implicit val storyTellerJson = Json.format[StoryTeller]
  implicit val storyJson = Json.format[Story]
  implicit val phraseJson = Json.format[Phrase]
  implicit val userJson = Json.format[User]
  implicit val commentJson = Json.format[Comment]
}
