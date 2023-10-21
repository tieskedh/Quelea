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
package org.quelea.windows.main.widgets

import javafx.animation.FadeTransition
import javafx.animation.Transition
import javafx.beans.property.BooleanProperty
import javafx.beans.property.DoubleProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.geometry.Pos
import javafx.scene.control.ProgressBar
import javafx.scene.layout.StackPane
import org.quelea.services.languages.LabelGrabber
import tornadofx.*

/**
 * A pane that can be overlaid on a component when it's loading something.
 *
 *
 * @author Michael
 */

class LoadingPane @JvmOverloads constructor(
    private val progressProp: DoubleProperty = doubleProperty(),
    showing : ObservableBooleanValue = booleanProperty(false)
) : StackPane() {
    private var trans: FadeTransition? = null
    private lateinit var bar: ProgressBar

    /**
     * Create the loading pane.
     */
    init {
        alignment = Pos.CENTER
        opacity = 0.0
        style = "-fx-background-color: #555555;"
        isVisible = showing.value
        vbox {
            alignment = Pos.CENTER

            text(LabelGrabber.INSTANCE.getLabel("loading.text") + "..."){
                style = " -fx-font: bold italic 20pt \"Arial\";"
                fade(
                    1.5.seconds,
                    1.0
                ){
                    isAutoReverse = true
                    cycleCount = Transition.INDEFINITE
                }
            }
            bar = progressbar(progressProp)
        }
        showing.onChange {
            if (it) show() else hide()
        }

    }

    /**
     * Show (fade in) the loading pane.
     */
    @Synchronized
    private fun show() {
        isVisible = true
        progressProp.set(-1.0)
        trans?.stop()
        trans = fade(
            time=0.2.seconds,
            opacity = 0.6
        )
    }

    /**
     * Hide (fade out) the loading pane.
     */
    @Synchronized
    private fun hide() {
        isVisible = false
        trans?.stop()

        trans = fade(0.2.seconds, 0) {
            setOnFinished { isVisible = false }
        }
    }
}
