package org.quelea.utils

import javafx.scene.input.MouseEvent

fun String.javaTrim() = this.trim { it <= ' ' }

val MouseEvent.isCtrlClick : Boolean get() = this.clickCount == 2