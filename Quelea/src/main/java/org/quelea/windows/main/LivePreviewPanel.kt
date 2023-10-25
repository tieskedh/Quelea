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

import javafx.scene.Node
import javafx.scene.input.*
import javafx.scene.layout.BorderPane
import org.quelea.data.displayable.*
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.services.utils.checkFXThread
import org.quelea.windows.image.ImagePanel
import org.quelea.windows.imagegroup.ImageGroupPanel
import org.quelea.windows.lyrics.SelectLyricsPanel
import org.quelea.windows.main.quickedit.QuickEditDialog
import org.quelea.windows.main.widgets.CardPane
import org.quelea.windows.multimedia.MultimediaPanel
import org.quelea.windows.pdf.PdfPanel
import org.quelea.windows.presentation.PresentationPanel
import org.quelea.windows.timer.TimerPanel
import org.quelea.windows.web.WebPanel
import java.util.logging.Level

/**
 * The common superclass of the live / preview panels used for selecting the
 * lyrics / picture.
 *
 *
 *
 * @author Michael
 */
abstract class LivePreviewPanel : BorderPane() {
    private val windows: MutableSet<DisplayStage> = HashSet()

    /**
     * Get the displayable currently being displayed, or null if there isn't
     * one.
     *
     *
     *
     * @return the current displayable.
     */
    var displayable: Displayable? = null
        private set

    private val cardPanel = CardPane<AbstractPanel>()
    private var currentLabel: String? = null

    /**
     * Get the select lyrics panel on this panel.
     *
     *
     *
     * @return the select lyrics panel.
     */
    val lyricsPanel = SelectLyricsPanel(this)
    private val imagePanel = ImagePanel()

    /**
     * Get the presentation panel on this live / preview panel.
     *
     *
     *
     * @return the presentation panel.
     */
    val presentationPanel = PresentationPanel(this)

    /**
     * Get the PDF panel on this live / preview panel.
     *
     *
     *
     * @return the PDF panel.
     */
    val pdfPanel = PdfPanel(this)

    /**
     * Get the video panel on this live / preview panel.
     *
     *
     *
     * @return the presentation panel.
     */
    val videoPanel = MultimediaPanel()
    private val audioPanel = MultimediaPanel()

    /**
     * Get the timer panel.
     *
     *
     *
     * @return the timer panel.
     */
    val timerPanel = TimerPanel()

    /**
     * Get the web panel.
     *
     *
     *
     * @return the web panel.
     */
    val webPanel = WebPanel()

    /**
     * Get the image group panel on this live / preview panel.
     *
     *
     *
     * @return the image group panel.
     */

    val imageGroupPanel = ImageGroupPanel(this)
    private val quickEditDialog = QuickEditDialog()

