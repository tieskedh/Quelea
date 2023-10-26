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
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.services.utils.UpdateChecker
import org.quelea.utils.DesktopApi
import org.quelea.windows.help.AboutDialog
import tornadofx.*
import java.awt.Desktop

/**
 * Quelea's help menu.
 *
 *
 * @author Michael
 */
class HelpMenu : Menu(LabelGrabber.INSTANCE.getLabel("help.menu")) {
    /**
     * Create a new help menu
     */
    init {
        if (Desktop.isDesktopSupported()) {
            queleaMenuItem(
                labelName = "help.menu.facebook",
                icon = "facebook"
            ) { launchPage(get().facebookPageLocation) }

            queleaMenuItem(
                labelName = "help.menu.discussion",
                icon = "discuss"
            ) { launchPage(get().discussLocation) }

            queleaMenuItem(
                labelName = "help.menu.wiki",
                icon = "wiki"
            ) { launchPage(get().wikiPageLocation) }
        }

        queleaMenuItem(
            labelName = "help.menu.update",
            icon = "update"
        ) { UpdateChecker().checkUpdate(true, true, true) }


        queleaMenuItem(
            labelName = "help.menu.about",
            icon = "about"
        ){ find<AboutDialog>().openModal() }
    }

    private fun launchPage(page: String) = DesktopApi.browse(page)
}

fun Menu.queleaMenuItem(
    labelName : String,
    icon : String,
    action : () -> Unit
) = item(
    name = LabelGrabber.INSTANCE.getLabel(labelName),
    graphic = ImageView(Image("file:icons/$icon.png", 16.0, 16.0, false, true))
).action(action)