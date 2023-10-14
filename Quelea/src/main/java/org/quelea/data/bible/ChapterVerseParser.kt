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

import org.quelea.utils.javaTrim

/**
 * Parses an input string into from / to chapter / verse.
 * This class accepts an input string that's base 1
 * (i.e. 1:1 is the first verse of the first chapter)
 * but for ease of transition gives output in base 0
 * (so the above input would give all values returning as 0.)
 *
 * @constructor Create the parser and parse the given string.
 * - Strings must be in the format fromchapter:fromverse-tochapter:toverse
 * - the entire "to" section can be omitted if it's just one verse.
 * - The tochapter can also be left out if the from and to chapters are the same.
 * @param str the string to parse.
 *
 *
 * @author Michael
 */
class ChapterVerseParser(str: String) {
    private var rawFromChapter = -1
    private var rawFromVerse = -1
    private var rawToChapter = -1
    private var rawToVerse = -1

    init {
        try {
            parseFull(str.javaTrim())
        } catch (ex: Exception) {
            //Just ignore, invalid input
        }
    }


    /**
     * Show the parsed value
     */
    override fun toString() = "$fromChapter:$fromVerse-$toChapter:$toVerse"

    /**
     * Get the starting chapter.
     * @return the starting chapter.
     */
    val fromChapter get() = rawFromChapter - 1

    /**
     * Get the starting verse.
     * @return the starting verse.
     */
    val fromVerse get() = if (rawFromVerse == -1) 0 else rawFromVerse


    /**
     * Get the ending chapter.
     * @return the ending chapter.
     */
    val toChapter
        get() = when {
            rawToChapter == -1 -> rawFromChapter - 1
            else -> rawToChapter - 1
        }

    /**
     * Get the ending verse.
     * @return the ending verse.
     */
    val toVerse get() = if (rawToVerse == -1) rawFromVerse else rawToVerse

    /**
     * Contains only one chapter
     * @return true if only one chapter.
     */
    val isSingleChapter: Boolean get() = fromChapter == toChapter


    /**
     * Parse a full string (mustn't have whitespace, call trim() first)
     * @param str the string to parse.
     */
    private fun parseFull(str: String) {
        var str = when {
            str.endsWith(":") -> str.dropLast(1)
            str.endsWith("-") -> str + "1000"
            else -> str
        }

        if (str.last() != '-' && "-" in str) {
            val toStr = str.substringAfter('-')
            parseToStr(toStr)
            str = str.substringBefore('-')
        }
        parseFromStr(str)
    }

    /**
     * Parse the part of the string that sets the "to" chapter and verse.
     * @param str the string to parse.
     */
    private fun parseToStr(str: String) {
        rawToChapter = getChapterTo(str)
        rawToVerse = getVerseTo(str)
    }

    /**
     * Parse the part of the string that sets the "from" chapter and verse.
     * @param str the string to parse.
     */
    private fun parseFromStr(str: String) {
        rawFromChapter = getChapterFrom(str)
        rawFromVerse = getVerseFrom(str)
    }

    /**
     * Get the "from chapter" part of a single chapter:verse declaration.
     * @param str the string to parse.
     */
    private fun getChapterFrom(str: String) = when {
        ":" in str -> str.split(':')[0]
        else -> str
    }.javaTrim().toInt()

    /**
     * Get the "from verse" part of a single chapter:verse declaration.
     * @param str the string to parse.
     */
    private fun getVerseFrom(str: String) = if (':' in str) {
        str.split(':')[1].javaTrim().toInt()
    } else -1

    /**
     * Get the "to chapter" part of a single chapter:verse declaration.
     * @param str the string to parse.
     */
    private fun getChapterTo(str: String): Int = if (":" in str) {
        str.split(':')[0].javaTrim().toInt()
    } else -1

    /**
     * Get the "to verse" part of a single chapter:verse declaration.
     * @param str the string to parse.
     */
    private fun getVerseTo(str: String) = when {
        ":" in str -> str.split(':')[1]
        else -> str
    }.javaTrim().toInt()
}
