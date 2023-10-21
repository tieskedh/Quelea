package org.quelea.data.bible

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

    val bibleFilterProp = stringProperty(allBibleVersions)
    var bibleFilter : String by bibleFilterProp
    val bibleList = observableListOf<String>()

    val isBibleSelected get() = bibleFilter != allBibleVersions

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

    private interface ExecRunnable : Runnable {
        fun cancel()
    }


    companion object{
        val allBibleVersions : String =
            LabelGrabber.INSTANCE.getLabel("all.text")
    }
}

object ResetAndExpandRoot : FXEvent()