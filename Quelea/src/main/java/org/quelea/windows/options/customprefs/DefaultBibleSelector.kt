package org.quelea.windows.options.customprefs

import com.dlsc.formsfx.model.structure.SingleSelectionField
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.stage.FileChooser
import org.javafx.dialog.Dialog
import org.quelea.data.bible.Bible.Companion.parseBible
import org.quelea.data.bible.BibleManager
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.FileFilters
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.QueleaProperties
import org.quelea.services.utils.copyFileWithFiltersTo
import org.quelea.windows.main.QueleaApp
import tornadofx.*
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean
import java.util.logging.Level

/**
 * - The fieldLabel is the container that displays the label property of
 * the field.
 * - The comboBox is the container that displays the values in the
 * ComboBox.
 * - The readOnlyLabel is used to show the current selection in read only.
 * - The node is a StackPane to hold the field and read only label.
 */
class DefaultBibleSelector : SimpleControl<SingleSelectionField<String>, StackPane>() {
    private lateinit var comboBox: ComboBox<String>

    override fun initializeParts() {
        super.initializeParts()
        node = StackPane().apply {
            maxWidth = Double.MAX_VALUE
            styleClass.add("simple-select-control")
        }
        comboBox = ComboBox(field.items as ObservableList<String>).apply {
            selectionModel.select(field.items.indexOf(field.selection))
        }
    }


    override fun layoutParts() {
        node.apply {
            alignment = Pos.CENTER_LEFT

            hbox {
                comboBox.attachTo(this) {
                    hboxConstraints { hGrow = Priority.ALWAYS }
                    visibleRowCount = 4
                    maxWidth = Double.MAX_VALUE
                    minWidth = 100.0
                }

                addBibleButton()
                deleteBibleButton()
            }
        }
    }

    override fun setupBindings() {
        super.setupBindings()
        comboBox.visibleProperty().bind(field.editableProperty())
    }

    override fun setupValueChangedListeners() {
        super.setupValueChangedListeners()
        field.itemsProperty().onChange { _: ObservableList<String>? ->
            comboBox.setItems(field.items as ObservableList<String>)
        }
        field.selectionProperty().onChange {
            if (it != null) {
                comboBox.selectionModel.select(field.items.indexOf(it))
            } else {
                comboBox.selectionModel.clearSelection()
            }
        }

        field.errorMessagesProperty().onChange { _: ObservableList<String>? ->
            toggleTooltip(comboBox)
        }
        field.tooltipProperty().onChange { toggleTooltip(comboBox) }
        comboBox.focusedProperty().onChange { toggleTooltip(comboBox) }
    }

    /**
     * {@inheritDoc}
     */
    override fun setupEventHandlers() {
        comboBox.valueProperty().onChange {
            field.select(comboBox.selectionModel.selectedIndex)
        }
    }

    private fun EventTarget.addBibleButton() = button(
        LabelGrabber.INSTANCE.getLabel("add.bible.label"),
        ImageView(Image("file:icons/add.png"))
    ) {
        setOnAction {
            val chooser = FileChooser()
            QueleaProperties.get().lastDirectory?.let { lastDir ->
                chooser.initialDirectory = lastDir
            }

            chooser.extensionFilters.add(FileFilters.XML_BIBLE)
            val file = chooser.showOpenDialog(QueleaApp.get().mainWindow) ?: return@setOnAction

            QueleaProperties.get().setLastDirectory(file.parentFile)
            if (parseBible(file) == null) {
                LOGGER.log(Level.WARNING, "Tried to add corrupt bible: ", file.absolutePath)
                Dialog.showError(
                    LabelGrabber.INSTANCE.getLabel("bible.load.error.title"),
                    LabelGrabber.INSTANCE.getLabel("bible.load.error.question")
                )
            } else {
                try {
                    file.copyFileWithFiltersTo(File(QueleaProperties.get().bibleDir, file.name))
                    BibleManager.get().refreshAndLoad()
                } catch (ex: IOException) {
                    LOGGER.log(Level.WARNING, "Error copying bible file", ex)
                    Dialog.showError(
                        LabelGrabber.INSTANCE.getLabel("bible.copy.error.heading"),
                        LabelGrabber.INSTANCE.getLabel("bible.copy.error.text")
                    )
                }
            }
        }
    }

    private fun EventTarget.deleteBibleButton() = button(
        LabelGrabber.INSTANCE.getLabel("delete.bible.label"),
        ImageView(Image("file:icons/cross.png"))
    ) {
        setOnAction {
            val bible = BibleManager.get().getBibleFromName(comboBox.selectionModel.selectedItem)
            val biblePath = bible?.filePath ?: return@setOnAction

            val yes = AtomicBoolean()

            Dialog.buildConfirmation(
                LabelGrabber.INSTANCE.getLabel("delete.bible.label"),
                LabelGrabber.INSTANCE.getLabel("delete.bible.confirmation").replace("$1", bible.bibleName)
            ).addYesButton { yes.set(true) }
                .addNoButton { }
                .build().showAndWait()
            if (yes.get()) {
                try {
                    Files.delete(Paths.get(biblePath))
                    BibleManager.get().refreshAndLoad()
                } catch (ex: IOException) {
                    LOGGER.log(Level.WARNING, "Error deleting bible file", ex)
                    Dialog.showError(
                        LabelGrabber.INSTANCE.getLabel("bible.delete.error.heading"),
                        LabelGrabber.INSTANCE.getLabel("bible.delete.error.text")
                    )
                }
            }
        }
    }

    companion object {
        private val LOGGER = LoggerUtils.getLogger()
    }
}