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
package org.quelea.windows.library

import javafx.beans.value.ObservableValue
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.QueleaProperties.Companion.get
import tornadofx.*
import java.util.logging.Level


class LibraryPanelController: Controller() {
    private val controller = find<LibraryTimerController>()
    private val _timerPanelVisible = booleanProperty(
        !get().timerDir.listFiles().isNullOrEmpty()
    )
    val timerPanelVisible : ObservableValue<Boolean> = _timerPanelVisible

    /**
     * Method to force the display of timers folder
     */
    fun forceTimer() {
        _timerPanelVisible.set(true)
        controller.refreshTimers()
    }
}

/**
 * The panel that's used to display the library of media (pictures, video) and
 * songs. Items can be selected from here and added to the order of service.
 *
 * @author Michael
 */
class LibraryPanel : View() {
    /**
     * The contoller for the library song.
     */
    @Deprecated("use FX instead", ReplaceWith(
        "FX.find<LibrarySongController>()",
        "tornadofx.FX",
        "org.quelea.windows.library.LibrarySongController"
    ))
    val librarySongController by inject<LibrarySongController>()

    /**
     * Get the library bible panel.
     *
     * @return the library bible panel.
     */
    lateinit var biblePanel: LibraryBiblePanel
        private set

    /**
     * Get the library video panel.
     *
     * @return the library video panel.
     */
    var videoPanel: LibraryVideoPanel? = null
        private set

    lateinit var tabPane: TabPane
        private set


    val controller by inject<LibraryPanelController>()


    override val root = vbox {
        LOGGER.log(Level.INFO, "Creating library panel")
        tabPane = tabpane {
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }


            tab(
                LabelGrabber.INSTANCE.getLabel("library.songs.heading")
            ) {
                isClosable = false

                LOGGER.log(Level.INFO, "Creating library song panel")
                add<LibrarySongPanel>()
            }

            tab(
                LabelGrabber.INSTANCE.getLabel("library.bible.heading")
            ) {
                isClosable = false

                LOGGER.log(Level.INFO, "Creating library bible panel")
                biblePanel = opcr(this, LibraryBiblePanel())
            }


            tab(
                LabelGrabber.INSTANCE.getLabel("library.image.heading")
            ) {
                isClosable = false
                LOGGER.log(Level.INFO, "Creating library image panel")
                add<LibraryImagePanel>()
            }

            if (get().displayVideoTab) tab(
                LabelGrabber.INSTANCE.getLabel("library.video.heading")
            ) {
                isClosable = false
                LOGGER.log(Level.INFO, "Creating library video panel")
                videoPanel = opcr(this, LibraryVideoPanel())
            }

            tab(
                LabelGrabber.INSTANCE.getLabel("library.timer.heading")
            ) {
                isClosable = false
                LOGGER.log(Level.INFO, "Creating library timer panel")
                add<LibraryTimerPanel>()
                visibleWhen(controller.timerPanelVisible)
            }
        }
    }



    companion object {
        private val LOGGER = LoggerUtils.getLogger()
    }
}