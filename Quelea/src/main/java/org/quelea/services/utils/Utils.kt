/*
 * This file is part of Quelea, free projection software for churches.
 *
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
@file:JvmName("Utils")

/**
 * General utility class containing a bunch of static methods.
 * <p/>
 * @author Michael, Ben
 */

package org.quelea.services.utils

import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.geometry.Rectangle2D
import javafx.scene.Node
import javafx.scene.control.ToggleButton
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import javafx.stage.Screen
import javafx.stage.Stage
import org.apache.commons.text.StringEscapeUtils
import org.javafx.dialog.Dialog
import org.jcodec.api.awt.AWTFrameGrab
import org.mozilla.universalchardet.UniversalDetector
import org.quelea.data.ThemeDTO
import org.quelea.data.db.SongManager
import org.quelea.data.displayable.SongDisplayable
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.utils.javaTrim
import org.quelea.windows.main.QueleaApp
import org.quelea.windows.main.StatusController
import tornadofx.FX
import tornadofx.onChange
import tornadofx.runLater
import java.awt.*
import java.awt.font.TextAttribute
import java.awt.image.BufferedImage
import java.io.*
import java.nio.charset.Charset
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Level
import java.util.zip.ZipFile
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.io.path.*
import kotlin.math.abs
import org.w3c.dom.Node as DomNode

private val LOGGER = LoggerUtils.getLogger()
const val TOOLBAR_BUTTON_STYLE =
    "-fx-background-insets: 0;-fx-background-color: rgba(0, 0, 0, 0);-fx-padding:3,6,3,6;-fx-text-fill: grey;"
const val HOVER_TOOLBAR_BUTTON_STYLE = "-fx-background-insets: 0;-fx-padding:3,6,3,6;-fx-text-fill: grey;"

/**
 * Beep!
 */
fun beep() = Toolkit.getDefaultToolkit().beep()

fun DomNode.getChangedFile(fileChanges: Map<String, String>) =
    File(textContent).getChangedFile(fileChanges)

fun File.getChangedFile(fileChanges: Map<String, String>): File {
    val changedFile = fileChanges[absolutePath]
    if (!exists() && changedFile != null) {
        LOGGER.info { "Changing $absolutePath to $changedFile" }
        return File(changedFile)
    }
    return this
}

@Deprecated("use File.getChangedFile instead",
    ReplaceWith("File(filePath).getChangedFile(fileChanges)", "java.io.File")
)
fun getChangedFile(filePath: String, fileChanges: Map<String, String>) =
    File(filePath).getChangedFile(fileChanges)

fun toRelativeStorePath(f: File) = f.absolutePath
    .split(Regex.fromLiteral(System.getProperty("file.separator")))
    .dropLastWhile { it.isEmpty() }
    .drop(1)
    .joinToString(separator = System.getProperty("file.separator"))

/**
 * Get the debug log file, useful for debugging if something goes wrong (the
 * log is printed out to this location.)
 * <p>
 * @return the debug log file.
 */
fun getDebugLog() = File(get().queleaUserHome, "quelea-debuglog.txt")

/**
 * Check if a given file is directly in a given directory. (Doesn't include
 * subfolders.)
 *
 *
 * @param dir the directory to check.
 * @param file the file to check.
 * @return true if the given file is in the given directory, false
 * otherwise.
 */
//KOTLINIZE-REMARK: this doesn't use the dir-parameter!!!
//KOTLINIZE-REMARK: should this return false on error or throw like it does now?
fun isInDir(dir: File?, file: File) =
    file in get().imageDir.listFiles()


/**
 * Set the button style for any buttons that are to be placed on a toolbar.
 * Change the padding and remove the default border.
 * <p/>
 * @receiver the button to style.
 */
fun Node.setToolbarButtonStyle() {
    style = TOOLBAR_BUTTON_STYLE
    if (this is ToggleButton) selectedProperty().onChange {
        style = if (it) HOVER_TOOLBAR_BUTTON_STYLE else TOOLBAR_BUTTON_STYLE
    }

    setOnMouseEntered { style = HOVER_TOOLBAR_BUTTON_STYLE }
    setOnMouseExited {
        if (!(this is ToggleButton && isSelected))
            style = HOVER_TOOLBAR_BUTTON_STYLE
    }
}

