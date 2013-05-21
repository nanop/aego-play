package models

import play.api.libs.json.Json

/**
 * Domain class for storing the properties of phrases, elements of comments. Text or context or both can be present, only one of them can be null at the same time.
 *
 * @param text Some sentences.
 * @param context Context of the text.
 *
 * @author jupi
 */
case class Phrase(text: String, context: Context)

object Phrase {
  implicit val format = Json.format[Phrase]
}