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
package org.quelea.services.utils

import javafx.geometry.BoundingBox
import javafx.geometry.Bounds
import javafx.scene.paint.Color
import org.quelea.data.bible.Bible
import org.quelea.data.displayable.TextAlignment
import org.quelea.services.languages.spelling.Dictionary
import org.quelea.services.languages.spelling.DictionaryManager
import org.quelea.services.notice.NoticeDrawer.NoticePosition
import org.quelea.utils.javaTrim
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.logging.Level

/**
 * Manages the properties specific to Quelea.
 *
 * @constructor Load the properties from the properties file.
 * @property propFile the properties file.
 * @author Michael
 */
internal class QueleaProperties private constructor(userHome: String?) : SortedProperties() {
    private val userHome: String = userHome
        .takeUnless { it.isNullOrEmpty() }
        ?: requireNotNull(System.getProperty("user.home")){
            "user.home is not set"
        }


    private val propFile: File
        get() = File(queleaUserHome, "quelea.properties")

    /**
     * Save these properties to the file.
     */
    private fun write() {
        try {
            FileWriter(propFile).use { writer -> store(writer, "Auto save") }
        } catch (ex: IOException) {
//            LOGGER.log(Level.WARNING, "Couldn't store properties", ex);
        }
    }

    /**
     * The languages file that for the GUI that should be used as specified in the properties file.
     */
    val languageFile: File
        get() = File("languages", getProperty(QueleaPropertyKeys.languageFileKey, "gb.lang"))


    val isDictionaryEnabled: Boolean
        get() = getProperty(QueleaPropertyKeys.enableDictKey, "false").toBoolean()

    /**
     * The GUI - languages file that should be used as specified in the properties file.
     */
    val dictionary: Dictionary
        get() {
            val dict = getProperty(QueleaPropertyKeys.languageFileKey, "gb.lang")
            val parts = dict.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
            val fileName = parts.joinToString(".") + ".words"
            return DictionaryManager.INSTANCE.getFromFilename(fileName)
        }

    /**
     * Set the name of the language file to use.
     *
     *
     *
     * @param file the name of the language file to use.
     */
    fun setLanguageFile(file: String?) {
        setProperty(QueleaPropertyKeys.languageFileKey, file)
        write()
    }

    /**
     * The english languages file that should be present on all installations.
     *
     * We can default to this if labels are missing in other languages.
     */
    val englishLanguageFile: File
        get() = File("languages", "gb.lang")


    /**
     * Whether or not to display the video tab.
     *
     * true if the video tab should be displayed, false otherwise.
     */
    var displayVideoTab: Boolean
        get() = try {
            getProperty(QueleaPropertyKeys.videoTabKey, "false").toBoolean()
        } catch (ex: Exception) {
            true
        }
        set(videoTab) {
            setProperty(QueleaPropertyKeys.videoTabKey, videoTab.toString())
            write()
        }

    /**
     * The scene info as stored from the last exit of Quelea (or some
     * default values if it doesn't exist in the properties file.)
     */
    val sceneInfo: SceneInfo?
        get() = try {
            val parts = getProperty(QueleaPropertyKeys.sceneInfoKey, "461,15,997,995,false")
                .split(",".toRegex())
                .dropLastWhile { it.isEmpty() }
            when (parts.size) {
                4 -> SceneInfo(
                    parts[0].toInt().toDouble(),
                    parts[1].toInt().toDouble(),
                    parts[2].toInt().toDouble(),
                    parts[3].toInt().toDouble(),
                    false
                )

                5 -> SceneInfo(
                    parts[0].toInt().toDouble(),
                    parts[1].toInt().toDouble(),
                    parts[2].toInt().toDouble(),
                    parts[3].toInt().toDouble(),
                    parts[4].toBoolean()
                )

                else -> null
            }
        } catch (ex: Exception) {
            LoggerUtils.getLogger()
                .log(Level.WARNING, "Invalid scene info: " + getProperty(QueleaPropertyKeys.sceneInfoKey), ex)
            null
        }

    /**
     * Set the scene info for Quelea's main window - generally called just
     * before exit so the next invocation of the program can display the window
     * in the same position.
     *
     *
     *
     * @param info the scene info.
     */
    fun setSceneInfo(info: SceneInfo) {
        setProperty(QueleaPropertyKeys.sceneInfoKey, info.toString())
        write()
    }

    /**
     * the posiotion of the main splitpane divider:  0-1, or -1 if none is set.
     */
    var mainDivPos: Double
        get() = getProperty(QueleaPropertyKeys.mainDivposKey, "-1").toDouble()
        set(value) {
            setProperty(QueleaPropertyKeys.mainDivposKey, value.toString())
            write()
        }
    val elevantoClientId: String
        get() = getProperty(QueleaPropertyKeys.elevantoClientIdKey, "91955")

    /**
     * The library / schedule splitpane divider position property: 0-1, or -1 if none is set.
     */
    var libraryDivPos: Double
        get() = getProperty(QueleaPropertyKeys.libraryDivposKey, "-1").toDouble()
        set(value) {
            setProperty(QueleaPropertyKeys.libraryDivposKey, value.toString())
            write()
        }

    /**
     * The preview / live splitpane divider position property: 0-1, or -1 if none is set.
     */

    var prevLiveDivPos: Double
        get() = getProperty(QueleaPropertyKeys.preliveDivposKey, "-1").toDouble()
        set(value) {
            setProperty(QueleaPropertyKeys.preliveDivposKey, value.toString())
            write()
        }

    /**
     * The canvas divider position property: 0-1, or -1 if none is set.
     */
    var canvasDivPos: Double
        get() = getProperty(QueleaPropertyKeys.canvasDivposKey, "-1").toDouble()
        set(value) {
            setProperty(QueleaPropertyKeys.canvasDivposKey, value.toString())
            write()
        }

    /**
     * The preview panel divider position property: 0-1, or -1 if none is set.
     */
    var previewDivPosKey: Double
        get() = getProperty(QueleaPropertyKeys.previewDivposKey, "-1").toDouble()
        set(value) {
            setProperty(QueleaPropertyKeys.previewDivposKey, value.toString())
            write()
        }


    /**
     * A list of user chosen fonts to appear in the theme dialog.
     *
     *
     *
     * @return a list of user chosen fonts to appear in the theme dialog.
     */
    var chosenFonts: List<String>
        get() {
            val fontStr = getProperty(
                QueleaPropertyKeys.chosenFontsKey,
                "Arial|Liberation Sans|Noto Sans|Oxygen|Roboto|Vegur|Roboto Mono|Ubuntu Mono"
            )
            return fontStr.split("\\|".toRegex())
                .dropLastWhile { it.isEmpty() }
                .filter { it.javaTrim().isNotEmpty() }
        }
        set(fonts) {
            val fontsString = fonts.joinToString("|")
            setProperty(QueleaPropertyKeys.chosenFontsKey, fontsString)
            write()
        }