/**
 * Sleep ignoring the exception.
 *
 *
 * @param millis milliseconds to sleep.
 */
fun sleep(millis: Long) = try {
    Thread.sleep(millis)
} catch (ex: InterruptedException) {
    //Nothing
}

fun SceneInfo.isOffScreen() = Screen.getScreens().none {
    it.bounds.intersects(bounds)
}

fun incrementExtension(name: String, ext: String): String {
    val withoutExt = name.dropLast(4).javaTrim()
    val regex = Regex(""".*\((\d+)\)$""")
    return regex.find(withoutExt)?.destructured?.let { (it) ->
        val nextNum = it.toInt() + 1
        return withoutExt.dropLast(it.length) + "($nextNum).$ext"
    } ?: "$withoutExt(2).$ext"
}

/**
 * Run something on the JavaFX platform thread and wait for it to complete.
 * <p/>
 * @param exec the lambda to run.
 */
@JvmSynthetic //hide from java, since they have runnable
fun fxRunAndWait(exec: () -> Unit) = fxRunAndWait(Runnable(exec))

/**
 * Run something on the JavaFX platform thread and wait for it to complete.
 * <p/>
 * @param runnable the runnable to run.
 */
fun fxRunAndWait(runnable: Runnable) = try {
    if (Platform.isFxApplicationThread()) {
        try {
            runnable.run()
        } catch (e: Exception) {
            throw ExecutionException(e)
        }
    } else {
        val lock: Lock = ReentrantLock()
        val condition = lock.newCondition()
        lock.withLock {
            runLater {
                lock.withLock {
                    try {
                        runnable.run()
                    } finally {
                        condition.signal()
                    }
                }
            }
            condition.await()
        }
    }
} catch (ex: Exception) {
    LOGGER.log(Level.SEVERE, "Execution error", ex)
}

/**
 * Add the Quelea icon(s) to a stage.
 * <p/>
 * @param stage the stage to add the icons to.
 */
@JvmName("addIconsToStage")
fun Stage.addIcons() {
//    icons.add(Image("file:icons/logo64.png"));
//    icons.add(Image("file:icons/logo48.png"));
    icons.add(Image("file:icons/logo32.png"))
//    icons.add(Image("file:icons/logo16.png"));
}

/**
 * Split the options from a single string into an array recognised by VLC.
 *
 * @param options the input options.
 * @return the options split as an array.
 */
fun splitVLCOpts(options: String): Array<String> {
    val parts = options.split(Regex(" \\:"))
        .dropLastWhile { it.isEmpty() }

    return parts.map { vlcPart ->
        val trimmed = vlcPart.javaTrim()
        if (trimmed.startsWith(':')) trimmed else ":$trimmed"
    }.toTypedArray()
}

/**
 * Get the string to pass VLC from the given video file. In many cases this
 * is just the path, in the case of vlcarg files it is the contents of the
 * file to pass VLC.
 *
 * @param file the file to grab the VLC path from.
 * @return the VLC path.
 */
@JvmName("getVLCStringFromFile")
fun extractVLCString(file: File): String {
    val path = file.absolutePath
    val lastPart = path.split(Regex("\\."))
        .last { it.isNotEmpty() }

    return if (lastPart.javaTrim().equals("vlcarg", ignoreCase = true)) {
        try {
            file.readText(Charsets.UTF_8)
        } catch (ex: IOException) {
            LOGGER.log(Level.WARNING, "Couldn't get VLC string from file", ex)
            path
        }
    } else path
}

/**
 * Converts an AWT rectangle to a JavaFX bounds object.
 * <p/>
 * @param rect the rectangle to convert.
 * @return the equivalent bounds.
 */
@JvmName("getBoundsFromRect")
fun Rectangle.getBound(): Bounds = BoundingBox(
    x.toDouble(),
    y.toDouble(),
    width.toDouble(),
    height.toDouble()
)

