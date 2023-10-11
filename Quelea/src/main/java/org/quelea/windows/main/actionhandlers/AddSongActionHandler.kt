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
package org.quelea.windows.main.actionhandlers

import javafx.event.ActionEvent
import javafx.event.EventHandler
import org.quelea.data.VideoBackground
import org.quelea.data.displayable.SongDisplayable
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.services.utils.getVidBlankImage
import org.quelea.utils.javaTrim
import org.quelea.windows.library.LibrarySongController
import org.quelea.windows.main.QueleaApp
import tornadofx.*
import kotlin.concurrent.thread

/**
 * The action listener for adding a song, called when something fires off an
 * action that adds a song from the library to the schedule.
 *
 *
 * @author Michael
 */
class AddSongActionHandler(private val updateInDB: Boolean) : EventHandler<ActionEvent?> {
    /**
     * Get the current selected song from the library to the schedule.
     *
     *
     * @param t the event.
     */
    override fun handle(t: ActionEvent?) {
        val libraryPanel = QueleaApp.get().mainWindow.mainPanel.libraryPanel
        val schedulePanel = QueleaApp.get().mainWindow.mainPanel.schedulePanel


        var song = FX.find<LibrarySongController>().selectedValue ?: run{
            LOGGER.warning("No song selected")
            return
        }
        if (get().songOverflow || !updateInDB) {
            song = SongDisplayable(song)
        }
        if (!updateInDB) {
            song.id = -1
            song.setNoDBUpdate()
        }
        if (get().useDefaultTranslation) {
            val defaultTranslation = get().defaultTranslationName
            if (defaultTranslation.javaTrim().isNotEmpty()) {
                song.currentTranslationLyrics = defaultTranslation
            }
        }
        cacheVidPreview(song)
        schedulePanel.scheduleList.add(song)
        libraryPanel.librarySongPanel.searchBox.clear()
    }

    private fun cacheVidPreview(song: SongDisplayable?) {
        val background = song?.sections?.firstOrNull()
            ?.theme?.background as? VideoBackground
            ?: return

        thread {
            background.getVideoFile().getVidBlankImage() //cache it
        }
    }

    companion object{
        private val LOGGER = LoggerUtils.getLogger()
    }
}
