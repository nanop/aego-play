package models

import java.awt.Color

/**
 * Domain class for storing the properties of a story teller user. All data inside this class attached to a story.
 *
 * @param alias Alias name of the story teller in a story.
 * @param color Text color of a story teller in a story.
 * @param description Description of a story teller in a story.
 *
 * @author jupi
 *
 */
case class StoryTeller(alias: String, color: String, description: String = "")