/**
 * Converts a JavaFX Rectangle2D to a JavaFX bounds object.
 * <p/>
 * @param rect the Rectangle2D to convert.
 * @return the equivalent bounds.
 * <p/>
 */
@JvmName("getBoundsFromRect2D")
fun Rectangle2D.getBound() = BoundingBox(minX, minY, width, height)

/**
 * Determine if we're running in a 64 bit JVM.
 * <p/>
 * @return true if it's a 64 bit JVM, false if it's 32 bit (or something
 * else.)
 */
fun is64Bit() = "64" in System.getProperty("os.arch") //Rudimentary...

/**
 * Determine if we're running on a mac.
 *
 *
 * @return true if we're running on a mac, false otherwise.
 */
fun isMac() = "mac" in System.getProperty("os.name").lowercase()

/**
 * Determine if we're running on Linux.
 *
 *
 * @return true if we're running on Linux, false otherwise.
 */
fun isLinux() = "linux" in System.getProperty("os.name").lowercase()

/**
 * Determine if we're running on Windows.
 *
 *
 * @return true if we're running on Windows, false otherwise.
 */
fun isWindows() = "windows" in System.getProperty("os.name").lowercase()

/**
 * Get a file name without its extension.
 *
 *
 * @param nameWithExtension the file name with the extension.
 * @return the file name without the extension.
 */
fun getFileNameWithoutExtension(nameWithExtension: String): String {
    if ("." !in nameWithExtension) return nameWithExtension

    return nameWithExtension
        .split("\\.".toRegex())
        .dropLastWhile { it.isEmpty() }
        .dropLast(1)
        .joinToString(".")
}

/**
 * Update a song in the background.
 *
 * @receiver the song to update.
 * @param showError show error-dialog if there's a problem while updating the song
 * @param silent update the song without showing a bar on the status panel.
 */
@JvmName("updateSongInBackground")
fun SongDisplayable.updateInBackground(showError: Boolean = true, silent: Boolean = false) {
    if (!checkDBUpdate() || isQuickInsert) return

    fun updateRunner() {
        val result = SongManager.get().updateSong(this)
        if (result && checkDBUpdate()) {
            val songs = QueleaApp.get().mainWindow.mainPanel.libraryPanel.librarySongPanel.songList.listView.items
            val replaceIdx = songs.indexOfFirst { it.id == id }

            if (replaceIdx != -1) runLater {
                songs.removeAt(replaceIdx)
                songs.add(replaceIdx, this)
            }
        }
        if (!result && showError) runLater {
            Dialog.showError(
                LabelGrabber.INSTANCE.getLabel("error.text"),
                LabelGrabber.INSTANCE.getLabel("error.udpating.song.text")
            )
        }
    }

    if (silent) thread(block = ::updateRunner)
    else fxRunAndWait {
        val statusPanel = FX.find<StatusController>().addPanel(
            LabelGrabber.INSTANCE.getLabel("updating.db")
        )
        statusPanel.removeCancelButton()
        thread {
            updateRunner()
            runLater(statusPanel::done)
        }
    }
}

/**
 * Wrap a runnable as one having a low priority.
 * <p/>
 * @receiver the runnable to wrap.
 * @return a runnable having a low priority.
 */
fun Runnable.wrapAsLowPriority() = Runnable {
    val t = Thread.currentThread()
    val oldPriority = t.priority
    t.setPriority(Thread.MIN_PRIORITY)
    Thread.yield()
    this@wrapAsLowPriority.run()
    t.setPriority(oldPriority)
}

/**
 * Get a font identical to the one given apart from in size.
 *
 *
 * @receiver the original font.
 * @param size the size of the new font.
 * @return the resized font.
 */
@JvmName("getDifferentSizeFont")
fun Font.withSize(size: Float): Font {
    val newAttributes = attributes.toMutableMap()
    if (TextAttribute.SIZE in newAttributes) {
        newAttributes[TextAttribute.SIZE] = size
    }
    return Font(newAttributes)
}


