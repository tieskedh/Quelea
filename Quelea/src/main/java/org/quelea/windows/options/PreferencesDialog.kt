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
 * MERCHANTABILITYs or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.windows.options

import com.dlsc.formsfx.model.structure.Field
import com.dlsc.preferencesfx.PreferencesFx
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl
import com.dlsc.preferencesfx.model.Setting
import javafx.application.Platform
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Bounds
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.WindowEvent
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.services.utils.getBound
import org.quelea.windows.main.DisplayStage
import org.quelea.windows.main.QueleaApp
import org.quelea.windows.main.schedule.SchedulePanel
import org.quelea.windows.multimedia.VLCWindow
import org.quelea.windows.options.customprefs.ColorPickerPreference
import tornadofx.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.set

class PreferencesDialog(parent: Class<*>?, hasVLC: Boolean) : Stage() {
    private val preferencesFx: PreferencesFx
    private val mainPane: BorderPane
    private val okButton: Button
    private val generalPanel: OptionsGeneralPanel
    val displaySetupPanel: OptionsDisplaySetupPanel
    private val noticePanel: OptionsNoticePanel
    private val presentationPanel: OptionsPresentationPanel
    private val biblePanel: OptionsBiblePanel
    private val stageViewPanel: OptionsStageViewPanel
    @JvmField
    val optionsServerSettingsPanel: OptionsServerSettingsPanel
    private val recordingPanel: OptionsRecordingPanel
    private val importExportPanel: OptionsImportExportPanel
    private val bindings = HashMap<Field<*>, ObservableValue<Boolean>>()
    private var previousLinkPreviewLiveDividers = false

