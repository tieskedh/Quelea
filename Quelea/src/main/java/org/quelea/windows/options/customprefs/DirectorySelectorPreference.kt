package org.quelea.windows.options.customprefs

import com.dlsc.formsfx.model.structure.StringField
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableValue
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.stage.DirectoryChooser
import tornadofx.*
import java.io.File

class DirectorySelectorPreference(private val buttonText: String, private val initialDirectory: File?) :
    SimpleControl<StringField, StackPane>() {
    /**
     * - The fieldLabel is the container that displays the label property of
     * the field.
     * - The editableField allows users to modify the field's value.
     */
    private lateinit var editableField: TextField
    private lateinit var directoryChooserButton : Button
    private lateinit var hBox : HBox

    override fun initializeParts() {
        super.initializeParts()
        node = StackPane().apply {
            styleClass.add("simple-text-control")
        }
        val directoryChooser = DirectoryChooser()
        if (initialDirectory != null) {
            directoryChooser.initialDirectory = initialDirectory
        }

        hBox = HBox().apply {
            editableField = textfield(field.value){
                promptText = field.placeholder
            }
            directoryChooserButton = button(buttonText){
                setOnAction {
                    directoryChooser.showDialog(node.scene.window)?.also { dir ->
                        editableField.text = dir.absolutePath
                    }
                }
            }
        }
    }

    override fun layoutParts() {
        HBox.setHgrow(editableField, Priority.ALWAYS)
        node.alignment = Pos.CENTER_LEFT
        node.children.addAll(hBox)
    }

    override fun setupBindings() {
        super.setupBindings()
        editableField.visibleProperty().bind(
            field.editableProperty().and(!field.multilineProperty())
        )
        editableField.textProperty().bindBidirectional(field.userInputProperty())
        editableField.promptTextProperty().bind(field.placeholderProperty())
        editableField.managedProperty().bind(editableField.visibleProperty())
    }

    override fun setupValueChangedListeners() {
        super.setupValueChangedListeners()
        editableField.focusedProperty().onChange { toggleTooltip(editableField) }
    }
}