/**
 * Calculates the largest size of the given font for which the given string
 * will fit into the given size.
 *
 *
 * @receiver the graphics to use in the current context.
 * @param font the original font to base the returned font on.
 * @param string the string to fit.
 * @param width the maximum width available.
 * @param height the maximum height available.
 * @return the maximum font size that fits into the given area.
 */
fun Graphics.getMaxFittingFontSize(
    font: Font, string: String, width: Int, height: Int
): Int {
    tailrec fun search(
        minSize: Int,
        maxSize: Int,
        curSize: Int = (minSize + maxSize) / 2
    ): Int {
        if (maxSize - minSize <= 2) return curSize

        val fm = getFontMetrics(Font(font.name, font.style, curSize))
        val fontWidth = fm.stringWidth(string)
        val fontHeight = fm.leading + fm.maxAscent + fm.maxDescent

        val shouldBeSmaller = fontWidth > width || fontHeight > height
        return when (shouldBeSmaller) {
            true -> search(minSize, curSize)
            false -> search(curSize, maxSize)
        }
    }
    return search(0, 288, font.size)
}

/**
 * Get the difference between two colours, from 0 to 100 where 100 is most
 * difference and 0 is least different.
 *
 *
 * @receiver the first colour
 * @param other the second colour
 * @return the difference between the colours.
 */
@JvmName("getColorDifference")
fun Color.difference(other: Color): Double {
    val ret = abs(red - other.red) + abs(green - other.green) + abs(blue - other.blue)
    return ret / (255 * 3) * 100
}


/**
 * Remove all HTML tags from a string.
 *
 *
 * @param str the string to remove the tags from.
 * @return the string with the tags removed.
 */
fun removeTags(str: String): String =
    str.replace(Regex("\\<.*?>"), "")


/**
 * Determine whether the given stage is completely on the given screen.
 *
 *
 * @receiver the stage to check.
 * @param monitorNum the monitor number to check.
 * @return true if the frame is totally on the screen, false otherwise.
 */
@JvmName("isFrameOnScreen")
infix fun Stage.isOnScreen(monitorNum: Int): Boolean {
    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    val gds = ge.screenDevices
    return gds[monitorNum].defaultConfiguration.bounds.contains(x, y, width, height)
}

fun fxThread(): Boolean = Platform.isFxApplicationThread()


fun checkFXThread() {
    if (!fxThread()) {
        LOGGER.log(Level.WARNING, "Not on FX Thread!", AssertionError())
    }
}


/**
 * Remove duplicates in a list whilst maintaining the order.
 *
 *
 * @receiver the list to remove duplicates.
 * @param T the type of the list.
 */
@JvmName("removeDuplicateWithOrder")
fun <T> MutableList<T>.distinctInPlace() {
    val newList = distinct()
    clear()
    addAll(newList)
}

/**
 * Copy a file from one place to another.
 *
 * Skips subversion files.
 *
 *
 * @receiver  the source file
 * @param destFile the destination file
 * @throws IOException if something goes wrong.
 */
@Throws(IOException::class)
@JvmName("copyFile")
fun File.copyFileWithFilters(destFile: File) {
    if (isDirectory && name == ".svn") return
    copyRecursively(destFile, overwrite = true)
}

/**
 * Capitalise the first letter of a string.
 *
 *
 * @receiver the input string.
 * @return the string with the first letter capitalised.
 */
fun String.capitaliseFirst() = replaceFirstChar {
    it.uppercaseChar()
}

/**
 * Get an abbreviation from a name based on the first letter of each word of
 * the name.
 *
 *
 * @param name the name to use for the abbreviation.
 * @return the abbreviation.
 */
fun getAbbreviation(name: String) = buildString {
    name.splitToSequence(' ')
        .filter { it.isNotEmpty() }
        .forEach {
            append(it.first().uppercaseChar())
        }
}

/**
 * Escape the XML special characters.
 *
 *
 * @receiver the string to escape.
 * @return the escaped string.
 */
fun String.escapeXML() : String = StringEscapeUtils.escapeXml11(this)


