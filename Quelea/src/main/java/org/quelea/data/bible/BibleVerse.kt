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
import org.quelea.utils.javaTrim
import org.w3c.dom.Node
import java.io.Serializable

/**
 * A verse in the bible.
 * @constructor for interal use only
 *
 * @property verseText the textual content of the verse.
 * @author Michael
 */
class BibleVerse private constructor(
    override val num: Int,
    val verseText: String,
    var chapterNum : Int,
    chapter: BibleChapter?
) : BibleInterface, Serializable {

    /**
     * Get the chapter this verse is part of.
     *
     * @return the chapter this verse is part of.
     */
    lateinit var chapter: BibleChapter
        private set

    init {
        chapter?.let { setChapter(it) }
    }

    override fun hashCode(): Int {
        var hash = 5
        hash = 97 * hash + verseText.hashCode()
        hash = 97 * hash + num
        return hash
    }

    override fun equals(other: Any?): Boolean = when {
        other !is BibleVerse -> false
        verseText != other.verseText -> false
        else -> num == other.num
    }

    /**
     * Set the chapter this verse is part of.
     *
     * @param chapter the chapter this verse is part of.
     */
    fun setChapter(chapter: BibleChapter) {
        this.chapter = chapter
        chapterNum = chapter.num
    }

    /**
     * Generate an XML representation of this verse.
     *
     * @return an XML representation of this verse.
     */
    fun toXML() = buildString {
        append("<vers cnumber=\"")
        append(chapterNum)
        append("\" vnumber=\"")
        append(num)
        append("\">")
        append(verseText.escapeXML())
        append("</vers>")
    }

    /**
     * Get this verse as a string.
     *
     * @return this verse as a string.
     */
    override fun toString(): String = "$num $verseText"

    override val text = verseText
    override val name = "$num $verseText"

    override val parent: BibleInterface
        get() = chapter

    companion object {
        /**
         * Parse some XML representing this object and return the object it
         * represents.
         *
         * @param node the XML node representing this object.
         * @return the object as defined by the XML.
         */
        @Suppress("SimpleRedundantLet") // otherwise a lot of null checks
        @JvmStatic
        fun parseXML(node: Node): BibleVerse? {
            val (cNumber, chapter) = node.attributes.getNamedItem("cnumber")?.let { cNumber ->
                cNumber.textContent.toInt().let {
                    it to BibleChapter.parseXML(node.parentNode, it)
                }
            } ?: (0 to null)

            return try {
                val num = node.attributes.run {
                    val simpleNumberNode = getNamedItem("vnumber")
                        ?: getNamedItem("number")
                        ?: getNamedItem("n")
                        ?: getNamedItem("id")

                    simpleNumberNode?.nodeValue?.javaTrim()?.toInt()
                        ?: getNamedItem("osisID")?.let {
                            it.nodeValue.javaTrim().split("\\.".toRegex())
                                .dropLastWhile(String::isEmpty)
                                .last()
                                .toInt()
                        }
                } ?: 0

                val verseText = node.textContent.replace('\n', ' ').javaTrim()

                BibleVerse(num, verseText, cNumber, chapter)
            } catch (nfe: NumberFormatException) {
                null
            }
        }
    }
}
