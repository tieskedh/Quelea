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
package org.quelea.services.notice

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties
import org.quelea.services.utils.bindSingleSelectedBidirectional
import tornadofx.*

class NoticeDialogView : View(
    title = LabelGrabber.INSTANCE.getLabel("notices.heading"),
    icon = ImageView(Image("file:icons/info.png"))
) {
    val controller = find<NoticeController>()

    init {
        if (QueleaProperties.get().useDarkTheme) importStylesheet("org/modena_dark.css")
    }
    override val root = borderpane {
        left {
            borderpane {
                borderpaneConstraints {
                    margin = Insets(5.0)
                }

                top {
                    vbox(5) {
                        button(LabelGrabber.INSTANCE.getLabel("new.notice.text")) {
                            alignment = Pos.CENTER
                            maxWidth = Double.MAX_VALUE
                            setOnAction {
                                controller.newNotice {
                                    NoticeEntryDialog.getNotice(null, false)
                                }
                            }
                        }

//                      editNoticeButton
                        button(LabelGrabber.INSTANCE.getLabel("edit.notice.text")) {
                            alignment = Pos.CENTER
                            isDisable = true
                            maxWidth = Double.MAX_VALUE

                            disableWhen(controller.noNoticeSelected)

                            setOnAction {
                                controller.editSelectedNotice {
                                    NoticeEntryDialog.getNotice(it, false)
                                }
                            }
                        }

//                      removeNoticeButton
                        button(
                            LabelGrabber.INSTANCE.getLabel("remove.notice.text")
                        ) {
                            alignment = Pos.CENTER
                            isDisable = true
                            maxWidth = Double.MAX_VALUE
                            disableWhen(controller.noNoticeSelected)
                            setOnAction {
                                controller.removeSelectedNotice()
                            }
                        }

                        text(LabelGrabber.INSTANCE.getLabel("saved.notices")) {
                            styleClass.add("text")
                        }

                        listview(controller.templateList) {
                            bindSelected(controller.selectedTemplateProp)
                            maxWidth = Double.MAX_VALUE

                            setOnMouseClicked {
                                controller.loadFromSelectedTemplate {
                                    NoticeEntryDialog.getNotice(it, false)
                                }
                            }
                        }
                    }
                }
            }
        }

        center {
            listview(controller.noticesProp) {
                bindSingleSelectedBidirectional(controller.selectedNoticeProp)
            }
        }

        bottom {
            //doneButton
            button(
                text = LabelGrabber.INSTANCE.getLabel("done.text"),
                graphic = ImageView(Image("file:icons/tick.png"))
            ) {

                borderpaneConstraints {
                    alignment = Pos.CENTER
                    margin = Insets(5.0)
                }
                setOnAction { hide() }
            }
        }
    }

    /**
     * Called when the notice status has updated, i.e. it's removed or the
     * counter is decremented.
     */
    fun noticesUpdated() {
        controller.noticesUpdated(
            onlyRetain = NoticeEntryDialog::noticesUpdated
        )
    }

}