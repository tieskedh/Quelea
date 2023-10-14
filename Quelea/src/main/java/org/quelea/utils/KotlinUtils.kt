package org.quelea.utils

import javafx.beans.property.ReadOnlyListWrapper
import javafx.collections.transformation.FilteredList
import javafx.scene.control.ComboBox
import javafx.scene.control.SelectionModel
import javafx.scene.input.MouseEvent

fun String.javaTrim() = this.trim { it <= ' ' }

val MouseEvent.isCtrlClick : Boolean get() = this.clickCount == 2

fun sequenceOfRange(range : IntRange) = range.asSequence()

fun <T> FilteredList<T>.readOnly() = ReadOnlyListWrapper(this)