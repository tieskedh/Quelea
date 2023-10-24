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
import com.dlsc.preferencesfx.model.Category
import com.dlsc.preferencesfx.model.Setting
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.value.ObservableValue
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.services.utils.QueleaPropertyKeys.noticeBackgroundColourKey
import org.quelea.services.utils.QueleaPropertyKeys.noticeFontSizeKey
import org.quelea.services.utils.QueleaPropertyKeys.noticePositionKey
import org.quelea.services.utils.QueleaPropertyKeys.noticeSpeedKey
import org.quelea.windows.options.PreferencesDialog.Companion.getColorPicker
import org.quelea.windows.options.PreferencesDialog.Companion.getPositionSelector

/**
 * The panel that shows the notice options
 *
 *
 *
 * @author Arvid
 */
class OptionsNoticePanel internal constructor(private val bindings: HashMap<Field<*>, ObservableValue<Boolean>>) {
    private val noticeSpeed: DoubleProperty
    private val noticeSize: DoubleProperty

    /**
     * Create the options bible panel.
     *
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    init {
        noticeSpeed = SimpleDoubleProperty(get().noticeSpeed)
        noticeSize = SimpleDoubleProperty(get().noticeFontSize)
    }

    val noticesTab: Category
        get() = Category.of(
            LabelGrabber.INSTANCE.getLabel("notice.options.heading"),
            ImageView(Image("file:icons/noticessettingsicon.png")),
            getPositionSelector(
                LabelGrabber.INSTANCE.getLabel("notice.position.text"),
                false,
                get().noticePosition.text,
                null,
                bindings
            ).customKey(noticePositionKey),
            getColorPicker(
                LabelGrabber.INSTANCE.getLabel("notice.background.colour.text"),
                get().noticeBackgroundColour
            ).customKey(noticeBackgroundColourKey),
            Setting.of(LabelGrabber.INSTANCE.getLabel("notice.speed.text"), noticeSpeed, 2.0, 20.0, 1)
                .customKey(noticeSpeedKey),
            Setting.of(LabelGrabber.INSTANCE.getLabel("notice.font.size"), noticeSize, 20.0, 100.0, 1)
                .customKey(noticeFontSizeKey)
        )
}
