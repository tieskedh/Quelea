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
package org.quelea.windows.main

import javafx.application.Platform
import tornadofx.*


class StatusController : Controller() {
    val panels = observableListOf<StatusPanel?>()
    /**
     * Add a status panel to the given group.
     *
     *
     * @param label the label to put on the status panel.
     * @return the status panel.
     */
    @Synchronized
    fun addPanel(label: String): StatusPanel {
        val panelIndex = panels.size
        val panel = StatusPanel(label, panelIndex){
            Platform.runLater { removePanel(panelIndex) }
        }
        panels.add(panel)
        return panel
    }

    /**
     * Remove a status panel at the given index.
     *
     *
     * @param index the index of the panel to remove.
     */
    fun removePanel(index: Int) {
        val panel = panels[index]
        if (panel != null) {
            panels[index] = null
        }
    }

    /**
     * Remove a status panel.
     *
     *
     * @param panel the panel to remove.
     */
    fun removePanel(panel: StatusPanel?) {
        removePanel(panels.indexOf(panel))
    }
}

/**
 * A group of status panels that shows all the background tasks Quelea is
 * currently processing.
 *
 *
 * @constructor Create a new status panel group.
 * @author Michael
 */
class StatusPanelGroup : View() {
    private val statusController by inject<StatusController>()
    override val root = vbox {
        bindChildren(statusController.panels.filtered { it != null }){
            it!!.root
        }
    }
}
