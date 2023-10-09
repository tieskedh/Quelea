package org.quelea.windows.library

import org.quelea.data.displayable.TimerDisplayable
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.QueleaProperties
import org.quelea.services.utils.isTimer
import org.quelea.windows.main.schedule.SchedulePanel
import org.quelea.windows.timer.TimerIO
import tornadofx.*
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.logging.Level
import kotlin.concurrent.thread
import kotlin.io.path.copyTo
import kotlin.io.path.deleteExisting

class LibraryTimerController : Controller() {
    var dir: String = params.getValue("dir") as String

    val items = observableListOf<TimerDisplayable>()
    private var updateThread: Thread? = null

    val selectedTimerProperty = objectProperty<TimerDisplayable>()
    val selectedItem by selectedTimerProperty


    fun filesDraggedToTimerList(files: List<File>) {
        files.filter { it.isTimer() && !it.isDirectory() }
            .forEach { f ->
                try {
                    f.absoluteFile.toPath().copyTo(
                        Path.of(dir, f.name),
                        StandardCopyOption.COPY_ATTRIBUTES
                    )
                } catch (ex: IOException) {
                    LoggerUtils.getLogger().log(
                        Level.WARNING,
                        "Could not copy file into TimerPanel through system drag and drop.",
                        ex
                    )
                }
                updateTimers()
            }
    }

    /**
     * Add the files.
     *
     *
     */
    private fun updateTimers() {
        items.clear()
        val files = File(dir).listFiles() ?: return
        if (updateThread != null && updateThread!!.isAlive) return

        updateThread = thread {
            files.forEach {
                runLater {
                    val timer = TimerIO.timerFromFile(it)
                    if (timer != null) items.add(timer)
                }
            }
        }
    }


    fun import(
        files :List<File>,
        confirmOverride : (fileName : String) -> Boolean
    ) {
        var refresh = false
        files.forEach { f->
            QueleaProperties.get().setLastDirectory(f.parentFile)
            try {
                val sourceFile = f.absoluteFile.toPath()
                if (!File(dir, f.name).exists()) {
                    sourceFile.copyTo(
                        Path.of(dir, f.name),
                        StandardCopyOption.COPY_ATTRIBUTES
                    )
                    refresh = true
                    return@forEach
                }
                if (confirmOverride(f.name)) {
                    try {
                        Paths.get(dir, f.name).deleteExisting()
                        sourceFile.copyTo(
                            Paths.get(dir, f.name),
                            StandardCopyOption.COPY_ATTRIBUTES
                        )
                        refresh = true
                    } catch (e: IOException) {
                        LOGGER.log(
                            Level.WARNING,
                            "Could not delete or copy file back into directory.",
                            e
                        )
                    }
                }
            } catch (e : IOException) {
                LOGGER.log(Level.WARNING, "Could not copy file into TimerPanel from FileChooser selection", e)
            }
        }
        if (refresh) refreshTimers()
    }


    fun addSelectedToSchedule() {
        FX.find<SchedulePanel>().scheduleList.add(selectedItem)
    }


    /**
     * Refresh the contents of this video list panel.
     */
    fun refreshTimers() = updateTimers()

    fun changeDir(absoluteFile: File) {
        dir = absoluteFile.absolutePath
    }

    companion object {
        private val LOGGER = LoggerUtils.getLogger()
    }
}
