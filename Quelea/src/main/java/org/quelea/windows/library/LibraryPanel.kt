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
import javafx.scene.control.Tab
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

    val selectedTab = intProperty(0)

    private val songController by inject<LibrarySongController>()
    private val bibleController by inject<LibraryBibleController>()

    fun showSongTab(): LibrarySongController {
        selectedTab.set(0)
        return songController
    }
    fun showBibleTab(): LibraryBibleController{
        selectedTab.set(1)
        return bibleController
    }

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
 * @property librarySongController The controller for the library song.
 * @property bibleController The controller for the Bible panell.
 * @author Michael
 */
class LibraryPanel : View() {

    val controller by inject<LibraryPanelController>()


    override val root = vbox {
        LOGGER.log(Level.INFO, "Creating library panel")
        tabpane {
            selectionModel.selectedIndexProperty().onChange {
                controller.selectedTab.set(it)
            }
            controller.selectedTab.onChange { selectionModel.select(it) }

            vboxConstraints {
                vGrow = Priority.ALWAYS
            }

            LOGGER.log(Level.INFO, "Creating library song panel")
            notCloseableTab<LibrarySongPanel>()

            LOGGER.log(Level.INFO, "Creating library bible panel")
            notCloseableTab<LibraryBiblePanel>()

            LOGGER.log(Level.INFO, "Creating library image panel")
            notCloseableTab<LibraryImagePanel>()


            if (get().displayVideoTab) tab(
                LabelGrabber.INSTANCE.getLabel("library.video.heading")
            ) {
                isClosable = false
                LOGGER.log(Level.INFO, "Creating library video panel")
                add(LibraryVideoPanel())
            }


            LOGGER.log(Level.INFO, "Creating library timer panel")
            notCloseableTab<LibraryTimerPanel>{
                visibleWhen(controller.timerPanelVisible)
            }

        }
    }


    private inline fun <reified T : UIComponent> TabPane.notCloseableTab(
        crossinline op : Tab.(T)->Unit = {}
    ) = find<T>().also {
        tab(it.title) {
            isClosable = false
            add(it.root)
            op(it)
        }
    }

    @Deprecated("use FX instead", ReplaceWith(
        "FX.find(LibrarySongController::class.java)",
        "tornadofx.FX",
        "org.quelea.windows.library.LibrarySongController"
    ))
    val librarySongController by inject<LibrarySongController>()

    @Deprecated("use FX instead", ReplaceWith(
        "FX.find(LibraryBibleController::class.java)",
        "tornadofx.FX",
        "org.quelea.windows.library.LibraryBibleController"
    ))
    val bibleController by inject<LibraryBibleController>()



    companion object {
        private val LOGGER = LoggerUtils.getLogger()
    }
}