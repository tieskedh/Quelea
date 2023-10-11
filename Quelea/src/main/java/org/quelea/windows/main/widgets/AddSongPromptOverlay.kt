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
import javafx.beans.property.BooleanProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import org.quelea.services.languages.LabelGrabber
import org.quelea.windows.library.LibrarySongController
import tornadofx.*

/**
 * An overlay that should be put on the song database when no songs are present.
 * It prompts the user to press the add song button to add a song - this is
 * useful from a HCI perspective because otherwise the button is not greatly
 * noticeable.
 *
 * @constructor create the overlay.
 *
 * @author Michael
 */
class AddSongPromptOverlay(
    isVisibleProp : ObservableBooleanValue
) : StackPane() {
    private var trans: FadeTransition? = null

    init {
        alignment = Pos.CENTER
        opacity = 0.0
        style = "-fx-background-color: #555555;"
        isVisible = false

        vbox {
            alignment = Pos.TOP_LEFT
            stackpaneConstraints {
                marginTop = 10.0
                marginLeft = 15.0
            }

            imageview(Image("file:icons/whitearrow.png"))

            label(
                LabelGrabber.INSTANCE.getLabel("add.song.hint.text")
            ) {
                isWrapText = true
                textFill = Color.WHITESMOKE
                style = "-fx-font-size:16pt; -fx-font-family:Calibri;"

                runLater {
                    FX.find<LibrarySongController>().searchText.onChange {
                        text = when(it.isNullOrEmpty()) {
                            true -> LabelGrabber.INSTANCE.getLabel("add.song.hint.text")
                            false ->LabelGrabber.INSTANCE.getLabel("add.song.hint.search.text")
                        }
                    }
                }
            }
        }

        isVisibleProp.onChange { visible->
            if (visible) show() else hide()
        }
    }

    /**
     * Show (fade in) the overlay.
     */
    @Synchronized
    private fun show() {
        isVisible = true
        trans?.stop()

        trans = fade(
            time=0.2.seconds,
            opacity = 0.6
        )
    }

    /**
     * Hide (fade out) the overlay.
     */
    @Synchronized
    private fun hide() {
        trans?.stop()
        trans = fade(
            time = 0.2.seconds,
            opacity = 0.0
        ).apply {
            setOnFinished { isVisible = false }
        }
    }
}
