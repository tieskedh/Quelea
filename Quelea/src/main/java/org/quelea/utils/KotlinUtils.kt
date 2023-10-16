package org.quelea.utils

import javafx.beans.property.ReadOnlyListWrapper
import javafx.collections.transformation.FilteredList
import javafx.event.EventTarget
import javafx.scene.control.ComboBox
import javafx.scene.control.SelectionModel
import javafx.scene.control.ToggleButton
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import tornadofx.*

fun String.javaTrim() = this.trim { it <= ' ' }

val MouseEvent.isCtrlClick : Boolean get() = this.clickCount == 2

fun sequenceOfRange(range : IntRange) = range.asSequence()

fun <T> FilteredList<T>.readOnly() = ReadOnlyListWrapper(this)



fun EventTarget.dumbToggleButton(
    text : String = "",
    graphic : ImageView? = null,
    op : ToggleButton.() -> Unit = {}
) = ToggleButton(text,graphic).attachTo(this,op)
