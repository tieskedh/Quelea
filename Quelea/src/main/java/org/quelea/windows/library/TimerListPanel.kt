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
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.TransferMode
import javafx.util.StringConverter
import org.quelea.data.displayable.TimerDisplayable
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.bindSingleSelectedBidirectional
import org.quelea.windows.main.actionhandlers.RemoveTimerActionHandler
import tornadofx.*



/**
 * The panel displayed on the library to select the list of videos...
 *
 * @constructor Create a new video list panel.
 * @property dir the absolute path of the currently selected directory
 *
 * @author Ben
 */
class TimerListPanel : View() {
    private val timerController = params[CONTROLLER_PARAM] as LibraryTimerController

    override val root = borderpane {
        center {
            scrollpane(fitToWidth = true) {
                listview(timerController.items) {
                    bindSingleSelectedBidirectional(timerController.selectedTimerProperty)
                    selectionModel.selectedIndexProperty()
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
        const val CONTROLLER_PARAM = "controller"

        private const val BORDER_STYLE_SELECTED =
            "-fx-padding: 0.2em;-fx-border-color: #0093ff;-fx-border-radius: 5;-fx-border-width: 0.1em;"
        private const val BORDER_STYLE_DESELECTED =
            "-fx-padding: 0.2em;-fx-border-color: rgb(0,0,0,0);-fx-border-radius: 5;-fx-border-width: 0.1em;"
    }
}