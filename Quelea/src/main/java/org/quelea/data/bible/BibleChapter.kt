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
import org.quelea.utils.javaTrim
import org.quelea.utils.nameMatchesAny
import org.w3c.dom.Node
import java.io.Serializable
import java.lang.ref.SoftReference
import java.util.*

/**
 * A chapter in the bible.
 *
 * 
 * @constructor Create a new bible chapter.
 * @property num the chapter number (or -1 if it's unknown.)
 * @property verses the verses in this chapter.
 * @property verseMap the verses in this chapter, associated by their number.
 * @property iD the unique ID for this bible chapter.
 * @property book the book this chapter is part of.
 *
 * @author Michael
 */
class BibleChapter private constructor(
    override val num: Int,
    verseList: List<BibleVerse>
) : BibleInterface, Serializable {
    private val verseMap = verseList.associateBy { it.num }
    val verses : List<BibleVerse> get() = verseMap.values.toList()

    init {
        verseList.forEach { it.setChapter(this) }
    }

    @Transient
    private var softRefText: SoftReference<String?>? = null

    val iD = statId++

    lateinit var book: BibleBook


    override fun hashCode(): Int {
        var hash = 3
        hash = 67 * hash + num
        hash = 67 * hash + verses.hashCode()
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BibleChapter) return false
        if (num != other.num) return false
        return verses == other.verses
    }

    /**
     * Generate an XML representation of this chapter.
     *
     *
     * @return an XML representation of this chapter.
     */
    fun toXML() = buildString {
        append("<chapter")
        if (num != -1) {
            append(" cnumber=\"")
            append(num)
            append('\"')
        }
        append(">")
        verses.joinTo(this, "") {
            it.toXML().escapeXML()
        }
        append("</chapter>")
    }

    /**
     * Return the chapter number as a string in this chapter.
     *
     * @return the chapter number as a string.
     */
    override fun toString() = num.toString()

    /**
     * Get a specific verse from this chapter.
     *
     *
     * @param i the verse number to get.
     * @return the verse at the specified number, or null if it doesn't exist.
     */
    fun getVerse(i: Int): BibleVerse? = verseMap[i]

    override val name = toString()

    /**
     * Get all the text in this chapter as a string.
     *
     *
     * @return all the text in this chapter as a string.
     */
    override val text: String
        get() = softRefText?.get() ?: run {
            verses.joinToString(" ")
                .also { softRefText = SoftReference(it) }
        }
    override val parent: BibleBook
        get() = book

    companion object {
        private var statId = 0

        /**
         * Parse some XML representing this object and return the object it
         * represents.
         *
         *
         * @param node the XML node representing this object.
         * @param defaultNum the default chapter number if no other information is
         * available.
         * @return the object as defined by the XML.
         */
        @JvmStatic
        fun parseXML(node: Node, defaultNum: Int): BibleChapter {
            val num = node.attributes.run {
                getNamedItem("cnumber")
                    ?: getNamedItem("number")
                    ?: getNamedItem("n")
                    ?: getNamedItem("id")
            }?.nodeValue?.javaTrim()?.toIntOrNull()
                ?: defaultNum

            val verses = node.childNodes.asSequence()
                .filter { it.nameMatchesAny("verse", "vers", "v") }
                .mapNotNull { BibleVerse.parseXML(it) }
                .toList()

            return BibleChapter(num, verses)
        }
    }
}