    /**
     * Wheter the same font size should be used for each section in a
     * displayable
     *
     * - this can stop the sizes jumping all over the place
     * depending on how much text there is per slide.
     */
    var useUniformFontSize: Boolean
        get() = getProperty(QueleaPropertyKeys.uniformFontSizeKey, "true").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.uniformFontSizeKey, value.toString())
        }

    /**
     * Wheter we should show verse numbers for bible passages.
     */
    var showVerseNumbers: Boolean
        get() = getProperty(QueleaPropertyKeys.showVerseNumbersKey, "true").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.showVerseNumbersKey, value.toString())
        }

    /**
     * The colour to use for notice backgrounds.
     */
    var noticeBackgroundColour: Color
        get() = getColor(getProperty(QueleaPropertyKeys.noticeBackgroundColourKey, Color.BROWN.getStr()))
        set(colour) {
            setProperty(QueleaPropertyKeys.noticeBackgroundColourKey, colour.getStr())
        }

    /**
     * The position at which to display the notices.
     */
    var noticePosition: NoticePosition
        get() = if (
            getProperty(QueleaPropertyKeys.noticePositionKey, "Bottom")
                .equals("top", ignoreCase = true)
        ) NoticePosition.TOP else NoticePosition.BOTTOM
        set(position) {
            setProperty(QueleaPropertyKeys.noticePositionKey, position.text)
        }

    /**
     * The speed at which to display the notices.
     */
    var noticeSpeed: Double
        get() = getProperty(QueleaPropertyKeys.noticeSpeedKey, "10").toDouble()
        set(speed) {
            setProperty(QueleaPropertyKeys.noticeSpeedKey, speed.toString())
        }

    /**
     * The last directory used in the general file chooser.
     *
     * @return the last directory used in the general file chooser.
     */
    val lastDirectory: File?
        get() {
            val path = getProperty(QueleaPropertyKeys.lastDirectoryKey) ?: return null
            val f = File(path)
            return if (f.isDirectory()) f
            else {
                LoggerUtils.getLogger()
                    .log(Level.INFO, "Cannot find last directory, reverting to default location")
                null
            }
        }

    /**
     * Set the last directory used in the general file chooser.
     *
     * @param directory the last directory used in the general file chooser.
     */
    fun setLastDirectory(directory: File) {
        setProperty(QueleaPropertyKeys.lastDirectoryKey, directory.absolutePath)
    }

    /**
     * The last directory used in the schedule file chooser.
     */
    val lastScheduleFileDirectory: File?
        get() {
            val path = getProperty(QueleaPropertyKeys.lastSchedulefileDirectoryKey) ?: return null
            val f = File(path)
            return if (f.isDirectory()) f
            else {
                LoggerUtils.getLogger()
                    .log(Level.INFO, "Cannot find last schedule directory, reverting to default location")
                null
            }
        }

    /**
     * Whether the schedule should embed videos when saving:
     * true if should embed, false otherwise
     */
    var embedMediaInScheduleFile: Boolean
        get() = getProperty(QueleaPropertyKeys.scheduleEmbedMediaKey, "true").toBoolean()
        set(embed) {
            setProperty(QueleaPropertyKeys.scheduleEmbedMediaKey, "$embed")
        }

    /**
     * Whether item themes can override the global theme.
     */
    var itemThemeOverride: Boolean
        get() = getProperty(QueleaPropertyKeys.itemThemeOverrideKey, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.itemThemeOverrideKey, value.toString() + "")
        }

    /**
     * The currently selected global theme file.
     */
    var globalSongThemeFile: File?
        get() = getProperty(QueleaPropertyKeys.globalSongThemeFileKey)
            .takeUnless { it.isNullOrEmpty() }
            ?.let(::File)
        set(file) {
            setProperty(QueleaPropertyKeys.globalSongThemeFileKey, file?.absolutePath.orEmpty())
        }

    /**
     * The currently selected global theme file.
     */
    var globalBibleThemeFile: File?
        get() = getProperty(QueleaPropertyKeys.globalBibleThemeFileKey)
            .takeUnless { it.isNullOrEmpty() }
            ?.let(::File)
        set(file) {
            setProperty(QueleaPropertyKeys.globalBibleThemeFileKey, file?.absolutePath.orEmpty())
        }

    /**
     * Set the last directory used in the schedule file chooser.
     *
     * @param directory the last directory used in the schedule file chooser.
     */
    fun setLastScheduleFileDirectory(directory: File) {
        setProperty(QueleaPropertyKeys.lastSchedulefileDirectoryKey, directory.absolutePath)
    }

    /**
     * The last directory used in the video file chooser.
     *
     * @return the last directory used in the video file chooser.
     */
    val lastVideoDirectory: File?
        get() {
            val path = getProperty(QueleaPropertyKeys.lastVideoDirectoryKey) ?: return null
            val f = File(path)
            return if (f.isDirectory()) f else {
                LoggerUtils.getLogger()
                    .log(Level.INFO, "Cannot find last video directory, reverting to default location")
                null
            }
        }

    /**
     * Set the last directory used in the video file chooser.
     *
     * @param directory the last directory used in the video file chooser.
     */
    fun setLastVideoDirectory(directory: File) {
        setProperty(QueleaPropertyKeys.lastVideoDirectoryKey, directory.absolutePath)
    }

    /**
     * Whether to auto-play videos after they have been set in live view.
     * true if auto-play is enabled, false otherwise.
     */

    var autoPlayVideo: Boolean
        get() = getProperty(QueleaPropertyKeys.autoplayVidKey, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.autoplayVidKey, value.toString())
        }
    /**
     * Whether to use Java FX rendering for video playback with VLC.
     * This approach is totally cross-platform capable.
     *
     * true if should use java fx for VLC Rendering, false otherwise
     */
    var useJavaFXforVLCRendering: Boolean
        get() = getProperty(QueleaPropertyKeys.useVlcJavafxRenderingKey, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.useVlcJavafxRenderingKey, value.toString())
        }

    /**
     * The font size at which to display the notices.
     */
    var noticeFontSize: Double
        get() = getProperty(QueleaPropertyKeys.noticeFontSizeKey, "50").toDouble()
        set(fontSize) {
            setProperty(QueleaPropertyKeys.noticeFontSizeKey, fontSize.toString())
        }
    /**
     * Whether we should attempt to fetch translations automatically.
     */
    var autoTranslate: Boolean
        get() = getProperty(QueleaPropertyKeys.autoTranslateKey, "true").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.autoTranslateKey, value.toString())
        }

    /**
     * The maximum font size used by text displayables.
     */
    var maxFontSize: Double
        get() = getProperty(QueleaPropertyKeys.maxFontSizeKey, "1000").toDouble()
        set(fontSize) {
            setProperty(QueleaPropertyKeys.maxFontSizeKey, fontSize.toString())
        }

    /**
     * The additional line spacing (in pixels) to be used between each line.
     */
    var additionalLineSpacing: Double
        get() = getProperty(QueleaPropertyKeys.additionalLineSpacingKey, "10").toDouble()
        set(spacing) {
            setProperty(QueleaPropertyKeys.additionalLineSpacingKey, spacing.toString())
        }

    /**
     * The thumbnail size.
     */
    var thumbnailSize: Int
        get() = getProperty(QueleaPropertyKeys.thumbnailSizeKey, "200").toInt()
        set(thumbnailSize) {
            setProperty(QueleaPropertyKeys.thumbnailSizeKey, thumbnailSize.toString())
        }
    var planningCentrePrevDays: Int
        get() = getProperty(QueleaPropertyKeys.planningCentrePrevDaysKey, "31").toInt()
        set(days) {
            setProperty(QueleaPropertyKeys.planningCentrePrevDaysKey, days.toString())
        }
    var useDefaultTranslation: Boolean
        get() = getProperty(QueleaPropertyKeys.useDefaultTranslation, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.useDefaultTranslation, value.toString())
        }
    var defaultTranslationName: String?
        get() = getProperty(QueleaPropertyKeys.defaultTranslationName, "")
        set(value) {
            setProperty(QueleaPropertyKeys.defaultTranslationName, value)
        }

    /**
     * Wheter the extra live panel toolbar options setting should be shown. Hidden by default.
     */
    var showExtraLivePanelToolbarOptions: Boolean
        get() = getProperty(QueleaPropertyKeys.showExtraLivePanelToolbarOptionsKey, "false").toBoolean()
        set(show) {
            setProperty(QueleaPropertyKeys.showExtraLivePanelToolbarOptionsKey, show.toString())
        }

    /**
     * Whether the preview and live dividers should be linked. eg move together
     * true if the preview and live dividers should be linked, else false
     */
    val linkPreviewAndLiveDividers: Boolean
        get() = getProperty(QueleaPropertyKeys.linkPreviewAndLiveDividers, "true").toBoolean()

    /**
     * Should also remove from live view, the alternative is waiting until something replaces it.
     * - true: should remove from liveview
     * - false: wait until something replaces it
     */
    var clearLiveOnRemove: Boolean
        get() = getProperty(QueleaPropertyKeys.clearLiveOnRemoveKey, "true").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.clearLiveOnRemoveKey, value.toString())
        }

    /**
     * The location of Quelea's Facebook page.
     */
    val facebookPageLocation: String
        get() = getProperty(QueleaPropertyKeys.facebookPageKey, "http://www.facebook.com/quelea.projection")

    /**
     * The location of Quelea's Facebook page.
     */
    val wikiPageLocation: String
        get() = getProperty(QueleaPropertyKeys.wikiPageKey, "http://quelea.org/wiki/index.php/Main_Page")

    /**
     * The Quelea home directory in the user's directory.
     */
    val queleaUserHome: File
        get() {
            val ret = File(File(userHome), ".quelea")
            if (!ret.exists()) {
                ret.mkdir()
            }
            return ret
        }

    /**
     * The user's turbo db exe converter file.
     */
    val turboDBExe: File
        get() = File(queleaUserHome, "TdbDataX.exe")

    val translationFontSizeOffset: Int
        get() = getProperty(QueleaPropertyKeys.translationFontSizeOffsetKey, "3").toInt()

    /**
     * The font to use for stage text.
     */
    var stageTextFont: String?
        get() = getProperty(QueleaPropertyKeys.stageFontKey, "SansSerif")
        set(font) {
            setProperty(QueleaPropertyKeys.stageFontKey, font)
            write()
        }

    /**
     * The alignment of the text on stage view.
     */
    val stageTextAlignment: String
        get() = TextAlignment.parse(getProperty(QueleaPropertyKeys.stageTextAlignmentKey, "LEFT")).toFriendlyString()

    /**
     * The alignment of the text on stage view.
     */
    fun setStageTextAlignment(alignment: TextAlignment) {
        setProperty(QueleaPropertyKeys.stageTextAlignmentKey, alignment.toString())
        write()
    }

    /**
     * Whether we should display the chords in stage view.
     */
    var showChords: Boolean
        get() = getProperty(QueleaPropertyKeys.stageShowChordsKey, "true").toBoolean()
        set(showChords) {
            setProperty(QueleaPropertyKeys.stageShowChordsKey, showChords.toString())
            write()
        }

    /**
     * Determine whether we should phone home at startup with anonymous
     * information. Simply put phonehome=false in the properties file to disable
     * phonehome.
     *
     *
     * @return true if we should phone home, false otherwise.
     */
    val phoneHome: Boolean
        get() = getProperty(QueleaPropertyKeys.phonehomeKey, "true").toBoolean()

    /**
     * The directory used for storing the bibles.
     */
    val bibleDir: File
        get() = File(queleaUserHome, "bibles")


    /**
     * The directory used for storing images.
     */
    val imageDir: File get() = File(queleaUserHome, "img")

    /**
     * The directory used for storing dictionaries.
     */
    val dictionaryDir: File
        get() = File(queleaUserHome, "dictionaries")

    /**
     * The directory used for storing videos.
     */
    val vidDir: File
        get() = File(queleaUserHome, "vid")

    /**
     * The directory used for storing temporary recordings.
     */
    val tempDir: File
        get() = File(queleaUserHome, "temp")


    /**
     * The extension used for quelea schedules.
     */
    val scheduleExtension: String
        get() = getProperty(QueleaPropertyKeys.queleaScheduleExtensionKey, "qsch")

    /**
     * The extension used for quelea song packs.
     */
    val songPackExtension: String
        get() = getProperty(QueleaPropertyKeys.queleaSongpackExtensionKey, "qsp")

    /**
     * The control screen number.
     * This is the screen that the main Quelea operator window will be displayed on.
     */
    var controlScreen: Int
        get() = getProperty(QueleaPropertyKeys.controlScreenKey, "0").toInt()
        set(screen) {
            setProperty(QueleaPropertyKeys.controlScreenKey, screen.toString())
            write()
        }

    /**
     * Whether one line mode should be enabled.
     */
    var oneLineMode: Boolean
        get() = getProperty(QueleaPropertyKeys.oneLineModeKey, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.oneLineModeKey, value.toString())
            write()
        }

    /**
     * Whether texts have shadows.
     */
    var textShadow: Boolean
        get() = getProperty(QueleaPropertyKeys.textShadowKey, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.textShadowKey, value.toString())
            write()
        }

    /**
     * The number of the projector screen: the screen that the
     * projected output will be displayed on.
     *
     * @return the projector screen number.
     */
    var projectorScreen: Int
        get() = getProperty(QueleaPropertyKeys.projectorScreenKey, "1").toInt()
        set(screen) {
            setProperty(QueleaPropertyKeys.projectorScreenKey, screen.toString())
            write()
        }

    /**
     * Whether the projection screen automatically should be moved to
     * a recently inserted monitor.
     *
     *
     *
     * @return true if the projector screen should be moved, false otherwise.
     */
    var useAutoExtend: Boolean
        get() = getProperty(QueleaPropertyKeys.useAutoExtendKey, "false").toBoolean()
        set(extend) {
            setProperty(QueleaPropertyKeys.useAutoExtendKey, extend.toString())
        }

    /**
     * The maximum number of characters allowed on any one line of projected
     * text. If the line is longer than this, it will be split up intelligently.
     *
     */
    var maxChars: Int
        get() = getProperty(QueleaPropertyKeys.maxCharsKey, "30").toInt()
        set(maxChars) {
            setProperty(QueleaPropertyKeys.maxCharsKey, maxChars.toString())
            write()
        }

    /**
     * The custom projector co-ordinates.
     */
    var projectorCoords: Bounds
        get() {
            val prop = getProperty(QueleaPropertyKeys.projectorCoordsKey, "0,0,0,0").javaTrim()
                .split(",".toRegex()).dropLastWhile { it.isEmpty() }
            return BoundingBox(
                prop[0].toInt().toDouble(),
                prop[1].toInt().toDouble(),
                prop[2].toInt().toDouble(),
                prop[3].toInt().toDouble()
            )
        }
        set(coords) {
            val rectStr = coords.toCommaString()
            setProperty(QueleaPropertyKeys.projectorCoordsKey, rectStr)
            write()
        }

    private fun Bounds.toCommaString() = "${minX.toInt()},${minY.toInt()},${width.toInt()},${height.toInt()}"

    fun setXProjectorCoord(x: String) {
        val prop = getProperty(QueleaPropertyKeys.projectorCoordsKey, "0,0,0,0").javaTrim()
            .split(",".toRegex()).dropLastWhile { it.isEmpty() }
        val rectStr = (x
                + "," + prop[1]
                + "," + prop[2]
                + "," + prop[3])
        setProperty(QueleaPropertyKeys.projectorCoordsKey, rectStr)
        write()
    }

    fun setYProjectorCoord(y: String) {
        val prop = getProperty(QueleaPropertyKeys.projectorCoordsKey, "0,0,0,0").javaTrim()
            .split(",".toRegex()).dropLastWhile { it.isEmpty() }
        val rectStr = (prop[0]
                + "," + y
                + "," + prop[2]
                + "," + prop[3])
        setProperty(QueleaPropertyKeys.projectorCoordsKey, rectStr)
        write()
    }

    fun setWidthProjectorCoord(width: String) {
        val prop = getProperty(QueleaPropertyKeys.projectorCoordsKey, "0,0,0,0").javaTrim()
            .split(",".toRegex()).dropLastWhile { it.isEmpty() }
        val rectStr = (prop[0]
                + "," + prop[1]
                + "," + width
                + "," + prop[3])
        setProperty(QueleaPropertyKeys.projectorCoordsKey, rectStr)
        write()
    }

    fun setHeightProjectorCoord(height: String) {
        val prop = getProperty(QueleaPropertyKeys.projectorCoordsKey, "0,0,0,0").javaTrim()
            .split(",".toRegex()).dropLastWhile { it.isEmpty() }
        val rectStr = (prop[0]
                + "," + prop[1]
                + "," + prop[2]
                + "," + height)
        setProperty(QueleaPropertyKeys.projectorCoordsKey, rectStr)
        write()
    }

    fun setXStageCoord(x: String) {
        val prop = getProperty(QueleaPropertyKeys.stageCoordsKey, "0,0,0,0").javaTrim()
            .split(",".toRegex()).dropLastWhile { it.isEmpty() }
        val rectStr = (x
                + "," + prop[1]
                + "," + prop[2]
                + "," + prop[3])
        setProperty(QueleaPropertyKeys.stageCoordsKey, rectStr)
        write()
    }

    fun setYStageCoord(y: String) {
        val prop = getProperty(QueleaPropertyKeys.stageCoordsKey, "0,0,0,0").javaTrim()
            .split(",".toRegex()).dropLastWhile { it.isEmpty() }
        val rectStr = (prop[0]
                + "," + y
                + "," + prop[2]
                + "," + prop[3])
        setProperty(QueleaPropertyKeys.stageCoordsKey, rectStr)
        write()
    }

    fun setWidthStageCoord(width: String) {
        val prop = getProperty(QueleaPropertyKeys.stageCoordsKey, "0,0,0,0").javaTrim()
            .split(",".toRegex()).dropLastWhile { it.isEmpty() }
        val rectStr = (prop[0]
                + "," + prop[1]
                + "," + width
                + "," + prop[3])
        setProperty(QueleaPropertyKeys.stageCoordsKey, rectStr)
        write()
    }

    fun setHeightStageCoord(height: String) {
        val prop = getProperty(QueleaPropertyKeys.stageCoordsKey, "0,0,0,0").javaTrim()
            .split(",".toRegex()).dropLastWhile { it.isEmpty() }
        val rectStr = (prop[0]
                + "," + prop[1]
                + "," + prop[2]
                + "," + height)
        setProperty(QueleaPropertyKeys.stageCoordsKey, rectStr)
        write()
    }


    /**
     * Wheter the projector mode is set to manual co-ordinates or a screen
     * number.
     * - true = it's set to manual co-ordinates
     * - false = it's set to a screen number.
     */
    val isProjectorModeCoords: Boolean
        get() = "coords" == getProperty(QueleaPropertyKeys.projectorModeKey)

    /**
     * Set the projector mode to be manual co-ordinates.
     */
    fun setProjectorModeCoords() {
        setProperty(QueleaPropertyKeys.projectorModeKey, "coords")
        write()
    }

    /**
     * Set the projector mode to be a screen number.
     */
    fun setProjectorModeScreen() {
        setProperty(QueleaPropertyKeys.projectorModeKey, "screen")
        write()
    }

    /**
     * The number of the stage screen. This is the screen that the projected
     * output will be displayed on.
     */
    var stageScreen: Int
        get() = getProperty(QueleaPropertyKeys.stageScreenKey, "-1").toInt()
        set(screen) {
            setProperty(QueleaPropertyKeys.stageScreenKey, screen.toString())
            write()
        }

    /**
     * The custom stage screen co-ordinates.
     */
    var stageCoords: Bounds
        get() {
            val prop = getProperty(QueleaPropertyKeys.stageCoordsKey, "0,0,0,0").javaTrim()
                .split(",".toRegex()).dropLastWhile { it.isEmpty() }
            return BoundingBox(
                prop[0].toInt().toDouble(),
                prop[1].toInt().toDouble(),
                prop[2].toInt().toDouble(),
                prop[3].toInt().toDouble()
            )
        }
        set(coords) {
            val rectStr =coords.toCommaString()
            setProperty(QueleaPropertyKeys.stageCoordsKey, rectStr)
            write()
        }

    /**
     * Whether the stage mode is set to manual co-ordinates or a screen
     * number.
     * - true = it's set to manual co-ordinates
     * - false =  it's set to a screen number.
     */
    val isStageModeCoords: Boolean
        get() = "coords" == getProperty(QueleaPropertyKeys.stageModeKey)

    /**
     * Set the stage mode to be manual co-ordinates.
     */
    fun setStageModeCoords() {
        setProperty(QueleaPropertyKeys.stageModeKey, "coords")
        write()
    }

    /**
     * Set the stage mode to be a screen number.
     */
    fun setStageModeScreen() {
        setProperty(QueleaPropertyKeys.stageModeKey, "screen")
        write()
    }

    /**
     * The minimum number of lines that should be displayed on each page.
     * This purely applies to font sizes, the font will be adjusted so this
     * amount of lines can fit on. This stops small lines becoming huge in the
     * preview window rather than displaying normally.
     */

    var minLines: Int
        get() = getProperty(QueleaPropertyKeys.minLinesKey, "10").toInt()
        set(minLines) {
            setProperty(QueleaPropertyKeys.minLinesKey, minLines.toString())
            write()
        }

    /**
     * Determine whether the single monitor warning should be shown (this warns
     * the user they only have one monitor installed.)
     *
     *
     *
     * @return true if the warning should be shown, false otherwise.
     */
    fun showSingleMonitorWarning() =
        getProperty(QueleaPropertyKeys.singleMonitorWarningKey, "true").toBoolean()

    /**
     * Set whether the single monitor warning should be shown.
     *
     *
     *
     * @param val true if the warning should be shown, false otherwise.
     */
    fun setSingleMonitorWarning(value: Boolean) {
        setProperty(QueleaPropertyKeys.singleMonitorWarningKey, value.toString())
        write()
    }

    /**
     * The URL to download Quelea.
     */
    val downloadLocation: String
        get() = "https://github.com/quelea-projection/Quelea/releases/"

    /**
     * The URL to the Quelea website.
     */
    val websiteLocation: String
        get() = getProperty(QueleaPropertyKeys.websiteLocationKey, "http://www.quelea.org/")

    /**
     * The URL to the Quelea discussion forum.
     */
    val discussLocation: String
        get() = getProperty(QueleaPropertyKeys.discussLocationKey, "https://quelea.discourse.group/")

    /**
     * The URL to the Quelea feedback form.
     */
    val feedbackLocation: String
        get() = getProperty(QueleaPropertyKeys.feedbackLocationKey, "https://quelea.org/feedback/")

    /**
     * The URL used for checking the latest version.
     */

    val updateURL: String
        get() = "https://quelea-projection.github.io/changelog"


    /**
     * Whether we should check for updates each time the program
     * starts.
     */
    fun checkUpdate() =
        getProperty(QueleaPropertyKeys.checkUpdateKey, "true").toBoolean()

    /**
     * Set whether we should check for updates each time the program starts.
     */
    fun setCheckUpdate(value: Boolean) {
        setProperty(QueleaPropertyKeys.checkUpdateKey, value.toString())
        write()
    }


    /**
     * Determine whether the first letter of all displayed lines should be a
     * capital.
     *
     *
     *
     * @return true if it should be a capital, false otherwise.
     */
    fun checkCapitalFirst() : Boolean =
        getProperty(QueleaPropertyKeys.capitalFirstKey, "false").toBoolean()

    /**
     * Set whether the first letter of all displayed lines should be a capital.
     *
     *
     *
     * @param val true if it should be a capital, false otherwise.
     */
    fun setCapitalFirst(value: Boolean) {
        setProperty(QueleaPropertyKeys.capitalFirstKey, value.toString())
        write()
    }

    /**
     * Determine whether the song info text should be displayed.
     *
     *
     *
     * @return true if it should be a displayed, false otherwise.
     */
    fun checkDisplaySongInfoText(): Boolean =
        getProperty(QueleaPropertyKeys.displaySonginfotextKey, "true").toBoolean()

    /**
     * Set whether the song info text should be displayed.
     *
     *
     *
     * @param val true if it should be displayed, false otherwise.
     */
    fun setDisplaySongInfoText(value: Boolean) {
        setProperty(QueleaPropertyKeys.displaySonginfotextKey, value.toString())
        write()
    }

    /**
     * The default bible to use.
     */
    val defaultBible: String?
        get() = getProperty(QueleaPropertyKeys.defaultBibleKey)

    /**
     * The default bible.
     */
    fun setDefaultBible(bible: Bible) {
        setProperty(QueleaPropertyKeys.defaultBibleKey, bible.name)
        write()
    }


    /**
     * The colour used to display chords in stage view.
     */
    var stageChordColor: Color
        get() = getColor(getProperty(QueleaPropertyKeys.stageChordColorKey, "200,200,200"))
        set(color) {
            setProperty(QueleaPropertyKeys.stageChordColorKey, color.getStr())
        }

    /**
     * The colour used to display chords in stage view.
     */
    val textBackgroundColor: Color
        get() = getColor(getProperty(QueleaPropertyKeys.lyricsTextBackgroundColor))

    /**
     * Whether to advance the schedule item when the current item is sent live.
     */
    val textBackgroundEnable: Boolean
        get() = getProperty(QueleaPropertyKeys.lyricsTextBackgroundEnable, "false").toBoolean()

    /**
     * The colour used to display lyrics in stage view.
     */
    var stageLyricsColor: Color
        get() = getColor(getProperty(QueleaPropertyKeys.stageLyricsColorKey, "255,255,255"))
        set(color) {
            setProperty(QueleaPropertyKeys.stageLyricsColorKey, color.getStr())
        }
    /**
     * The colour used for the background in stage view.
     */
    var stageBackgroundColor: Color
        get() = getColor(getProperty(QueleaPropertyKeys.stageBackgroundColorKey, "0,0,0"))
        set(color) {
            setProperty(QueleaPropertyKeys.stageBackgroundColorKey, color.getStr())
        }

    /**
     * Get a color from a string.
     *
     *
     *
     * @param str the string to use to get the color value.
     * @return the color.
     */
    private fun getColor(str: String): Color {
        val color = str.split(",".toRegex()).dropLastWhile { it.isEmpty() }
        var red = color[0].javaTrim().toDouble()
        var green = color[1].javaTrim().toDouble()
        var blue = color[2].javaTrim().toDouble()
        if (red > 1 || green > 1 || blue > 1) {
            red /= 255.0
            green /= 255.0
            blue /= 255.0
        }
        return Color(red, green, blue, 1.0)
    }

    /**
     * Get a color value as a string.
     *
     *
     *
     * @param color the color to get as a string.
     * @return the color as a string.
     */
    @Deprecated("replace with color.getStr()", ReplaceWith("color.getStr()"))
    fun getStr(color: Color)=color.getStr()

    /**
     * The colour used to signify an active list.
     */
    val activeSelectionColor: Color
        get() = getColor(getProperty(QueleaPropertyKeys.activeSelectionColorKey, "30,160,225"))

    /**
     * The colour used to signify an active list.
     */
    val inactiveSelectionColor: Color
        get() = getColor(getProperty(QueleaPropertyKeys.inactiveSelectionColorKey, "150,150,150"))

    /**
     * The thickness (px) of the outline to use for displaying the text.
     */
    var outlineThickness: Int
        get() = getProperty(QueleaPropertyKeys.outlineThicknessKey, "2").toInt()
        set(px) {
            setProperty(QueleaPropertyKeys.outlineThicknessKey, px.toString())
            write()
        }

    /**
     * The notice box height (px).
     */
    var noticeBoxHeight: Int
        get() = getProperty(QueleaPropertyKeys.noticeBoxHeightKey, "40").toInt()
        set(height) {
            setProperty(QueleaPropertyKeys.noticeBoxHeightKey, height.toString())
            write()
        }

    /**
     * The notice box speed.
     */
    var noticeBoxSpeed: Int
        get() = getProperty(QueleaPropertyKeys.noticeBoxSpeedKey, "8").toInt()
        set(speed) {
            setProperty(QueleaPropertyKeys.noticeBoxSpeedKey, speed.toString())
            write()
        }

    /**
     * Words auto-capitalized by the song importer when deciding how to un-caps-lock a line of text.
     * seperated by commas  in the properties file
     */
    val godWords: Array<String>
        get() = getProperty(
            QueleaPropertyKeys.godWordsKey,
            "god,God,jesus,Jesus,christ,Christ,you,You,he,He,lamb,Lamb,"
                    + "lord,Lord,him,Him,son,Son,i,I,his,His,your,Your,king,King,"
                    + "saviour,Saviour,savior,Savior,majesty,Majesty,alpha,Alpha,omega,Omega"
        ) //Yeah.. default testing properties.
            .javaTrim().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    /**
     * Determine whether to advance the schedule item when the current item is sent live.
     */
    var advanceOnLive: Boolean
        get() = getProperty(QueleaPropertyKeys.advanceOnLiveKey, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.advanceOnLiveKey, value.toString())
            write()
        }

    /**
     * Whether to preview the schedule item when the background image
     * has been updated.
     */
    var previewOnImageUpdate: Boolean
        get() = getProperty(QueleaPropertyKeys.previewOnImageChangeKey, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.previewOnImageChangeKey, value.toString())
            write()
        }

    /**
     * Whether to use openoffice for presentations.
     * - true if we should use openoffice
     * - false if we should use basic POI images
     */
    var useOO: Boolean
        get() = getProperty(QueleaPropertyKeys.useOoKey, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.useOoKey, value.toString())
            write()
        }

    /**
     * The path to the openoffice installation on this machine.
     */
    var oOPath: String?
        get() = getProperty(QueleaPropertyKeys.ooPathKey, "")
        set(path) {
            setProperty(QueleaPropertyKeys.ooPathKey, path)
            write()
        }

    /**
     * Whether to use PowerPoint for presentations.
     * - true if we should use PowerPoint
     * - false if we should just use the basic POI images or openoffice.
     */
    var usePP: Boolean
        get() = getProperty(QueleaPropertyKeys.usePpKey, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.usePpKey, value.toString())
            write()
        }

    /**
     * The path to the PowerPoint installation on this machine.
     */
    var pPPath: String?
        get() = getProperty(QueleaPropertyKeys.ppPathKey, "")
        set(path) {
            setProperty(QueleaPropertyKeys.ppPathKey, path)
            write()
        }

    /**
     * The path to the desired directory for recordings.
     */
    var recordingsPath: String?
        get() = getProperty(QueleaPropertyKeys.recPathKey, "")
        set(path) {
            setProperty(QueleaPropertyKeys.recPathKey, path)
            write()
        }

    /**
     * The path to the desired directory for downloading.
     */
    var downloadPath: String?
        get() = getProperty(QueleaPropertyKeys.downloadPathKey, "")
        set(path) {
            setProperty(QueleaPropertyKeys.downloadPathKey, path)
            write()
        }

    /**
     * Whether to automatically convert the recordings to MP3 files.
     * - true to convert to MP3
     * - false to just store recordings as WAV files.
     */
    var convertRecordings: Boolean
        get() = getProperty(QueleaPropertyKeys.convertMp3Key, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.convertMp3Key, value.toString())
            write()
        }

    /**
     * Whether the OO presentation should be always on top or not.
     * _Not user controlled, but useful for testing._
     */

    val oOPresOnTop: Boolean
        get() = getProperty(QueleaPropertyKeys.ooOntopKey, "true").toBoolean()

    /**
     * Sets the logo image location for persistent use
     *
     *
     *
     * @param location File location
     */
    fun setLogoImage(location: String?) {
        setProperty(QueleaPropertyKeys.logoImageLocationKey, location)
        write()
    }

    /**
     * The location of the logo image
     */
    val logoImageURI: String
        get() = "file:" + getProperty(QueleaPropertyKeys.logoImageLocationKey, "icons/logo default.png")

    /**
     * The port used for mobile lyrics display.
     */
    var mobLyricsPort: Int
        get() = getProperty(QueleaPropertyKeys.mobLyricsPortKey, "1111").toInt()
        set(port) {
            setProperty(QueleaPropertyKeys.mobLyricsPortKey, port.toString())
            write()
        }

    /**
     * Whether we should use mobile lyrics.
     */
    var useMobLyrics: Boolean
        get() = getProperty(QueleaPropertyKeys.useMobLyricsKey, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.useMobLyricsKey, value.toString())
            write()
        }

    /**
     * Whether we should set up remote control server.
     */
    var useRemoteControl: Boolean
        get() = getProperty(QueleaPropertyKeys.useRemoteControlKey, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.useRemoteControlKey, value.toString())
            write()
        }

    /**
     * The port used for remote control server.
     */
    var remoteControlPort: Int
        get() = try {
            getProperty(QueleaPropertyKeys.remoteControlPortKey, "1112").toInt()
        } catch (e: NumberFormatException) {
            1112
        }
        set(port) {
            setProperty(QueleaPropertyKeys.remoteControlPortKey, port.toString())
            write()
        }

    var remoteControlPassword: String?
        get() = getProperty(QueleaPropertyKeys.remoteControlPasswordKey, "quelea")
        set(text) {
            setProperty(QueleaPropertyKeys.remoteControlPasswordKey, text)
            write()
        }
    var planningCenterRefreshToken: String?
        get() = getProperty(QueleaPropertyKeys.planningCenterRefreshToken, null)
        set(text) {
            setProperty(QueleaPropertyKeys.planningCenterRefreshToken, text)
            write()
        }
    var smallSongTextPositionH: String?
        get() = getProperty(QueleaPropertyKeys.smallSongTextHPositionKey, "right")
        set(position) {
            setProperty(QueleaPropertyKeys.smallSongTextHPositionKey, position)
            write()
        }
    var smallSongTextPositionV: String?
        get() = getProperty(QueleaPropertyKeys.smallSongTextVPositionKey, "bottom")
        set(position) {
            setProperty(QueleaPropertyKeys.smallSongTextVPositionKey, position)
            write()
        }
    var smallSongTextSize: Double
        get() = getProperty(QueleaPropertyKeys.smallSongTextSizeKey, "0.1").toDouble()
        set(size) {
            setProperty(QueleaPropertyKeys.smallSongTextSizeKey, size.toString())
            write()
        }
    var smallBibleTextPositionH: String?
        get() = getProperty(QueleaPropertyKeys.smallBibleTextHPositionKey, "right")
        set(position) {
            setProperty(QueleaPropertyKeys.smallBibleTextHPositionKey, position)
            write()
        }
    var smallBibleTextPositionV: String?
        get() = getProperty(QueleaPropertyKeys.smallBibleTextVPositionKey, "bottom")
        set(position) {
            setProperty(QueleaPropertyKeys.smallBibleTextVPositionKey, position)
            write()
        }
    var smallBibleTextSize: Double
        get() = getProperty(QueleaPropertyKeys.smallBibleTextSizeKey, "0.1").toDouble()
        set(size) {
            setProperty(QueleaPropertyKeys.smallBibleTextSizeKey, size.toString())
            write()
        }
    var smallSongTextShow: Boolean
        get() = getProperty(QueleaPropertyKeys.showSmallSongTextKey, "true").toBoolean()
        set(show) {
            setProperty(QueleaPropertyKeys.showSmallSongTextKey, show.toString())
            write()
        }
    var smallBibleTextShow: Boolean
        get() = getProperty(QueleaPropertyKeys.showSmallBibleTextKey, "true").toBoolean()
        set(show) {
            setProperty(QueleaPropertyKeys.showSmallBibleTextKey, show.toString())
            write()
        }

    /**
     * Maximum number to show per slide
     * (depends on use.max.bible.verses)
     */

    var maxBibleVerses: Int
        get() = getProperty(QueleaPropertyKeys.maxBibleVersesKey, "5").toInt()
        set(number) {
            setProperty(QueleaPropertyKeys.maxBibleVersesKey, number.toString())
            write()
        }

    /**
     * Whether the max items is verses or words
     * - true if using maximum verses per slide
     */
    var bibleUsingMaxChars: Boolean
        get() = getProperty(QueleaPropertyKeys.useMaxBibleCharsKey, "true").toBoolean()
        set(useChars) {
            setProperty(QueleaPropertyKeys.useMaxBibleCharsKey, useChars.toString())
            write()
        }

    /**
     * The maximum number of characters allowed on any one line of bible text.
     */
    var maxBibleChars: Int
        get() = getProperty(QueleaPropertyKeys.maxBibleCharsKey, "80").toInt()
        set(maxChars) {
            setProperty(QueleaPropertyKeys.maxBibleCharsKey, maxChars.toString())
            write()
        }

    /**
     * The fade duration (ms) of the logo button text.
     */
    val logoFadeDuration: Int
        get() {
            var t = getProperty(QueleaPropertyKeys.logoFadeDurationKey, "")
            if (t == "") {
                t = "1000"
                setProperty(QueleaPropertyKeys.logoFadeDurationKey, t)
                write()
            }
            return t.toInt()
        }

    /**
     * The fade duration (ms) of the black button text.
     */
    val blackFadeDuration: Int
        get() {
            var t = getProperty(QueleaPropertyKeys.blackFadeDurationKey, "")
            if (t == "") {
                t = "1000"
                setProperty(QueleaPropertyKeys.blackFadeDurationKey, t)
                write()
            }
            return t.toInt()
        }

    /**
     * The fade duration (ms) of the clear button text.
     */
    val clearFadeDuration: Int
        get() {
            var t = getProperty(QueleaPropertyKeys.clearFadeDurationKey, "")
            if (t == "") {
                t = "1000"
                setProperty(QueleaPropertyKeys.clearFadeDurationKey, t)
                write()
            }
            return t.toInt()
        }

    /**
     * The Translate ID from the properties file
     */
    val translateClientID: String
        get() {
            var t = getProperty(QueleaPropertyKeys.translateClientIdKey, "")
            if (t == "") {
                t = "quelea-projection"
                setProperty(QueleaPropertyKeys.translateClientIdKey, t)
                write()
            }
            return t
        }


    /**
     * The Translate secret key from the properties file
     */
    val translateClientSecret: String
        get() {
            var t = getProperty(QueleaPropertyKeys.translateClientSecretKey, "")
            if (t == "") {
                t = "wk4+wd9YJkjIHmz2qwD1oR7pP9/kuHOL6OsaOKEi80U="
                setProperty(QueleaPropertyKeys.translateClientSecretKey, t)
                write()
            }
            return t
        }

    var clearStageWithMain: Boolean
        get() = getProperty(QueleaPropertyKeys.clearStageviewWithMainKey, "true").toBoolean()
        set(clear) {
            setProperty(QueleaPropertyKeys.clearStageviewWithMainKey, clear.toString())
            write()
        }

    /**
     * The directory used for storing countdown timers.
     */
    val timerDir: File
        get() = File(queleaUserHome, "timer")
    var songOverflow: Boolean
        get() = getProperty(QueleaPropertyKeys.songOverflowKey, "false").toBoolean()
        set(overflow) {
            setProperty(QueleaPropertyKeys.songOverflowKey, overflow.toString())
            write()
        }
    val autoDetectPort: Int
        get() = getProperty(QueleaPropertyKeys.autoDetectPortKey, "50015").toInt()
    val stageShowClock: Boolean
        get() = getProperty(QueleaPropertyKeys.stageShowClockKey, "true").toBoolean()
    var use24HourClock: Boolean
        get() = getProperty(QueleaPropertyKeys.use24hClockKey, "true").toBoolean()
        set(s24h) {
            setProperty(QueleaPropertyKeys.use24hClockKey, s24h.toString())
            write()
        }
    var bibleSplitVerses: Boolean
        get() = getProperty(QueleaPropertyKeys.splitBibleVersesKey, "false").toBoolean()
        set(selected) {
            setProperty(QueleaPropertyKeys.splitBibleVersesKey, selected.toString())
            write()
        }
    val lyricWidthBounds: Double
        get() = getProperty(QueleaPropertyKeys.lyricWidthBoundKey, "0.92").toDouble()
    val lyricHeightBounds: Double
        get() = getProperty(QueleaPropertyKeys.lyricHeightBoundKey, "0.9").toDouble()
    var defaultSongDBUpdate: Boolean
        get() = getProperty(QueleaPropertyKeys.defaultSongDbUpdateKey, "true").toBoolean()
        set(updateInDB) {
            setProperty(QueleaPropertyKeys.defaultSongDbUpdateKey, updateInDB.toString())
            write()
        }
    var showDBSongPreview: Boolean
        get() = getProperty(QueleaPropertyKeys.dbSongPreviewKey, "false").toBoolean()
        set(value) {
            setProperty(QueleaPropertyKeys.dbSongPreviewKey, value.toString())
        }
    var immediateSongDBPreview: Boolean
        get() = getProperty("db.song.immediate.preview", "false").toBoolean()
        set(value) {
            setProperty("db.song.immediate.preview", value.toString())
        }
    val webDisplayableRefreshRate: Int
        get() = getProperty(QueleaPropertyKeys.webRefreshRateKey, "500").toInt()
    val webProxyHost: String?
        get() = getProperty(QueleaPropertyKeys.webProxyHostKey, null)
    val webProxyPort: String?
        get() = getProperty(QueleaPropertyKeys.webProxyPortKey, null)
    val webProxyUser: String?
        get() = getProperty(QueleaPropertyKeys.webProxyUserKey, null)
    val webProxyPassword: String?
        get() = getProperty(QueleaPropertyKeys.webProxyPasswordKey, null)
    val churchCcliNum: String?
        get() = getProperty(QueleaPropertyKeys.churchCcliNumKey, null)

    /**
     * The directory used for storing notices.
     */
    val noticeDir: File
        get() = File(queleaUserHome, "notices")
    val newSongKeys: Array<String>
        get() = getProperty("new.song.keys", "Ctrl,Alt,N")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    val searchKeys: Array<String>
        get() = getProperty("search.keys", "Ctrl,L")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    val optionsKeys: Array<String>
        get() = getProperty("options.keys", "Shortcut,T")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    val liveTextKeys: Array<String>
        get() = getProperty("live.text.keys", "Shortcut,Shift,L")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    val logoKeys: Array<String>
        get() = getProperty("logo.keys", "F5")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    val blackKeys: Array<String>
        get() = getProperty("black.keys", "F6")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    val clearKeys: Array<String>
        get() = getProperty("clear.keys", "F7")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    val hideKeys: Array<String>
        get() = getProperty("hide.keys", "F8")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    val advanceKeys: Array<String>
        get() = getProperty("advance.keys", "Page Down")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    val previousKeys: Array<String>
        get() = getProperty("previous.keys", "Page Up")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    val noticesKeys: Array<String>
        get() = getProperty("notices.keys", "Ctrl,M")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    val scheduleFocusKeys: Array<String>
        get() = getProperty("schedule.focus.keys", "Ctrl,D")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()
    val bibleFocusKeys: Array<String>
        get() = getProperty("bible.focus.keys", "Ctrl,B")
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()

    /**
     * Whether fade should be used.
     */
    var useSlideTransition: Boolean
        get() = getProperty(QueleaPropertyKeys.useSlideTransitionKey, "false").toBoolean()
        set(useFade) {
            setProperty(QueleaPropertyKeys.useSlideTransitionKey, useFade.toString())
        }
    /**
     * The slide fade-in effect in duration (ms).
     */
    var slideTransitionInDuration: Int
        get() = getProperty(QueleaPropertyKeys.slideTransitionInDurationKey, "750").toInt()
        set(millis) {
            setProperty(QueleaPropertyKeys.slideTransitionInDurationKey, millis.toString())
        }

    /**
     * The slide fade-out effect in duration (ms).
     */
    var slideTransitionOutDuration: Int
        get() = getProperty(QueleaPropertyKeys.slideTransitionOutDurationKey, "400").toInt()
        set(millis) {
            setProperty(QueleaPropertyKeys.slideTransitionOutDurationKey, millis.toString())
        }
    var useDarkTheme: Boolean
        get() = getProperty(QueleaPropertyKeys.darkThemeKey, "false").toBoolean()
        set(useDarkTheme) {
            setProperty(QueleaPropertyKeys.darkThemeKey, useDarkTheme.toString())
        }

    companion object {
        @JvmField
        val VERSION = Version("2022.0", VersionType.CI)
        private var INSTANCE: QueleaProperties? = null

        @JvmStatic
        fun init(userHome: String?) {
            INSTANCE = QueleaProperties(userHome)
            try {
                if (!get()!!.propFile.exists()) {
                    get()!!.propFile.createNewFile()
                }
                Utils.getTextFromFile(get()!!.propFile.absolutePath, "")
                    .reader().use { reader ->
                        get()!!.load(reader)
                    }
            } catch (ex: IOException) { //Never mind.
            }
        }

        /**
         * Get the singleton instance of this class.
         *
         *
         *
         * @return the instance.
         */
        @JvmStatic
        fun get(): QueleaProperties? = INSTANCE
    }
}

/**
 * Get a color value as a string.
 *
 *
 *
 * @param color the color to get as a string.
 * @return the color as a string.
 */
fun Color.getStr(): String ="$red,$green,$blue"