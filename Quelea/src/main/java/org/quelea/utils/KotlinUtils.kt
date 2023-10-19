package org.quelea.utils

import javafx.beans.property.ReadOnlyListWrapper
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.ToggleButton
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import tornadofx.*

fun String.javaTrim() = this.trim { it <= ' ' }

val MouseEvent.isCtrlClick : Boolean get() = this.clickCount == 2

fun sequenceOfRange(range : IntRange) = range.asSequence()

fun <T> ObservableList<T>.readOnly() = ReadOnlyListWrapper(this)



fun EventTarget.dumbToggleButton(
    text : String = "",
    graphic : ImageView? = null,
    op : ToggleButton.() -> Unit = {}
) = ToggleButton(text,graphic).attachTo(this,op)

operator fun NodeList.iterator() = iterator<Node> {
    for (i in 0 until length) yield(item(i))
}

fun NodeList.asSequence() = iterator().asSequence()
fun Node.nameMatchesAny(vararg names: String) = names.any { it.equals(nodeName, ignoreCase = true) }
infix fun Node.nameMatches(name : String) = name.equals(nodeName, ignoreCase = true)