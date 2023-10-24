/*
 * This file is part of Quelea, free projection software for churches.
 *
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.options

import com.dlsc.formsfx.model.structure.Field
import com.dlsc.preferencesfx.model.Category
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.stage.Screen
import org.quelea.services.languages.LabelGrabber
import org.quelea.windows.main.GraphicsDeviceWatcher
import org.quelea.windows.main.QueleaApp

/**
 * A panel that the user uses to set up the displays that match to the outputs.
 *
 * @constructor Create a new display setup panel.
 * @param bindings HashMap of bindings to setup after the dialog has been created
 * @author Arvid
 */
class OptionsDisplaySetupPanel internal constructor(
    private val bindings: HashMap<Field<*>, ObservableValue<Boolean>>
) {
    private val controlScreen = DisplayGroup(
        groupName = LabelGrabber.INSTANCE.getLabel("control.screen.label"),
        custom = false,
        bindings=bindings
    )

    private val projectorScreen = DisplayGroup(
        groupName = LabelGrabber.INSTANCE.getLabel("projector.screen.label"),
        custom = true,
        bindings=bindings
    )

    private val stageScreen: DisplayGroup = DisplayGroup(
        groupName = LabelGrabber.INSTANCE.getLabel("stage.screen.label"),
        custom = true,
        bindings=bindings
    )

    init {
        GraphicsDeviceWatcher.INSTANCE.addGraphicsDeviceListener {
            QueleaApp.get().mainWindow.preferencesDialog.updatePos()
        }
    }

    fun getDisplaySetupTab(): Category = Category.of(
        LabelGrabber.INSTANCE.getLabel("display.options.heading"),
        ImageView(Image("file:icons/monitorsettingsicon.png")),
        controlScreen.group,
        projectorScreen.group,
        stageScreen.group
    )


    var isDisplayChange: Boolean
        get() = controlScreen.isDisplayChange ||
                projectorScreen.isDisplayChange ||
                stageScreen.isDisplayChange
        set(displayChange) {
            controlScreen.isDisplayChange = displayChange
            projectorScreen.isDisplayChange = displayChange
            stageScreen.isDisplayChange = displayChange
        }
}