/**
 * Get the textual content from a file as a string, returning the given
 * error string if a problem occurs retrieving the content.
 *
 *
 * @param fileName the filename to get the text from.
 * @param errorText the error string to return if things go wrong.
 * @param encoding the encoding to use.
 * @return hopefully the text content of the file, or the errorText string
 * if we can't get the text content for some reason.
 */

@Deprecated("Use File.readOrElse instead", ReplaceWith(
    "File(fileName).readOrElse(errorText, Charset.forName(encoding))",
    "java.io.File",
    "java.nio.charset.Charset"
))
fun getTextFromFile(
    fileName: String,
    errorText: String,
    encoding: String
) = File(fileName).readOrElse(errorText, Charset.forName(encoding))

/**
 * Get the textual content from a file as a string, returning the given
 * error string if a problem occurs retrieving the content.
 *
 *
 * @receiver the file to get the text from.
 * @param errorText the error string to return if things go wrong.
 * @return hopefully the text content of the file, or the errorText string
 * if we can't get the text content for some reason.
 */
@Synchronized
fun File.readOrElse(
    errorText: String,
    encoding: Charset = Charsets.UTF_8
) = try {
    buildString {
        forEachLine(encoding, ::appendLine)
    }
} catch (ex: IOException) {
    LOGGER.log(Level.WARNING, "Couldn't get the contents of $name", ex)
    errorText
}

/**
 * Get the textual content from a file as a string, returning the given
 * error string if a problem occurs retrieving the content.
 *
 *
 * @param fileName the filename to get the text from.
 * @param errorText the error string to return if things go wrong.
 * @return hopefully the text content of the file, or the errorText string
 * if we can't get the text content for some reason.
 */
@Deprecated("Use File.readOrElse instead", ReplaceWith(
    "File(fileName).readOrElse(errorText)",
    "java.io.File"
))
fun getTextFromFile(fileName: String, errorText: String) =
    File(fileName).readOrElse(errorText)

/**
 * Resize a given image to the given width and height.
 *
 *
 * @receiver the image to resize.
 * @param width the width of the new image.
 * @param height the height of the new image.
 * @return the resized image.
 */
fun BufferedImage.resizeImage(width: Int, height: Int): BufferedImage {
    if (width <= 0 || height <= 0 || !(this.width != width || this.height != height)) return this

    return BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB).also { buffered ->
        buffered.createGraphics().also {
            it.setRenderingHint(
                RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
            )
            it.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            it.drawImage(this, 0, 0, width, height, null)
        }
    }
}

/**
 * Determine whether a file is an image file.
 *
 *
 * @receiver the file to check.
 * @return true if the file is an image, false otherwise.
 */
fun File.fileIsImage(): Boolean = (isDirectory() && !isHidden()) ||
        this.extensionMatchesAny(getImageExtensions())

/**
 * Determine whether a file is an video file.
 *
 *
 * @receiver the file to check.
 * @return true if the file is a video, false otherwise.
 */
@JvmName("fileIsVideo")
fun File.isVideoFile(): Boolean = (isDirectory() && !isHidden()) ||
        this.extensionMatchesAny(getVideoExtensions())

/**
 * Determine whether a file is a timer file.
 *
 *
 * @receiver  the file to check.
 * @return true if the file is a timer, false otherwise.
 */
@JvmName("fileIsTimer")
fun File.isTimer(): Boolean = (isDirectory() && !isHidden()) ||
        this.hasExtension("*.cdt")

/**
 * Get a list of all supported image extensions.
 *
 *
 * @return a list of all supported image extensions.
 */
fun getImageExtensions() = listOf(
    "png", "PNG",
    "tiff", "TIFF",
    "jpg", "JPG",
    "jpeg", "JPEG",
    "gif", "GIF",
    "bmp", "BMP"
)

/**
 * Get a list of all supported video extensions.
 *
 *
 * @return a list of all supported video extensions.
 */
fun getVideoExtensions() = listOf(
    "mkv", "MKV",
    "mp4", "MP4",
    "m4v", "M4V",
    "flv", "FLV",
    "avi", "AVI",
    "mov", "MOV",
    "rm", "RM",
    "mpg", "MPG",
    "mpeg", "MPEG",
    "wmv", "WMV",
    "ogm", "OGM",
    "mrl", "MRL",
    "asx", "ASX",
    "m2ts", "M2TS",
    "vlcarg", "VLCARG"
)

