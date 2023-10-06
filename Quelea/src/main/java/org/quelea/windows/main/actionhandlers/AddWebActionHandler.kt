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
import javafx.event.EventHandler
import javafx.scene.control.TextInputDialog
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Stage
import org.quelea.data.displayable.WebDisplayable
import org.quelea.services.languages.LabelGrabber
import org.quelea.windows.main.QueleaApp

/**
 * The action handler responsible for letting the user add a websites to the
 * schedule.
 *
 *
 * @author Arvid
 */

class AddWebActionHandler : EventHandler<ActionEvent> {
    override fun handle(t: ActionEvent){
        val dialog = TextInputDialog("https://").apply {
            title = LabelGrabber.INSTANCE.getLabel("website.dialog.title")
            headerText = LabelGrabber.INSTANCE.getLabel("website.dialog.header")
            contentText = LabelGrabber.INSTANCE.getLabel("website.dialog.content")
            graphic = ImageView(Image("file:icons/website.png"))
        }

        val stage = dialog.dialogPane.scene.window as Stage
        stage.icons.add(Image("file:icons/web-small.png"))
        dialog.showAndWait().ifPresent { url->
            val displayable = WebDisplayable(WebDisplayable.sanitiseUrl(url))
            QueleaApp.get().mainWindow.mainPanel.schedulePanel.scheduleList.add(displayable)
        }
    }
}
