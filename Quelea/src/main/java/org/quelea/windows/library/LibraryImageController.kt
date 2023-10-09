package org.quelea.windows.library

import javafx.scene.image.Image
import org.quelea.data.displayable.ImageDisplayable
import org.quelea.services.utils.ImageManager
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.QueleaProperties
import org.quelea.services.utils.isImage
import org.quelea.windows.main.schedule.SchedulePanel
import tornadofx.*
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.logging.Level
import kotlin.concurrent.thread
import kotlin.io.path.copyTo
import kotlin.io.path.deleteExisting


class ImageLoader(
    val width : Double,
    val height : Double,
){
    fun load(file : File) : LoadedImage = LoadedImage(
        preview = ImageManager.INSTANCE.getImage(
            file.toURI().toString(),
            width, height, false
        ),
        file = file
    )

    class LoadedImage(
        val preview : Image,
        val file: File
    ){
        val displayable by lazy { ImageDisplayable(file) }
        override fun equals(other: Any?) = other is LoadedImage && other.file == file
        override fun hashCode() = file.hashCode()
    }
}

class LibraryImageController : Controller() {
    var dir: String = QueleaProperties.get().imageDir.absolutePath
    private val imageLoader = params["imageLoader"] as ImageLoader

    val imageItems = observableListOf<ImageLoader.LoadedImage>()
    private var updateThread: Thread? = null

    fun importDraggedImageFiles(files : List<File>) {
        files.filter { it.isImage() && !it.isDirectory() }
            .forEach {f->
                try {
                    f.absoluteFile.toPath().copyTo(
                        Path.of(dir, f.name),
                        StandardCopyOption.COPY_ATTRIBUTES
                    )
                } catch (ex : IOException) {
                    LoggerUtils.getLogger().log(
                        Level.WARNING,
                        "Could not copy file into ImagePanel through system drag and drop.",
                        ex
                    )
                }
            }
        updateImages()
    }

    fun importImageFiles(
        files : List<File>,
        confirmOverride : (String) -> Boolean
    ) {
        var refresh = false
        files.forEach { f ->
            QueleaProperties.get().setLastDirectory(f.parentFile)
            try {
                val sourceFile = f.absoluteFile.toPath()
                if (File(dir, f.name).exists()) {
                    if (confirmOverride(f.name)){
                        try {
                            Path.of(dir, f.name).deleteExisting()
                            sourceFile.copyTo(
                                Path.of(dir, f.name),
                                StandardCopyOption.COPY_ATTRIBUTES
                            )
                            refresh = true
                        } catch (e: IOException) {
                            LoggerUtils.getLogger().log(
                                Level.WARNING,
                                "Could not delete or copy file back into directory.",
                                e
                            )
                        }
                    }
                } else {
                    sourceFile.copyTo(
                        Path.of(dir, f.name),
                        StandardCopyOption.COPY_ATTRIBUTES
                    )
                    refresh = true
                }
            } catch (ex: IOException) {
                LoggerUtils.getLogger().log(
                    Level.WARNING,
                    "Could not copy file into ImagePanel from FileChooser selection",
                    ex
                )
            }
        }
        if (refresh) updateImages()
    }



    fun changeDir(absoluteFile: File) {
        dir = absoluteFile.absolutePath
    }

    /**
     * Refresh the contents of this image list panel.
     */
    fun refresh() = updateImages()

    /**
     * Add the files.
     *
     *
     */
    private fun updateImages() {
        imageItems.clear()
        val files = File(dir).listFiles() ?: return
        if (updateThread?.isAlive == true) return

        updateThread = thread {
            for (file in files) {
                if (!file.isImage() || file.isDirectory()) continue
                runLater { imageItems+=imageLoader.load(file) }
            }
        }
    }

    fun delete(
        loadedImage: ImageLoader.LoadedImage,
        confirm : () -> Boolean
    ) {
        if (!confirm()) return
        loadedImage.file.delete()
        imageItems.remove(loadedImage)
    }

    private val schedulePanel by inject<SchedulePanel>()
    fun addToSchedule(image: ImageLoader.LoadedImage) {
        schedulePanel.scheduleList.add(image.displayable)
    }
}