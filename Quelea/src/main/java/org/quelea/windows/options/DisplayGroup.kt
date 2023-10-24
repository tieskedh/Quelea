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
package org.quelea.windows.options

import com.dlsc.formsfx.model.structure.Field
import com.dlsc.formsfx.model.structure.IntegerField
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleComboBoxControl
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleIntegerControl
import com.dlsc.preferencesfx.model.Group
import com.dlsc.preferencesfx.model.Setting
import javafx.beans.property.*
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.geometry.Bounds
import javafx.stage.Screen
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.services.utils.QueleaPropertyKeys.controlScreenKey
import org.quelea.services.utils.QueleaPropertyKeys.projectorHCoordKey
import org.quelea.services.utils.QueleaPropertyKeys.projectorModeKey
import org.quelea.services.utils.QueleaPropertyKeys.projectorScreenKey
import org.quelea.services.utils.QueleaPropertyKeys.projectorWCoordKey
import org.quelea.services.utils.QueleaPropertyKeys.projectorXCoordKey
import org.quelea.services.utils.QueleaPropertyKeys.projectorYCoordKey
import org.quelea.services.utils.QueleaPropertyKeys.stageHCoordKey
import org.quelea.services.utils.QueleaPropertyKeys.stageModeKey
import org.quelea.services.utils.QueleaPropertyKeys.stageScreenKey
import org.quelea.services.utils.QueleaPropertyKeys.stageWCoordKey
import org.quelea.services.utils.QueleaPropertyKeys.stageXCoordKey
import org.quelea.services.utils.QueleaPropertyKeys.stageYCoordKey
import tornadofx.*



class DisplayGroup internal constructor(
    groupName: String,
    custom: Boolean,
    bindings: MutableMap<Field<*>, ObservableValue<Boolean>>
) {
    var isDisplayChange = false
    @JvmField
    var group: Group? = null

    init {
        val useCustomPosition: BooleanProperty = SimpleBooleanProperty(when (groupName) {
            LabelGrabber.INSTANCE.getLabel("projector.screen.label") ->
                get().isProjectorModeCoords

            LabelGrabber.INSTANCE.getLabel("stage.screen.label") ->
                get().isStageModeCoords

            else -> false
        })

        useCustomPosition.onChange { _ : Boolean? -> isDisplayChange = true }

        val availableScreens = getAvailableScreens(includeNone = custom)

        val screenSelectProperty = SimpleObjectProperty(availableScreens.first())
        val customControl = Field.ofSingleSelectionType(
            SimpleListProperty(availableScreens),
            screenSelectProperty
        ).render(
            SimpleComboBoxControl()
        )

        availableScreens.onChange { isDisplayChange = true }
        screenSelectProperty.onChange { isDisplayChange = true }

        if (!custom) {
            val screen = get().controlScreen
            screenSelectProperty.value = if (screen in 0 until availableScreens.size) availableScreens[screen]
            else availableScreens.first()

            group = Group.of(
                groupName,
                Setting.of(groupName, customControl, screenSelectProperty)
                    .customKey(controlScreenKey)
            )
        } else {
            var screen: Int
            val bounds: Bounds
            if (groupName == LabelGrabber.INSTANCE.getLabel("projector.screen.label")) {
                screen = get().projectorScreen
                bounds = get().projectorCoords
            } else {
                screen = get().stageScreen
                bounds = get().stageCoords
            }

            val widthProperty = SimpleIntegerProperty(bounds.width.toInt()).apply {
                onChange { isDisplayChange = true }
            }
            val heightProperty = SimpleIntegerProperty(bounds.height.toInt()).apply {
                onChange { isDisplayChange = true }
            }
            val xProperty = SimpleIntegerProperty(bounds.minX.toInt()).apply {
                onChange { isDisplayChange = true }
            }
            val yProperty = SimpleIntegerProperty(bounds.minY.toInt()).apply {
                onChange { isDisplayChange = true }
            }

            val sizeWith = Field.ofIntegerType(widthProperty).render(
                SimpleIntegerControl()
            )

            val sizeHeight = Field.ofIntegerType(heightProperty).render(
                SimpleIntegerControl()
            )
            val posX = Field.ofIntegerType(xProperty).render(
                SimpleIntegerControl()
            )
            val posY = Field.ofIntegerType(yProperty).render(
                SimpleIntegerControl()
            )



            screen++ // Compensate for "none" value in available screens
            screenSelectProperty.value = if (screen in 1 until availableScreens.size) availableScreens[screen] else availableScreens[0]

            val projectorGroup = groupName == LabelGrabber.INSTANCE.getLabel("projector.screen.label")
            group = Group.of(
                groupName,
                Setting.of(groupName, customControl, screenSelectProperty)
                    .customKey(if (projectorGroup) projectorScreenKey else stageScreenKey),
                Setting.of(LabelGrabber.INSTANCE.getLabel("custom.position.text"), useCustomPosition)
                    .customKey(if (projectorGroup) projectorModeKey else stageModeKey),
                Setting.of<IntegerField, IntegerProperty>("W", sizeWith, widthProperty)
                    .customKey(if (projectorGroup) projectorWCoordKey else stageWCoordKey),
                Setting.of<IntegerField, IntegerProperty>("H", sizeHeight, heightProperty)
                    .customKey(if (projectorGroup) projectorHCoordKey else stageHCoordKey),
                Setting.of<IntegerField, IntegerProperty>("X", posX, xProperty)
                    .customKey(if (projectorGroup) projectorXCoordKey else stageXCoordKey),
                Setting.of<IntegerField, IntegerProperty>("Y", posY, yProperty)
                    .customKey(if (projectorGroup) projectorYCoordKey else stageYCoordKey)
            )
            bindings[sizeWith] = !useCustomPosition
            bindings[sizeHeight] = !useCustomPosition
            bindings[posX] = !useCustomPosition
            bindings[posY] = !useCustomPosition
            bindings[customControl] = useCustomPosition
        }
    }

    /**
     * Get a list model describing the available graphical devices.
     *
     * @return a list model describing the available graphical devices.
     */
    private fun getAvailableScreens(includeNone: Boolean): ObservableList<String> = observableListOf<String>().apply {
        if (includeNone) this += LabelGrabber.INSTANCE.getLabel("none.text")

        Screen.getScreens().indices.mapTo(this) {
            LabelGrabber.INSTANCE.getLabel("output.text") + " " + (it + 1)
        }
    }
}
