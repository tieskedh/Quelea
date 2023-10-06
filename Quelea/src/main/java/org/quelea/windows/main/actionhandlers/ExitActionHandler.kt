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
package org.quelea.windows.main.actionhandlers

import javafx.event.ActionEvent
import javafx.event.Event
import javafx.event.EventHandler
import org.javafx.dialog.Dialog
import org.quelea.data.ScheduleSaver
import org.quelea.data.displayable.PresentationDisplayable
import org.quelea.data.powerpoint.OOUtils
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.services.utils.SceneInfo
import org.quelea.windows.main.QueleaApp
import org.quelea.windows.presentation.PowerPointHandler
import tornadofx.runLater
import java.util.logging.Level
import kotlin.concurrent.thread
import kotlin.system.exitProcess

/**
 * The exit action listener - called when the user requests they wish to exit
 * Quelea.
 *
 *
 * @author Michael
 */
class ExitActionHandler : EventHandler<ActionEvent> {
    private var cancel = false

    /**
     * Call this method when the event is fired.
     */
    override fun handle(t: ActionEvent) {
        exit(t)
    }

    private var block = false

    /**
     * Process the necessary logic to cleanly exit from Quelea.
     *
     * @param t the event that caused the exit.
     */
    fun exit(t: Event) {
        LOGGER.log(Level.INFO, "exit() called")

        val mainWindow = QueleaApp.get().mainWindow
        t.consume()
        val schedule = mainWindow.mainPanel.schedulePanel.scheduleList.getSchedule()
        if (!schedule.isEmpty && schedule.isModified) {
            cancel = true
            val d = Dialog.buildConfirmation(
                LabelGrabber.INSTANCE.getLabel("save.before.exit.title"),
                LabelGrabber.INSTANCE.getLabel("save.before.exit.text")
            ).addYesButton {
                //Save schedule
                block = true
                ScheduleSaver().saveSchedule(false) { success: Boolean ->
                    cancel = !success
                    block = false
                }
            }.addNoButton { cancel = false }
                .addCancelButton { /*no need to do anything*/ }
                .build()
            d.showAndWait()
            while (block) {
                try {
                    Thread.sleep(5)
                } catch (ex: InterruptedException) {
                    //Meh.
                }
            }
            if (cancel) return  //Don't exit
        }
        LOGGER.log(Level.INFO, "Saving window position...")
        get().setSceneInfo(
            SceneInfo(
                mainWindow.x,
                mainWindow.y,
                mainWindow.width,
                mainWindow.height,
                mainWindow.isMaximized
            )
        )
        get().mainDivPos = mainWindow.mainPanel.mainDivPos
        get().prevLiveDivPos = mainWindow.mainPanel.prevLiveDivPos
        get().canvasDivPos = mainWindow.mainPanel.livePanel.lyricsPanel.splitPane.getDividerPositions()[0]
        get().previewDivPosKey = mainWindow.mainPanel.previewPanel.lyricsPanel.splitPane.getDividerPositions()[0]
        get().libraryDivPos = mainWindow.mainPanel.libraryDivPos
        LOGGER.log(Level.INFO, "Hiding main window...")
        mainWindow.hide()
        LOGGER.log(Level.INFO, "Cleaning up displayables before exiting..")
        mainWindow.mainPanel.schedulePanel.scheduleList.itemsProperty().get()
            .filterNotNull()
            .forEach {
                LOGGER.log(Level.INFO, "Cleaning up {0}", it.javaClass)
                it.dispose()
            }

        LOGGER.log(Level.INFO, "Try to close OOfice if opened")
        OOUtils.closeOOApp()

        QueleaApp.get().mobileLyricsServer?.let {
            LOGGER.log(Level.INFO, "Stopping mobile lyrics server")
            it.stop()
        }
        QueleaApp.get().remoteControlServer?.let {
            LOGGER.log(Level.INFO, "Stopping remote control server")
            QueleaApp.get().remoteControlServer.stop()
        }

        if (QueleaApp.get().mainWindow.mainPanel.livePanel.displayable is PresentationDisplayable) {
            LOGGER.log(Level.INFO, "Closing open PowerPoint presentations")
            PowerPointHandler.closePresentation()
        }
        LOGGER.log(Level.INFO, "Checking if Quelea currently is recording audio")
        val toolbar = mainWindow.mainToolbar
        val recHandler = toolbar.recordButtonHandler.recordingsHandler
        if (toolbar.recordButtonHandler != null && recHandler != null) {
            if (recHandler.isRecording) {
                block = true
                val d = Dialog.buildConfirmation(
                    LabelGrabber.INSTANCE.getLabel("save.recording.before.exit.title"),
                    LabelGrabber.INSTANCE.getLabel("save.recording.before.exit.message")
                ).addYesButton { toolbar.stopRecording() }
                    .addNoButton {  exitProcess(0) }
                    .build()

                d.setOnCloseRequest { exitProcess(0) }
                thread(isDaemon = true) {
                    while (block) {
                        try {
                            Thread.sleep(500)
                            if (recHandler.finishedSaving) {
                                runLater { d.close() }
                                if (get().convertRecordings) {
                                    val converting = recHandler.isConverting
                                    if (!converting) {
                                        block = false
                                        exitProcess(0)
                                    }
                                } else {
                                    block = false
                                    exitProcess(0)
                                }
                            }
                        } catch (ex: InterruptedException) {
                        }
                    }
                }
                d.showAndWait()
                return  //Don't exit until the recording is saved and converted
            }
        }
        exitProcess(0)
    }

    companion object {
        private val LOGGER = LoggerUtils.getLogger()
    }
}
