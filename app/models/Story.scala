package models

import play.api.libs.json.Json

/**
 * <p>Domain class of a story.</p>
 *
 * <p>A story is an equivalent of a chat room. The stories contain posts but linked from the side of posts.
 * The stories can be searched by story tags, without its posts.</p>
 *
 * <p>The story can be public or private. Public stories can be viewed by everyone, either guest users.</p>
 *
 * <p>The story can hold posts with adult contents, those should be discriminated to protect the under-aged users.</p>
 *
 * <p>Each story has a master who can administer the story. The master is also created on story creation.
 * The master is a story teller already and has all the rights over the story.
 * The master can be either a registered or a guest user.</p>
 *
 * <p>The story tellers are users who can add posts to the story. Story tellers can be guests or registered users.<p>
 *
 * <p>Readers of a private story can read the posts of the story.
 * The public stories can be read by anybody, so there should be no distinguished users of a public story.<p>
 *
 * @param id Short ID of the story, 10 char long hexadecimal hash. This will be shown in URLs.
 * @param title Title of the story.
 * @param public Public flag. Indicates that all users can read the story.
 * @param adult Adult content flag.
 * @param tags Tags of the story.
 * @param master Master story teller.
 * @param storyTellers List of story tellers.
 * @param readerIds List of the IDs of the readers.
 *
 * @author jupi
 *
 */
case class Story(id: String,
                 title: String,
                 public: Boolean = true,
                 adult: Boolean = false,
                 tags: Seq[String],
                 master: StoryTeller,
                 storyTellers: Seq[StoryTeller] = Nil,
                 readerIds: Seq[String] = Nil)

object Story {

  /**
   * Default text color of the posts.
   */
  val DEFAULT_COLOR = "#000000"

  implicit val format = Json.format[Story]

  /**
   * Create a new Story without readers and story tellers.
   * Generate ID only on insert.
   *
   * @param title Title of the story.
   * @param public If anyone can read the story it should be true.
   * @param adult If the story contains adult content it should be true.
   * @param tags Tags of the story.
   * @param masterAlias Alias name of the master of the story.
   */
  def create(title: String, public: Boolean, adult: Boolean, tags: Seq[String], masterAlias: String) =
    Story("", title, public, adult, tags, StoryTeller(masterAlias, DEFAULT_COLOR))

}
