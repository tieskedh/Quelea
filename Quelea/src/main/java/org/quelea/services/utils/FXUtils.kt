package org.quelea.services.utils

import javafx.beans.property.ObjectProperty
import javafx.scene.control.ListView
import tornadofx.onChange

fun <T> ListView<T>.bindSingleSelectedBidirectional(selectedItem: ObjectProperty<T>) {
    selectedItem.onChange {
        if (it != selectionModel.selectedItem) selectionModel.select(it)
    }
    selectionModel.selectedItemProperty().onChange {
        if (it != selectedItem.get()) selectedItem.set(it)
    }
}