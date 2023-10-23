package org.quelea.data.bible

import org.quelea.data.displayable.BiblePassage
import org.quelea.services.languages.LabelGrabber
import org.quelea.utils.javaTrim
import org.quelea.windows.main.schedule.SchedulePanel
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

    val bibleList = observableListOf(ALL_BIBLE_VERSIONS)
    val bibleFilterProp = stringProperty(ALL_BIBLE_VERSIONS).apply {
        onChange { update() }
    }

    var bibleFilter : String? by bibleFilterProp

    val isBibleSelected get() = bibleFilter != ALL_BIBLE_VERSIONS || bibleFilter == null
    val showLoading = booleanProperty(false)


    init {
        BibleManager.runOnIndexInit { cannotSearch.value = false }
        BibleManager.registerBibleChangeListener(invokeImmediately = true) {
            bibleList.clear()
            bibleList.add(ALL_BIBLE_VERSIONS)
            bibleList.addAll(BibleManager.biblesList.map { it.name })
            bibleFilterProp.value = ALL_BIBLE_VERSIONS
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
        searchResultCount.set(-1)
        versesOfCurrentChapter.clear()
    }


    class TreeViewData(
        verses : List<BibleVerse>,
        bibleSelected : Boolean
    ) {
        val chapterToVerse = verses.groupBy { it.chapter }
        val bookToChapter = chapterToVerse.keys.groupBy { it.book }
        val bibleToBook = bookToChapter.keys.groupBy { it.bible }
        val treeRootElements : List<BibleInterface> = when {
            !bibleSelected -> bibleToBook.keys.toList()
            else -> when(val bible = bibleToBook.keys.firstOrNull()) {
                null -> emptyList()
                else -> bibleToBook[bible].orEmpty()
            }
        }
    }

    val selectedElement = objectProperty<BibleInterface?>()

    val treeViewData = objectProperty(TreeViewData(emptyList(), false))
    fun updateBibleMatches() {
        treeViewData.value = TreeViewData(filteredVerses, isBibleSelected)
    }

    fun refresh() {
        BibleManager.takeUnless { it.isIndexInit }?.refreshAndLoad()
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


    fun addToSchedule() {
        val chap = (selectedElement.value as? BibleVerse)?.parent
            ?: return

        val passage = BiblePassage(
            chap.parent.parent.bibleName,
            "${chap.book} $chap",
            chap.verses,
            false
        )
        FX.find<SchedulePanel>().scheduleList.add(passage)
    }

    companion object{
        val ALL_BIBLE_VERSIONS : String =
            LabelGrabber.INSTANCE.getLabel("all.text")
    }
}
