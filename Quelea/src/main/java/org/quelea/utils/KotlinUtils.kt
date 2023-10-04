package org.quelea.utils

fun String.javaTrim() = this.trim { it <= ' ' }