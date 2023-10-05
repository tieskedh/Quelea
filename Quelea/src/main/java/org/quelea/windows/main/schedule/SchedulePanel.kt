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
package org.quelea.windows.main.schedule

import javafx.application.Platform
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.quelea.data.ThemeDTO
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.services.utils.Utils
import org.quelea.windows.main.QueleaApp
import org.quelea.windows.main.actionhandlers.RemoveScheduleItemActionHandler
import tornadofx.*

/**
 * The panel displaying the schedule / order of service. Items from here are
 * loaded into the preview panel where they are viewed and then projected live.
 * Items can be added here from the library.
 *
 *
 *
 * @author Michael
 */
class SchedulePanel : View() {
    /**
     * Get the schedule list backing this panel.
     *
     *
     *
     * @return the schedule list.
     */
    lateinit var scheduleList: ScheduleList
        private set

    private lateinit var themeButton: Button
    lateinit var themeNode: ScheduleThemeNode
        private set


    private val buttonDisableProp = booleanProperty(true)
    private var buttonDisable by buttonDisableProp

    private lateinit var themePopup: Stage


    override val root = borderpane {
        val darkTheme = get().useDarkTheme

        top {
            toolbar {
                label(
                    LabelGrabber.INSTANCE.getLabel("order.service.heading")
                ) {
                    style { fontWeight = FontWeight.BOLD }
                }

                spacer()

                themeButton = button(
                    graphic = ImageView(Image("file:icons/theme.png")).apply {
                        fitWidth = 16.0
                        fitHeight = 16.0
                    }
                ) {
                    tooltip(LabelGrabber.INSTANCE.getLabel("theme.button.tooltip"))
                    action {
                        if (themePopup.isShowing) {
                            //fixes a JVM crash
                            if (Utils.isMac()) {
                                Platform.runLater { themePopup.hide() }
                            } else {
                                themePopup.hide()
                            }
                        } else {
                            themePopup.x = localToScene(0.0, 0.0).x + QueleaApp.get().mainWindow.x
                            themePopup.y = localToScene(0.0, 0.0).y + 45 + QueleaApp.get().mainWindow.y
                            themePopup.show()
                        }
                    }
                }
            }
        }
        left {
            toolbar {
                orientation = Orientation.VERTICAL
                button(
                    graphic = ImageView(
                        Image(if (darkTheme) "file:icons/cross-light.png" else "file:icons/cross.png")
                    ).apply {
                        fitWidth = 16.0
                        fitHeight = 16.0
                    }
                ) {
                    Utils.setToolbarButtonStyle(this)
                    tooltip(LabelGrabber.INSTANCE.getLabel("remove.song.schedule.tooltip"))
                    disableWhen(buttonDisableProp)
                    onAction = RemoveScheduleItemActionHandler()
                }
                button(
                    graphic = ImageView(
                        Image(if (darkTheme) "file:icons/up-light.png" else "file:icons/up.png")
                    ).apply {
                        fitWidth = 16.0
                        fitHeight = 16.0
                    }
                ) {
                    Utils.setToolbarButtonStyle(this)
                    tooltip(LabelGrabber.INSTANCE.getLabel("move.up.schedule.tooltip"))
                    disableWhen(buttonDisableProp)
                    action {
                        scheduleList.moveCurrentItem(ScheduleList.Direction.UP)
                    }
                }

                button(
                    graphic = ImageView(
                        Image(if (darkTheme) "file:icons/down-light.png" else "file:icons/down.png")
                    ).apply {
                        fitWidth = 16.0
                        fitHeight = 16.0
                    }
                ) {
                    Utils.setToolbarButtonStyle(this)
                    tooltip(LabelGrabber.INSTANCE.getLabel("move.down.schedule.tooltip"))
                    disableWhen(buttonDisableProp)
                    action {
                        scheduleList.moveCurrentItem(ScheduleList.Direction.DOWN)
                    }
                }
            }
        }

        center {
            scheduleList = opcr(this, ScheduleList()){
                itemsProperty().get().onChange {
                    themeNode.updateTheme()
                }
                selectionModel.selectedIndexProperty().addListener { _, _, _ -> updateScheduleDisplay() }
                listView.focusedProperty().addListener { _, _, newValue -> if (newValue) updateScheduleDisplay() }
            }
        }


    }

    /**
     * Create and initialise the schedule panel.
     */
    init {
        val darkTheme = get().useDarkTheme
        themePopup = Stage()
        themeNode = ScheduleThemeNode(
            { theme: ThemeDTO -> updateSongTheme(theme) },
            { theme: ThemeDTO -> updateBibleTheme(theme) },
            themePopup,
            themeButton
        )

        themePopup.title = LabelGrabber.INSTANCE.getLabel("theme.select.text")
        Utils.addIconsToStage(themePopup)
        themePopup.initStyle(StageStyle.UNDECORATED)
        themePopup.focusedProperty().addListener { observable, oldValue, newValue ->
            if (oldValue && !newValue) {
                if (Utils.isMac()) {
                    Platform.runLater { themePopup.hide() }
                } else {
                    themePopup.hide()
                }
            }
        }

        themeNode.style = "-fx-background-color:WHITE;-fx-border-color: rgb(49, 89, 23);-fx-border-radius: 5;"
        val scene = Scene(themeNode)
        if (darkTheme) {
            scene.stylesheets.add("org/modena_dark.css")
        }
        themePopup.setScene(scene)
        //        themeButton.setTooltip(new Tooltip(LabelGrabber.INSTANCE.getLabel("adjust.theme.tooltip")));

        //Needed to initialise theme preview. Without this calls to the theme thumbnail return a blank image
        //before hte theme popup is opened for the first time. TODO: Find a better way of doing this.
        themePopup.show()
        Platform.runLater { themePopup.hide() }
    }

    private fun updateSongTheme(theme: ThemeDTO) {
        QueleaApp.get().mainWindow.globalThemeStore.setSongThemeOverride(theme)
    }

    private fun updateBibleTheme(theme: ThemeDTO) {
        QueleaApp.get().mainWindow.globalThemeStore.setBibleThemeOverride(theme)
    }

    fun updateScheduleDisplay() {
        if (scheduleList.items.isEmpty()) {
            buttonDisable = true
        } else {
            buttonDisable = false
            QueleaApp.get().mainWindow.mainPanel.previewPanel.setDisplayable(
                scheduleList.selectionModel.selectedItem,
                0
            )
        }
    }

    companion object {
        private val LOGGER = LoggerUtils.getLogger()
    }
}
