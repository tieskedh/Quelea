package org.quelea.data.bible

import javafx.collections.ObservableList
import org.quelea.services.languages.LabelGrabber
import org.quelea.utils.javaTrim
import tornadofx.*
import java.util.concurrent.Executors

data class VerseText(
    val text : String,
    val isSelected : Boolean,
)

class BibleSearchController : Controller() {
    val versesOfCurrentChapter = observableListOf<VerseText>()
    val searchResultCount = intProperty(-1)

    val searchTextProp = stringProperty("").apply {
        onChange { update() }
    }
    val cannotSearch = booleanProperty(true)

    val bibleFilterProp = stringProperty(allBibleVersions).apply {
        onChange { if (it==null) value = allBibleVersions }
    }

    var bibleFilter : String? by bibleFilterProp
    val bibleList = observableListOf<String>()

    val isBibleSelected get() = bibleFilter != allBibleVersions || bibleFilter == null

    val showLoading = booleanProperty(false)


    init {
        BibleManager.runOnIndexInit { cannotSearch.value = false }
        BibleManager.registerBibleChangeListener(invokeImmediately = true) {
            bibleList.clear()
            bibleList.add(allBibleVersions)
            bibleList.addAll(BibleManager.biblesList.map { it.name })
            bibleFilterProp.value = allBibleVersions
        }
    }

    private val updateExecutor = Executors.newSingleThreadExecutor()
    private var lastUpdateRunnable : ExecRunnable? = null

    val filteredVerses = observableListOf<BibleVerse>()
        .onChange { updateBibleMatches() }

    fun update() {
        val text = searchTextProp.value
        if (text.length > 3) {
            if (BibleManager.isIndexInit) {
                fire(ResetAndExpandRoot)
                showLoading.value = true
                val execRunnable = object : ExecRunnable {
                    private var cancel = false
                    override fun cancel() {
                        cancel = true
                    }

                    override fun run() {
                        if (cancel) return

                        val results = BibleManager.index.filter(text, null)
                        runLater {
                            val newFilteredVersesList =  if (text.javaTrim().isNotEmpty()) {
                                results.asSequence().filter { chapter ->
                                    !isBibleSelected ||
                                            bibleFilter == chapter.book.bible.name
                                }.flatMap(BibleChapter::verses)
                                    .filter { it.text.contains(text, ignoreCase = true) }
                                    .toList()
                            } else listOf()

                            filteredVerses.setAll(newFilteredVersesList)
                            searchResultCount.set(filteredVerses.count())
                            showLoading.value = false
                        }
                    }
                }
                lastUpdateRunnable?.cancel()
                lastUpdateRunnable = execRunnable
                updateExecutor.submit(execRunnable)
            }
        }
        fire(ResetAndExpandRoot)
        searchResultCount.set(-1)
        versesOfCurrentChapter.clear()
    }


    val bibleMatches = observableListOf<Bible>()
    val bibleVersionToBook = mutableMapOf<Bible, ObservableList<BibleBook>>()
    val bibleBookToChap = mutableMapOf<BibleBook, ObservableList<BibleChapter>>()
    val bibleChapToVerse = mutableMapOf<BibleChapter, ObservableList<BibleVerse>>()

    val treeRootElements = observableListOf<BibleInterface>()
    fun updateBibleMatches() {
        val verses = if (isBibleSelected) filteredVerses.filter {
            it.chapter.book.bible.bibleName == bibleFilter
        } else filteredVerses

        val chapterToVerse = verses.groupBy { it.chapter }

        val bookToChapter = chapterToVerse.keys.groupBy { it.book }
        val bibleToBook = bookToChapter.keys.groupBy { it.bible }


        bibleMatches.setAll(bibleToBook.keys)

        bibleChapToVerse.keys.retainAll(chapterToVerse.keys)
        bibleVersionToBook.keys.retainAll(bibleToBook.keys)
        bibleBookToChap.keys.retainAll(bookToChapter.keys)

        bibleToBook.forEach { (bible, books) ->
            bibleVersionToBook.getOrPut(bible, ::observableListOf)
                .setAll(books)
        }

        chapterToVerse.forEach { (chapter, verses) ->
            bibleChapToVerse.getOrPut(chapter, ::observableListOf)
                .setAll(verses)
        }

        bookToChapter.forEach { (book, chapters) ->
            bibleBookToChap.getOrPut(book, ::observableListOf)
                .setAll(chapters)
        }



        treeRootElements.setAll(when{
            !isBibleSelected -> bibleMatches
            else -> when(val bible = bibleMatches.firstOrNull()) {
                null -> emptyList()
                else -> bibleToBook[bible]
            }
        })
    }

    fun onVerseSelected(selectedVerse: BibleVerse) {
        val x = selectedVerse.num - 1
        val chapterVerses = selectedVerse.parent.verses
        versesOfCurrentChapter.clear()
        chapterVerses.mapIndexedTo(versesOfCurrentChapter) { index, bibleVerse ->
            VerseText(
                text="$bibleVerse ",
                isSelected = index == x
            )
        }
    }

    private interface ExecRunnable : Runnable {
        fun cancel()
    }


    companion object{
        val allBibleVersions : String =
            LabelGrabber.INSTANCE.getLabel("all.text")
    }
}

object ResetAndExpandRoot : FXEvent()