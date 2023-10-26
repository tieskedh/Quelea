/*
 * This file is part of Quelea, free projection software for churches.
 *
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
package org.quelea.windows.main.menus

import javafx.scene.control.Menu
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.services.utils.ShortcutManager
import org.quelea.windows.main.QueleaApp
import org.quelea.windows.main.actionhandlers.LiveTextActionHandler
import org.quelea.windows.main.actionhandlers.SearchBibleActionHandler
import org.quelea.windows.main.actionhandlers.ShowOptionsActionHandler
import org.quelea.windows.main.actionhandlers.ViewBibleActionHandler
import org.quelea.windows.main.widgets.TestPaneDialog
import tornadofx.*
import java.lang.ref.SoftReference

/**
 * Quelea's tools menu.
 *
 *
 *
 * @author Michael
 */
class ToolsMenu : Menu(LabelGrabber.INSTANCE.getLabel("tools.menu")) {
    private var testDialog = SoftReference<TestPaneDialog?>(null)

    /**
     * Create the tools menu.
     */
    init {
        val darkTheme = get().useDarkTheme

        item(
            name = LabelGrabber.INSTANCE.getLabel("view.bible.button"),
            graphic = ImageView(
                Image(
                    if (darkTheme) "file:icons/bible-light.png" else "file:icons/bible.png",
                    20.0,
                    20.0,
                    false,
                    true
                )
            )
        ).onAction = ViewBibleActionHandler

        item(
            name= LabelGrabber.INSTANCE.getLabel("search.bible.button"),
            graphic = ImageView(
                Image(
                    if (darkTheme) "file:icons/bible-light.png" else "file:icons/bible.png",
                    20.0,
                    20.0,
                    false,
                    true
                )
            )
        ).onAction = SearchBibleActionHandler


        item(
            name=LabelGrabber.INSTANCE.getLabel("test.patterns.text"),
            graphic = ImageView(Image("file:icons/testbars.png",
                20.0,
                20.0,
                false,
                true))
        ).action {
            val dialog = testDialog.get() ?: TestPaneDialog()
                .also { testDialog = SoftReference(it) }

            dialog.show()
        }

        item(
            name = LabelGrabber.INSTANCE.getLabel("send.live.text"),
            graphic = ImageView(
                Image(
                    if (darkTheme) "file:icons/live_text-light.png" else "file:icons/live_text.png",
                    20.0,
                    20.0,
                    false,
                    true
                )
            ),
            keyCombination = KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN)
        ){
            isDisable = QueleaApp.get().mobileLyricsServer == null
            onAction = LiveTextActionHandler()
        }

        item(
            name=LabelGrabber.INSTANCE.getLabel("options.button"),
            graphic = ImageView(Image("file:icons/options.png",
                20.0,
                20.0,
                false,
                true
            )),
            keyCombination = ShortcutManager.getKeyCodeCombination(get().optionsKeys)
        ).onAction = ShowOptionsActionHandler()
    }
}
