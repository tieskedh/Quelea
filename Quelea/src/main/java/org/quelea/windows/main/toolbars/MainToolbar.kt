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
package org.quelea.windows.main.toolbars

import javafx.scene.control.*
import javafx.scene.control.PopupControl.USE_PREF_SIZE
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.scene.text.Text
import org.javafx.dialog.Dialog
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.services.utils.TOOLBAR_BUTTON_STYLE
import org.quelea.services.utils.isMac
import org.quelea.services.utils.setToolbarButtonStyle
import org.quelea.utils.dumbToggleButton
import org.quelea.windows.main.MainPanel
import org.quelea.windows.main.QueleaApp
import org.quelea.windows.main.actionhandlers.*
import tornadofx.*
import java.util.*
import javax.swing.Timer

/**
 * Quelea's main toolbar.
 *
 *
 *
 * @author Michael
 */
class MainToolbar : View() {
    private var newScheduleButton: Button? = null
    private var openScheduleButton: Button? = null
    private var saveScheduleButton: Button? = null
    private var printScheduleButton: Button? = null
    private var newSongButton: Button? = null
    private var quickInsertButton: Button? = null
    private var manageNoticesButton: Button? = null
    private lateinit var add: MenuButton
    private var loadingView: ImageView = when {
        isMac() -> getImageViewForButton("file:icons/loading.gif", useTheme = false)
        else -> getRequireResizeImageView("file:icons/loading.gif", useTheme = false)
    }

    private val dvdImageStack = StackPane(
        when(isMac()) {
            true -> getImageViewForButton("file:icons/dvd.png")
            false -> getRequireResizeImageView("file:icons/dvd.png")
        }
    )

    private lateinit var recordAudioButton: ToggleButton

    private val pb = ProgressBar(0.0)

    private var setRecordinPathWarning: Dialog? = null
    var recordButtonHandler: RecordButtonHandler = RecordButtonHandler()
        private set
    private val recordingPathTextField: TextField =  TextField().apply {
        minWidth = USE_PREF_SIZE
        maxWidth = USE_PREF_SIZE
        // Set dynamic TextField width
        textProperty().onChange {currText ->
            runLater {
                val text = Text(currText)
                text.styleClass.add("text")
                text.font = font
                val width = (text.layoutBounds.width
                        + padding.left + padding.right
                        + 2.0)
                prefWidth = width
                positionCaret(caretPosition)
            }
        }
    }
    private var recording = false
    private var openTime = 0L
    private var recTime = 0L
    private var recCount: Timer? = null

