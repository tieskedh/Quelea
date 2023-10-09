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
import org.quelea.windows.main.actionhandlers.AddTimerActionHandler
import org.quelea.windows.main.actionhandlers.RemoveTimerActionHandler
import tornadofx.*
import java.io.File

/**
 * The timer panel in the library.
 *
 * @constructor Create a new library timer panel.
 * @author Ben
 */
class LibraryTimerPanel : View() {
    private val timerListControl = find<LibraryTimerController>(
        "dir" to get().timerDir.absolutePath
    )

    override val root = borderpane {
        center {
            add<TimerListPanel>(
                TimerListPanel.CONTROLLER_PARAM to timerListControl
            )
        }
        left {
            hbox {
                toolbar{
                    orientation = Orientation.VERTICAL

//                  addButton
                    button("", ImageView(Image("file:icons/add.png"))) {
                        tooltip(LabelGrabber.INSTANCE.getLabel("add.timers.panel"))
                        onAction = AddTimerActionHandler()
                        setToolbarButtonStyle()
                    }

//                  importButton
                    button(
                        graphic = ImageView(Image("file:icons/importbw.png"))
                    ){
                        tooltip(LabelGrabber.INSTANCE.getLabel("import.heading"))
                        setToolbarButtonStyle()

                        setOnAction {
                            timerFileChooser()?.let {files->
                                timerListControl.import(files, ::confirmOverride)
                            }
                        }
                    }

//                  removeButton
                    button(
                        graphic = ImageView(Image("file:icons/removedb.png"))
                    ){
                        setToolbarButtonStyle()
                        disableWhen(timerListControl.selectedTimerProperty.isNull)
                        tooltip(LabelGrabber.INSTANCE.getLabel("remove.timer.text"))
                        onAction = RemoveTimerActionHandler()
                    }
                }
            }
        }
    }

    /*
    * KOTLINIZE: do you want to set and immediately replace the value of initialDirectory?
    * else chooseFile(
    *      filters = arrayOf(FileFilters.TIMERS),
    *      initialDirectory = get().lastDirectory ?: get().timerDir.absoluteFile,
    *      FileChooserMode.Multi,
    *      owner = QueleaApp.get().mainWindow
    *  )
    */
    private fun timerFileChooser(): List<File>? {
        val chooser = FileChooser()
        if (get().lastDirectory != null) {
            chooser.initialDirectory = get().lastDirectory
        }
        chooser.extensionFilters.add(FileFilters.TIMERS)
        chooser.initialDirectory = get().timerDir.getAbsoluteFile()
        return chooser.showOpenMultipleDialog(QueleaApp.get().mainWindow)
    }

    private fun confirmOverride(fileName : String) : Boolean{
        var replace = false
        val d = Dialog.buildConfirmation(
            LabelGrabber.INSTANCE.getLabel("confirm.overwrite.title"),
            fileName + "\n" + LabelGrabber.INSTANCE.getLabel("confirm.overwrite.text")
        ).addLabelledButton(LabelGrabber.INSTANCE.getLabel("file.replace.button")) {
            replace= true
        }.addLabelledButton(LabelGrabber.INSTANCE.getLabel("file.continue.button")) {}
            .build()
        d.showAndWait()

        return replace
    }
}
