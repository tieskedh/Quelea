/*
 * This file is part of Quelea, free projection software for churches.
 * 
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
package org.quelea.windows.help

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.QueleaProperties
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.utils.DesktopApi
import tornadofx.*
import java.awt.Desktop
import java.io.File

/**
 * Quelea's about Dialog, displaying general features about the program and the
 * debug log location (so we can point any users here who may be looking for
 * it.)
 *
 *
 * @author Michael
 */
class AboutDialog : View(
    title = LabelGrabber.INSTANCE.getLabel("help.about.title")
) {

    override fun onDock() {
        super.onDock()
        currentStage?.isResizable = false
    }

    /**
     * Create a new about dialog.
     */

    override val root = borderpane {
        if (get().useDarkTheme) stylesheets.add("org/modena_dark.css")
        top {
            imageview(Image("file:icons/full logo.png")).apply {
                borderpaneConstraints {
                    alignment = Pos.CENTER
                }
            }
        }

        center {
            vbox {

                borderpaneConstraints {
                    margin = Insets(10.0)
                }
                text(
                    initialValue = LabelGrabber.INSTANCE.getLabel("help.about.version") + " " +
                            QueleaProperties.VERSION.versionString
                ).apply {
                    font = Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 20.0)
                    styleClass.add("text")
                }
                text(" ")

                text(LabelGrabber.INSTANCE.getLabel("help.about.line1")) {
                    styleClass.add("text")
                }

                text(LabelGrabber.INSTANCE.getLabel("help.about.line2")) {
                    styleClass.add("text")
                }
                text(" ")

                label("Java: " + System.getProperty("java.version"))

                hbox(5) {
                    label(LabelGrabber.INSTANCE.getLabel("debug.location") + ":")

                    text(LoggerUtils.getHandlerFileLocation()) {
                        styleClass.add("text")
                        if (Desktop.isDesktopSupported()) {
                            cursor = Cursor.HAND
                            fill = Color.BLUE
                            style = "-fx-underline: true;"
                            setOnMouseClicked { DesktopApi.open(File(LoggerUtils.getHandlerFileLocation())) }
                        }
                    }
                }
            }
        }

        bottom {
            button(LabelGrabber.INSTANCE.getLabel("help.about.close")) {
                borderpaneConstraints {
                    alignment = Pos.CENTER
                    margin = Insets(10.0)
                }

                action { close() }
            }
        }
    }
}