    override val root = toolbar {
        newScheduleButton = button(
            graphic = getImageViewForButton(
                when (isMac()) {
                    true -> "file:icons/filenewbig.png"
                    false -> "file:icons/filenew.png"
                }
            )
        ) {
            setToolbarButtonStyle()
            tooltip(messages["new.schedule.tooltip"])
            onAction = NewScheduleActionHandler()
        }

        openScheduleButton = button(
            graphic = getImageViewForButton(
                when (isMac()) {
                    true -> "file:icons/fileopenbig.png"
                    false -> "file:icons/fileopen.png"
                }
            )
        ) {
            setToolbarButtonStyle()
            tooltip(messages["open.schedule.tooltip"])
            onAction = OpenScheduleActionHandler()
        }

        saveScheduleButton = button(
            graphic = getImageViewForButton(
                when (isMac()) {
                    true -> "file:icons/filesavebig.png"
                    else ->
                        "file:icons/filesave.png"
                }
            )
        ) {
            setToolbarButtonStyle()
            tooltip(messages["save.schedule.tooltip"])
            onAction = SaveScheduleActionHandler(false)
        }

        printScheduleButton = button(
            graphic = getImageViewForButton(
                when (isMac()) {
                    true -> "file:icons/fileprintbig.png"
                    else -> "file:icons/fileprint.png"
                }
            )
        ) {
            setToolbarButtonStyle()
            tooltip(messages["print.schedule.tooltip"])
            onAction = PrintScheduleActionHandler()
        }

        separator()
        newSongButton = button(
            graphic = getImageViewForButton(
                when (isMac()) {
                    true -> "file:icons/newsongbig.png"
                    else -> "file:icons/newsong.png"
                }
            )
        ) {
            setToolbarButtonStyle()
            tooltip(messages["new.song.tooltip"])
            onAction = NewSongActionHandler()
        }
        separator()

        quickInsertButton = button(
            graphic = getImageViewForButton(when (isMac()) {
                true -> "file:icons/lightningbig.png"
                else -> "file:icons/lightning.png"
            })
        ) {
            setToolbarButtonStyle()
            tooltip(messages["quick.insert.text"])
            onAction = QuickInsertActionHandler
            setOnMouseEntered { add.hide() }
        }

        add = menubutton(
            graphic = ImageView(Image(
                if (get().useDarkTheme) "file:icons/add_item-light.png" else "file:icons/add_item.png"
            )).apply {
                isSmooth = true
                fitWidth = 20.0
                fitHeight = 20.0
            }
        ){
            style = TOOLBAR_BUTTON_STYLE

            setOnMouseEntered {
                QueleaApp.get().mainWindow.requestFocus()
                show()
                openTime = Date().time
            }
            // Avoid menu being closed if users click to open it
            setOnMouseClicked {
                if (Date().time - openTime < 1000) show()
            }

            item(
                name =  messages["add.presentation.tooltip"],
                graphic = getImageViewForButton(when(isMac()){
                    true -> "file:icons/powerpointbig.png"
                    false -> "file:icons/powerpoint.png"
                })
            ){
                onAction = AddPowerpointActionHandler()
            }

            item(
                name = messages["add.multimedia.tooltip"],
                graphic = getImageViewForButton(when(isMac()){
                    true -> "file:icons/multimedia.png"
                    false -> "file:icons/multimedia.png"
                })
            ){
                onAction = AddVideoActionHandler()
            }

            item(
                name = messages["add.timer.tooltip"],
                graphic = when(isMac()) {
                    true->getImageViewForButton("file:icons/timer-dark.png")
                    false -> getRequireResizeImageView("file:icons/timer-dark.png")
                }
            ){
                onAction = AddTimerActionHandler()
            }


            if (!isMac()) item(
                name = messages["add.dvd.button"],
                graphic = dvdImageStack
            ){
                onAction = AddDVDActionHandler()
            }

            item(
                name = messages["add.pdf.tooltip"],
                graphic = getImageViewForButton(when(isMac()){
                    true -> "file:icons/add_pdfbig.png"
                    false -> "file:icons/add_pdf.png"
                })
            ){
                onAction = AddPdfActionHandler()
            }

            item(
                name = messages["add.website"],
                graphic = getImageViewForButton(when(isMac()){
                    true -> "file:icons/web.png"
                    false -> "file:icons/web-small.png"
                })
            ){
                onAction = AddWebActionHandler()
            }

            item(
                name=messages["add.images.panel"],
                graphic = when(isMac()) {
                    true ->getImageViewForButton("file:icons/image.png")
                    false ->getRequireResizeImageView("file:icons/image.png")
                }
            ){
                onAction = AddImageActionHandler()
            }
        }

        separator()
        manageNoticesButton = button(
            graphic = when(isMac()) {
                true -> getImageViewForButton("file:icons/infobig.png")
                false -> getImageViewForButton("file:icons/info.png")
            }
        ){
            setToolbarButtonStyle()
            tooltip(messages["manage.notices.tooltip"])
            onAction = ShowNoticesActionHandler()
            setOnMouseEntered { add.hide() }
        }

        recordAudioButton = dumbToggleButton(
            graphic = getImageViewForButton("file:icons/record.png")
        ).apply {
            setToolbarButtonStyle()
            setOnMouseClicked {
                if (get().recordingsPath != "") {
                    when {
                        recording -> stopRecording()
                        else -> startRecording()
                    }
                } else {
                    isSelected = false
                    val setRecordingWarningBuilder = Dialog.Builder()
                        .create()
                        .setTitle(messages["recording.warning.title"])
                        .setMessage(messages["recording.warning.message"])
                        .addLabelledButton(messages["ok.button"]) {
                            setRecordinPathWarning!!.hide()
                            setRecordinPathWarning = null
                        }
                    setRecordingWarningBuilder.setWarningIcon().build()
                        .also { setRecordinPathWarning = it }
                        .showAndWait()
                }
            }

            tooltip(messages["record.audio.tooltip"])
        }
    }

