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

import javafx.event.Event
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.input.KeyCode
import tornadofx.*

/**
 * The TreeView responsible for showing search results in a TreeView model
 *
 * @constructor Constructs a TreeView object with a blank root BibleInterface item.
 * @author Ben
 */
class BibleSearchTreeView : TreeView<BibleInterface?>() {
    private var rootItem = TreeItem<BibleInterface?>().also {
        root = it
        it.isExpanded = true
    }

    val controller = find<BibleSearchController>()


    init {
        selectionModel.selectionMode = SelectionMode.SINGLE
        setOnKeyReleased {
            if (it.code == KeyCode.RIGHT)
                trigger(it, false)
        }
        this.setOnMouseClicked(::trigger)

        controller.filteredVerses.onChange {
            reset()
            it.list.forEach { add(it) }
        }
    }

    private fun trigger(t: Event, toggleCollapse : Boolean = true) {
        val tv = t.source as BibleSearchTreeView
        val ti = tv.selectionModel.selectedItem ?: run {
            tv.selectionModelProperty().get().selectFirst()
            return
        }

        val selectedVerse = ti.value as? BibleVerse ?: run {
            if (toggleCollapse) ti.isExpanded = !ti.isExpanded
            return
        }

        val x = selectedVerse.num - 1
        val chapterVerses = selectedVerse.parent.verses
        controller.versesOfCurrentChapter.clear()
        chapterVerses.mapIndexedTo(controller.versesOfCurrentChapter) { index, bibleVerse ->
            VerseText(
                text="$bibleVerse ",
                isSelected = index == x
            )
        }
    }

    /**
     * Resets the root and expands it.
     *
     *
     */
    fun reset() {
        this.isShowRoot = false
        resetRoot()
        rootItem.isExpanded = true
    }

    /**
     * Adds the filtered results into the treeview. Sorts them from the verse's
     * parents
     *
     *
     * @param verse The bible verse to add into the tree.
     */
    fun add(verse: BibleVerse) {
        val chapter = verse.parent
        val book = chapter.parent

        // Get the current bible
        val cbible = when {
            !controller.isBibleSelected -> rootItem.children.getOrPutItem(book.parent)
            else -> rootItem
        }

        // Get the current book
        val cbook = cbible.children.getOrPutItem(book)

        //Get the current chapter.
        val cchapter = cbook.children.getOrPutItem(chapter)

        //See if verse is in results, or add it.
        val cverse = TreeItem<BibleInterface?>(verse)
        if (cverse !in cchapter.children) {
            cchapter.children += cverse
        }
    }

    /**
     * Checks if the bible section is already listed in the current tree
     */
    private fun MutableCollection<TreeItem<BibleInterface?>>.getOrPutItem(
        toFind: BibleInterface
    ): TreeItem<BibleInterface?> = find { it.value!!.name == toFind.name }
        ?: TreeItem(toFind).also { add(it) }

    /**
     * Resets the root based on the current selected index of the combobox. If
     * all, a blank root is created, if a bible is selected it becomes the root
     */
    fun resetRoot() {
        if (controller.isBibleSelected) {
            val bib = controller.bibleFilter
            BibleManager.biblesList.find { it.name == bib }?.let {
                root = TreeItem(it)
            }
        } else {
            root = TreeItem()
        }
        rootItem = root
        this.isShowRoot = false
    }
}
