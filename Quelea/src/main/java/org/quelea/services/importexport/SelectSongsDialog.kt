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
package org.quelea.services.importexport

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.Modality
import javafx.stage.Stage
import org.quelea.data.displayable.SongDisplayable
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties.Companion.get
import tornadofx.*

/**
 * A dialog where given songs can be selected.
 *
 * @constructor Create a new imported songs dialog.
 * @param text a list of lines to be shown in the dialog.
 * @param acceptText text to place on the accept button.
 * @author Michael
 */
open class SelectSongsDialog(text: Array<String?>, acceptText: String?) : Stage() {
    /**
     * Get the add button.
     *
     *
     * @return the add button.
     */
    lateinit var addButton: Button
    private lateinit var selectAllCheckBox: CheckBox
    private lateinit var gridPane: GridPane
    private var songs: List<SongDisplayable>? = null
    private val checkBoxes = mutableListOf<CheckBox>()
    private val gridScroll: ScrollPane

    init {
        initModality(Modality.APPLICATION_MODAL)
        title = LabelGrabber.INSTANCE.getLabel("select.songs.title")

        val mainPanel = VBox(5.0).apply {
            vbox {
                vboxConstraints { margin = Insets(10.0) }
                text.mapTo(children, ::Label)
            }

            gridScroll = scrollpane {
                vboxConstraints { vgrow = Priority.ALWAYS }

                isFitToWidth = true
                isFitToHeight = true

                stackpane {
                    vbox(10) {
                        stackpaneConstraints { margin = Insets(10.0) }

                        hbox(5) {
                            selectAllCheckBox = checkbox {
                                selectedProperty().onChange { selected ->
                                    checkBoxes.forEach { it.isSelected = selected }
                                }
                            }

                            label(LabelGrabber.INSTANCE.getLabel("check.uncheck.all.text")) {
                                style = "-fx-font-weight: bold;"
                            }
                        }

                        gridPane = gridpane {
                            hgap = 5.0
                            vgap = 5.0
                        }
                    }
                }
            }

            stackpane {
                vboxConstraints { margin = Insets(10.0) }
                addButton = button(acceptText.orEmpty(), ImageView(Image("file:icons/tick.png")))
            }
        }

        val scene = Scene(mainPanel, 800.0, 600.0)
        if (get().useDarkTheme) {
            scene.stylesheets.add("org/modena_dark.css")
        }
        setScene(scene)
    }

    /**
     * Set the songs to be shown in the dialog.
     *
     *
     * @param songs the list of songs to be shown.
     * @param checkList a list corresponding to the song list - each position is
     * true if the checkbox should be selected, false otherwise.
     * @param defaultVal the default value to use for the checkbox if checkList
     * is null or smaller than the songs list.
     */
    fun setSongs(
        songs: List<SongDisplayable>,
        checkList: Map<SongDisplayable, Boolean>? = null,
        defaultVal: Boolean = false
    ) {
        this.songs = songs
        gridPane.clear()
        checkBoxes.clear()

        gridPane.apply {
            constraintsForColumn(0).apply {
                prefWidth = 20.0
                usePrefSize = true
            }

            //title
            constraintsForColumn(1).apply {
                percentWidth = 50.0
                hgrow = Priority.ALWAYS
            }

            //author
            constraintsForColumn(1).apply {
                percentWidth = 45.0
                hgrow = Priority.ALWAYS
            }

            row {
                region()

                label(LabelGrabber.INSTANCE.getLabel("title.label")) {
                    titleStyle()
                }

                label(LabelGrabber.INSTANCE.getLabel("author.label")) {
                    titleStyle()
                }
            }

            for (song in songs) row {
                checkBoxes += checkbox {
                    selectedProperty().onChange { checkEnableButton() }

                    isSelected = checkList?.get(song) ?: defaultVal
                }

                label(song.title)
                label(song.author)
            }
        }

        gridScroll.vvalue = 0.0
        checkEnableButton()
    }


    private fun Labeled.titleStyle() {
        alignment = Pos.CENTER
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
        style = "-fx-alignment: center;-fx-font-weight: bold;"
    }

    /**
     * Disable / enable the add button depending on if anything is selected.
     */
    private fun checkEnableButton() {
        addButton.isDisable = checkBoxes.none { it.isSelected }
    }

    /**
     * Get the list of selected songs.
     *
     *
     * @return the list of selected songs.
     */
    val selectedSongs: List<SongDisplayable>
        get() = songs!!.filterIndexed { i, _ ->
            checkBoxes[i].isSelected
        }
}