/**
 * Get a list of all supported audio extensions.
 *
 *
 * @return a list of all supported audio extensions.
 */
fun getAudioExtensions() = listOf(
    "mp3", "MP3",
    "wav", "WAV",
    "wma", "WMA"
    // TODO: Add more extensions
)


/**
 * Get a list of all supported multimedia extensions.
 *
 *
 * @return a list of all supported multimedia extensions.
 */
fun getMultimediaExtensions() = getVideoExtensions() + getAudioExtensions()

fun getImageAndVideoExtensions() = getVideoExtensions() + getImageExtensions()

/**
 * Get file extensions (in *.ext format) from a list of normal extensions.
 *
 *
 * @param extensions the list of normal extensions.
 * @return a list of file extensions.
 */
fun getFileExtensions(extensions: List<String>) = extensions.map { "*.$it" }

/**
 * Determine whether the given file has the given case insensitive
 * extension.
 *
 *
 * @receiver the file to check.
 * @param ext the extension to check.
 * @return true if it has the given extension, false otherwise.
 */
fun File.hasExtension(ext: String) = extension.javaTrim()
    .ifEmpty { return false }
    .lowercase() == ext.javaTrim()


/**
 * Determine whether the given file has any of the given case insensitive
 * extensions.
 *
 *
 * @receiver the file to check.
 * @param candidates the extension to check.
 * @return true if it has the given extension, false otherwise.
 */
infix fun File.extensionMatchesAny(candidates: List<String>): Boolean {
    val realExtension = extension.javaTrim()
        .ifEmpty { return false }
        .lowercase()

    return candidates.any {
        it.javaTrim() == realExtension
    }
}


/**
 * Get the names of all the fonts available on the current system.
 *
 *
 * @return the names of all the fonts available.
 */
fun getAllFonts() = GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts
    .mapTo(mutableSetOf()) { it.family }
    .toTypedArray()
    .apply(Array<*>::sort)


/**
 * Get an image filled with the specified colour.
 *
 *
 * @receiver the colour of the image.
 * @return the image.
 */
fun Color.getImageFromColour(): Image {
    val width = 2
    val height = 2
    val image = WritableImage(width, height)

    val writer = image.getPixelWriter()
    for (i in 0..<width) {
        for (j in 0..<height) {
            writer.setColor(i, j, this)
        }
    }
    return image
}

private val videoPreviewCache: MutableMap<File, WritableImage?> = SoftHashMap()

/**
 * Get an image to be shown as the background in place of a playing video.
 *
 *
 * @receiver video file for which to get the preview image.
 * @return the image to be shown in place of a playing video.
 */
fun File.getVidBlankImage(): Image? {
    synchronized(videoPreviewCache) {
        if (!isVideoFile()) return Image("file:icons/audio preview.png")
        if (!isFile()) return Image("file:icons/vid preview.png")

        return try {
            videoPreviewCache.getOrPut(this) {
                AWTFrameGrab.getFrame(this, 0)?.let { img ->
                    val scaledImg = when {
                        img.width <= 720 && img.height <= 480 -> img
                        else -> img.scaleImage(720)
                    }
                    SwingFXUtils.toFXImage(scaledImg, null)
                }
            }
        } catch (ex: java.lang.Exception) {
            LOGGER.info {
                "Couldn''t get video preview image for $absolutePath"
            }
            Image("file:icons/vid preview.png")
        }
    }
}

private fun BufferedImage.scaleImage(newWidth: Int): BufferedImage {
    val ratio = (width.toDouble() / height)
    val newHeight = (newWidth / ratio).toInt()
    return BufferedImage(newWidth, newHeight, type).also { img ->
        img.createGraphics().also {
            it.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            it.drawImage(this, 0, 0, newWidth, newHeight, 0, 0, width, height, null)
            img.createGraphics()
            it.dispose()
        }
    }
}

