package org.quelea.data.bible

import org.quelea.services.utils.LoggerUtils
import java.util.logging.Level

object BibleBookNameUtil {
    private val LOGGER = LoggerUtils.getLogger()
    private val BOOK_NAME_STRATEGIES = buildMap {
        this[66] = listOf(
            "Genesis",
            "Exodus",
            "Leviticus",
            "Numbers",
            "Deuteronomy",
            "Joshua",
            "Judges",
            "Ruth",
            "1 Samuel",
            "2 Samuel",
            "1 Kings",
            "2 Kings",
            "1 Chronicles",
            "2 Chronicles",
            "Ezra",
            "Nehemiah",
            "Esther",
            "Job",
            "Psalms",
            "Proverbs",
            "Ecclesiastes",
            "Song of Solomon",
            "Isaiah",
            "Jeremiah",
            "Lamentations",
            "Ezekiel",
            "Daniel",
            "Hosea",
            "Joel",
            "Amos",
            "Obadiah",
            "Jonah",
            "Micah",
            "Nahum",
            "Habakkuk",
            "Zephaniah",
            "Haggai",
            "Zechariah",
            "Malachi",
            "Matthew",
            "Mark",
            "Luke",
            "John",
            "Acts",
            "Romans",
            "1 Corinthians",
            "2 Corinthians",
            "Galatians",
            "Ephesians",
            "Philippians",
            "Colossians",
            "1 Thessalonians",
            "2 Thessalonians",
            "1 Timothy",
            "2 Timothy",
            "Titus",
            "Philemon",
            "Hebrews",
            "James",
            "1 Peter",
            "2 Peter",
            "1 John",
            "2 John",
            "3 John",
            "Jude",
            "Revelation"
        )
    }

    @JvmStatic
    fun getBookNameForIndex(index: Int, length: Int): String {
        LOGGER.log(Level.INFO, "Getting book name if possible: length $length index $index")
        val bookNames = BOOK_NAME_STRATEGIES[length]
        return if (bookNames == null) {
            LOGGER.log(Level.INFO, "No book names known for length$length")
            "Book " + (index + 1)
        } else {
            bookNames[index]
        }
    }
}
