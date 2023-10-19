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

import org.quelea.services.utils.*
import org.quelea.utils.asSequence
import org.quelea.utils.nameMatches
import org.quelea.utils.nameMatchesAny
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.util.logging.Level
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * A bible containing a number of books as well as some information.
 *
 * @constructor Create a new bible.
 *
 * @property bibleName the name of the bible.
 * @property information the [BibleInfo] object providing general information about the bible.
 * @author Michael
 */
class Bible private constructor(
    val bibleName: String,
    val information : BibleInfo? = null,
    val bookList : List<BibleBook>
) : BibleInterface, Serializable {

    init { bookList.onEach { it.bible = this } }

    /**
     * @return the path of the file this bible has been read from on null if n.a.
     */
    var filePath: String? = null

    override fun hashCode(): Int {
        var hash = 5
        hash = 19 * hash + bibleName.hashCode()
        hash = 19 * hash + information.hashCode()
        hash = 19 * hash + bookList.hashCode()
        return hash
    }

    override fun equals(other: Any?): Boolean = when {
        other !is Bible -> false
        bibleName != other.bibleName -> false
        information != other.information -> false
        else -> bookList == other.bookList
    }

    /**
     * Generate an XML representation of this bible.
     *
     *
     * @return an XML representation of this bible.
     */
    fun toXML() = buildString {
        append("<xmlbible xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"zef2005.xsd\" version=\"2.0.1.18\" status=\"v\" biblename=\"")
        append(bibleName.escapeXML())
        append("\" type=\"x-bible\" revision=\"0\">")
        information?.let { append(it.toXML()) }
        bookList.joinTo(this, "", transform = BibleBook::toXML)
        append("</xmlbible>")
    }

    /**
     * Get a summary of the bible.
     *
     *
     * @return a summary of the bible.
     */
    override fun toString(): String {
        val abbrev = getAbbreviation(name)
        return buildString {
            append(name)
            if (!("(" in name || ")" in name) && abbrev.length > 1) {
                append(" (").append(abbrev).append(")")
            }
        }
    }

    /**
     * Get all the books currently contained within this bible.
     *
     *
     * @return all the books in the bible.
     */
    fun getBooks(): Array<BibleBook> = bookList.toTypedArray<BibleBook>()

    override val num = -1
    override val text get() = toString()
    override val name get() = bibleName

    override val parent = null

    companion object {
        private val LOGGER = LoggerUtils.getLogger()

        /**
         * Parse a bible from a specified bible and return it as an object.
         *
         *
         * @param file the file where the XML bible is stored.
         * @return the bible as a java object, or null if an error occurred.
         */
        @JvmStatic
        fun parseBible(file: File): Bible? {
            LOGGER.log(Level.INFO, "Parsing bible: " + file.absolutePath)
            return try {
                if (!file.exists()) {
                    LOGGER.log(Level.WARNING, "Couldn''t parse the bible {0} because the file doesn''t exist!", file)
                    return null
                }

                val factory = DocumentBuilderFactory.newInstance()
                val builder = factory.newDocumentBuilder()
                val doc = builder.parse(InputSource(UnicodeReader(file.inputStream(), file.resolveEncoding())))

                val bibleNode = doc.childNodes.asSequence().firstNotNullOfOrNull { item ->
                    when {
                        item.nameMatchesAny("xmlbible", "bible") -> item
                        item nameMatches "osis" -> item.childNodes.asSequence()
                            .firstOrNull { it nameMatches "osisText" }
                        else -> null
                    }
                }

                bibleNode?.let {
                    return parseXML(it, file.nameWithoutExtension)
                }

                LOGGER.log(
                    Level.WARNING,
                    "Couldn''t parse the bible {0} because I couldn''t find any <bible> or <xmlbible> root tags :-(",
                    file
                )
                null
            } catch (ex: Exception) {
                when (ex) {
                    is ParserConfigurationException, is SAXException, is IOException -> {
                        LOGGER.log(Level.WARNING, "Couldn''t parse the bible $file", ex)
                        null
                    }
                    else -> throw ex
                }
            }
        }

        private fun Node.isBibleBookNode() = nameMatchesAny("biblebook", "b", "book") ||
                (nameMatches("div") && attributes.getNamedItem("type")?.nodeValue == "book")

        /**
         * Parse some XML representing this object and return the object it
         * represents.
         *
         *
         * @param node the XML node representing this object.
         * @param defaultName the name of the bible if none is specified in the XML
         * file.
         * @return the object as defined by the XML.
         */
        fun parseXML(node: Node, defaultName: String): Bible {
            val name = node.attributes.run {
                getNamedItem("biblename") ?: getNamedItem("name")
            }?.nodeValue ?: defaultName


            val list = node.childNodes.asSequence().flatMap {
                when {
                    it.nameMatches("testament") -> it.childNodes.asSequence()
                    else -> sequenceOf(it)
                }
            }.filter { it.isBibleBookNode() }
                .toList()

            val books = mutableListOf<BibleBook>()

            var information : BibleInfo? = null
            list.asSequence().forEachIndexed { i, item ->
                if (item.nodeName.equals("information", ignoreCase = true)) {
                    information = BibleInfo.parseXML(item)
                } else if (item.isBibleBookNode()) {
                    val book = BibleBook.parseXML(item, i, BibleBookNameUtil.getBookNameForIndex(i, list.size))
                    books+=book
                }
            }

            LOGGER.log(Level.INFO, "Parsed bible: {0}. Contains {1} books.", arrayOf<Any>(name, books.size))
            return Bible(name, information, books)
        }
    }
}
