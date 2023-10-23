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
package org.quelea.data.bible

import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties
import org.quelea.windows.main.widgets.LoadingPane
import tornadofx.*

/**
 * A dialog that can be used for searching for bible passages.
 *
 *
 * @author mjrb5
 */
class BibleSearchDialog : View(
    title = LabelGrabber.INSTANCE.getLabel("bible.search.title"),
    icon = ImageView(Image("file:icons/search.png"))
) {
    private val controller = find<BibleSearchController>()

    override val root = borderpane {
        top {
            hbox {
                paddingAll = 5
                combobox(
                    property = controller.bibleFilterProp,
                    values = controller.bibleList
                ) {
                    isEditable = false

                    setOnAction {
                        controller.update()
                    }
                }
                textfield(controller.searchTextProp) {
                    promptText = LabelGrabber.INSTANCE.getLabel("initial.search.text")
                    disableWhen(controller.cannotSearch)
                }

                //add to schedule
                button(
                    text = LabelGrabber.INSTANCE.getLabel("add.to.schedule.text"),
                    graphic = ImageView(Image("file:icons/tick.png"))
                ) {
                    action(controller::addToSchedule)
                }

                //results field
                text(
                    controller.searchResultCount.stringBinding {
                        when {
                            it == -1 -> " " + LabelGrabber.INSTANCE.getLabel("bible.search.keep.typing")
                            it == 1 && LabelGrabber.INSTANCE.isLocallyDefined("bible.search.result.found") ->
                                " 1 " + LabelGrabber.INSTANCE.getLabel("bible.search.result.found")
                            else -> " $it " + LabelGrabber.INSTANCE.getLabel("bible.search.results.found")
                        }
                    }
                ) {
                    font = Font.font("Sans", 14.0)
                    styleClass.add("text")
                }
            }
        }

        center {
            splitpane {
                setDividerPosition(0, 0.3)
                //searchPane

                stackpane {
                    add<BibleSearchTreeView>()
                    LoadingPane(showing = controller.showLoading)
                        .attachTo(this)
                }

                scrollpane {
                    val textWidth = this.widthProperty() - 20 // -20 to account for scroll bar width
                    flowpane {
                        bindChildren(controller.versesOfCurrentChapter) {
                            Text("${it.text} ").apply {
                                styleClass.add("text")
                                font = Font.font(
                                    "Sans",
                                    if (it.isSelected) FontWeight.BOLD else null,
                                    14.0
                                )
                                wrappingWidthProperty().bind(textWidth)
                            }
                        }
                    }
                }
            }
        }

        //Sizing
        prefHeight=600.0
        prefWidth=800.0
    }

    /**
     * Create a new bible searcher dialog.
     */
    init {
        if (QueleaProperties.get().useDarkTheme)
            importStylesheet("org/modena_dark.css")
    }

    override fun onDock() {
        super.onDock()
        setWindowMinSize(500, 300)
        controller.refresh()
    }
}
