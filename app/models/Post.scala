package models

import play.api.libs.json.Json

/**
 * Domain class for storing the properties of a post of a story.
 *
 * @param storyteller The teller who wrote the post.
 * @param timestamp The timestamp of the post (in millis from epoch).
 * @param phrases A sequence of text and context elements of the post.
 * @param storyId ID of the story the post belongs to.
 *
 * @author jupi
 */
case class Post(storyteller: StoryTeller, timestamp: Long, phrases: Seq[Phrase], storyId: String)

object Post {
  implicit val format = Json.format[Post]
}