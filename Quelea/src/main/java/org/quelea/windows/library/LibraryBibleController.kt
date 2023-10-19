package org.quelea.windows.library

import javafx.beans.property.ReadOnlyListProperty
import javafx.collections.transformation.FilteredList
import org.quelea.data.bible.Bible
import org.quelea.data.bible.BibleBook
import org.quelea.data.bible.BibleManager
import org.quelea.data.bible.BibleVerse
import org.quelea.data.bible.ChapterVerseParser
import org.quelea.data.displayable.BiblePassage
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.utils.javaTrim
import org.quelea.utils.readOnly
import org.quelea.utils.sequenceOfRange
import org.quelea.windows.library.BibleScreenContent.Loaded.BibleScreenPart
import org.quelea.windows.library.BibleScreenContent.Loaded.BibleScreenPart.ChapterHeader
import org.quelea.windows.library.BibleScreenContent.Loaded.BibleScreenPart.Verse
import org.quelea.windows.library.BibleScreenContent.Loaded.SelectVerse
import org.quelea.windows.main.schedule.SchedulePanel
import tornadofx.*

object OpenBibleBookSelector : FXEvent()

class LibraryBibleController : Controller() {
    val verses = observableListOf<BibleVerse>()


    /**
     * All the verses currently shown in the [LibraryBiblePanel].
     */
    fun copyVerseList() = verses.toList()
    
    val booklessBiblePassageRefProp = stringProperty("").apply {
        onChange { updateSelectedVerses() }
    }

    var booklessBiblePassageRef : String by booklessBiblePassageRefProp
    
    val canAddToScheduleProp = booleanProperty()
    var canAddToSchedule by canAddToScheduleProp
    
    val bibles = observableListOf<Bible>()
    private val _bibleBooks = observableListOf<BibleBook>()
    val bibleBooks : ReadOnlyListProperty<BibleBook> = listProperty(_bibleBooks)
    
    
    val selectedBibleBookProp = objectProperty<BibleBook>().apply {
        onChange {  updateSelectedVerses() }
    }

    var selectedBibleBook by selectedBibleBookProp
    val selectedBibleProp = objectProperty<Bible>().apply {
        onChange{ new ->
            val index = _bibleBooks.indexOf(selectedBibleBook)
            _bibleBooks.setAll(new?.bookList.orEmpty())
            selectedBibleBook = bibleBooks.getOrElse(index){
                _bibleBooks.firstOrNull()
            }
        }
    }
    var selectedBible by selectedBibleProp



    private var cVP : ChapterVerseParser? = null

    val bibleScreenContentProp = objectProperty<BibleScreenContent>(BibleScreenContent.Blank)
    var bibleScreenContent by bibleScreenContentProp


    private fun updateBibles() {
        val newBibles = BibleManager.get().biblesList
        bibles.setAll(newBibles)
        selectedBible = newBibles
            .lastOrNull { it.bibleName == get().defaultBible }
            ?: bibles.firstOrNull()
    }
    init {
        updateBibles()
        BibleManager.get().registerBibleChangeListener {
            updateBibles()
        }
    }

    private var multiPart = false
    fun addSelectedPassageToSchedule() {
        if (canAddToSchedule){
            val passage = BiblePassage(
                selectedBible.name,
                bibleLocation,
                copyVerseList(),
                multiPart
            )
            find<SchedulePanel>().scheduleList.add(passage)
            booklessBiblePassageRef = ""
        }
    }



    private val _filteredBibleBooks = FilteredList(bibleBooks)
    val filteredBibleBooks = _filteredBibleBooks.readOnly()
    private val bibleBookFilterProp = stringProperty("").apply {
        onChange { search ->
            val trimmedSearch = search?.filter { it.isLetterOrDigit() }
            if (trimmedSearch.isNullOrBlank()) _filteredBibleBooks.setPredicate { true }
            else _filteredBibleBooks.setPredicate {
                it.bookName.filter { it.isLetterOrDigit() }.startsWith(trimmedSearch, ignoreCase = true) ||
                        it.bSName.filter { it.isLetterOrDigit() }.startsWith(trimmedSearch, ignoreCase = true)
            }
        }
    }
    private var bibleBookFilter by bibleBookFilterProp


    private var resetBibleFilterTask : FXTimerTask? = null
    fun onBibleBookFilterChange(
        update : (String) -> String,
    ){
        resetBibleFilterTask?.cancel()

        val newSearch = update(bibleBookFilter)
        if (bibleBooks.any { it.matchSearch(newSearch) })
            bibleBookFilter = update(bibleBookFilter)

        resetBibleFilterTask = runLater(5.seconds){
            clearBiblebookFilter()
            resetBibleFilterTask?.cancel()
            if (selectedBibleBook !in bibleBooks)
                selectedBibleBook = bibleBooks.firstOrNull()
        }
    }

    private fun BibleBook.matchSearch(search : String?) : Boolean{
        val trimmed = search?.javaTrim()
        return trimmed.isNullOrEmpty() || bookName.startsWith(trimmed, ignoreCase = true) || bSName.startsWith(trimmed, ignoreCase = true)
    }
    fun clearBiblebookFilter() {
        bibleBookFilterProp.set("")// = ""
    }

    /**
     * The book, chapter and verse numbers as a string.
     */
    val bibleLocation: String
        get() = buildString {
            append(selectedBibleBook)
            append(" ")
            append(booklessBiblePassageRef)
        }

