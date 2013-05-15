package models

import play.api.libs.json.Json

/**
 * Domain class for storing the properties of a story.
 *
 * @param name Name of the story.
 * @param master Master story teller.
 * @param storyTellers List of story tellers.
 * @param readerIds List of the IDs of the readers.
 * @param public Public flag. Indicates that all users can read the story.
 *
 * @author jupi
 *
 */
case class Story(name: String,
                 master: StoryTeller,
                 storyTellers: Seq[StoryTeller] = Nil,
                 readerIds: Seq[String] = Nil,
                 public: Boolean = true)
