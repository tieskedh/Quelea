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
    val controller = find<BibleSearchController>()

    init {
        selectionModel.selectionMode = SelectionMode.SINGLE
        setOnKeyReleased {
            if (it.code == KeyCode.RIGHT)
                trigger(it, false)
        }
        this.setOnMouseClicked(::trigger)

        cellFormat {
            text = it?.name ?: "IT IS NULL!"
        }

        root = TreeItem(null)
        isShowRoot = false
        populateTree(root, { TreeItem(it) }){
            when(val value = it.value) {
                is BibleVerse -> null
                is BibleChapter -> controller.bibleChapToVerse[value]
                is BibleBook -> controller.bibleBookToChap[value]
                is Bible -> controller.bibleVersionToBook[value]
                null -> controller.treeRootElements
                else -> null
            }
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

        controller.onVerseSelected(selectedVerse)
    }
}
