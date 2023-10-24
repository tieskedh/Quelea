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

import com.dlsc.formsfx.model.structure.DoubleField
import com.dlsc.formsfx.model.structure.Field
import com.dlsc.formsfx.model.structure.StringField
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleTextControl
import com.dlsc.preferencesfx.model.Category
import com.dlsc.preferencesfx.model.Group
import com.dlsc.preferencesfx.model.Setting
import javafx.beans.property.*
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.languages.LanguageFile
import org.quelea.services.languages.LanguageFileManager
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.services.utils.QueleaPropertyKeys.additionalLineSpacingKey
import org.quelea.services.utils.QueleaPropertyKeys.advanceOnLiveKey
import org.quelea.services.utils.QueleaPropertyKeys.autoTranslateKey
import org.quelea.services.utils.QueleaPropertyKeys.autoplayVidKey
import org.quelea.services.utils.QueleaPropertyKeys.capitalFirstKey
import org.quelea.services.utils.QueleaPropertyKeys.checkUpdateKey
import org.quelea.services.utils.QueleaPropertyKeys.churchCcliNumKey
import org.quelea.services.utils.QueleaPropertyKeys.clearLiveOnRemoveKey
import org.quelea.services.utils.QueleaPropertyKeys.darkThemeKey
import org.quelea.services.utils.QueleaPropertyKeys.dbSongPreviewKey
import org.quelea.services.utils.QueleaPropertyKeys.defaultSongDbUpdateKey
import org.quelea.services.utils.QueleaPropertyKeys.defaultTranslationName
import org.quelea.services.utils.QueleaPropertyKeys.itemThemeOverrideKey
import org.quelea.services.utils.QueleaPropertyKeys.languageFileKey
import org.quelea.services.utils.QueleaPropertyKeys.linkPreviewAndLiveDividers
import org.quelea.services.utils.QueleaPropertyKeys.maxCharsKey
import org.quelea.services.utils.QueleaPropertyKeys.maxFontSizeKey
import org.quelea.services.utils.QueleaPropertyKeys.oneLineModeKey
import org.quelea.services.utils.QueleaPropertyKeys.previewOnImageChangeKey
import org.quelea.services.utils.QueleaPropertyKeys.scheduleEmbedMediaKey
import org.quelea.services.utils.QueleaPropertyKeys.showExtraLivePanelToolbarOptionsKey
import org.quelea.services.utils.QueleaPropertyKeys.showSmallBibleTextKey
import org.quelea.services.utils.QueleaPropertyKeys.showSmallSongTextKey
import org.quelea.services.utils.QueleaPropertyKeys.singleMonitorWarningKey
import org.quelea.services.utils.QueleaPropertyKeys.smallBibleTextHPositionKey
import org.quelea.services.utils.QueleaPropertyKeys.smallBibleTextSizeKey
import org.quelea.services.utils.QueleaPropertyKeys.smallBibleTextVPositionKey
import org.quelea.services.utils.QueleaPropertyKeys.smallSongTextHPositionKey
import org.quelea.services.utils.QueleaPropertyKeys.smallSongTextSizeKey
import org.quelea.services.utils.QueleaPropertyKeys.smallSongTextVPositionKey
import org.quelea.services.utils.QueleaPropertyKeys.songOverflowKey
import org.quelea.services.utils.QueleaPropertyKeys.thumbnailSizeKey
import org.quelea.services.utils.QueleaPropertyKeys.uniformFontSizeKey
import org.quelea.services.utils.QueleaPropertyKeys.useDefaultTranslation
import org.quelea.services.utils.QueleaPropertyKeys.useSlideTransitionKey
import org.quelea.services.utils.QueleaPropertyKeys.videoTabKey
import org.quelea.windows.options.PreferencesDialog.Companion.getPositionSelector
import org.quelea.windows.options.customprefs.PercentSliderControl
import java.util.*

/**
 * A panel where the general options in the program are set.
 *
 *
 *
 * @author Arvid
 */
