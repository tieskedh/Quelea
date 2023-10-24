package org.quelea.windows.options.customprefs

import com.dlsc.formsfx.model.structure.StringField
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl
import javafx.geometry.Pos
import javafx.scene.control.ColorPicker
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import tornadofx.*

class ColorPickerPreference(
    private val initialValue: Color
) : SimpleControl<StringField, StackPane>() {
    /**
     * The colorPicker is the container that displays the node to select a color value.
     */
    private lateinit var colorPicker: ColorPicker

    override fun initializeParts() {
        super.initializeParts()
        node = StackPane().apply {
            styleClass.add("simple-text-control")
        }
        colorPicker = ColorPicker(initialValue).apply {
            maxWidth = Double.MAX_VALUE
            val colorStr =  value.toColorString()
            setOnAction {
                if (field.valueProperty().value != colorStr)
                    field.valueProperty().value = colorStr
            }
        }

        field.valueProperty().value = colorPicker.value.toColorString()
    }

    private fun Color.toColorString(): String = run { "$red,$green,$blue" }

    override fun layoutParts() {
        node.children.addAll(colorPicker)
        node.alignment = Pos.CENTER_LEFT
    }

    override fun setupBindings() {
        super.setupBindings()
        field.valueProperty().onChange { newValue ->
            if (!newValue.isNullOrEmpty()) {
                val (r,g,b) = newValue.split(',',limit=4)
                    .map { (it.toDouble() * 255).toInt() }
                val newColor = Color.rgb(r, g, b)
                if (colorPicker.value != newColor) colorPicker.value = newColor
            }
        }
    }


    override fun setupValueChangedListeners() {
        super.setupValueChangedListeners()
        colorPicker.focusedProperty().onChange { toggleTooltip(colorPicker) }
    }
}
