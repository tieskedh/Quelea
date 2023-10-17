/* 
 * This file is part of Quelea, free projection software for churches.
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.services.notice

import javafx.scene.paint.Color
import javafx.scene.text.Font
import org.quelea.services.utils.SerializableColor
import org.quelea.services.utils.SerializableFont
import org.quelea.services.utils.escapeXML
import org.quelea.utils.iterator
import org.w3c.dom.Node

/**
 * A notice to be displayed on the bottom of the main projection screen.
 *
 * @constructor Create a new notice.
 * @property text The text of the notice.
 * @param times The number of times to display the notice.
 * @property color The font color.
 * @property font The notice font.
 * @property creationTime The time of creation.
 *
 * @property times The number of times to display the notice.
 *
 * @author Michael
 */
class Notice(
    @JvmField var text: String,
    times: Int,
    @JvmField var color: SerializableColor,
    @JvmField var font: SerializableFont
) {

    var originalTimes: Int = times
        private set


    var times = times
        set(value) {
            field = value
            originalTimes = value
        }


    /**
     * Set the times back to the original times
     */
    fun resetTimes(){
        times = originalTimes
    }

    @JvmField
    var creationTime: Long = System.currentTimeMillis()

    /**
     * Decrement the times - call after the notice has been displayed once.
     */
    fun decrementTimes() {
        times--
    }

    /**
     * Convert to a string.
     *
     * @return the notice text.
     */
    override fun toString() = text

    override fun hashCode(): Int {
        var hash = 7
        hash = 11 * hash + color.hashCode()
        hash = 11 * hash + font.hashCode()
        hash = 11 * hash + text.hashCode()
        hash = 11 * hash + times
        return hash
    }


    override fun equals(other: Any?) = when {
        other == null -> false
        javaClass != other.javaClass -> false
        other !is Notice -> false
        color != other.color -> false
        font != other.font -> false
        text != other.text -> false
        else -> times == other.times
    }

    /**
     * Get the XML that forms this notice.
     *
     *
     * @return the XML.
     */
    val xML: String
        get() = buildString {
            append("<notice>")
            append("<text>")
            append(text.escapeXML())
            append("</text>")
            append("<duration>")
            append(when(val times = originalTimes){
                Int.MAX_VALUE -> 0
                else -> times
            })
            append("</duration>")
            append("<color>")
            append(color.color)
            append("</color>")
            append("<font>")
            append(font.font.name)
            append(",")
            append(font.font.size)
            append("</font>")
            append("</notice>")
        }

    companion object {
        /**
         * Parse some XML representing this object and return the object it
         * represents.
         *
         *
         * @param node the XML node representing this object.
         * @return the object as defined by the XML.
         */
        @JvmStatic
        fun parseXML(node: Node): Notice {
            val list = node.childNodes
            var text = ""
            var duration = 0
            var colorString: String? = "0xffffffff"
            var fontString = "System Regular,50.0"
            for (item in list) {
                when (item.nodeName) {
                    "text" -> text = item.textContent
                    "duration" -> {
                        duration = item.textContent.toInt()
                        if (duration == 0) {
                            duration = Int.MAX_VALUE
                        }
                    }

                    "color" -> colorString = item.textContent
                    "font" -> fontString = item.textContent
                }
            }
            val color = SerializableColor(Color.web(colorString))
            val (fontName, fontSize) = fontString.split(',', limit = 3)
            val font = SerializableFont(Font(fontName, fontSize.toDouble()))
            return Notice(text, duration, color, font)
        }
    }
}