class OptionsGeneralPanel internal constructor(private val bindings: HashMap<Field<*>, ObservableValue<Boolean>>) {
    private val showSmallSongProperty: BooleanProperty
    private val smallSongSizeSpinnerProperty: DoubleProperty
    private val smallSongSizeControllerField: DoubleField
    private val showSmallBibleProperty: BooleanProperty
    private val smallBibleSizeSpinnerProperty: DoubleProperty
    private val smallBibleSizeControllerField: DoubleField
    private val thumbnailSizeProperty: IntegerProperty
    private val maxFontSizeProperty: IntegerProperty
    private val additionalSpacingProperty: IntegerProperty
    private val maxCharsProperty: IntegerProperty
    private val languageItemsList: ObservableList<LanguageFile>
    private val languageSelectionProperty: ObjectProperty<LanguageFile>
    private val applicationThemeProperty: ObjectProperty<String>
    private val applicationThemeList: ObservableList<String>
    private val dbSongPreviewProperty: ObjectProperty<String>
    private val dbSongPreviewList: ObservableList<String>
    private val churchCcliNumProperty: StringProperty
    private val useDefaultTranslationProperty: BooleanProperty
    private val defaultTranslationNameProperty: StringProperty
    private val defaultTranslationNameField: StringField

    /**
     * Create the options general panel.
     *
     * @param bindings HashMap of bindings to setup after the dialog has been created
     */
    init {
        showSmallSongProperty = SimpleBooleanProperty(get().smallSongTextShow)
        smallSongSizeSpinnerProperty = SimpleDoubleProperty(get().smallSongTextSize)
        smallSongSizeControllerField = Field.ofDoubleType(smallSongSizeSpinnerProperty).render(
            PercentSliderControl(0.01, 0.5, 10)
        )
        showSmallBibleProperty = SimpleBooleanProperty(get().smallBibleTextShow)
        smallBibleSizeSpinnerProperty = SimpleDoubleProperty(get().smallBibleTextSize)
        smallBibleSizeControllerField = Field.ofDoubleType(smallBibleSizeSpinnerProperty).render(
            PercentSliderControl(0.01, 0.5, 10)
        )
        thumbnailSizeProperty = SimpleIntegerProperty(get().thumbnailSize)
        maxFontSizeProperty = SimpleIntegerProperty(get().maxFontSize.toInt())
        additionalSpacingProperty = SimpleIntegerProperty(get().additionalLineSpacing.toInt())
        maxCharsProperty = SimpleIntegerProperty(get().maxChars)
        languageItemsList = FXCollections.observableArrayList(LanguageFileManager.INSTANCE.languageFiles())
        languageSelectionProperty = SimpleObjectProperty(LanguageFileManager.INSTANCE.currentFile)
        applicationThemeList = FXCollections.observableArrayList(
            Arrays.asList(
                LabelGrabber.INSTANCE.getLabel("default.theme.label"),
                LabelGrabber.INSTANCE.getLabel("dark.theme.label")
            )
        )
        applicationThemeProperty = SimpleObjectProperty(LabelGrabber.INSTANCE.getLabel("default.theme.label"))
        dbSongPreviewList = FXCollections.observableArrayList(
            Arrays.asList(
                LabelGrabber.INSTANCE.getLabel("db.song.preview.label.control"),
                LabelGrabber.INSTANCE.getLabel("db.song.preview.label.databasepreview"),
                LabelGrabber.INSTANCE.getLabel("db.song.preview.label.previewpane")
            )
        )
        dbSongPreviewProperty = SimpleObjectProperty(LabelGrabber.INSTANCE.getLabel("db.song.preview.label.control"))
        churchCcliNumProperty = SimpleStringProperty(get().churchCcliNum)
        useDefaultTranslationProperty = SimpleBooleanProperty(get().useDefaultTranslation)
        defaultTranslationNameProperty = SimpleStringProperty(get().defaultTranslationName)
        defaultTranslationNameField = Field.ofStringType(defaultTranslationNameProperty).render(SimpleTextControl())
    }