    /**
     * Create the live preview panel, common superclass of live and preview
     * panels.
     */
    init {
        center = cardPanel
        cardPanel[LYRICS_LABEL] = lyricsPanel
        cardPanel[IMAGE_LABEL] = imagePanel
        cardPanel[VIDEO_LABEL] = videoPanel
        cardPanel[TIMER_LABEL] = timerPanel
        cardPanel[AUDIO_LABEL] = audioPanel
        cardPanel[PRESENTATION_LABEL] = presentationPanel
        cardPanel[PDF_LABEL] = pdfPanel
        cardPanel[WEB_LABEL] = webPanel
        cardPanel[IMAGE_GROUP_LABEL] = imageGroupPanel
        cardPanel.show(LYRICS_LABEL)


        lyricsPanel.lyricsList.setOnMouseClicked { me: MouseEvent ->
            if (me.isControlDown || me.isShiftDown)
                doQuickEdit(lyricsPanel.lyricsList.quickEditIndex)
        }
        lyricsPanel.lyricsList.setOnKeyPressed { ke: KeyEvent ->
            if (ke.isControlDown && ke.code == KeyCode.Q) {
                doQuickEdit(lyricsPanel.currentIndex)
            }
        }
        presentationPanel.buildLoopTimeline()
        setOnDragOver { event: DragEvent ->
            if (event.dragboard.getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) != null) {
                event.acceptTransferModes(*TransferMode.ANY)
            }
        }
        setOnDragDropped { event: DragEvent ->
            if (event.dragboard.getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) is SongDisplayable) {
                val displayable =
                    event.dragboard.getContent(SongDisplayable.SONG_DISPLAYABLE_FORMAT) as? SongDisplayable
                if (displayable != null) setDisplayable(displayable, 0)
            }
            event.consume()
        }
    }

    fun selectFirstLyric() {
        when (currentLabel) {
            LYRICS_LABEL -> lyricsPanel.selectFirst()
            PDF_LABEL -> pdfPanel.selectFirst()
            IMAGE_GROUP_LABEL -> imageGroupPanel.selectFirst()
        }
    }

    fun selectLastLyric() {
        when (currentLabel) {
            PRESENTATION_LABEL -> presentationPanel.selectLast()
            LYRICS_LABEL -> lyricsPanel.selectLast()
            PDF_LABEL -> pdfPanel.selectLast()
            IMAGE_GROUP_LABEL -> imageGroupPanel.selectLast()
        }
    }

    /**
     * Perform a quick edit on the given index.
     *
     *
     *
     * @param index the index on which to perform the quick edit.
     */
    fun doQuickEdit(index: Int) {
        (displayable as? SongDisplayable)?.let { song ->
            quickEditDialog.apply {
                setSongSection(song, index)
                show()
            }
            setDisplayable(song, this.index)
        }
    }

    /**
     * Update the one line mode for the lyrics panel from the properties file.
     */
    fun updateOneLineMode() = lyricsPanel.setOneLineMode(get().oneLineMode)

    /**
     * Get the container panel (the one using the cardlayout that flips between
     * the various available panels.
     *
     *
     *
     * @return the container panel.
     */
    val currentPane: Node?
        get() = cardPanel.currentPane


    /**
     * Clear all the contained panels to a null displayable.
     */
    open fun removeDisplayable() {
        displayable = null
        when (currentLabel) {
            PRESENTATION_LABEL -> presentationPanel.showDisplayable(null, 0)
            PDF_LABEL -> pdfPanel.showDisplayable(null, 0)
            IMAGE_GROUP_LABEL -> imageGroupPanel.showDisplayable(null, 0)
            VIDEO_LABEL -> videoPanel.showDisplayable(null, 0)
            WEB_LABEL -> webPanel.showDisplayable(null as WebDisplayable?)
        }

        for (panel in cardPanel) {
            panel.removeCurrentDisplayable()
        }

        currentLabel?.takeUnless { it ==  LYRICS_LABEL }?.let {
            cardPanel.show(it)
            currentLabel = it
        }
    }
    /**
     * The currently selected displayable index.
     * Only suitable for powerpoint / PDF / image group / lyrics panels.
     */
    val index: Int
        get() = when (currentLabel) {
            PRESENTATION_LABEL -> presentationPanel.index
            PDF_LABEL -> pdfPanel.index
            IMAGE_GROUP_LABEL -> imageGroupPanel.index
            else -> lyricsPanel.index
        }

    /**
     * The length of the current displayable.
     * Only suitable for powerpoint / PDF / image group / lyrics panels.
     */
    val lenght: Int
        get() = when (currentLabel) {
            PRESENTATION_LABEL -> presentationPanel.slideCount
            PDF_LABEL -> pdfPanel.slideCount
            IMAGE_GROUP_LABEL -> imageGroupPanel.slideCount
            else -> lyricsPanel.slideCount
        }

    /**
     * Advances currently selected displayable index. Only suitable for
     * powerpoint / lyrics panels.
     *
     *
     */
    fun advance() {
        when (currentLabel) {
            PRESENTATION_LABEL -> presentationPanel.advance()
            PDF_LABEL -> pdfPanel.advance()
            IMAGE_GROUP_LABEL -> imageGroupPanel.advance()
            LYRICS_LABEL -> lyricsPanel.advance()
            IMAGE_LABEL -> imagePanel.advance()
            VIDEO_LABEL -> videoPanel.advance()
            TIMER_LABEL -> timerPanel.advance()
            WEB_LABEL -> webPanel.advance()
        }
    }

    /**
     * Moves to previous slide in currently selected displayable index. Only
     * suitable for powerpoint / lyrics panels.
     *
     *
     */
    fun previous() {
        when (currentLabel) {
            PRESENTATION_LABEL -> presentationPanel.previous()
            PDF_LABEL -> pdfPanel.previous()
            IMAGE_GROUP_LABEL -> imageGroupPanel.previous()
            LYRICS_LABEL -> lyricsPanel.previous()
            IMAGE_LABEL -> imagePanel.previous()
            VIDEO_LABEL -> videoPanel.previous()
            TIMER_LABEL -> timerPanel.previous()
            WEB_LABEL -> webPanel.previous()
        }
    }

    /**
     * Set the displayable shown on this panel.
     *
     *
     *
     * @param displayable the displayable to show.
     * @param index       the index of the displayable to show, if relevant.
     */
    open fun setDisplayable(displayable: Displayable?, index: Int) {
        checkFXThread()

        if (!(this.displayable is TextDisplayable && displayable is TextDisplayable)) {
            lyricsPanel.removeCurrentDisplayable()
        }

        QueleaApp.get().mainWindow.mainPanel.schedulePanel.scheduleList.listView.refresh()
        this.displayable = displayable
        presentationPanel.stopCurrent()
        pdfPanel.stopCurrent()
        imageGroupPanel.stopCurrent()
        videoPanel.stopCurrent()
        audioPanel.removeCurrentDisplayable()
        videoPanel.removeCurrentDisplayable()
        timerPanel.removeCurrentDisplayable()
        imagePanel.removeCurrentDisplayable()
        presentationPanel.removeCurrentDisplayable()
        pdfPanel.removeCurrentDisplayable()
        webPanel.removeCurrentDisplayable()
        imageGroupPanel.removeCurrentDisplayable()

        when (currentLabel) {
            PRESENTATION_LABEL -> presentationPanel.showDisplayable(null, 0)
            PDF_LABEL -> pdfPanel.showDisplayable(null, 0)
            IMAGE_GROUP_LABEL -> imageGroupPanel.showDisplayable(null, 0)
            VIDEO_LABEL -> videoPanel.showDisplayable(null, 0)
            WEB_LABEL -> webPanel.showDisplayable(null as WebDisplayable?)
        }

        when (displayable) {
            is TextDisplayable -> {
                lyricsPanel.showDisplayable(displayable as TextDisplayable?, index)
                cardPanel.show(LYRICS_LABEL)
                currentLabel = LYRICS_LABEL
            }

            is ImageDisplayable -> {
                imagePanel.showDisplayable(displayable as ImageDisplayable?)
                cardPanel.show(IMAGE_LABEL)
                currentLabel = IMAGE_LABEL
            }

            is VideoDisplayable -> {
                videoPanel.showDisplayable(displayable as MultimediaDisplayable?, index)
                cardPanel.show(VIDEO_LABEL)
                currentLabel = VIDEO_LABEL
                if (get().autoPlayVideo && this@LivePreviewPanel is LivePanel) {
                    videoPanel.play()
                }
            }

            is TimerDisplayable -> {
                timerPanel.showDisplayable(displayable as MultimediaDisplayable?)
                cardPanel.show(TIMER_LABEL)
                currentLabel = TIMER_LABEL
                if (this@LivePreviewPanel is LivePanel) {
                    timerPanel.play()
                }
            }

            is AudioDisplayable -> {
                audioPanel.showDisplayable(displayable as MultimediaDisplayable?)
                cardPanel.show(AUDIO_LABEL)
                currentLabel = AUDIO_LABEL
            }

            is PresentationDisplayable -> {
                presentationPanel.showDisplayable(displayable as PresentationDisplayable?, index)
                cardPanel.show(PRESENTATION_LABEL)
                currentLabel = PRESENTATION_LABEL
            }

            is PdfDisplayable -> {
                pdfPanel.showDisplayable(displayable as PdfDisplayable?, index)
                cardPanel.show(PDF_LABEL)
                currentLabel = PDF_LABEL
            }

            is WebDisplayable -> {
                webPanel.showDisplayable(displayable as WebDisplayable?)
                cardPanel.show(WEB_LABEL)
                currentLabel = WEB_LABEL
                webPanel.setText()
            }

            is ImageGroupDisplayable -> {
                imageGroupPanel.showDisplayable(displayable as ImageGroupDisplayable?, index)
                cardPanel.show(IMAGE_GROUP_LABEL)
                currentLabel = IMAGE_GROUP_LABEL
            }

            null -> {
    //            LOGGER.log(Level.WARNING, "BUG: Called showDisplayable(null), should probably call clear() instead.",
    //                    new RuntimeException("BUG: Called showDisplayable(null), should probably call clear() instead.")); clear();
            }

            else -> throw RuntimeException("Displayable type not implemented: " + displayable.javaClass)
        }
    }

    /**
     * Refresh the current content of this panel, if any exists.
     */
    fun refresh() {
        displayable?.let {
            setDisplayable(it, index)
        }
    }

    /**
     * Register a display canvas with this lyrics panel.
     *
     * @param canvas the canvas to register.
     */
    fun registerDisplayCanvas(canvas: DisplayCanvas?) {
        if (canvas == null) return

        for (panel in cardPanel.panels) {
            panel.registerDisplayCanvas(canvas)
        }
    }

    /**
     * Register a display window with this lyrics panel.
     *
     * @param window the window to register.
     */
    fun registerDisplayWindow(window: DisplayStage?) {
        if (window == null) return
        windows.add(window)
    }
    /**
     * Get the canvases registered to this panel.
     *
     * @return the canvases.
     */
    val canvases: Set<DisplayCanvas>
        get() = (currentPane as AbstractPanel).canvases

    /**
     * Get the windows registered to this panel.
     *
     *
     *
     * @return the windows.
     */
    fun getWindows(): Set<DisplayStage> = windows

    companion object {
        private val LOGGER = LoggerUtils.getLogger()
        private const val LYRICS_LABEL = "LYRICS"
        private const val IMAGE_LABEL = "IMAGE"
        private const val VIDEO_LABEL = "VIDEO"
        private const val TIMER_LABEL = "TIMER"
        private const val AUDIO_LABEL = "AUDIO"
        private const val PRESENTATION_LABEL = "PPT"
        private const val PDF_LABEL = "PDF"
        private const val WEB_LABEL = "WEB"
        private const val IMAGE_GROUP_LABEL = "IMAGE_GROUP"
    }
}
