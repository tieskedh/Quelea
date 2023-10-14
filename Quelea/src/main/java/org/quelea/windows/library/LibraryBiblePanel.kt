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

import javafx.concurrent.Worker
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.web.WebEngine
import netscape.javascript.JSObject
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.windows.library.BibleScreenContent.Loaded.*
import tornadofx.*

/**
 * The panel used to get bible verses.
 *
 *
 * @author Michael
 */
class LibraryBiblePanel : View(LabelGrabber.INSTANCE.getLabel("library.bible.heading")) {

    val controller = find<LibraryBibleController>()

    override val root = vbox(5) {
        //select bibleType
        combobox(
            property = controller.selectedBibleProp,
            values = controller.bibles
        )

        hbox(5) {
            combobox(
                property=controller.selectedBibleBookProp,
                values = controller.filteredBibleBooks
            ) {
                focusedProperty().onChange { focused->
                    if (focused) {
                        show()
                        controller.clearBiblebookFilter()
                    }
                }


                subscribe<OpenBibleBookSelector> {
                    show()
                    requestFocus()
                }

                setOnKeyReleased { event ->
                    if (event.code.isLetterKey || event.code.isDigitKey || event.code == KeyCode.BACK_SPACE || event.code == KeyCode.SPACE) {
                        controller.onBibleBookFilterChange(
                            update = {search ->
                                if (event.code == KeyCode.BACK_SPACE) {
                                    if (search.isNotEmpty()) search.dropLast(1)
                                    else search
                                } else search + event.text
                            }
                        )
                    }
                }
            }


            textfield(
                controller.booklessBiblePassageRefProp
            ) {
                promptText = LabelGrabber.INSTANCE.getLabel("bible.passage.selector.prompt")
                setOnAction { controller.addSelectedPassageToSchedule() }
            }

//          addToSchedule
            button(
                LabelGrabber.INSTANCE.getLabel("add.to.schedule.text"),
                ImageView(Image("file:icons/tick.png"))
            ){
                disableWhen(controller.canAddToScheduleProp.not())
                action(controller::addSelectedPassageToSchedule)
            }
        }

        //bottomPane
        borderpane {
            vboxConstraints {
                vgrow = Priority.SOMETIMES
            }
            center {
                //text preview
                webview {

                    engine.clear()
                    engine.loadWorker.stateProperty().onChange {
                        // Setup JavaScript/Java bridge
                        if (it == Worker.State.SUCCEEDED) {
                            val window = engine.executeScript("window") as JSObject
                            window.setMember("java", JavaScriptBridge())
                        }
                    }
                    controller.bibleScreenContentProp.onChange {
                        if (it == null) return@onChange
                        engine.setBibleContent(it)
                    }
                }
            }
        }
    }

    private fun WebEngine.clear() {
        if (!get().useDarkTheme) {
            loadContent("$BIBLE_VIEW_HEAD<body/></html>")
        } else {
            loadContent(
                BIBLE_VIEW_HEAD.replace("#000", "#FFF")
                    .replace("white", "black") + "<body/></html>")
        }
    }


    /**
     * Update the text in the preview panel based on the contents of the fields.
     */
    private fun WebEngine.setBibleContent(bibleScreenContent: BibleScreenContent) {
        when(bibleScreenContent){
            BibleScreenContent.Blank -> {
                clear()
                return
            }
            BibleScreenContent.Loading -> {
                loadContent("")
                return
            }
            is BibleScreenContent.Loaded -> Unit
        }


        val previewText = buildString {
            append(
                if (bibleScreenContent.isDarkTheme) BIBLE_VIEW_HEAD
                    .replace("#000", "#FFF")
                    .replace("white", "black")
                else BIBLE_VIEW_HEAD
            )
            when(val select = bibleScreenContent.selectedVerse){
                is SelectVerse.SelectFirst -> append("    <body onload=\"scrollTo('").append(select.verse).append("')\">")
                is SelectVerse.SelectLast -> append("    <body onload=\"scrollToBottom('").append(select.verse).append("')\">")
                SelectVerse.SelectNone -> append("<body>")
            }
            bibleScreenContent.withEach {
                when(this) {
                    is BibleScreenPart.ChapterHeader -> {
                        append("<b><h3><span id=\"").append(chapter).append("\">")
                        append(chapter)
                        append("</span></h3></b>")
                    }
                    is BibleScreenPart.Verse -> {
                        append(marked = isMarked) {
                            append("<span onclick=\"java.send('").append(num).append("')\" id=\"")
                                .append(id).append("\"><sup>").append(num).append("</sup>")
                                .append(' ').append(text).append(' ')
                            append("</span>")
                        }
                    }
                    is BibleScreenPart.RemainingText -> append(text)
                }
            }
            append("    </body>\n</html>")
        }

        loadContent(previewText)
    }


    private inline fun StringBuilder.append(
        marked: Boolean,
        append: StringBuilder.() -> Unit
    ) {
        if (marked) append("<mark>")
        append()
        if (marked) append("</mark>")
    }


    companion object{
        private const val BIBLE_VIEW_HEAD =
            """<!DOCTYPE html>
               <html>
                   <head>
                       <title>Bible Browser</title>
                       <meta charset="utf-8">
                       <meta name="apple-mobile-web-app-capable" content="yes">
                       <meta name="mobile-web-app-capable" content="yes">
                    <style>
                        mark {
                        background-color: #D7D7D7;
                        color: black;
                        }
                        h3 {
                            display: block;
                            font-size: 1.67em;
                            margin-top: 0.67em;
                            margin-bottom: 0.0em;
                            margin-left: 0;
                            margin-right: 0;
                            color: #000;
                        }
                        body {
                              background-color: white;
                              color: #000;
                          }
                    </style>
                    <script>
                    function scrollTo(eleID) {
                        var e = document.getElementById(eleID);
                        if (!!e && e.scrollIntoView) {
                            e.scrollIntoView();
                        }
                       }
                    function scrollToBottom(elementID) {
                        var el = document.getElementById(elementID);
                        if (!!el && el.scrollIntoView) {
                           el.scrollIntoView(false);
                        }
                    }
                    </script>
                   </head>
               """
    }

    /**
     * Class to receive the clicked verses in the WebView
     */
    inner class JavaScriptBridge {
        @Suppress("unused")
        fun send(verse: String) = controller.onVerseClick(verse)
    }
}