    fun getGeneralTab(): Category {
        bindings[smallSongSizeControllerField] = showSmallSongProperty.not()
        bindings[smallBibleSizeControllerField] = showSmallBibleProperty.not()
        bindings[defaultTranslationNameField] = useDefaultTranslationProperty.not()
        return Category.of(
            LabelGrabber.INSTANCE.getLabel("general.options.heading"),
            ImageView(Image("file:icons/generalsettingsicon.png"))
        )
            .subCategories(
                Category.of(
                    LabelGrabber.INSTANCE.getLabel("interface.options.options"),
                    Group.of(
                        LabelGrabber.INSTANCE.getLabel("general.interface.options"),
                        Setting.of<LanguageFile>(
                            LabelGrabber.INSTANCE.getLabel("interface.language.label"),
                            languageItemsList,
                            languageSelectionProperty
                        ).customKey(languageFileKey),
                        Setting.of<String>(
                            LabelGrabber.INSTANCE.getLabel("interface.theme.label"),
                            applicationThemeList,
                            applicationThemeProperty
                        ).customKey(darkThemeKey),
                        Setting.of<String>(
                            LabelGrabber.INSTANCE.getLabel("db.song.preview.label"),
                            dbSongPreviewList,
                            dbSongPreviewProperty
                        ).customKey(dbSongPreviewKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("show.video.library.panel"),
                            SimpleBooleanProperty(get().displayVideoTab)
                        ).customKey(videoTabKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("show.extra.live.panel.toolbar.options.label"),
                            SimpleBooleanProperty(
                                get().showExtraLivePanelToolbarOptions
                            )
                        ).customKey(showExtraLivePanelToolbarOptionsKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("thumbnail.size.label"),
                            thumbnailSizeProperty,
                            100,
                            1000
                        ).customKey(thumbnailSizeKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("link.preview.and.live.dividers.label"),
                            SimpleBooleanProperty(get().linkPreviewAndLiveDividers)
                        ).customKey(linkPreviewAndLiveDividers)
                    ),
                    Group.of(
                        LabelGrabber.INSTANCE.getLabel("small.song.text.options"),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("show.small.song.text.label"),
                            showSmallSongProperty
                        ).customKey(showSmallSongTextKey),
                        getPositionSelector(
                            LabelGrabber.INSTANCE.getLabel("small.song.position.label"),
                            false,
                            get().smallSongTextPositionV,
                            showSmallSongProperty,
                            bindings
                        ).customKey(smallSongTextVPositionKey),
                        getPositionSelector(
                            LabelGrabber.INSTANCE.getLabel("small.song.position.label"),
                            true,
                            get().smallSongTextPositionH,
                            showSmallSongProperty,
                            bindings
                        ).customKey(smallSongTextHPositionKey),
                        Setting.of<DoubleField, DoubleProperty>(
                            LabelGrabber.INSTANCE.getLabel("small.song.size.label"),
                            smallSongSizeControllerField,
                            smallSongSizeSpinnerProperty
                        ).customKey(smallSongTextSizeKey)
                    ),
                    Group.of(
                        LabelGrabber.INSTANCE.getLabel("small.bible.text.options"),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("show.small.bible.text.label"),
                            showSmallBibleProperty
                        ).customKey(showSmallBibleTextKey),
                        getPositionSelector(
                            LabelGrabber.INSTANCE.getLabel("small.bible.position.label"),
                            false,
                            get().smallBibleTextPositionV,
                            showSmallBibleProperty,
                            bindings
                        ).customKey(smallBibleTextVPositionKey),
                        getPositionSelector(
                            LabelGrabber.INSTANCE.getLabel("small.bible.position.label"),
                            true,
                            get().smallBibleTextPositionH,
                            showSmallBibleProperty,
                            bindings
                        ).customKey(smallBibleTextHPositionKey),
                        Setting.of<DoubleField, DoubleProperty>(
                            LabelGrabber.INSTANCE.getLabel("small.bible.size.label"),
                            smallBibleSizeControllerField,
                            smallBibleSizeSpinnerProperty
                        ).customKey(smallBibleTextSizeKey)
                    )
                ),
                Category.of(
                    LabelGrabber.INSTANCE.getLabel("user.options.options"),
                    Group.of(
                        LabelGrabber.INSTANCE.getLabel("general.user.options"),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("check.for.update.label"),
                            SimpleBooleanProperty(get().checkUpdate())
                        ).customKey(checkUpdateKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("1.monitor.warn.label"),
                            SimpleBooleanProperty(get().showSingleMonitorWarning())
                        ).customKey(singleMonitorWarningKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("auto.translate.label"),
                            SimpleBooleanProperty(get().autoTranslate)
                        ).customKey(autoTranslateKey),
                        Setting.of(LabelGrabber.INSTANCE.getLabel("ccli.number.label"), churchCcliNumProperty)
                            .customKey(churchCcliNumKey)
                    ),
                    Group.of(
                        LabelGrabber.INSTANCE.getLabel("theme.options"),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("allow.item.theme.override.global"),
                            SimpleBooleanProperty(get().itemThemeOverride)
                        ).customKey(itemThemeOverrideKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("preview.on.image.change.label"),
                            SimpleBooleanProperty(get().previewOnImageUpdate)
                        ).customKey(previewOnImageChangeKey)
                    ),
                    Group.of(
                        LabelGrabber.INSTANCE.getLabel("schedule.options"),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("one.line.mode.label"),
                            SimpleBooleanProperty(get().oneLineMode)
                        ).customKey(oneLineModeKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("autoplay.vid.label"),
                            SimpleBooleanProperty(get().autoPlayVideo)
                        ).customKey(autoplayVidKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("advance.on.live.label"),
                            SimpleBooleanProperty(get().advanceOnLive)
                        ).customKey(advanceOnLiveKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("overflow.song.label"),
                            SimpleBooleanProperty(get().songOverflow)
                        ).customKey(songOverflowKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("copy.song.db.default"),
                            SimpleBooleanProperty(false)
                        ).customKey(defaultSongDbUpdateKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("clear.live.on.remove.schedule"),
                            SimpleBooleanProperty(get().clearLiveOnRemove)
                        ).customKey(clearLiveOnRemoveKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("embed.media.in.schedule"),
                            SimpleBooleanProperty(get().embedMediaInScheduleFile)
                        ).customKey(scheduleEmbedMediaKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("slide.transition.label"),
                            SimpleBooleanProperty(get().useSlideTransition)
                        ).customKey(useSlideTransitionKey)
                    )
                ),
                Category.of(
                    LabelGrabber.INSTANCE.getLabel("text.options.options"),
                    Group.of(
                        LabelGrabber.INSTANCE.getLabel("general.text.options"),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("capitalise.start.line.label"),
                            SimpleBooleanProperty(get().checkCapitalFirst())
                        ).customKey(capitalFirstKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("uniform.font.size.label"),
                            SimpleBooleanProperty(get().useUniformFontSize)
                        ).customKey(uniformFontSizeKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("max.font.size.label"),
                            maxFontSizeProperty,
                            12,
                            300
                        ).customKey(maxFontSizeKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("additional.line.spacing.label"),
                            additionalSpacingProperty,
                            0,
                            50
                        ).customKey(additionalLineSpacingKey),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("max.chars.line.label"),
                            maxCharsProperty,
                            10,
                            160
                        ).customKey(maxCharsKey)
                    ),
                    Group.of(
                        LabelGrabber.INSTANCE.getLabel("translation.text.options"),
                        Setting.of(
                            LabelGrabber.INSTANCE.getLabel("use.default.translation.label"),
                            useDefaultTranslationProperty
                        ).customKey(useDefaultTranslation),
                        Setting.of<StringField, StringProperty>(
                            LabelGrabber.INSTANCE.getLabel("translation.name.label"),
                            defaultTranslationNameField,
                            defaultTranslationNameProperty
                        ).customKey(defaultTranslationName)
                    )
                )
            ).expand()
    }
}