/**
 * Parse a colour string to a colour.
 *
 *
 * @param colour the colour string.
 * @return the colour.
 */
fun parseColour(colour: String?): Color {
    if (colour == null || colour.javaTrim().isEmpty())
        return ThemeDTO.DEFAULT_FONT_COLOR

    if ("[" !in colour) return try {
        Color.web(colour)
    } catch (ex: IllegalArgumentException) {
        ThemeDTO.DEFAULT_FONT_COLOR
    }


    val colour = colour.substringAfter('[').substringBefore(']')

    val parts = colour.split(Regex(",")).dropLasEmpty()
    var red = parts[0].split(Regex("="))[1].toDouble()
    var green = parts[1].split("=".toRegex())[1].toDouble()
    var blue = parts[2].split("=".toRegex())[1].toDouble()
    if (red > 1.0) {
        red /= 255.0
    }
    if (green > 1.0) {
        green /= 255.0
    }
    if (blue > 1.0) {
        blue /= 255.0
    }
    return Color(red, green, blue, 1.0)
}

private fun List<String>.dropLasEmpty() = dropLastWhile { it.isEmpty() }

fun String.escapeHTML() = buildString {
    for (c in this@escapeHTML) {
        if (c.code > 127 || c == '"' || c == '<' || c == '>') {
            append("&#").append(c.code).append(";")
        } else {
            append(c)
        }
    }
}

/**
 * Extract a zip file to a temporary location and retrieve a list of all
 * extracted files.
 *
 * @receiver the zip file to extract
 * @return a list of all extracted files.
 */
fun File.extractZip(): List<File> = try {
    extractZipWithCharset()
} catch (ex: java.lang.Exception) {
    extractZipWithCharset(Charsets.CP866)
}

/**
 * Extract a zip file to a temporary location and retrieve a list of all
 * extracted files.
 *
 * @receiver the zip file to extract
 * @param charset the charset to use on this zip file
 * @return a list of all extracted files.
 */
private fun File.extractZipWithCharset(
    charset: Charset = Charsets.UTF_8,
): List<File> {
    LOGGER.info { "Extracting zip file $absolutePath" }

    val BUFFER = 2048
    try {
        ZipFile(this, charset).use { zipFile ->
            val tempFolder = Files.createTempDirectory("qzipextract").toFile()
            tempFolder.deleteOnExit()
            for (entry in zipFile.entries()) {
                val currentEntry = entry.name
                val destFile = File(tempFolder, currentEntry)
                val destinationParent = destFile.getParentFile()
                destinationParent.mkdirs()
                if (!entry.isDirectory) {
                    zipFile.getInputStream(entry).buffered(BUFFER).use { zipStream ->
                        destFile.outputStream().buffered(BUFFER).use { dest ->
                            zipStream.copyTo(dest, BUFFER)
                        }
                    }
                }
            }

            return tempFolder.walkTopDown().maxDepth(999).filter {
                it.toPath().isRegularFile()
            }.toList()
        }
    } catch (ex: IOException) {
        LOGGER.log(Level.WARNING, "Error extracting zip", ex)
        return emptyList()
    }
}

@Suppress("DEPRECATION")
@JvmOverloads
fun File.resolveEncoding(fallback : Charset = Charsets.UTF_8): Charset =
    Charset.forName(getEncoding(fallback.name()))

@Deprecated("Use File.resolveEncoding instead", ReplaceWith("File.resolveEncoding()"))
@JvmOverloads
fun File.getEncoding(fallback : String = "UTF-8"): String {
    val encoding = try {
        UniversalDetector.detectCharset(this)
    } catch (ex: IOException) {
        null
    }

    return when (encoding) {
        null -> {
            LOGGER.warning { "Couldn't detect encoding, defaulting to $fallback for $absolutePath" }
            fallback
        }
        else -> {
            LOGGER.info { "Detected $encoding encoding for $absolutePath" }
            encoding
        }
    }
}

@Suppress("UnusedReceiverParameter")
val Charsets.CP866: Charset
    get() = Charset.forName("CP866")

