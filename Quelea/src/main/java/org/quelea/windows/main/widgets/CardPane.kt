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
package org.quelea.windows.main.widgets

import javafx.scene.Node
import javafx.scene.layout.StackPane

/**
 * Emulates (sort of) a swing card layout - JavaFX doesn't have one but we can
 * trivially create one out of a StackPane.
 *
 *
 * @author Michael
 */
class CardPane<T : Node> : StackPane(), Iterable<T> {
    private val items = HashMap<String, T>()
    /**
     * The current panel being shown, or null if none is shown.
     */
    var currentPane: T? = null
        private set

    /**
     * Add a node to this card pane.
     *
     *
     * @param node the node to add.
     * @param label the label used for selecting this node.
     */
    fun add(node: T, label: String) {
        items[label] = node
        node.isVisible = false
        children.add(node)
    }

    operator fun set(label: String, node: T) = add(node, label)
    operator fun minusAssign(label: String) = remove(label)

    /**
     * Remove a node on this card pane.
     *
     *
     * @param label the label of the node to remove.
     */
    fun remove(label: String) {
        children.remove(items[label])
        items.remove(label)
    }


    /**
     * Get all the panels currently on the card pane.
     *
     *
     * @return
     */
    val panels: MutableCollection<T>
        get() = items.values

    /**
     * Show the node with the given label.
     *
     *
     * @param label the label whose node to show.
     * @throws IllegalArgumentException if the label isn't valid.
     */
    fun show(label: String) {
        for (node in items.values) {
            node.isVisible = false
        }
        currentPane = items.getValue(label).also {
            it.isVisible = true
        }
    }

    override fun iterator(): MutableIterator<T> = panels.iterator()
}
