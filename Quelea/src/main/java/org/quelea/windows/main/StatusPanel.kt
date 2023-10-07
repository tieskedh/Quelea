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
package org.quelea.windows.main

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import org.quelea.services.utils.fxRunAndWait
import org.quelea.services.utils.setToolbarButtonStyle
import tornadofx.*


/**
 * A status panel that denotes a background task in Quelea.
 *
 *
 * @author Michael
 *
 * @constructor Create a new status panel.
 * @param labelText the text to put on the label on this panel.
 * @param index the index of this panel on the group.
 *
 */
class StatusPanel(
    labelText: String,
    private val index: Int,
    private val onDone : () -> Unit
) : Fragment() {

    /**
     * Get the progress bar associated with this panel.
     *
     *
     * @return the progress bar associated with this panel.
     */
    lateinit var progressBar: ProgressBar
    private lateinit var label: Label

    /**
     * Get the cancel button on this panel.
     *
     *
     * @return the cancel button on this panel.
     */
    lateinit var cancelButton: Button

    /**
     * Remove the cancel button from this status bar.
     */
    fun removeCancelButton() {
        root.children.remove(cancelButton)
    }

    private val labelTextProp = stringProperty(labelText)
    /**
     * The label text for this panel.
     */
    var labelText by labelTextProp



    private var progressVal = 0.0

    override val root = hbox(
        spacing = 5,
        alignment = Pos.CENTER
    ) {
        label = label(labelTextProp){
            hboxConstraints {
                margin = Insets(5.0)
            }
        }
        progressBar = progressbar {
            maxWidth = Double.MAX_VALUE
            hgrow = Priority.ALWAYS
        }
        cancelButton = button(
            graphic = ImageView(Image("file:icons/cross.png", 13.0, 13.0, false, true))
        ){
            alignment=Pos.CENTER
            setToolbarButtonStyle()
        }
    }

    /**
     * The current progress of the progress bar between 0-1. Thread safe.
     */
    var progress: Double
        get() {
            progressVal = 0.0
            fxRunAndWait { progressVal = progressBar.progress }
            return progressVal
        }
        set(progress) {
            Platform.runLater { progressBar.progress = progress }
        }

    /**
     * Called to indicate that the task associated with this panel has finished,
     * and therefore the panel can be removed.
     */
    fun done() = onDone()

    /**
     * Set whether this panel is active.
     *
     *
     * @param active true if active, false otherwise.
     */
    fun setActive(active: Boolean) {
        root.isVisible = active
    }
}
