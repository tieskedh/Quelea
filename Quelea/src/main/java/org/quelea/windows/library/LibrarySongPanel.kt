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
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.services.utils.setToolbarButtonStyle
import org.quelea.windows.main.actionhandlers.NewSongActionHandler
import org.quelea.windows.main.actionhandlers.RemoveSongDBActionHandler
import tornadofx.*

/**
 * The panel used for browsing the database of songs and adding any songs to the
 * order of service.
 *
 * @constructor Create and initialise the library song panel.
 *
 * @author Michael
 */
class LibrarySongPanel : View(LabelGrabber.INSTANCE.getLabel("library.songs.heading")) {
    /**
     * Get the search box in this panel.
     *
     *
     *
     * @return the search box.
     */
    lateinit var searchBox: TextField
        private set

    /**
     * Get the song list behind this panel.
     *
     *
     *
     * @return the song list.
     */
    @JvmField
    val songController = find<LibrarySongController>()

    override val root = borderpane {
        val darkTheme = get().useDarkTheme

        top {
            hbox(3) {
                borderpaneConstraints {
                    marginLeftRight(5.0)
                }

                //searchLabel
                label(
                    LabelGrabber.INSTANCE.getLabel("library.song.search")
                ){
                    maxHeight = Double.MAX_VALUE
                    alignment = Pos.CENTER
                }
                textfield(songController.searchText) {
                    hboxConstraints {
                        hGrow = Priority.SOMETIMES
                    }
                    maxWidth = Double.MAX_VALUE
                    setOnKeyPressed {
                        if (it.code == KeyCode.ESCAPE)
                            songController.clearSearch()
                    }
                    focusedProperty()
                    subscribe<SearchBoxFocused> {
                        requestFocus()
                    }
                }

//              searchCancelButton
                button(
                    graphic = ImageView(Image(if (darkTheme) "file:icons/cross-light.png" else "file:icons/cross.png"))
                ) {
                    setToolbarButtonStyle()
                    tooltip(LabelGrabber.INSTANCE.getLabel("clear.search.box"))
                    disableWhen(songController.disableClearSearch)
                    setOnAction {
                        songController.clearSearch()
                    }
                }
            }
        }
        center {
            add<LibrarySongList>("popup" to true)
        }
        left {
            toolbar{
                orientation = Orientation.VERTICAL

                //addButton
                button(
                    graphic = ImageView(Image(when(darkTheme) {
                            true -> "file:icons/newsongdb-light.png"
                            false -> "file:icons/newsongdb.png"
                    })).apply {
                        fitHeight = 16.0
                        fitWidth = 16.0
                    }
                ) {
                    setToolbarButtonStyle()
                    tooltip = Tooltip(LabelGrabber.INSTANCE.getLabel("add.song.text"))
                    onAction = NewSongActionHandler()
                }

                //removeButton
                button(
                    graphic = ImageView(Image(when {
                        darkTheme -> "file:icons/removedb-light.png"
                        else -> "file:icons/removedb.png"
                    })).apply {
                        fitHeight = 16.0
                        fitWidth = 16.0
                    }
                ) {
                    setToolbarButtonStyle()
                    tooltip(LabelGrabber.INSTANCE.getLabel("remove.song.text"))
                    disableWhen(songController.selectedValueProp.isNull)
                    onAction = RemoveSongDBActionHandler()
                }
            }
        }
    }
}
