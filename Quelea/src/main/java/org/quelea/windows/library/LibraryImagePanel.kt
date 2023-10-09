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
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.FileChooser
import org.javafx.dialog.Dialog
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.FileFilters
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.services.utils.setToolbarButtonStyle
import org.quelea.windows.main.QueleaApp
import tornadofx.*

/**
 * The image panel in the library.
 *
 *
 * @author Michael
 */
class LibraryImagePanel : View() {

    override val root = borderpane {
        val controller = find<ImageListPanel>().also {
            center = it.root
        }.controller

        left {
            hbox {
                toolbar {
                    orientation = Orientation.VERTICAL
//                  addButton
                    button(
                        graphic = ImageView(Image("file:icons/add.png"))
                    ) {
                        tooltip(LabelGrabber.INSTANCE.getLabel("add.images.panel"))
                        setToolbarButtonStyle()

                        setOnAction {
                            val chooser = FileChooser()
                            if (get().lastDirectory != null) {
                                chooser.initialDirectory = get().lastDirectory
                            }
                            chooser.extensionFilters.add(FileFilters.IMAGES)
                            chooser.initialDirectory = get().imageDir.getAbsoluteFile()

                            val files = chooser.showOpenMultipleDialog(QueleaApp.get().mainWindow)
                                ?: return@setOnAction


                            controller.importImageFiles(files, ::confirmOverride)
                        }
                    }
                }
            }
        }
    }

    private fun confirmOverride(fileName: String): Boolean {
        var confirmOverride = false
        val dialog = Dialog.buildConfirmation(
            LabelGrabber.INSTANCE.getLabel("confirm.overwrite.title"),
            "$fileName\n" + LabelGrabber.INSTANCE.getLabel("confirm.overwrite.text")
        ).addLabelledButton(LabelGrabber.INSTANCE.getLabel("file.replace.button")) {
            confirmOverride = true
        }.addLabelledButton(LabelGrabber.INSTANCE.getLabel("file.continue.button")) {}
            .build()
        dialog.showAndWait()
        return confirmOverride
    }
}
