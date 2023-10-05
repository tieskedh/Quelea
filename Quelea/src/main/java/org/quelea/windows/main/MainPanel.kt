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
package org.quelea.windows.main

import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.windows.library.LibraryPanel
import org.quelea.windows.main.schedule.SchedulePanel
import tornadofx.*
import java.util.logging.Level

/**
 * The main body of the main window, containing the schedule, the media bank,
 * the preview and the live panels.
 *
 * @author Michael
 */
class MainPanel : View() {
    /**
     * Get the panel displaying the order of service.
     *
     * @return the panel displaying the order of service.
     */
    lateinit var schedulePanel: SchedulePanel

    /**
     * Get the panel displaying the library of media.
     *
     * @return the library panel.
     */
    lateinit var libraryPanel: LibraryPanel

    /**
     * Get the panel displaying the selection of the preview lyrics.
     *
     * @return the panel displaying the selection of the preview lyrics.
     */

    lateinit var previewPanel: PreviewPanel

    /**
     * Get the panel displaying the selection of the live lyrics.
     *
     * @return the panel displaying the selection of the live lyrics.
     */
    lateinit var livePanel: LivePanel

    private lateinit var mainSplit: SplitPane
    private lateinit var scheduleAndLibrary: SplitPane

    override val root = borderpane {
        center {
            mainSplit = splitpane{

                scheduleAndLibrary = splitpane(Orientation.VERTICAL) {
                    minWidth = 160.0
                    LOGGER.log(Level.INFO, "Creating schedule panel")
                    opcr(this, FX.find<SchedulePanel>().also {
                        schedulePanel = it
                    }.root)
                    LOGGER.log(Level.INFO, "Creating library panel")
                    libraryPanel = opcr(this, LibraryPanel())
                }

                LOGGER.log(Level.INFO, "Creating preview panel")
                previewPanel = opcr(this, PreviewPanel()){
                    lyricsPanel.splitPane.setDividerPositions(0.58)
                }

                LOGGER.log(Level.INFO, "Creating live panel")
                livePanel= opcr(this, LivePanel()){
                    lyricsPanel.splitPane.setDividerPositions(0.58)
                }

                if (get().linkPreviewAndLiveDividers) {
                    previewPanel.lyricsPanel.splitPane.dividers.first().positionProperty().bindBidirectional(
                        livePanel.lyricsPanel.splitPane.dividers.first().positionProperty()
                    )
                }
            }
        }

        bottom<StatusPanelGroup>()
        LOGGER.log(Level.INFO, "Created main panel")
    }

    /**
     * Set the position of the dividers based on the properties file.
     */
    fun setSliderPos() {
        val mainPos = get().mainDivPos
        val prevLivePos = get().prevLiveDivPos
        val canvasPos = get().canvasDivPos
        val previewPos = get().previewDivPosKey
        val libraryPos = get().libraryDivPos
        if (prevLivePos != -1.0 && mainPos != -1.0) {
            mainSplit.setDividerPositions(mainPos, prevLivePos)
        } else {
            mainSplit.setDividerPositions(0.2717, 0.6384)
        }
        if (canvasPos != -1.0 && (get().linkPreviewAndLiveDividers || previewPos == -1.0)) {
            // live divider pos is found and either the preview and live dividers are linked, or we don't have a
            // position saved for the preview divider so set both live and preview to saved value for live
            previewPanel.lyricsPanel.splitPane.setDividerPositions(canvasPos)
            livePanel.lyricsPanel.splitPane.setDividerPositions(canvasPos)
        } else if (canvasPos != -1.0) {
            // live and preview dividers not linked, and we have a position saved for the live divider so just set live
            livePanel.lyricsPanel.splitPane.setDividerPositions(canvasPos)
        }
        if (previewPos != -1.0 && !get().linkPreviewAndLiveDividers) {
            // preview divider pos is found, and not linked with live divider, so just set preview divider position
            previewPanel.lyricsPanel.splitPane.setDividerPositions(previewPos)
        }
        if (libraryPos != -1.0) {
            scheduleAndLibrary.setDividerPositions(libraryPos)
        } else {
            scheduleAndLibrary.setDividerPositions(0.5)
        }
    }

    /**
     * The main splitpane divider position.
     */
    val mainDivPos: Double get() = mainSplit.getDividerPositions()[0]

    /**
     * The preview / live splitpane divider position.
     */
    val prevLiveDivPos: Double
        get() = mainSplit.getDividerPositions()[1]

    /**
     * The library / schedule splitpane divider position.
     */
    val libraryDivPos: Double
        get() = scheduleAndLibrary.getDividerPositions()[0]

    companion object {
        private val LOGGER = LoggerUtils.getLogger()
    }
}
