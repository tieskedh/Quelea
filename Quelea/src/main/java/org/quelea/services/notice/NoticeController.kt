package org.quelea.services.notice

import javafx.beans.binding.BooleanBinding
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.utils.readOnly
import tornadofx.*
import java.io.File
import kotlin.concurrent.thread

interface NoticeContainer{
    val notices : List<Notice>
    fun addNotice(notice: Notice)
    fun removeNotice(notice: Notice)
}

class NoticeController : Controller() {
    val selectedNoticeProp = objectProperty<Notice>()
    val noNoticeSelected: BooleanBinding = selectedNoticeProp.isNull

    private val _noticesProp = observableListOf<Notice>()
    val noticesProp = _noticesProp.readOnly()

    val selectedTemplateProp = objectProperty<Notice>()
    val templateList = observableListOf<Notice>()

    private val noticeContainers = observableListOf<NoticeContainer>()

    init {
        loadNoticesFromFile()
    }

    fun newNotice(
        build : () -> Notice?,
    ) {
        build()?.let { notice ->
            _noticesProp += notice
            noticeContainers.forEach { it.addNotice(notice) }
        }
    }

    fun editSelectedNotice(
        edit : (Notice) -> Notice?,
    ) {
        selectedNoticeProp.value?.let { notice ->
            edit(notice)?.let { editedNotice ->
                _noticesProp += editedNotice
                noticeContainers.forEach { it.addNotice(editedNotice) }
            }
        }
    }

    fun replaceSelectedTemplate(notice: Notice) {
        val index = templateList.indexOf(selectedTemplateProp.value)
        templateList[index] = notice
    }

    fun removeSelectedNotice() {
        selectedNoticeProp.value?.let { notice->
            _noticesProp -= notice
            noticeContainers.forEach {
                it.removeNotice(notice)
            }
        }
    }


    fun loadFromSelectedTemplate(
        createInstance : (existing: Notice) -> Notice?,
    ) {
        selectedTemplateProp.value?.let { template ->
            template.resetTimes()
            createInstance(template)?.let { notice ->
                _noticesProp += notice
                noticeContainers.forEach { it.addNotice(notice) }
            }

            selectedNoticeProp.value = null
        }
    }

    /**
     * Register a [NoticeContainer] to be updated using this controller.
     *
     *
     * @param noticeContainer the container to register.
     */
    fun registerNoticeContainer(noticeContainer: NoticeContainer) {
        noticeContainers += noticeContainer
    }

    fun noticesUpdated(
        onlyRetain : (Set<Notice>) -> Unit
    ) {
        val selected = selectedNoticeProp.value
        val noticesSet = noticeContainers.flatMapTo(mutableSetOf()){
            it.notices
        }
        println("NOTICE UPDATED: $noticesSet, selected: $selected in notices: ${selected in noticesSet}")
        _noticesProp.setAll(noticesSet)
        onlyRetain(noticesSet)
        selectedNoticeProp.set(selected)
    }


    private var noticeTemplatesUpdateThread: Thread? = null

    private fun loadNoticesFromFile() {
        if (noticeTemplatesUpdateThread?.isAlive != true) {
            noticeTemplatesUpdateThread = thread {
                File(get().noticeDir.absolutePath).listFiles()?.forEach { file ->
                    runLater {
                        NoticeFileHandler.noticeFromFile(file)?.let {
                            this.templateList.add(it)
                        }
                    }
                }
            }
        }
    }
}