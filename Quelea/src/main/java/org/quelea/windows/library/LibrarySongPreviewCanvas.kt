/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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

import javafx.animation.FadeTransition
import javafx.beans.property.BooleanProperty
import javafx.beans.property.ObjectProperty
import org.quelea.data.displayable.SongDisplayable
import org.quelea.windows.lyrics.LyricDrawer
import org.quelea.windows.main.DisplayCanvas
import tornadofx.*

/**
 * A pane that can be overlaid on a component when it's loading something.
 *
 * @constructor Create the loading pane.
 *
 * @author Michael
 */
class LibrarySongPreviewCanvas(
    private val visibleProp : BooleanProperty = booleanProperty(false),
    private val displayableProp: ObjectProperty<SongDisplayable?> = objectProperty(null)
) : View() {
    private var trans: FadeTransition? = null
    private lateinit var canvas: DisplayCanvas


    override val root = stackpane {
        opacity=0.0
        isVisible=false
        //        setStyle("-fx-background-color: #555555;");
        isMouseTransparent=true
        setMaxSize(250.0, 167.0)

        canvas = DisplayCanvas(false, false, false, ::updateCanvas, DisplayCanvas.Priority.LOW)
        opcr(parent=this, canvas){
            setMaxSize(250.0, 167.0)
            displayableProp.onChange { update() }
        }

        visibleProp.onChange {
            if (it) fadeIn() else fadeOut()
        }
    }

    private fun updateCanvas() {
        LyricDrawer().also {
            it.canvas = canvas
            if (displayableProp.get()?.sections.isNullOrEmpty()) {
                it.eraseText()
            } else {
                val currentSection = displayableProp.get()!!.sections[0]
                it.theme = currentSection.theme
                it.setCapitaliseFirst(currentSection.shouldCapitaliseFirst())
                it.setText(displayableProp.get(), 0)
            }
        }
    }

    /**
     * Show (fade in) the loading pane.
     */
    @Synchronized
    private fun fadeIn() {
        root.isVisible = true
        trans?.stop()

        trans = root.fade(
            0.2.seconds,
            opacity = 0.8
        )
    }

    /**
     * Hide (fade out) the loading pane.
     */
    @Synchronized
    private fun fadeOut() {
//        root.isVisible = false
        trans?.stop()
        trans = root.fade(
            time = 0.2.seconds,
            opacity = 0.0
        ){
            setOnFinished {
                root.isVisible = false
            }
        }
    }
}
