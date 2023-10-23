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

import javafx.scene.Parent
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
class BibleSearchTreeView : View() {
    val controller = find<BibleSearchController>()

    override val root: Parent = treeview<BibleInterface?>(TreeItem(null)) {
        bindSelected(controller.selectedElement)

        setOnKeyReleased {
            if (it.code == KeyCode.RIGHT)
                trigger(false)
        }
        setOnMouseClicked { trigger() }

        cellFormat { text = it?.name ?: "null" }

        root = TreeItem(null)
        isShowRoot = false
        controller.treeViewData.onChange { data ->
            data ?: return@onChange
            populateTree(root, { TreeItem(it) }) {
                when (val value = it.value) {
                    is BibleVerse -> null
                    is BibleChapter -> data.chapterToVerse[value]
                    is BibleBook -> data.bookToChapter[value]
                    is Bible -> data.bibleToBook[value]
                    null -> data.treeRootElements
                    else -> null
                }
            }
        }
    }

    private fun TreeView<BibleInterface?>.trigger(toggleCollapse : Boolean = true) {
        val ti = selectionModel.selectedItem ?: run {
            selectFirst()
            return
        }

        val selectedVerse = ti.value as? BibleVerse ?: run {
            if (toggleCollapse) ti.isExpanded = !ti.isExpanded
            return
        }

        controller.onVerseSelected(selectedVerse)
    }
}
