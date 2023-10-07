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

import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.TransferMode
import javafx.util.StringConverter
import org.quelea.data.displayable.TimerDisplayable
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.bindSingleSelectedBidirectional
import org.quelea.services.utils.isTimer
import org.quelea.windows.main.actionhandlers.RemoveTimerActionHandler
import org.quelea.windows.main.schedule.SchedulePanel
import org.quelea.windows.timer.TimerIO
import tornadofx.*
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.logging.Level
import kotlin.concurrent.thread
import kotlin.io.path.copyTo


fun findTimerListController(
    dir : String
) = find<TimerListController>(params = mapOf("dir" to dir))
class TimerListController : Controller() {
    var dir: String = params.getValue("dir") as String

    val items = observableListOf<TimerDisplayable>()
    private var updateThread: Thread? = null

    val selectedTimerProperty = objectProperty<TimerDisplayable>()
    val selectedItem by selectedTimerProperty


    fun filesDraggedToTimerList(files: List<File>) {
        files.filter { it.isTimer() && !it.isDirectory() }
            .forEach { f ->
                try {
                    f.absoluteFile.toPath().copyTo(
                        Path.of(dir, f.name),
                        StandardCopyOption.COPY_ATTRIBUTES
                    )
                } catch (ex: IOException) {
                    LoggerUtils.getLogger().log(
                        Level.WARNING,
                        "Could not copy file into TimerPanel through system drag and drop.",
                        ex
                    )
                }
                updateTimers()
            }
    }

    /**
     * Add the files.
     *
     *
     */
    private fun updateTimers() {
        items.clear()
        val files = File(dir).listFiles() ?: return
        if (updateThread != null && updateThread!!.isAlive) return

        updateThread = thread {
            files.forEach {
                runLater {
                    val timer = TimerIO.timerFromFile(it)
                    if (timer != null) items.add(timer)
                }
            }
        }
    }


    fun addSelectedToSchedule() {
        FX.find<SchedulePanel>().scheduleList.add(selectedItem)
    }

    /**
     * Refresh the contents of this video list panel.
     */
    fun refreshTimers() = updateTimers()

    fun changeDir(absoluteFile: File) {
        dir = absoluteFile.absolutePath
    }
}

/**
 * The panel displayed on the library to select the list of videos...
 *
 * @constructor Create a new video list panel.
 * @property dir the absolute path of the currently selected directory
 *
 * @author Ben
 */
class TimerListPanel(
    private val timerController : TimerListController
) : View() {
    lateinit var listView: ListView<TimerDisplayable>
        private set

    override val root = borderpane {
        center {
            scrollpane(fitToWidth = true) {
                listView = listview(timerController.items) {
                    bindSingleSelectedBidirectional(timerController.selectedTimerProperty)
                    setOnDragOver { it.acceptTransferModes(*TransferMode.COPY_OR_MOVE) }
                    onDoubleClick(timerController::addSelectedToSchedule)

                    setOnDragDropped { event ->
                        if (event.gestureSource == null)
                            event.dragboard.files
                                ?.let(timerController::filesDraggedToTimerList)
                    }

                    setCellsWithContextMenu(
                        itemContextmenu = ContextMenu().apply {
                            item(
                                LabelGrabber.INSTANCE.getLabel("remove.timer.text"),
                                graphic = ImageView(Image("file:icons/removedb.png", 16.0, 16.0, false, false)),
                            ).onAction = RemoveTimerActionHandler()
                        }
                    ) {
                        TextFieldListCell<TimerDisplayable>(object : StringConverter<TimerDisplayable?>() {
                            override fun toString(timer: TimerDisplayable?) = timer?.previewText
                            override fun fromString(string: String) = null
                        })
                    }
                }
            }
        }
    }

    init { timerController.refreshTimers() }

    companion object {
        private const val BORDER_STYLE_SELECTED =
            "-fx-padding: 0.2em;-fx-border-color: #0093ff;-fx-border-radius: 5;-fx-border-width: 0.1em;"
        private const val BORDER_STYLE_DESELECTED =
            "-fx-padding: 0.2em;-fx-border-color: rgb(0,0,0,0);-fx-border-radius: 5;-fx-border-width: 0.1em;"
    }
}