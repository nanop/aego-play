package models

import play.api.libs.json.Json

/**
 * Context of the text of a {@link Phrase}.
 *
 * @author jupi
 */
case class Context(ctx: String)

object Context {
  implicit val format = Json.format[Context]
}

/**
 * Rule of an RPG or a convention of a Story.
 */
object RULE extends Context("rule")

/**
 * Dice, refers that the text of the phrase is a value of some dice.
 */
object DICE extends Context("dice")

/**
 * The storyteller or her character is doing some action.
 */
object ACTION extends Context("action")

/**
 * Express some emotion.
 */
object EMOTE extends Context("emote")

/**
 * Thought of a storyteller or her character.
 */
object THOUGHT extends Context("thought")

/**
 * Denotes a sound effect.
 */
object SOUND_EFFECT extends Context("sound")

/**
 * The phrase is offtopic/off-story, unrelated of the story.
 */
object OFFTOPIC extends Context("off")

/**
 * The phrase is unrelated of the story, related to a real-life action or thought.
 */
object IRL extends Context("IRL")