    /**
     * Create a new preference dialog.
     *
     * @author Arvid
     */
    init {
        title = LabelGrabber.INSTANCE.getLabel("options.title")
        initModality(Modality.APPLICATION_MODAL)
        initOwner(QueleaApp.get().mainWindow)
        icons.add(Image("file:icons/options.png", 16.0, 16.0, false, true))
        mainPane = BorderPane()
        generalPanel = OptionsGeneralPanel(bindings)
        displaySetupPanel = OptionsDisplaySetupPanel(bindings)
        stageViewPanel = OptionsStageViewPanel(bindings)
        noticePanel = OptionsNoticePanel(bindings)
        presentationPanel = OptionsPresentationPanel(bindings)
        biblePanel = OptionsBiblePanel(bindings)
        optionsServerSettingsPanel = OptionsServerSettingsPanel(bindings)
        recordingPanel = OptionsRecordingPanel(bindings, hasVLC)
        importExportPanel = OptionsImportExportPanel(bindings)
        preferencesFx = PreferencesFx.of(
            PreferenceStorageHandler(parent),
            generalPanel.getGeneralTab(),
            displaySetupPanel.getDisplaySetupTab(),
            stageViewPanel.stageViewTab,
            noticePanel.noticesTab,
            presentationPanel.getPresentationsTab(),
            biblePanel.getBiblesTab(),
            optionsServerSettingsPanel.serverTab,
            recordingPanel.getRecordingsTab(),
            importExportPanel.importExportTab
        )
        okButton = Button(LabelGrabber.INSTANCE.getLabel("ok.button"), ImageView(Image("file:icons/tick.png")))
        BorderPane.setMargin(okButton, Insets(5.0))
        okButton.onAction = EventHandler { t: ActionEvent? ->
            preferencesFx.saveSettings()
            if (displaySetupPanel.isDisplayChange) {
                updatePos()
            }
            displaySetupPanel.isDisplayChange = false
            FX.find(SchedulePanel::class.java).themeNode.refresh()

            // If the option has changed for linking the preview and live dividers, update the current UI to respect
            // the options now
            if (previousLinkPreviewLiveDividers != get().linkPreviewAndLiveDividers) {
                // Get both the preview and live split panes
                val previewSplit = QueleaApp.get().mainWindow.mainPanel.previewPanel.lyricsPanel.splitPane
                val liveSplit = QueleaApp.get().mainWindow.mainPanel.livePanel.lyricsPanel.splitPane
                if (get().linkPreviewAndLiveDividers) {
                    // If the option is now to have them linked, need to move the preview divider to line up with the
                    // live one, before linking them together
                    previewSplit.setDividerPositions(liveSplit.getDividerPositions()[0])
                    previewSplit.dividers[0].positionProperty()
                        .bindBidirectional(liveSplit.dividers[0].positionProperty())
                } else {
                    // Option is now to have them unlinked, so do that
                    previewSplit.dividers[0].positionProperty()
                        .unbindBidirectional(liveSplit.dividers[0].positionProperty())
                }
            }
            hide()
        }
        BorderPane.setAlignment(okButton, Pos.CENTER)
        mainPane.bottom = okButton
        mainPane.minWidth = 1005.0
        mainPane.minHeight = 600.0
        mainPane.center = preferencesFx.view.center
        val scene = Scene(mainPane)
        if (get().useDarkTheme) {
            scene.stylesheets.add("org/modena_dark.css")
        }
        setScene(scene)
        getScene().window.addEventFilter(WindowEvent.WINDOW_SHOWN) { e: WindowEvent? -> callBeforeShowing() }
        getScene().window.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST) { e: WindowEvent? -> callBeforeHiding() }
        bindings.forEach { (field: Field<*>, booleanProperty: ObservableValue<Boolean>) -> bind(field, booleanProperty) }
    }

    private fun callBeforeShowing() {
        displaySetupPanel.isDisplayChange = false

        // Before showing the preferences dialog we save the current state of the link preview and live dividers so
        // that we can tell if it has been changed and whether we need to action anything
        previousLinkPreviewLiveDividers = get().linkPreviewAndLiveDividers
    }

    private fun bind(field: Field<*>, booleanProperty: ObservableValue<out Boolean?>) {
        (field.getRenderer() as SimpleControl<*, *>).node.disableProperty().bind(booleanProperty)
    }

    fun updatePos() {
        var appWindow = QueleaApp.get().projectionWindow
        var stageWindow = QueleaApp.get().stageWindow
        if (appWindow == null) {
            appWindow = DisplayStage(get().projectorCoords, false)
        }
        val fiLyricWindow = appWindow //Fudge for AIC
        val monitors = Screen.getScreens()
        Platform.runLater {
            val projectorScreen = get().projectorScreen
            val bounds: Bounds
            bounds = if (get().isProjectorModeCoords) {
                get().projectorCoords
            } else {
                monitors[if (projectorScreen < 0 || projectorScreen >= monitors.size) 0 else projectorScreen]
                    .bounds.getBound()
            }
            fiLyricWindow.setAreaImmediate(bounds)
            if (!QueleaApp.get().mainWindow.mainPanel.livePanel.hide.isSelected) {
                fiLyricWindow.show()
            }

            // non-custom positioned windows are fullscreen
            if (!get().isProjectorModeCoords) {
                if (get().projectorScreen == -1) {
                    fiLyricWindow.hide()
                    VLCWindow.INSTANCE.refreshPosition()
                } else {
                    fiLyricWindow.setFullScreenAlwaysOnTop(true)
                }
            } else {
                fiLyricWindow.setFullScreenAlwaysOnTop(false)
            }
        }
        if (stageWindow == null) {
            stageWindow = DisplayStage(get().stageCoords, true)
        }
        val fiStageWindow = stageWindow //Fudge for AIC
        Platform.runLater {
            val stageScreen = get().stageScreen
            val bounds: Bounds
            bounds = if (get().isStageModeCoords) {
                get().stageCoords
            } else {
                monitors[if (stageScreen < 0 || stageScreen >= monitors.size) 0 else stageScreen]
                    .bounds.getBound()
            }
            fiStageWindow.setAreaImmediate(bounds)
            if (!QueleaApp.get().mainWindow.mainPanel.livePanel.hide.isSelected) {
                fiStageWindow.show()
            }
            if (get().stageScreen == -1 && !get().isStageModeCoords) fiStageWindow.hide()
        }
    }

    private fun callBeforeHiding() {
        preferencesFx.discardChanges()
    }

    companion object {
        @JvmStatic
        fun getColorPicker(label: String?, color: Color?): Setting<*, *> {
            val property: StringProperty = SimpleStringProperty(
                get().getStr(
                    color!!
                )
            )
            val field = Field.ofStringType(property).render(
                ColorPickerPreference(color)
            )
            return Setting.of(label, field, property)
        }

        @JvmStatic
        @JvmSuppressWildcards
        fun getPositionSelector(
            label: String?,
            horizontal: Boolean,
            selectedValue: String,
            booleanBind: BooleanProperty?,
            bindings: HashMap<Field<*>, ObservableValue<Boolean>>
        ): Setting<*, *> {
            val setting: Setting<*, *>
            setting = if (horizontal) Setting.of(
                label, FXCollections.observableArrayList(
                    LabelGrabber.INSTANCE.getLabel("left"),
                    LabelGrabber.INSTANCE.getLabel("right")
                ),
                SimpleObjectProperty(
                    LabelGrabber.INSTANCE.getLabel(
                        selectedValue.lowercase(
                            Locale.getDefault()
                        )
                    )
                )
            ) else Setting.of(
                label, FXCollections.observableArrayList(
                    LabelGrabber.INSTANCE.getLabel("top.text.position"),
                    LabelGrabber.INSTANCE.getLabel("bottom.text.position")
                ),
                SimpleObjectProperty(
                    LabelGrabber.INSTANCE.getLabel(
                        selectedValue.lowercase(
                            Locale.getDefault()
                        )
                    )
                )
            )
            if (booleanBind != null) bindings[setting.element as Field<*>] = booleanBind.not()
            return setting
        }
    }
}
