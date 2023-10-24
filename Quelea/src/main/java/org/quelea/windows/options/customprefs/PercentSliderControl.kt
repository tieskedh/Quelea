package org.quelea.windows.options.customprefs

import com.dlsc.formsfx.model.structure.DoubleField
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.*
import java.math.RoundingMode

/**
 * Provides an implementation of a percent slider control for an [DoubleField].
 *
 * @constructor Creates a slider for double values with a minimum and maximum value, with a set precision.
 *
 * @param min       minimum slider value
 * @param max       maximum slider value
 * @param precision number of digits after the decimal point
 *
 *
 * @author François Martin
 * @author Marco Sanfratello
 * @author Arvid Nyström
 */
class PercentSliderControl(
    private val min: Double,
    private val max: Double,
    private val precision: Int
) : SimpleControl<DoubleField, HBox>() {
    /**
     * - fieldLabel is the container that displays the label property of the
     * field.
     * - slider is the control to change the value.
     * - node holds the control so that it can be styled properly.
     */
    private lateinit var slider: Slider
    private lateinit var valueLabel: Label

    /**
     * Rounds a value to a given precision, using [RoundingMode.HALF_UP].
     *
     * @receiver        value to be rounded
     * @param precision number of digits after the decimal point
     * @return
     */
    private fun Double.round(precision: Int) = toBigDecimal()
        .setScale(precision, RoundingMode.HALF_UP)
        .toDouble()


    override fun initializeParts() {
        super.initializeParts()
        fieldLabel = Label(field.labelProperty().value)
        valueLabel = Label((100 * field.value).toInt().toString() + "%")
        slider = Slider().also {
            it.min = min
            it.max = max
            it.isShowTickLabels = false
            it.isShowTickMarks = false
            it.value = field.value
        }

        node = HBox()
        node.styleClass.add("double-slider-control")
    }


    override fun layoutParts() {
        node.apply {
            spacing = VALUE_LABEL_PADDING.toDouble()

            slider.attachTo(this) {
                hboxConstraints { hgrow = Priority.ALWAYS }
            }

            valueLabel.attachTo(this) {
                hboxConstraints { marginRight = VALUE_LABEL_PADDING.toDouble() }
                alignment = Pos.CENTER
                minWidth = VALUE_LABEL_PADDING.toDouble()
            }
        }
    }

    override fun setupValueChangedListeners() {
        super.setupValueChangedListeners()
        field.userInputProperty().onChange {
                val sliderValue = field.userInput.toDouble()
                    .round(precision)
                slider.value = sliderValue
                valueLabel.text = (100 * field.value).toInt().toString() + "%"
            }

        field.errorMessagesProperty().onChange { _ : ObservableList<String?>? ->
            toggleTooltip(slider)
        }


        field.tooltipProperty().onChange { toggleTooltip(slider) }
        slider.focusedProperty().onChange { _: Boolean? -> toggleTooltip(slider) }
    }


    override fun setupEventHandlers() {
        slider.valueProperty().onChange { newValue: Number ->
            field.userInputProperty().value = newValue.toDouble().round(precision).toString()
        }
    }

    companion object {
        const val VALUE_LABEL_PADDING = 25
    }
}
