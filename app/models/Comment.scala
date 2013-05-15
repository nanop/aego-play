package models

/**
 * Domain class for storing the properties of a comment of a story.
 *
 * @param storyteller The teller who wrote wrote the comment.
 * @param timestamp The timestamp of the comment (in millis from epoch).
 * @param phrases A sequence of text and context elements of the comment.
 * @param storyId ID of the story the comment belongs to.  
 *
 * @author jupi
 */
case class Comment(storyteller: StoryTeller, timestamp: Long, phrases: Seq[Phrase], storyId: String)