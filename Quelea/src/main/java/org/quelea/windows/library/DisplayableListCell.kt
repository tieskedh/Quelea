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
package org.quelea.windows.library

import javafx.scene.control.ContextMenu
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.util.Callback
import tornadofx.onChange

/**
 * A fully fleshed out class that allows for context menus to be shown on right
 * click.
 *
 *
 * @author Michael
 */
object DisplayableListCell  {
    /**
     * Provide a callback that sets the given context menu on each cell.
     *
     *
     * @param <T> the generic type of the cell.
     * @param contextMenu the context menu to show.
     * @return a callback that sets the given context menu on each cell.
    </T> */
    fun <T> forListView(contextMenu: ContextMenu) =
        forListView<T>(contextMenu, null, null)

    /**
     * Provide a callback that sets the given context menu on each cell, if and
     * only if the constraint given passes. If the constraint is null, it will
     * always pass.
     *
     *
     * @param T the generic type of the cell.
     * @param contextMenu the context menu to show.
     * @param cellFactory the cell factory to use.
     * @param constraint the constraint placed on showing the context menu - it
     * will only be shown if this constraint passes, or it is null.
     * @return a callback that sets the given context menu on each cell.
    </T> */
    @JvmStatic
    fun <T> forListView(
        contextMenu: ContextMenu?,
        cellFactory: Callback<ListView<T>?, ListCell<T>?>? =null,
        constraint: Constraint<T>? = null
    )  = Callback<ListView<T>, ListCell<T>> { listView: ListView<T>? ->
        val cell = cellFactory?.call(listView) ?: DefaultListCell()
        cell.withContextMenu(contextMenu, constraint)
        cell
    }
}


/**
 * Set the context menu on the cell ⟺ the constraint passes.
 * If the constraint is null, it will always pass.
 *
 *
 * @param T the generic type of the cell.
 * @param contextMenu the context menu to show.
 * @param constraint the constraint controlling if the contextmenu is set
 * @return a callback that sets the given context menu on each cell.
 */
fun <T> ListCell<T>.withContextMenu(
    contextMenu: ContextMenu?,
    constraint: Constraint<T>? = null
) = apply{
    itemProperty().onChange {new->
        if (new == null || constraint?.isTrue(new) != true) {
            setContextMenu(null)
        } else {
            setContextMenu(contextMenu)
        }
    }
}

fun <T> ListView<T>.setCellsWithContextMenu(
    itemContextmenu : ContextMenu,
    applyIf: Constraint<T>? = null,
    cellFactory: Callback<ListView<T>?, ListCell<T>?>?
) = setCellFactory {
    val cell = cellFactory?.call(this) ?: DefaultListCell()
    cell.withContextMenu(itemContextmenu, applyIf)
}