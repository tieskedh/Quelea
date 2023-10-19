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

import org.quelea.data.bible.Bible.Companion.parseBible
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.lucene.BibleSearchIndex
import org.quelea.services.lucene.SearchIndex
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.QueleaProperties
import org.quelea.services.utils.fxRunAndWait
import org.quelea.windows.main.QueleaApp
import org.quelea.windows.main.StatusController
import org.quelea.windows.main.StatusPanel
import tornadofx.*
import java.util.logging.Level
import kotlin.concurrent.thread

/**
 * Loads and manages the available getBibles.
 *
 *
 * @author Michael
 */
object BibleManager {
    private val bibles = mutableListOf<Bible>()
    private val listeners = mutableListOf<BibleChangeListener>()

    /**
     * Get the underlying search index used by this bible manager.
     *
     *
     * @return the search index.
     */
    val index: SearchIndex<BibleChapter> = BibleSearchIndex()

    /**
     * Determine if the search index is initialised.
     *
     *
     * @return true if the index is initialised, false otherwise.
     */
    var isIndexInit: Boolean = false
        private set
    private val onIndexInit = mutableListOf<Runnable>()

    /**
     * Create a new bible manager.
     */
    init {
        loadBibles(false)
    }

    /**
     * Run the given runnable as soon as the index is initialised, or
     * immediately if the index is currently initialised.
     *
     *
     * @param r the runnable to run.
     */
    fun runOnIndexInit(r: Runnable) {
        if (isIndexInit) {
            r.run()
        } else {
            onIndexInit.add(r)
        }
    }

    /**
     * Register a bible change listener on this bible manager. The listener will
     * be activated whenever a change occurs.
     *
     *
     * @param listener the listener to register.
     */
    fun registerBibleChangeListener(listener: BibleChangeListener) {
        listeners.add(listener)
    }

    /**
     * Notify all the listeners that a change has occurred.
     */
    private fun updateListeners() = listeners.forEach { it.updateBibles() }

    /**
     * Get all the bibles held in this manager.
     *
     *
     * @return all the getBibles.
     */
    fun getBibles() = bibles.toTypedArray<Bible>()

    val biblesList: List<Bible>
        get() = bibles.toList()

    fun getBibleFromName(name: String): Bible? =
        bibles.firstOrNull { it.name == name }

    /**
     * Reload bibles and trigger listeners.
     *
     *
     */
    fun refresh() {
        loadBibles(false)
        updateListeners()
    }

    /**
     * Reload bibles, update search index and trigger listeners
     *
     *
     */
    fun refreshAndLoad() {
        loadBibles(true)
        updateListeners()
    }

    /**
     * Reload all the bibles from the bibles directory into this bible manager.
     *
     *
     * @param updateIndex update the search index with new bible structure
     */
    fun loadBibles(updateIndex: Boolean) {
        if (updateIndex) isIndexInit = false

        bibles.clear()
        val biblesFile = QueleaProperties.get().bibleDir
        if (!biblesFile.exists()) biblesFile.mkdir()


        for (file in biblesFile.listFiles()!!) {
            if (file.extension.lowercase() == "xml" || file.extension.lowercase() == "xmm") {
                parseBible(file)?.let { bible ->
                    bible.filePath = file.absolutePath
                    bibles.add(bible)
                }
            }
        }
        if (updateIndex) buildIndex()
    }

    /**
     * Builds the search index from the current bibles.
     */
    fun buildIndex() {
        isIndexInit = false
        val panel = arrayOfNulls<StatusPanel>(1)
        if (QueleaApp.get().mainWindow != null) fxRunAndWait {
            panel[0] = FX.find<StatusController>().addPanel(
                LabelGrabber.INSTANCE.getLabel("building.bible.index")
            ).apply {
                removeCancelButton()
                progressBar.progress = -1.0
            }
        }
        thread {
            LOGGER.log(Level.INFO, "Adding bibles to index")
            val chapters = mutableListOf<BibleChapter>()
            bibles.forEach { bible ->
                LOGGER.log(Level.FINE, "Adding {0} bible to index", bible.name)
                index.clear()
                bible.bookList.flatMapTo(chapters) { it.chapterList }
                LOGGER.log(Level.FINE, "Added {0}.", bible.name)
            }
            index.addAll(chapters)

            LOGGER.log(Level.INFO, "Finished Adding bibles to index")
            isIndexInit = true
            for (r in onIndexInit) r.run()
            onIndexInit.clear()
            runLater {
                panel[0]?.done()
            }
        }
    }

    private val LOGGER = LoggerUtils.getLogger()

    /**
     * Get the instance of this singleton class.
     *
     *
     * @return the instance of this singleton class.
     */
    @JvmStatic
    fun get(): BibleManager = this
}