    /**
     * Create the toolbar and any associated shortcuts.
     */
    init {
        // Auto-hide add menu
        FX.find<MainPanel>().root.setOnMouseEntered { add.hide() }
        QueleaApp.get().mainWindow.mainMenuBar.setOnMouseEntered { add.hide() }
    }

    fun setToggleButtonText(text: String?) {
        recordAudioButton.text = text
    }

    private fun getRequireResizeImageView(
        uri: String,
        width: Int = 24,
        height: Int = 24,
        preserveRatio: Boolean = false,
        smooth: Boolean = true,
        useTheme : Boolean = true
    ): ImageView {
        val iv = ImageView(
            Image(
                if (useTheme && get().useDarkTheme) uri.replace(".png", "-light.png") else uri,
                width.toDouble(),
                height.toDouble(),
                preserveRatio,
                smooth
            )
        )
        iv.isSmooth = true
        iv.fitWidth = 24.0
        iv.fitHeight = 24.0
        return iv
    }


    private fun getImageViewForButton(uri: String, useTheme: Boolean = true): ImageView {
        val iv = ImageView(Image(if (useTheme && get().useDarkTheme) uri.replace(".png", "-light.png") else uri))
        iv.isSmooth = true
        iv.fitWidth = 24.0
        iv.fitHeight = 24.0
        return iv
    }

    /**
     * Set if the DVD is loading.
     *
     *
     *
     * @param loading true if it's loading, false otherwise.
     */
    fun setDVDLoading(loading: Boolean) {
        if (loading && loadingView !in dvdImageStack.children) {
            dvdImageStack.children.add(loadingView)
        } else if (!loading) {
            dvdImageStack.children.remove(loadingView)
        }
    }

    fun startRecording() {
        recordAudioButton.isSelected = true
        recording = true
        //        getItems().add(pb);
        root.items.add(recordingPathTextField)
        recordAudioButton.text = "Recording..."
        recordAudioButton.isSelected = true
        recordButtonHandler = RecordButtonHandler()
        recordButtonHandler.passVariables("rec", pb, recordingPathTextField, recordAudioButton)
        val interval = 1000 // 1000 ms
        recCount = Timer(interval) {
            recTime += interval.toLong()
            recordAudioButton.setTime(recTime)
        }
        recCount!!.start()
    }

    fun stopRecording() {
        recordButtonHandler.passVariables("stop", pb, recordingPathTextField, recordAudioButton)
        recordAudioButton.isSelected = false
        recording = false
        recCount!!.stop()
        //        getItems().remove(pb);
        root.items.remove(recordingPathTextField)
        recordAudioButton.text = ""
        recordAudioButton.isSelected = false
        recTime = 0
    }

    /**
     * Method to set elapsed time on ToggleButton
     *
     * @receiver                ToggleButton to set time
     * @param elapsedTimeMillis Time elapsed recording last was started
     */
    private fun ToggleButton.setTime(elapsedTimeMillis: Long) {
        val elapsedTimeSec = elapsedTimeMillis / 1000f
        val hours = elapsedTimeSec.toInt() / 3600
        val minutes = (elapsedTimeSec % 3600).toInt() / 60
        val seconds = elapsedTimeSec.toInt() % 60
        val time = "%02d:%02d:%02d".format(hours, minutes, seconds)
        runLater { text = time }
    }
}
