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

import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.escapeXML
import org.quelea.utils.asSequence
import org.quelea.utils.javaTrim
import org.quelea.utils.nameMatchesAny
import org.w3c.dom.Node
import java.io.Serializable
import java.util.logging.Level

/**
 * A book in the bible.
 *
 * @constructor Create a new book.
 * @property bookNumber the number of the book.
 * @property bookName the name of the book.
 * @property bible the bible this book is part of.
 * @author Michael
 */
class BibleBook private constructor(
    val bookNumber: Int,
    val bookName: String,
    val bSName: String,
    val chapterList: List<BibleChapter>
) : BibleInterface, Serializable {

    init { chapterList.onEach { it.book = this } }


    private val caretPosList = mutableListOf<Int>()
    lateinit var bible: Bible

    override fun hashCode(): Int {
        var hash = 7
        hash = 43 * hash + bookNumber
        hash = 43 * hash + bookName.hashCode()
        hash = 43 * hash + chapterList.hashCode()
        return hash
    }

    override fun equals(other: Any?): Boolean = when {
        other !is BibleBook -> false
        bookNumber != other.bookNumber -> false
        bookName != other.bookName -> false
        else -> chapterList == other.chapterList
    }

    /**
     * Get the caret index of the chapter when used with the getHTML() method.
     *
     * @param num the number of the chapter in which to get the index.
     * @return
     */
    fun getCaretIndexOfChapter(num: Int) = caretPosList[num - 1]

    /**
     * Generate an XML representation of this book.
     *
     * @return an XML representation of this book.
     */
    fun toXML() = buildString {
        append("<biblebook bnumber=\"")
        append(bookNumber)
        append("\" bname=\"")
        append(bookName.escapeXML())
        append("\">")
        chapterList.joinTo(this, "", transform = BibleChapter::toXML)
        append("</biblebook>")
    }

    /**
     * Get the name of the book.
     *
     * @return the book's name.
     */
    override fun toString(): String = bookName

    /**
     * Get a specific chapter from this book.
     *
     * @param i the chapter number to get.
     * @return the chapter at the specified number, or null if it doesn't exist.
     */
    fun getChapter(i: Int): BibleChapter? = chapterList.getOrNull(i)

    /**
     * Get all the chapters in this book.
     *
     * @return all the chapters in the book.
     */
    fun getChapters(): Array<BibleChapter> = chapterList.toTypedArray<BibleChapter>()
    override val num = bookNumber
    override val name = bookName

    /**
     * Get the text of this chapter as nicely formatted HTML.
     *
     * @return the text of this chapter.
     */
    override val text by lazy {
        buildString(1000) {
            caretPosList.clear()
            var pos = 0
            for (chapter in getChapters()) {
                caretPosList.add(pos)
                val numStr = chapter.num.toString()
                pos += numStr.length
                append("Chapter ").append(numStr)
                appendLine()
                chapter.verses.joinTo(this, " ") { verse ->
                    verse.text.also {
                        pos += it.length
                    }
                }
                appendLine(' ')
            }
        }
    }

    override val parent : Bible get() = bible

    companion object {
        private val LOGGER = LoggerUtils.getLogger()

        /**
         * Parse some XML representing this object and return the object it
         * represents.
         *
         * @param node the XML node representing this object.
         * @param defaultBookNum the default book number if none is available on the
         * XML file.
         * @return the object as defined by the XML.
         */
        @JvmStatic
        fun parseXML(node: Node, defaultBookNum: Int, defaultBookName: String): BibleBook {

            val bookNumber = node.attributes.run {
                getNamedItem("bnumber")
                    ?: getNamedItem("number")
                    ?: getNamedItem("id")
            }?.nodeValue?.javaTrim()?.toInt() ?: defaultBookNum

            val bookName = node.attributes.run {
                getNamedItem("bname")
                    ?: getNamedItem("n")
                    ?: getNamedItem("name")
                    ?: getNamedItem("osisID")
            }?.nodeValue ?: defaultBookName

            val bSName = node.attributes.getNamedItem("bsname")?.nodeValue
                ?: bookName


            val chapters = node.childNodes.asSequence().mapIndexedNotNull {  i, item->
                item.takeIf { it.nameMatchesAny("chapter", "c") }
                    ?.let { BibleChapter.parseXML(it, i) }
            }.toList()

            val ret = BibleBook(bookNumber, bookName, bSName, chapters)


            LOGGER.log(Level.INFO, "Parsed " + ret.chapterList.size + " chapters in " + ret.bookName)
            return ret
        }
    }
}
