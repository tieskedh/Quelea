package org.quelea.windows.library

import org.quelea.data.db.SongManager
import org.quelea.data.displayable.SongDisplayable
import org.quelea.services.lucene.SearchIndex
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.utils.javaTrim
import org.quelea.windows.main.MainPanel
import org.quelea.windows.main.actionhandlers.AddSongActionHandler
import tornadofx.*
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.logging.Level
import java.util.regex.Pattern
import kotlin.concurrent.thread

/**
 * Controller for the list that displays the songs in the library.
 *
 * @property selectedValue The currently selected song or null if none is selected.
 */
class LibrarySongController : Controller() {
    val loadingProperty = doubleProperty(-1.0)

    val showLoading = booleanProperty(false)
    var loading by showLoading

    val items = observableListOf<SongDisplayable?>()

    private val filterService = Executors.newSingleThreadExecutor()
    private var filterFuture: Future<*>? = null


    val selectedValueProp = objectProperty<SongDisplayable?>(null)
    var selectedValue by selectedValueProp
        private set


    val previewVisible = booleanProperty(false)


    init {
        thread(block = ::refresh)
        SongManager.get().registerDatabaseListener { refresh() }
        selectedValueProp.onChange { song->
            previewVisible.value = song != null && get().showDBSongPreview
            FX.find<MainPanel>().previewPanel.setDisplayable(
                selectedValue,
                0
            )
        }
    }

    fun refresh() {
        runLater { loading = true }
        val songs = SongManager.get().getSongs(loadingProperty)
        runLater {
            items.setAll(*songs)
            loading= false
        }
    }


    var isFocused = false
        private set
    fun onListViewFocusChanged(focused:  Boolean) {
        isFocused = focused
        previewVisible.value = get().showDBSongPreview && selectedValue == null
    }

    /**
     * Filter the results in this list by a specific search term.
     *
     *
     *
     * @param search the search term to use.
     */
    fun filter(
       search : String?
    ) {
        filterFuture?.cancel(true)
        runLater { loading = true }
        LOGGER.info { "Performing search for $search" }
        filterFuture = filterService.submit {
            val songs = mutableListOf<SongDisplayable>()
            // empty or null search strings do not need to be filtered - lest they get added twice
            if (search == null || search.javaTrim().isEmpty() || Pattern.compile(
                    "[^\\w ]",
                    Pattern.UNICODE_CHARACTER_CLASS
                ).matcher(search).replaceAll("").isEmpty()
            ) {
                LOGGER.info("Empty song search performed")
                songs += SongManager.get().songs.onEach {
                    it.lastSearch = null
                }.toSortedSet()
                LOGGER.info { "${songs.size} songs in list" }
            } else {
                songs.addFiltered(SearchIndex.FilterType.TITLE, "title", search) { lastSearch = search }
                songs.addFiltered(SearchIndex.FilterType.BODY, "lyrics", search) { lastSearch = null }
                songs.addFiltered(SearchIndex.FilterType.AUTHOR, "author", search)
            }
            runLater {
                LOGGER.info("Setting song list")
//                //KOTLINIZE: didn't test if this forces search display update
                items.setAll(songs)

                if (songs.isNotEmpty()) {
                    LOGGER.info("Selecting first song")
                    selectedValue = songs.first()
                }
                LOGGER.info("Setting no longer loading")
                loading = false
                LOGGER.info("Song search done")
            }
        }
    }


    private inline fun MutableList<SongDisplayable>.addFiltered(
        type : SearchIndex.FilterType,
        name : String,
        search : String,
        onEach : SongDisplayable.() -> Unit = {},
    ) {
        LOGGER.info("Filtering songs by $name")
        val matches = SongManager.get().index.filter(search, type)
        LOGGER.log(Level.INFO, "Filtered songs by $name")
        this +=  matches.onEach(onEach).toSortedSet()
        LOGGER.info { "$size songs in list" }
    }

    fun addSelectedSongToSchdule(song : SongDisplayable?) {
        AddSongActionHandler(get().defaultSongDBUpdate).handle(null)
    }

    fun setSelectedSongToPreview() {
        FX.find<MainPanel>().previewPanel.setDisplayable(
            selectedValue,
            0
        )
    }

    companion object {
        private val LOGGER = LoggerUtils.getLogger()
    }
}