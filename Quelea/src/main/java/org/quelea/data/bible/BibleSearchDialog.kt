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

import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.layout.FlowPane
import javafx.scene.text.Font
import javafx.stage.Stage
import org.quelea.data.displayable.BiblePassage
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties
import org.quelea.utils.javaTrim
import org.quelea.utils.onChangeWhile
import org.quelea.windows.main.schedule.SchedulePanel
import org.quelea.windows.main.widgets.LoadingPane
import tornadofx.*
import java.util.concurrent.Executors
import kotlin.concurrent.Volatile

/**
 * A dialog that can be used for searching for bible passages.
 *
 *
 * @author mjrb5
 */
class BibleSearchDialog : Stage(), BibleChangeListener {
    private lateinit var searchField: TextField
    private lateinit var searchResults: BibleSearchTreeView
    private lateinit var bibles: ComboBox<String>

    private val chapterVerses = observableListOf<Node>()
    private val searchResultCount = intProperty(-1)
    private val showLoading = booleanProperty(false)

    /**
     * Reset this dialog.
     */
    fun reset() {
//        searchResults.itemsProperty().get().clear();
        searchField.text = LabelGrabber.INSTANCE.getLabel("initial.search.text")
        searchField.focusedProperty().onChangeWhile { isFocussed ->
            if (isFocussed) searchField.text = ""
            !isFocussed
        }
        searchField.isDisable = true
        BibleManager.get().runOnIndexInit { searchField.isDisable = false }
    }

    private val updateExecutor = Executors.newSingleThreadExecutor()
    private var lastUpdateRunnable: ExecRunnable? = null


    /**
     * Create a new bible searcher dialog.
     */
    init {
        title = LabelGrabber.INSTANCE.getLabel("bible.search.title")
        icons.add(Image("file:icons/search.png"))


        val mainPane = BorderPane().apply {
            top {
                hbox {
                    paddingAll = 5
                    bibles  =combobox {
                        isEditable = false

                        setOnAction {
                            searchResults.resetRoot()
                            update()
                        }
                    }
                    searchField = textfield {
                        textProperty().onChange { update() }
                    }

                    //add to schedule
                    button(
                        text=LabelGrabber.INSTANCE.getLabel("add.to.schedule.text"),
                        graphic = ImageView(Image("file:icons/tick.png"))
                    ){
                        action {
                            val chap = (searchResults.selectedValue as? BibleVerse)?.parent
                                ?: return@action

                            val passage = BiblePassage(
                                chap.parent.parent.bibleName,
                                "${chap.book} $chap",
                                chap.verses,
                                false
                            )
                            FX.find<SchedulePanel>().scheduleList.add(passage)
                        }
                    }

                    //results field
                    text(
                        searchResultCount.stringBinding {
                            when {
                                it == -1 -> " " + LabelGrabber.INSTANCE.getLabel("bible.search.keep.typing")
                                it == 1 && LabelGrabber.INSTANCE.isLocallyDefined("bible.search.result.found") ->
                                    " 1 " + LabelGrabber.INSTANCE.getLabel("bible.search.result.found")
                                else -> " $it " + LabelGrabber.INSTANCE.getLabel("bible.search.results.found")
                            }
                        }
                    ){
                        font = Font.font("Sans", 14.0)
                        styleClass.add("text")
                    }
                }
            }

            val chapterPane = FlowPane().apply {
                bindChildren(chapterVerses){ it }
            }

            center {
                splitpane {
                    setDividerPosition(0, 0.3)
                    //searchPane

                    stackpane {
                        searchResults = BibleSearchTreeView(
                            chapterTexts = chapterVerses,
                            widthProp = chapterPane.widthProperty() - 20, //-20 to account for scroll bar width
                            bibles = bibles.selectionModel.selectedItemProperty().stringBinding {
                                it.takeUnless { it == LabelGrabber.INSTANCE.getLabel("all.text") }
                            }
                        ).attachTo(this)
                        LoadingPane(showing = showLoading)
                            .attachTo(this)
                    }

                    scrollpane {
                        content = chapterPane
                    }
                }
            }
        }

        BibleManager.get().registerBibleChangeListener(this)
        updateBibles()

        //Sizing
        height=600.0
        width=800.0
        minHeight = 300.0
        minWidth = 500.0

        // Event handlers
        setOnShown {
            BibleManager.get().takeUnless { it.isIndexInit }?.refreshAndLoad()
        }

        reset()
        val scene = Scene(mainPane)
        if (QueleaProperties.get().useDarkTheme) {
            scene.stylesheets.add("org/modena_dark.css")
        }
        setScene(scene)
    }

    private interface ExecRunnable : Runnable {
        fun cancel()
    }

    /**
     * Update the results based on the entered text.
     */
    private fun update() {
        val text = searchField.text
        if (text.length > 3) {
            if (BibleManager.get().isIndexInit) {
                searchResults.reset()
                showLoading.value = true
                val execRunnable: ExecRunnable = object : ExecRunnable {
                    @Volatile
                    private var cancel = false
                    override fun cancel() {
                        cancel = true
                    }

                    override fun run() {
                        if (cancel) return

                        val results = BibleManager.get().index.filter(text, null)
                        runLater {
                            val filteredVerses =  if (text.javaTrim().isNotEmpty()) {
                                results.asSequence().filter { chapter ->
                                    bibles.selectionModel.selectedIndex == 0 ||
                                            chapter.book.bible.name == bibles.selectedItem
                                }.flatMap { it.verses }
                                    .filter { it.text.contains(text, ignoreCase = true) }
                            } else { sequenceOf() }

                            searchResults.setFiltered(filteredVerses)
                            showLoading.value = false
                            searchResultCount.set(searchResults.size())
                        }
                    }
                }
                lastUpdateRunnable?.cancel()
                lastUpdateRunnable = execRunnable
                updateExecutor.submit(execRunnable)
            }
        }
        searchResults.reset()
        searchResultCount.set(-1)
        chapterVerses.clear()
    }

    /**
     * Update the list of bibles on this search dialog.
     */
    override fun updateBibles() {
        bibles.itemsProperty().get().also { items->
            items.clear()
            items += LabelGrabber.INSTANCE.getLabel("all.text")
            BibleManager.get().getBibles().mapTo(items) { it.name }
        }
        bibles.selectionModel.selectFirst()
    }
}