    fun updateSelectedVerses() {
        verses.clear()
        val sections = if (":" in booklessBiblePassageRef || "-" !in booklessBiblePassageRef)
            booklessBiblePassageRef
                .split(Regex("([;,])"))
                .dropLastWhile { it.isEmpty() }
        else {
            val (first, second) = booklessBiblePassageRef.split('-', limit = 2)

            (first.toInt()..second.toInt())
                .joinToString(separator = ",")
                .split(',')
                .dropLastWhile { it.isEmpty() }
        }

        if (booklessBiblePassageRef.isEmpty()) {
            canAddToSchedule = false
            bibleScreenContent = BibleScreenContent.Blank
        }

        multiPart = sections.size > 1
        val content = BibleScreenContent.Loaded(get().useDarkTheme)

        for (s in sections) {
            val cVP = ChapterVerseParser(s).also {this.cVP = it }
            val book = selectedBibleBook


            val fromChapterVerses = book?.getChapter(cVP.fromChapter)?.verses
            
            if (fromChapterVerses == null || book.getChapter(cVP.toChapter) == null) {
                canAddToSchedule = false
                bibleScreenContent = BibleScreenContent.Loading
                return
            }

            canAddToSchedule = true
            val toVerse = when(
                cVP.isSingleChapter && cVP.toVerse in fromChapterVerses.indices
            ) {
                true -> cVP.toVerse
                false -> fromChapterVerses.size
            }

            content += ChapterHeader(cVP.fromChapter + 1)


            val oldText = booklessBiblePassageRef
            for (verse in fromChapterVerses) {
                if (verse == null) {
                    content.selectedVerse = SelectVerse.SelectNone
                    continue
                }


                // Scroll to selected verse
                if (!content.isSelectVerseInitialized) {
                    var firstVerse = oldText.replace(
                        Regex("((\\d+)(:\\d+)?-?(\\d+)?([;,]))+((\\d+)(:\\d+)?)(-?\\d+)?"),
                        "$6"
                    )

                    // Remove any non-numeric character in the end
                    if (firstVerse.last() in "-,;") {
                        firstVerse = firstVerse.dropLast(1)
                    }

                    // Find the last number entered for passages separated with a hyphen
                    var lastVerse = ""
                    if ("-" in firstVerse && ":" in firstVerse) {
                        lastVerse = firstVerse.substringBefore(':') + ':' +
                                firstVerse.substringAfter('-')
                        firstVerse = firstVerse.substringBefore('-')
                    }
                    // Delete the last character if it is a colon
                    firstVerse = firstVerse.removeSuffix(":")

                    // Scroll so that the most recent verse entered always is visible
                    content.selectedVerse = when (lastVerse.isNotEmpty()) {
                        true -> SelectVerse.SelectLast(lastVerse)
                        false -> SelectVerse.SelectFirst(firstVerse)
                    }
                }

                // Only add and mark the selected verses but load the others from the chapter as well
                val id = "" + (cVP.fromChapter + 1) + ":" + verse.num

                val isMarked = cVP.fromVerse == 0 || verse.num in cVP.fromVerse..toVerse

                if (isMarked) verses.add(verse)

                content += Verse(num=verse.num, text=verse.text, isMarked=isMarked, id=id)
            }

            val wholeChapters = sequenceOfRange(cVP.fromChapter+1 until cVP.toChapter)
                .flatMap { book.getChapter(it)!!.verses }

            val remainingVerses = if (cVP.isSingleChapter) emptySequence()
            else sequenceOfRange(0 .. cVP.toVerse)
                .mapNotNull(book.getChapter(cVP.toChapter)!!::getVerse)

            val nextChapterVerses = (wholeChapters + remainingVerses)
            verses+= nextChapterVerses
            val rawText =  nextChapterVerses.joinToString(separator = " ") { it.text }
            content += BibleScreenPart.RemainingText(rawText)
        }
        canAddToSchedule = !content.isEmpty()
        if (!content.isSelectVerseInitialized) content.selectedVerse = SelectVerse.SelectNone
        bibleScreenContent = content
    }

    fun onVerseClick(verse : String){
        val oldText = booklessBiblePassageRef
        val verseNum = verse.toInt()
        val chapterNum = oldText.substringBefore(':')

        val cvp = cVP ?: return


        val fromVerse = cvp.fromVerse
        runLater {
            booklessBiblePassageRef = buildString {
                append(chapterNum).append(":")
                when {
                    fromVerse == 0 -> append(verseNum)
                    verseNum > cvp.toVerse -> append(fromVerse).append("-").append(verseNum)
                    else -> append(verseNum).append("-").append(cvp.toVerse)
                }
            }
        }
    }

    fun focusBibleBookSelector() {
        fire(OpenBibleBookSelector)
    }
}

sealed interface BibleScreenContent{
    data object Loading:  BibleScreenContent
    data object Blank : BibleScreenContent
    class Loaded(
        var isDarkTheme : Boolean,
    ) : MutableList<BibleScreenPart> by mutableListOf(), BibleScreenContent{
        @Transient
        lateinit var selectedVerse : SelectVerse
        val isSelectVerseInitialized get() = ::selectedVerse.isInitialized
        sealed interface BibleScreenPart {
            data class ChapterHeader(val chapter : Int) : BibleScreenPart
            data class Verse(val num : Int, val text : String, val id : String, val isMarked : Boolean) : BibleScreenPart

            data class RemainingText(val text : String) : BibleScreenPart
        }

        sealed interface SelectVerse {
            data class SelectFirst(val verse : String) : SelectVerse
            data class SelectLast(val verse : String) : SelectVerse
            data object SelectNone : SelectVerse
        }
    }
}