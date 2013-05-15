package models

/**
 * Domain class for storing the properties of a user.
 *
 * @param email Email of log in.
 * @param username Public, general user name.
 * @param online Online state.
 * @param readerOfStories The user has read access of these stories, with other words, the user is a reader of the specified stories.
 * @param tellerOfStories The user has r/w access of these stories, with other words, the user is a story teller of the specified stories.
 * @param masterOfStories The user is the master of the specified stories (r/w access, more rights than a story teller).
 * @param tags Tags in (key => (list of values)) format. Name, email, tokens, language is stored as tags to be more dynamic.
 *
 * @author jupi
 */
case class User(email: String,
                username: String,
                online: Boolean,
                readerOfStories: Seq[Story],
                tellerOfStories: Seq[Story],
                masterOfStories: Seq[Story],
                tags: Map[String, Seq[String]])