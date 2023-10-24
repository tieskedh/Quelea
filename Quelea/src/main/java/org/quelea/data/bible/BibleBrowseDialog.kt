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
package org.quelea.data.bible

import javafx.geometry.Insets
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties
import org.quelea.services.utils.bindSingleSelectedBidirectional
import tornadofx.*

class BibleBrowseController : Controller() {
    val bibleText = stringProperty("")
    val selectedBookProp = objectProperty<BibleBook>().apply {
        onChange { book ->
            book?.text?.let {
                bibleText.value = it
            }
        }
    }

    val bibleBooks = observableListOf<BibleBook>()
    val selectedBibleProp = objectProperty<Bible>().apply {
        onChange { bible ->
            val selectedBook = selectedBookProp.value?.bookName
            bibleBooks.clear()
            bible ?: return@onChange

            val books = bible.bookList
            bibleBooks.setAll(books)
            selectedBookProp.value = when(selectedBook){
                null -> books.firstOrNull()
                else -> books.firstOrNull { it.bookName == selectedBook }
                    ?: books.firstOrNull()
            }
        }
    }

    val bibleList = observableListOf<Bible>()

    init {
        BibleManager.registerBibleChangeListener(invokeImmediately = true){
            val selectedBible = selectedBibleProp.value?.bibleName
            val newBibleList = BibleManager.biblesList
            bibleList.setAll(newBibleList)

            selectedBibleProp.value = when(selectedBible){
                null -> newBibleList.firstOrNull()
                else -> newBibleList
                    .firstOrNull { it.bibleName == selectedBible }
                    ?: newBibleList.firstOrNull()
            }
        }
    }
}

/**
 * A dialog where the user can browse through the installed bibles.
 *
 * @constructor Create the bible browse dialog.
 * @author Michael
 */
class BibleBrowseDialog : View(
    title = LabelGrabber.INSTANCE.getLabel("bible.browser.title"),
    icon = ImageView(Image("file:icons/bible.png"))
) {

    val controller = find<BibleBrowseController>()
    override val root = borderpane {
        top {
            hbox(5) {
                borderpaneConstraints {
                    margin = Insets(0.0, 0.0, 5.0, 5.0)
                }

                val label = label(LabelGrabber.INSTANCE.getLabel("bible.heading"))

                combobox(
                    property = controller.selectedBibleProp,
                    values = controller.bibleList
                ) {
                    isEditable = false
                    label.labelFor = this
                }
            }
        }

        center {
            textarea(controller.bibleText) {
                isWrapText = true
                isEditable = false
            }
        }

        left {
            listview(controller.bibleBooks) {
                bindSingleSelectedBidirectional(controller.selectedBookProp)
            }
        }
    }

    init {
        if (QueleaProperties.get().useDarkTheme)
            importStylesheet("org/modena_dark.css")
    }
}
