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
package org.quelea.data.bible

import org.quelea.services.utils.escapeXML
import org.quelea.utils.asSequence
import org.w3c.dom.Node
import java.io.Serializable
import java.util.*

/**
 * General information about a specified bible.
 * @property attributes all the attributes of this bible.
 * @author Michael
 */
class BibleInfo(
    val attributes : Map<String, String>
) : Serializable {
    override fun hashCode(): Int {
        var hash = 3
        hash = 37 * hash + attributes.hashCode()
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BibleInfo) return false
        return attributes == other.attributes
    }

    /**
     * Generate an XML representation of this bible info object.
     *
     * @return an XML representation of this bible info object.
     */
    fun toXML() = buildString {
        append("<information>")
        attributes.forEach { (key, value) ->
            append('<')
            append(key.escapeXML())
            append('>')
            append(value.escapeXML())
            append("</")
            append(key.escapeXML())
            append('>')
        }
        append("</information>")
    }

    companion object {
        /**
         * Parse some XML representing this object and return the object it
         * represents.
         *
         * @param info the XML node representing this object.
         * @return the object as defined by the XML.
         */
        fun parseXML(info: Node): BibleInfo {
            val attributes = info.childNodes.asSequence()
                .filter { "#" !in it.nodeName }
                .associate { it.nodeName to it.textContent }

            return BibleInfo(attributes)
        }
    }
}
