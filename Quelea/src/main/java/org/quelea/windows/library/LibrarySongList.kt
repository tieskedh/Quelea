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

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ListCell
import javafx.scene.input.*
import javafx.scene.layout.HBox
import org.quelea.data.displayable.SongDisplayable
import org.quelea.services.utils.bindSingleSelectedBidirectional
import org.quelea.utils.isCtrlClick
import org.quelea.windows.main.widgets.AddSongPromptOverlay
import org.quelea.windows.main.widgets.LoadingPane
import tornadofx.*


/**
 * The list that displays the songs in the library.
 *
 * @constructor Create a new library song list.
 * @property popup show popup menu when right-clicking a listItem.
 * @author Michael
 */
class LibrarySongList : View() {
    private val popup = params["popup"] as Boolean

    private lateinit var loadingOverlay: LoadingPane
    private lateinit var addSongOverlay: AddSongPromptOverlay

    private val controller = find<LibrarySongController>()

    companion object{
        @JvmStatic
        fun create(popup: Boolean) = find<LibrarySongList>(params = mapOf("popup" to popup))
    }

    override val root = stackpane {
        alignment = Pos.CENTER
        listview(controller.items) {
            bindSingleSelectedBidirectional(controller.selectedValueProp)
            onUserSelect(action = controller::addSelectedSongToSchdule)
            focusedProperty().onChange(controller::onListViewFocusChanged)
            setOnMouseClicked {
                if (it.isCtrlClick) controller.setSelectedSongToPreview()
            }

            if (popup) {
                setCellsWithContextMenu(LibraryPopupMenu()){
                    buildCell()
                }
            }
        }

        addSongOverlay = AddSongPromptOverlay(
            controller.showAddSongOverlay
        ).attachTo(this){
            controller.items.onChange { change ->
                if (change.list.isNullOrEmpty()) show()
                else hide()
            }
            show()
        }
        loadingOverlay = LoadingPane(controller.loadingProperty).attachTo(this){
            controller.showLoading.onChange {
                if (it) show() else hide()
            }
        }



        LibrarySongPreviewCanvas(
            visibleProp = controller.previewVisible,
            displayableProp = controller.selectedValueProp
        ).root.attachTo(this) {
            stackpaneConstraints {
                alignment = Pos.BOTTOM_RIGHT
                margin = Insets(10.0)
            }
        }
    }


    private fun buildCell() : ListCell<SongDisplayable?> = object : ListCell<SongDisplayable?>() {
        init {
            setOnDragDetected {
                val displayable = item ?: return@setOnDragDetected
                startDragAndDrop(*TransferMode.ANY).setContent {
                    put(SongDisplayable.SONG_DISPLAYABLE_FORMAT, displayable)
                }
                it.consume()
            }
        }

        override fun updateItem(item: SongDisplayable?, empty: Boolean) {
            super.updateItem(item, empty)
            if (empty) {
                graphic = null
                return
            }

            item!!
            graphic = HBox().apply {
                if (item.lastSearch == null) {
                    text(item.title) {
                        styleClass.add("text")
                    }
                } else {
                    val startIndex = item.title.lowercase().indexOf(
                        item.lastSearch.lowercase()
                    )

                    if (startIndex == -1) {
                        text(item.title) {
                            styleClass.add("text")
                        }
                    } else {
                        text(item.title.take(startIndex)) {
                            styleClass.add("text")
                        }
                        text(item.title.substring(startIndex, startIndex + item.lastSearch.length)) {
                            style = "-fx-font-weight:bold;"
                            styleClass.add("text")
                        }
                        text(item.title.drop(startIndex + item.lastSearch.length)) {
                            styleClass.add("text")
                        }
                    }
                }
            }
        }
    }
}
