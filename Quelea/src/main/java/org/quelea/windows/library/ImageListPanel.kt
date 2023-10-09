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

import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.input.*
import javafx.scene.layout.TilePane
import org.javafx.dialog.Dialog
import org.quelea.services.languages.LabelGrabber
import tornadofx.*

/**
 * The panel displayed on the library to select the list of images.
 *
 *
 * @author Michael
 * @property dir the absolute path of the currently selected directory
 *
 * @constructor Create a new image list panel.
 * @param dir the directory to use.
 */

class ImageListPanel : View() {
    private lateinit var imageList: TilePane

    val controller = find<LibraryImageController>(
        "imageLoader" to ImageLoader(160.0, 90.0)
    )

    override val root = borderpane {
        val borderPane = this
        center {
            scrollpane(fitToWidth = true) {
                imageList = tilepane {
                    alignment = Pos.CENTER
                    hgap = 15.0
                    vgap = 15.0
                    orientation = Orientation.HORIZONTAL
                    setOnDragOver { it.acceptTransferModes(*TransferMode.COPY_OR_MOVE) }

                    setOnDragDropped { event ->
                        if (event.gestureSource == null)
                            event.dragboard.files
                                ?.let(controller::importDraggedImageFiles)
                    }


                    bindChildren(controller.imageItems){ loadedImage->
                        hbox {
                            setupHover()

                            imageview(loadedImage.preview) {
                                isPreserveRatio = true
                                fitWidth = 160.0
                                fitHeight = 90.0
                                contextmenu {
                                    item(
                                        LabelGrabber.INSTANCE.getLabel("remove.image.text")
                                    ).action {
                                        controller.delete(loadedImage, ::confirmDelete)
                                    }
                                }
                                onDoubleClick {
                                    controller.addToSchedule(loadedImage)
                                }

                                setOnDragDetected {
                                    val db = borderPane.startDragAndDrop(*TransferMode.ANY)
                                    val content = ClipboardContent()
                                    content.putString(loadedImage.file.absolutePath)
                                    db.setContent(content)
                                    it.consume()
                                }
                            }
                        }
                    }

                }
            }
        }
    }
    init { controller.refresh() }


    fun confirmDelete(): Boolean {
        var reallyDelete = false
        val dialog = Dialog.buildConfirmation(
            LabelGrabber.INSTANCE.getLabel("delete.image.title"),
            LabelGrabber.INSTANCE.getLabel("delete.image.confirmation")
        )
            .addYesButton { reallyDelete = true }
            .addNoButton {  }
            .build()

        dialog.showAndWait()
        return reallyDelete
    }

    private fun Node.setupHover() {
        style = BORDER_STYLE_DESELECTED
        setOnMouseEntered {
            style = BORDER_STYLE_SELECTED
        }
        setOnMouseExited {
            style = BORDER_STYLE_DESELECTED
        }
    }

    companion object {
        private const val BORDER_STYLE_SELECTED =
            "-fx-padding: 0.2em;-fx-border-color: #0093ff;-fx-border-radius: 5;-fx-border-width: 0.1em;"
        private const val BORDER_STYLE_DESELECTED =
            "-fx-padding: 0.2em;-fx-border-color: rgb(0,0,0,0);-fx-border-radius: 5;-fx-border-width: 0.1em;"
    }
}