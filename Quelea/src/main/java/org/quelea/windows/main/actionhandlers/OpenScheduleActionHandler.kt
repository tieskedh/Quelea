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
package org.quelea.windows.main.actionhandlers

import javafx.event.ActionEvent
import javafx.stage.FileChooser
import org.quelea.services.utils.FileFilters
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.windows.main.QueleaApp
import org.quelea.windows.main.schedule.SchedulePanel
import tornadofx.FX

/**
 * The open schedule action listener.
 *
 * @author Michael
 */
class OpenScheduleActionHandler : ClearingEventHandler() {
    override fun handle(t: ActionEvent) {
        if (confirmClear()) {
            val chooser = FileChooser()
            if (get().lastScheduleFileDirectory != null) {
                chooser.initialDirectory = get().lastScheduleFileDirectory
            }
            chooser.extensionFilters.add(FileFilters.SCHEDULE)
            val file = chooser.showOpenDialog(QueleaApp.get().mainWindow)
            if (file != null) {
                get().setLastScheduleFileDirectory(file.parentFile)
                QueleaApp.get().openSchedule(file)
                FX.find<SchedulePanel>().themeNode.refresh()
            }
        }
    }
}
