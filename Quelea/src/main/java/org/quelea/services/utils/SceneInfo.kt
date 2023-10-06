/*
 * This file is part of Quelea, free projection software for churches.
 * 
 * Copyright (C) 2012 Michael Berry
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
package org.quelea.services.utils

import javafx.geometry.Rectangle2D

/**
 * The scene info used for storing the position and size of the main window
 * between runs of Quelea. Just a convenience class really; nothing fancy going
 * on here.
 *
 *
 * @author Michael
 */
data class SceneInfo(
    val x: Int,
    val y: Int,
    val width : Int,
    val height: Int,
    val isMaximised: Boolean
) {
    constructor(
        x: Double,
        y: Double,
        w: Double,
        h: Double,
        isMaximised: Boolean
    ) : this(
        x=x.toInt(),
        y=y.toInt(),
        width=w.toInt(),
        height=h.toInt(),
        isMaximised
    )

    val bounds: Rectangle2D
        get() = Rectangle2D(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble())

    override fun toString() = "$x,$y,$width,$height,$isMaximised"
}
