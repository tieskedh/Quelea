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

import com.russhwolf.settings.*
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
import java.io.IOException
import java.util.Properties
import java.util.logging.Level

/**
 * Manages the properties specific to Quelea.
 *
 * @constructor Load the properties from the properties file.
 * @property propFile the properties file.
 * @author Michael
 */
class QueleaProperties private constructor(
    settings : Settings,
    private val _queleaUserHome: File
) : Settings by settings {

    /**
     * The Quelea home directory in the user's directory.
     */
    val queleaUserHome: File
        get() {
            val ret = _queleaUserHome
            if (!ret.exists()) {
                ret.mkdir()
            }
            return ret
        }

    /**
     * The languages file that for the GUI that should be used as specified in the properties file.
     */
    val languageFile: File
        get() = File("languages", getString(QueleaPropertyKeys.languageFileKey, "gb.lang"))


    val isDictionaryEnabled by boolean(QueleaPropertyKeys.enableDictKey, false)

    /**
     * The GUI - languages file that should be used as specified in the properties file.
     */
    val dictionary: Dictionary
        get() {
            val dict = getString(QueleaPropertyKeys.languageFileKey, "gb.lang")
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
        this[QueleaPropertyKeys.languageFileKey] = file
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
            getBoolean(QueleaPropertyKeys.videoTabKey, false)
        } catch (ex: Exception) {
            true
        }
        set(videoTab) {
            this[QueleaPropertyKeys.videoTabKey] = videoTab
        }

    /**
     * The scene info as stored from the last exit of Quelea (or some
     * default values if it doesn't exist in the properties file.)
     */
    val sceneInfo: SceneInfo?
        get() = try {
            val parts = getString(QueleaPropertyKeys.sceneInfoKey, "461,15,997,995,false")
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
                .log(Level.WARNING, "Invalid scene info: " + get(QueleaPropertyKeys.sceneInfoKey), ex)
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
        this[QueleaPropertyKeys.sceneInfoKey] = info.toString()
    }

    /**
     * the posiotion of the main splitpane divider:  0-1, or -1 if none is set.
     */
    var mainDivPos by double(QueleaPropertyKeys.mainDivposKey, -1.0)

    val elevantoClientId by string(QueleaPropertyKeys.elevantoClientIdKey, "91955")


    /** The library / schedule splitpane divider position property: 0-1, or -1 if none is set. */
    var libraryDivPos by double(QueleaPropertyKeys.libraryDivposKey, -1.0)

    /** The preview / live splitpane divider position property: 0-1, or -1 if none is set. */
    var prevLiveDivPos by double(QueleaPropertyKeys.preliveDivposKey, -1.0)

    /**  The canvas divider position property: 0-1, or -1 if none is set. */
    var canvasDivPos by double(QueleaPropertyKeys.canvasDivposKey, -1.0)

    /** The preview panel divider position property: 0-1, or -1 if none is set. */
    var previewDivPosKey: Double by double(QueleaPropertyKeys.previewDivposKey, -1.0)


    /**
     * A list of user chosen fonts to appear in the theme dialog.
     *
     *
     *
     * @return a list of user chosen fonts to appear in the theme dialog.
     */
    var chosenFonts: List<String>
        get() {
            val fontStr = getString(
                QueleaPropertyKeys.chosenFontsKey,
                "Arial|Liberation Sans|Noto Sans|Oxygen|Roboto|Vegur|Roboto Mono|Ubuntu Mono"
            )
            return fontStr.split("\\|".toRegex())
                .dropLastWhile { it.isEmpty() }
                .filter { it.javaTrim().isNotEmpty() }
        }
        set(fonts) {
            val fontsString = fonts.joinToString("|")
            this[QueleaPropertyKeys.chosenFontsKey]= fontsString
        }

    /**
     * Wheter the same font size should be used for each section in a
     * displayable
     *
     * - this can stop the sizes jumping all over the place
     * depending on how much text there is per slide.
     */
    var useUniformFontSize by boolean(QueleaPropertyKeys.uniformFontSizeKey, true)

    /** Wheter we should show verse numbers for bible passages. */
    var showVerseNumbers: Boolean by boolean(QueleaPropertyKeys.showVerseNumbersKey, true)

    /**
     * The colour to use for notice backgrounds.
     */
    var noticeBackgroundColour : Color
        get() = getColor(getString(QueleaPropertyKeys.noticeBackgroundColourKey, getStr(Color.BROWN)))
        set(colour) {
            this[QueleaPropertyKeys.noticeBackgroundColourKey] = getStr(colour)
        }

    /**
     * The position at which to display the notices.
     */
    var noticePosition: NoticePosition
        get() = if (
            getString(QueleaPropertyKeys.noticePositionKey, "Bottom")
                .equals("top", ignoreCase = true)
        ) NoticePosition.TOP else NoticePosition.BOTTOM
        set(position) {
            this[QueleaPropertyKeys.noticePositionKey] = position.text
        }

    /** The speed at which to display the notices. */
    var noticeSpeed by double(QueleaPropertyKeys.noticeSpeedKey, 10.0)

    /**
     * The last directory used in the general file chooser.
     *
     * @return the last directory used in the general file chooser.
     */
    val lastDirectory: File?
        get() {
            val path = getStringOrNull(QueleaPropertyKeys.lastDirectoryKey) ?: return null
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
        this[QueleaPropertyKeys.lastDirectoryKey]= directory.absolutePath
    }

    /**
     * The last directory used in the schedule file chooser.
     */
    val lastScheduleFileDirectory: File?
        get() {
            val path = getStringOrNull(QueleaPropertyKeys.lastSchedulefileDirectoryKey) ?: return null
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
    var embedMediaInScheduleFile by boolean(QueleaPropertyKeys.scheduleEmbedMediaKey, true)

    /**
     * Whether item themes can override the global theme.
     */
    var itemThemeOverride by boolean(QueleaPropertyKeys.itemThemeOverrideKey, false)

    /**
     * The currently selected global theme file.
     */
    var globalSongThemeFile: File?
        get() = getStringOrNull(QueleaPropertyKeys.globalSongThemeFileKey)
            .takeUnless { it.isNullOrEmpty() }
            ?.let(::File)
        set(file) {
            this[QueleaPropertyKeys.globalSongThemeFileKey] = file?.absolutePath.orEmpty()
        }

    /**
     * The currently selected global theme file.
     */
    var globalBibleThemeFile: File?
        get() = getStringOrNull(QueleaPropertyKeys.globalBibleThemeFileKey)
            .takeUnless { it.isNullOrEmpty() }
            ?.let(::File)
        set(file) {
            this[QueleaPropertyKeys.globalBibleThemeFileKey] = file?.absolutePath.orEmpty()
        }

    /**
     * Set the last directory used in the schedule file chooser.
     *
     * @param directory the last directory used in the schedule file chooser.
     */
    fun setLastScheduleFileDirectory(directory: File) {
        this[QueleaPropertyKeys.lastSchedulefileDirectoryKey] = directory.absolutePath
    }

    /**
     * The last directory used in the video file chooser.
     *
     * @return the last directory used in the video file chooser.
     */
    val lastVideoDirectory: File?
        get() {
            val path = getStringOrNull(QueleaPropertyKeys.lastVideoDirectoryKey) ?: return null
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
        this[QueleaPropertyKeys.lastVideoDirectoryKey] = directory.absolutePath
    }

    /**
     * Whether to auto-play videos after they have been set in live view.
     * true if auto-play is enabled, false otherwise.
     */

    var autoPlayVideo by boolean(QueleaPropertyKeys.autoplayVidKey, false)

    /**
     * Whether to use Java FX rendering for video playback with VLC.
     * This approach is totally cross-platform capable.
     *
     * true if should use java fx for VLC Rendering, false otherwise
     */
    var useJavaFXforVLCRendering by boolean(QueleaPropertyKeys.useVlcJavafxRenderingKey, false)

    /**
     * The font size at which to display the notices.
     */
    var noticeFontSize by double(QueleaPropertyKeys.noticeFontSizeKey, 50.0)

    /**
     * Whether we should attempt to fetch translations automatically.
     */
    var autoTranslate by boolean(QueleaPropertyKeys.autoTranslateKey, true)

    /**
     * The maximum font size used by text displayables.
     */
    var maxFontSize by double(QueleaPropertyKeys.maxFontSizeKey, 1000.0)

    /**
     * The additional line spacing (in pixels) to be used between each line.
     */
    var additionalLineSpacing by double(QueleaPropertyKeys.additionalLineSpacingKey, 10.0)

    /**
     * The thumbnail size.
     */
    var thumbnailSize by int(QueleaPropertyKeys.thumbnailSizeKey, 200)

    var planningCentrePrevDays by int(QueleaPropertyKeys.planningCentrePrevDaysKey, 31)

    var useDefaultTranslation by boolean(QueleaPropertyKeys.useDefaultTranslation, false)

    var defaultTranslationName by string(QueleaPropertyKeys.defaultTranslationName, "")

    /**
     * Wheter the extra live panel toolbar options setting should be shown. Hidden by default.
     */
    var showExtraLivePanelToolbarOptions by boolean(
        QueleaPropertyKeys.showExtraLivePanelToolbarOptionsKey, false
    )

    /**
     * Whether the preview and live dividers should be linked. eg move together
     * true if the preview and live dividers should be linked, else false
     */
    val linkPreviewAndLiveDividers by boolean(QueleaPropertyKeys.linkPreviewAndLiveDividers, true)

    /**
     * Should also remove from live view, the alternative is waiting until something replaces it.
     * - true: should remove from liveview
     * - false: wait until something replaces it
     */
    var clearLiveOnRemove by boolean(QueleaPropertyKeys.clearLiveOnRemoveKey, true)

    /**
     * The location of Quelea's Facebook page.
     */
    val facebookPageLocation: String by string(
        QueleaPropertyKeys.facebookPageKey, "http://www.facebook.com/quelea.projection"
    )

    /**
     * The location of Quelea's Facebook page.
     */
    val wikiPageLocation: String by string(
        QueleaPropertyKeys.wikiPageKey, "http://quelea.org/wiki/index.php/Main_Page"
    )



    /**
     * The user's turbo db exe converter file.
     */
    val turboDBExe: File
        get() = File(queleaUserHome, "TdbDataX.exe")

    val translationFontSizeOffset by int(QueleaPropertyKeys.translationFontSizeOffsetKey, 3)

    /**
     * The font to use for stage text.
     */
    var stageTextFont: String by string(QueleaPropertyKeys.stageFontKey, "SansSerif")

    /**
     * The alignment of the text on stage view.
     */
    val stageTextAlignment: String
        get() = TextAlignment.parse(
            getString(QueleaPropertyKeys.stageTextAlignmentKey, "LEFT")
        ).toFriendlyString()

    /**
     * The alignment of the text on stage view.
     */
    fun setStageTextAlignment(alignment: TextAlignment) {
        this[QueleaPropertyKeys.stageTextAlignmentKey] = alignment.toString()
    }

    /**
     * Whether we should display the chords in stage view.
     */
    var showChords: Boolean by boolean(QueleaPropertyKeys.stageShowChordsKey, true)

    /**
     * Determine whether we should phone home at startup with anonymous
     * information. Simply put phonehome=false in the properties file to disable
     * phonehome.
     *
     *
     * @return true if we should phone home, false otherwise.
     */
    val phoneHome: Boolean
        get() = getBoolean(QueleaPropertyKeys.phonehomeKey, true)

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
    val scheduleExtension by string(QueleaPropertyKeys.queleaScheduleExtensionKey, "qsch")

    /**
     * The extension used for quelea song packs.
     */
    val songPackExtension by string(QueleaPropertyKeys.queleaSongpackExtensionKey, "qsp")

    /**
     * The control screen number.
     * This is the screen that the main Quelea operator window will be displayed on.
     */
    var controlScreen by int(QueleaPropertyKeys.controlScreenKey, 0)

    /**
     * Whether one line mode should be enabled.
     */
    var oneLineMode by boolean(QueleaPropertyKeys.oneLineModeKey, false)

    /**
     * Whether texts have shadows.
     */
    var textShadow by boolean(QueleaPropertyKeys.textShadowKey, false)

    /**
     * The number of the projector screen: the screen that the
     * projected output will be displayed on.
     *
     * @return the projector screen number.
     */
    var projectorScreen: Int by int(QueleaPropertyKeys.projectorScreenKey, 1)

    /**
     * Whether the projection screen automatically should be moved to
     * a recently inserted monitor.
     *
     *
     *
     * @return true if the projector screen should be moved, false otherwise.
     */
    var useAutoExtend: Boolean by boolean(QueleaPropertyKeys.useAutoExtendKey, false)

    /**
     * The maximum number of characters allowed on any one line of projected
     * text. If the line is longer than this, it will be split up intelligently.
     *
     */
    var maxChars: Int by int(QueleaPropertyKeys.maxCharsKey, 30)

    /**
     * The custom projector co-ordinates.
     */
    var projectorCoords: Bounds
        get() {
            val prop = getString(QueleaPropertyKeys.projectorCoordsKey, "0,0,0,0").javaTrim()
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
            this[QueleaPropertyKeys.projectorCoordsKey] = rectStr
        }

    private fun Bounds.toCommaString() = "${minX.toInt()},${minY.toInt()},${width.toInt()},${height.toInt()}"

    private fun updateCoord(
        x : String? = null,
        y : String? = null,
        width:  String? = null,
        height: String? = null,
        key : String
    ){
        val prop = getString(key, "0,0,0,0").javaTrim()
            .split(",".toRegex()).dropLastWhile { it.isEmpty() }
        val rectStr = (
                (x ?: prop[0])
                        + "," + (y ?: prop[1])
                        + "," + (width ?: prop[2])
                        + "," + (height ?: prop[3]))
        this[key]= rectStr
    }

    private fun setProjectorCoord(
        x : String? = null,
        y : String? = null,
        width:  String? = null,
        height: String? = null
    ) = updateCoord(x, y, width, height, QueleaPropertyKeys.projectorCoordsKey)

    fun setXProjectorCoord(x: String) = setProjectorCoord(x = x)
    fun setYProjectorCoord(y: String) = setProjectorCoord(y=y)
    fun setWidthProjectorCoord(width: String) = setProjectorCoord(width=width)
    fun setHeightProjectorCoord(height: String) = setProjectorCoord(height=height)

    private fun setStageCoord(
        x : String? = null,
        y : String? = null,
        width:  String? = null,
        height: String? = null
    ) = updateCoord(x,y,width,height, QueleaPropertyKeys.stageCoordsKey)

    fun setXStageCoord(x: String) = setStageCoord(x=x)
    fun setYStageCoord(y: String) = setStageCoord(y=y)
    fun setWidthStageCoord(width: String) = setStageCoord(width=width)
    fun setHeightStageCoord(height: String) = setStageCoord(height=height)


    /**
     * Wheter the projector mode is set to manual co-ordinates or a screen
     * number.
     * - true = it's set to manual co-ordinates
     * - false = it's set to a screen number.
     */
    val isProjectorModeCoords : Boolean
        get() = "coords" == getStringOrNull(QueleaPropertyKeys.projectorModeKey)

    /**
     * Set the projector mode to be manual co-ordinates.
     */
    fun setProjectorModeCoords() {
        this[ QueleaPropertyKeys.projectorModeKey]= "coords"
    }

    /**
     * Set the projector mode to be a screen number.
     */
    fun setProjectorModeScreen() {
        this[QueleaPropertyKeys.projectorModeKey] = "screen"
    }

    /**
     * The number of the stage screen. This is the screen that the projected
     * output will be displayed on.
     */
    var stageScreen by int(QueleaPropertyKeys.stageScreenKey, -1)

    /**
     * The custom stage screen co-ordinates.
     */
    var stageCoords: Bounds
        get() {
            val prop = getString(QueleaPropertyKeys.stageCoordsKey, "0,0,0,0").javaTrim()
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
            this[QueleaPropertyKeys.stageCoordsKey]=rectStr
        }

    /**
     * Whether the stage mode is set to manual co-ordinates or a screen
     * number.
     * - true = it's set to manual co-ordinates
     * - false =  it's set to a screen number.
     */
    val isStageModeCoords: Boolean
        get() = "coords" == getStringOrNull(QueleaPropertyKeys.stageModeKey)

    /**
     * Set the stage mode to be manual co-ordinates.
     */
    fun setStageModeCoords() {
        this[QueleaPropertyKeys.stageModeKey] = "coords"
    }

    /**
     * Set the stage mode to be a screen number.
     */
    fun setStageModeScreen() {
        this[QueleaPropertyKeys.stageModeKey] = "screen"
    }

    /**
     * The minimum number of lines that should be displayed on each page.
     * This purely applies to font sizes, the font will be adjusted so this
     * amount of lines can fit on. This stops small lines becoming huge in the
     * preview window rather than displaying normally.
     */

    var minLines by int(QueleaPropertyKeys.minLinesKey, 10)

    /**
     * Determine whether the single monitor warning should be shown (this warns
     * the user they only have one monitor installed.)
     *
     *
     *
     * @return true if the warning should be shown, false otherwise.
     */
    fun showSingleMonitorWarning() =
        getBoolean(QueleaPropertyKeys.singleMonitorWarningKey, true)

    /**
     * Set whether the single monitor warning should be shown.
     *
     *
     *
     * @param val true if the warning should be shown, false otherwise.
     */
    fun setSingleMonitorWarning(value: Boolean) {
        this[QueleaPropertyKeys.singleMonitorWarningKey] = value
    }

    /**
     * The URL to download Quelea.
     */
    val downloadLocation  = "https://github.com/quelea-projection/Quelea/releases/"

    /**
     * The URL to the Quelea website.
     */
    val websiteLocation by string(QueleaPropertyKeys.websiteLocationKey, "http://www.quelea.org/")

    /**
     * The URL to the Quelea discussion forum.
     */
    val discussLocation by string(
        QueleaPropertyKeys.discussLocationKey, "https://quelea.discourse.group/"
    )

    /**
     * The URL to the Quelea feedback form.
     */
    val feedbackLocation: String by string(
        QueleaPropertyKeys.feedbackLocationKey, "https://quelea.org/feedback/"
    )

    /**
     * The URL used for checking the latest version.
     */

    val updateURL: String = "https://quelea-projection.github.io/changelog"


    /**
     * Whether we should check for updates each time the program
     * starts.
     */
    fun checkUpdate() = getBoolean(QueleaPropertyKeys.checkUpdateKey, true)

    /**
     * Set whether we should check for updates each time the program starts.
     */
    fun setCheckUpdate(value: Boolean) {
        this[QueleaPropertyKeys.checkUpdateKey] = value
    }


    /**
     * Determine whether the first letter of all displayed lines should be a
     * capital.
     *
     *
     *
     * @return true if it should be a capital, false otherwise.
     */
    fun checkCapitalFirst() : Boolean = getBoolean(QueleaPropertyKeys.capitalFirstKey, false)

    /**
     * Set whether the first letter of all displayed lines should be a capital.
     *
     *
     *
     * @param val true if it should be a capital, false otherwise.
     */
    fun setCapitalFirst(value: Boolean) {
        this[QueleaPropertyKeys.capitalFirstKey] =value.toString()
    }

    /**
     * Determine whether the song info text should be displayed.
     *
     *
     *
     * @return true if it should be a displayed, false otherwise.
     */
    fun checkDisplaySongInfoText(): Boolean =
        getBoolean(QueleaPropertyKeys.displaySonginfotextKey, true)

    /**
     * Set whether the song info text should be displayed.
     *
     *
     *
     * @param val true if it should be displayed, false otherwise.
     */
    fun setDisplaySongInfoText(value: Boolean) {
        this[QueleaPropertyKeys.displaySonginfotextKey] = value
    }

    /**
     * The default bible to use.
     */
    val defaultBible: String? by nullableString(QueleaPropertyKeys.defaultBibleKey)

    /**
     * The default bible.
     */
    fun setDefaultBible(bible: Bible) {
        this[QueleaPropertyKeys.defaultBibleKey] = bible.name
    }

    /**
     * The colour used to display chords in stage view.
     */
    var stageChordColor: Color
        get() = getColor(getString(QueleaPropertyKeys.stageChordColorKey, "200,200,200"))
        set(color) {
            this[QueleaPropertyKeys.stageChordColorKey] = getStr(color)
        }

    /**
     * The colour used to display chords in stage view.
     */
    val textBackgroundColor: Color
        get() = getColor(getStringOrNull(QueleaPropertyKeys.lyricsTextBackgroundColor)!!)

    /**
     * Whether to advance the schedule item when the current item is sent live.
     */
    val textBackgroundEnable: Boolean by boolean(
        QueleaPropertyKeys.lyricsTextBackgroundEnable, false
    )

    /**
     * The colour used to display lyrics in stage view.
     */
    var stageLyricsColor: Color
        get() = getColor(getString(QueleaPropertyKeys.stageLyricsColorKey, "255,255,255"))
        set(color) {
            this[QueleaPropertyKeys.stageLyricsColorKey] = getStr(color)
        }
    /**
     * The colour used for the background in stage view.
     */
    var stageBackgroundColor: Color
        get() = getColor(getString(QueleaPropertyKeys.stageBackgroundColorKey, "0,0,0"))
        set(color) {
            this[QueleaPropertyKeys.stageBackgroundColorKey] = getStr(color)
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
     * @param this@getStr the color to get as a string.
     * @return the color as a string.
     */
    fun getStr(color: Color) =color.run { "$red,$green,$blue" }


    /**
     * The colour used to signify an active list.
     */
    val activeSelectionColor: Color
        get() = getColor(getString(QueleaPropertyKeys.activeSelectionColorKey, "30,160,225"))

    /**
     * The colour used to signify an active list.
     */
    val inactiveSelectionColor: Color
        get() = getColor(getString(QueleaPropertyKeys.inactiveSelectionColorKey, "150,150,150"))

    /**
     * The thickness (px) of the outline to use for displaying the text.
     */
    var outlineThickness by int(QueleaPropertyKeys.outlineThicknessKey, 2)

    /**
     * The notice box height (px).
     */
    var noticeBoxHeight by int(QueleaPropertyKeys.noticeBoxHeightKey, 40)

    /**
     * The notice box speed.
     */
    var noticeBoxSpeed by int(QueleaPropertyKeys.noticeBoxSpeedKey, 8)

    /**
     * Words auto-capitalized by the song importer when deciding how to un-caps-lock a line of text.
     * seperated by commas  in the properties file
     */
    val godWords: Array<String>
        get() = getString(
            QueleaPropertyKeys.godWordsKey,
            "god,God,jesus,Jesus,christ,Christ,you,You,he,He,lamb,Lamb,"
                    + "lord,Lord,him,Him,son,Son,i,I,his,His,your,Your,king,King,"
                    + "saviour,Saviour,savior,Savior,majesty,Majesty,alpha,Alpha,omega,Omega"
        ) //Yeah.. default testing properties.
            .javaTrim().split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    /**
     * Determine whether to advance the schedule item when the current item is sent live.
     */
    var advanceOnLive by boolean(QueleaPropertyKeys.advanceOnLiveKey, false)

    /**
     * Whether to preview the schedule item when the background image
     * has been updated.
     */
    var previewOnImageUpdate by boolean(QueleaPropertyKeys.previewOnImageChangeKey, false)

    /**
     * Whether to use openoffice for presentations.
     * - true if we should use openoffice
     * - false if we should use basic POI images
     */
    var useOO by boolean(QueleaPropertyKeys.useOoKey, false)

    /**
     * The path to the openoffice installation on this machine.
     */
    var oOPath by string(QueleaPropertyKeys.ooPathKey, "")

    /**
     * Whether to use PowerPoint for presentations.
     * - true if we should use PowerPoint
     * - false if we should just use the basic POI images or openoffice.
     */
    var usePP by boolean(QueleaPropertyKeys.usePpKey, false)

    /**
     * The path to the PowerPoint installation on this machine.
     */
    var pPPath by string(QueleaPropertyKeys.ppPathKey, "")

    /**
     * The path to the desired directory for recordings.
     */
    var recordingsPath by string(QueleaPropertyKeys.recPathKey, "")

    /**
     * The path to the desired directory for downloading.
     */
    var downloadPath by string(QueleaPropertyKeys.downloadPathKey, "")

    /**
     * Whether to automatically convert the recordings to MP3 files.
     * - true to convert to MP3
     * - false to just store recordings as WAV files.
     */
    var convertRecordings by boolean(QueleaPropertyKeys.convertMp3Key, false)

    /**
     * Whether the OO presentation should be always on top or not.
     * _Not user controlled, but useful for testing._
     */

    val oOPresOnTop by boolean(QueleaPropertyKeys.ooOntopKey, true)

    /**
     * Sets the logo image location for persistent use
     *
     *
     *
     * @param location File location
     */
    fun setLogoImage(location: String?) {
        this[QueleaPropertyKeys.logoImageLocationKey] = location
    }

    /**
     * The location of the logo image
     */
    val logoImageURI: String
        get() = "file:" + getString(QueleaPropertyKeys.logoImageLocationKey, "icons/logo default.png")

    /**
     * The port used for mobile lyrics display.
     */
    var mobLyricsPort by int(QueleaPropertyKeys.mobLyricsPortKey, 1111)

    /**
     * Whether we should use mobile lyrics.
     */
    var useMobLyrics by boolean(QueleaPropertyKeys.useMobLyricsKey, false)

    /**
     * Whether we should set up remote control server.
     */
    var useRemoteControl by boolean(QueleaPropertyKeys.useRemoteControlKey, false)

    /**
     * The port used for remote control server.
     */
    var remoteControlPort: Int
        get() = try {
            getInt(QueleaPropertyKeys.remoteControlPortKey, 1112)
        } catch (e: NumberFormatException) {
            1112
        }
        set(port) {
            this[QueleaPropertyKeys.remoteControlPortKey] = port
        }

    var remoteControlPassword by string(QueleaPropertyKeys.remoteControlPasswordKey, "quelea")

    var planningCenterRefreshToken by nullableString(QueleaPropertyKeys.planningCenterRefreshToken)

    var smallSongTextPositionH by string(QueleaPropertyKeys.smallSongTextHPositionKey, "right")
    var smallSongTextPositionV by string(QueleaPropertyKeys.smallSongTextVPositionKey, "bottom")
    var smallSongTextSize by double(QueleaPropertyKeys.smallSongTextSizeKey, 0.1)
    var smallBibleTextPositionH by string(QueleaPropertyKeys.smallBibleTextHPositionKey, "right")
    var smallBibleTextPositionV by string(QueleaPropertyKeys.smallBibleTextVPositionKey, "bottom")
    var smallBibleTextSize by double(QueleaPropertyKeys.smallBibleTextSizeKey, 0.1)
    var smallSongTextShow by boolean(QueleaPropertyKeys.showSmallSongTextKey, true)
    var smallBibleTextShow by boolean(QueleaPropertyKeys.showSmallBibleTextKey, true)

    /**
     * Maximum number to show per slide
     * (depends on use.max.bible.verses)
     */

    var maxBibleVerses by int(QueleaPropertyKeys.maxBibleVersesKey, 5)

    /**
     * Whether the max items is verses or words
     * - true if using maximum verses per slide
     */
    var bibleUsingMaxChars by boolean(QueleaPropertyKeys.useMaxBibleCharsKey, true)

    /**
     * The maximum number of characters allowed on any one line of bible text.
     */
    var maxBibleChars by int(QueleaPropertyKeys.maxBibleCharsKey, 80)


    private fun getFadeDuration(key: String): Int {
        var t = getString(key, "")
        if (t == "") {
            t = "1000"
            this[key] = t
        }
        return t.toInt()
    }

    /**
     * The fade duration (ms) of the logo button text.
     */
    val logoFadeDuration: Int
        get() = getFadeDuration(QueleaPropertyKeys.logoFadeDurationKey)

    /**
     * The fade duration (ms) of the black button text.
     */
    val blackFadeDuration: Int
        get() = getFadeDuration(QueleaPropertyKeys.blackFadeDurationKey)

    /**
     * The fade duration (ms) of the clear button text.
     */
    val clearFadeDuration: Int
        get() = getFadeDuration(QueleaPropertyKeys.clearFadeDurationKey)

    /**
     * The Translate ID from the properties file
     */
    val translateClientID: String
        get() {
            var t = getString(QueleaPropertyKeys.translateClientIdKey, "")
            if (t == "") {
                t = "quelea-projection"
                this[QueleaPropertyKeys.translateClientIdKey] = t
            }
            return t
        }


    /**
     * The Translate secret key from the properties file
     */
    val translateClientSecret: String
        get() {
            var t = getString(QueleaPropertyKeys.translateClientSecretKey, "")
            if (t == "") {
                t = "wk4+wd9YJkjIHmz2qwD1oR7pP9/kuHOL6OsaOKEi80U="
                this[QueleaPropertyKeys.translateClientSecretKey] = t
            }
            return t
        }

    var clearStageWithMain by boolean(QueleaPropertyKeys.clearStageviewWithMainKey, true)

    /**
     * The directory used for storing countdown timers.
     */
    val timerDir: File
        get() = File(queleaUserHome, "timer")

    var songOverflow by boolean(QueleaPropertyKeys.songOverflowKey, false)

    val autoDetectPort by int(QueleaPropertyKeys.autoDetectPortKey, 50015)
    val stageShowClock by boolean(QueleaPropertyKeys.stageShowClockKey, true)
    var use24HourClock by boolean(QueleaPropertyKeys.use24hClockKey, true)
    var bibleSplitVerses by boolean(QueleaPropertyKeys.splitBibleVersesKey, false)
    val lyricWidthBounds by double(QueleaPropertyKeys.lyricWidthBoundKey, 0.92)
    val lyricHeightBounds by double(QueleaPropertyKeys.lyricHeightBoundKey, 0.9)
    var defaultSongDBUpdate by boolean(QueleaPropertyKeys.defaultSongDbUpdateKey, true)
    var showDBSongPreview by boolean(QueleaPropertyKeys.dbSongPreviewKey, false)

    var immediateSongDBPreview by boolean("db.song.immediate.preview",false)

    val webDisplayableRefreshRate by int(QueleaPropertyKeys.webRefreshRateKey, 500)
    val webProxyHost by nullableString(QueleaPropertyKeys.webProxyHostKey)
    val webProxyPort by nullableString(QueleaPropertyKeys.webProxyPortKey)
    val webProxyUser by nullableString(QueleaPropertyKeys.webProxyUserKey)
    val webProxyPassword by nullableString(QueleaPropertyKeys.webProxyPasswordKey)
    val churchCcliNum by nullableString(QueleaPropertyKeys.churchCcliNumKey)

    /**
     * The directory used for storing notices.
     */
    val noticeDir: File
        get() = File(queleaUserHome, "notices")
    private fun getKeyBinding(key : String, default: String) =
        getString(key, default)
            .split(",".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()


    val newSongKeys: Array<String>
        get() = getKeyBinding("new.song.keys", "Ctrl,Alt,N")

    val searchKeys: Array<String>
        get() = getKeyBinding("search.keys", "Ctrl,L")

    val optionsKeys: Array<String>
        get() = getKeyBinding("options.keys", "Shortcut,T")

    val liveTextKeys: Array<String>
        get() = getKeyBinding("live.text.keys", "Shortcut,Shift,L")

    val logoKeys: Array<String>
        get() = getKeyBinding("logo.keys", "F5")

    val blackKeys: Array<String>
        get() =getKeyBinding("black.keys", "F6")

    val clearKeys: Array<String>
        get() = getKeyBinding("clear.keys", "F7")

    val hideKeys: Array<String>
        get() = getKeyBinding("hide.keys", "F8")

    val advanceKeys: Array<String>
        get() = getKeyBinding("advance.keys", "Page Down")

    val previousKeys: Array<String>
        get() = getKeyBinding("previous.keys", "Page Up")

    val noticesKeys: Array<String>
        get() = getKeyBinding("notices.keys", "Ctrl,M")

    val scheduleFocusKeys: Array<String>
        get() = getKeyBinding("schedule.focus.keys", "Ctrl,D")

    val bibleFocusKeys: Array<String>
        get() = getKeyBinding("bible.focus.keys", "Ctrl,B")

    /**
     * Whether fade should be used.
     */
    var useSlideTransition by boolean(QueleaPropertyKeys.useSlideTransitionKey, false)
    /**
     * The slide fade-in effect in duration (ms).
     */
    var slideTransitionInDuration by int(QueleaPropertyKeys.slideTransitionInDurationKey, 750)

    /**
     * The slide fade-out effect in duration (ms).
     */
    var slideTransitionOutDuration by int(QueleaPropertyKeys.slideTransitionOutDurationKey, 400)
    var useDarkTheme by boolean(QueleaPropertyKeys.darkThemeKey, false)


    fun setProperty(key : String, value : String) {
        this[key] = value
    }
    fun getProperty(key: String): String? = getStringOrNull(key)


    companion object {
        @JvmField
        val VERSION = Version("2022.0", VersionType.CI)
        private var INSTANCE: QueleaProperties? = null

        private fun resolveQueleaDir(userHome: String?): File {
            val home =  userHome
                .takeUnless { it.isNullOrEmpty() }
                ?: requireNotNull(System.getProperty("user.home")){
                    "user.home is not set"
                }
            return File(File(home), ".quelea")
        }

        @JvmStatic
        fun init(userHome: String?) {
            val queleaUserHome = resolveQueleaDir(userHome)
            queleaUserHome.createNewFile()

            val settings = File(queleaUserHome, "quelea.properties")
                .loadPropertySettings()

            INSTANCE = QueleaProperties(
                settings,
                queleaUserHome
            )
        }

        @JvmStatic
        fun init(userHome: String?, settings: Settings) {
            val queleaUserHome = resolveQueleaDir(userHome)
            queleaUserHome.createNewFile()

            INSTANCE = QueleaProperties(
                settings,
                queleaUserHome
            )
        }
        /**
         * Get the singleton instance of this class.
         *
         *
         *
         * @return the instance.
         */
        @JvmStatic
        fun get(): QueleaProperties = INSTANCE!!
    }
}

fun File.loadPropertySettings(): Settings {
    val props = Properties()
    try {
        createNewFile()
        readText().reader().use(props::load)
    } catch (ex: IOException) { /* no-op */ }

    return PropertiesSettings(props){props->
        try {
            writer().use {
                props.sorted().store(it, "Auto save")
            }
        } catch (e : IOException) {
//            LOGGER.log(Level.WARNING, "Couldn't store properties", ex);
        }
    }
}

private fun Properties.sorted(): Properties {
    val sortedMap = toSortedMap(compareBy { it.toString() })
    val sortedProps = Properties()
    sortedProps.putAll(sortedMap)
    return sortedProps
}