package org.quelea.windows.options.customprefs

import com.dlsc.formsfx.model.structure.StringField
import com.dlsc.preferencesfx.formsfx.view.controls.SimpleControl
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import javafx.collections.ObservableList
import javafx.embed.swing.SwingFXUtils
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import org.quelea.services.languages.LabelGrabber
import org.quelea.services.utils.FileFilters
import org.quelea.services.utils.LoggerUtils
import org.quelea.services.utils.QueleaProperties.Companion.get
import org.quelea.utils.DesktopApi
import org.quelea.windows.main.QueleaApp
import tornadofx.*
import java.awt.Desktop
import java.awt.image.BufferedImage
import java.io.IOException
import java.net.*
import java.util.*
import java.util.logging.Level
import javax.imageio.ImageIO

class MobileServerPreference(private val isLyrics: Boolean) : SimpleControl<StringField, StackPane>() {
    /**
     * - The fieldLabel is the container that displays the label property of
     * the field.
     * - The editableField allows users to modify the field's value.
     * - The readOnlyLabel displays the field's value if it is not editable.
     */
    private lateinit var editableField: TextField
    private var qrImage: BufferedImage? = null

    /**
     * {@inheritDoc}
     */
    override fun initializeParts() {
        super.initializeParts()
        node = StackPane()
        node.styleClass.add("simple-text-control")
        editableField = TextField(field.value).apply {
            promptText = field!!.placeholderProperty().value
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun layoutParts() {
        val url = if (isLyrics) mLURL else rCURL

        node.apply {
            alignment = Pos.CENTER_LEFT

            hbox(10) {
                alignment = Pos.CENTER_LEFT

                vbox(10) {
                    hboxConstraints { hGrow = Priority.ALWAYS }
                    alignment = Pos.CENTER_LEFT

                    editableField.addTo(this)
                    //mobile url label
                    text(url) {
                        if (Desktop.isDesktopSupported() && url.startsWith("http")) {
                            cursor = Cursor.HAND
                            fill = Color.BLUE
                            style = "-fx-underline: true;"
                            setOnMouseClicked { DesktopApi.browse(url) }
                        }
                    }
                }

                stackpane {
                    alignment = Pos.CENTER_LEFT

                    if (isLyrics && LabelGrabber.INSTANCE.getLabel("not.started.label") !in mLURL) {
                        imageview(getQRImage()) {
                            stackpaneConstraints { alignment = Pos.CENTER_LEFT }
                            fitHeight = 100.0
                            fitWidth = 100.0
                        }

                        val saveButton = button(LabelGrabber.INSTANCE.getLabel("save.qr.code.text")) {
                            stackpaneConstraints { alignment = Pos.CENTER_LEFT }
                            opacity = 0.0

                            setOnAction {
                                val file = FileChooser().apply {
                                    if (get().lastDirectory != null) {
                                        initialDirectory = get().lastDirectory
                                    }
                                    extensionFilters.add(FileFilters.PNG)
                                    title = LabelGrabber.INSTANCE.getLabel("save.qr.code.text")
                                }.showSaveDialog(QueleaApp.get().mainWindow)

                                if (file != null) {
                                    get().setLastDirectory(file.parentFile)
                                    try {
                                        ImageIO.write(qrImage, "png", file)
                                    } catch (ex: IOException) {
                                        LOGGER.log(Level.WARNING, "Error saving QR file", ex)
                                    }
                                }
                            }
                        }
                        setOnMouseEntered { saveButton.opacity = 0.8 }
                        setOnMouseExited { saveButton.opacity = 0.0 }
                    }
                }
            }
        }
    }

    private fun getQRImage() : Image {
        if (qrImage == null) {
            val qrCodeWriter = QRCodeWriter()
            val qrWidth = 500
            val qrHeight = 500
            var byteMatrix: BitMatrix? = null
            try {
                byteMatrix = qrCodeWriter.encode(mLURL, BarcodeFormat.QR_CODE, qrWidth, qrHeight)
            } catch (ex: WriterException) {
                LOGGER.log(Level.WARNING, "Error writing QR code", ex)
            }
            qrImage = MatrixToImageWriter.toBufferedImage(byteMatrix)
        }
        val fxImg = WritableImage(500, 500)
        SwingFXUtils.toFXImage(qrImage, fxImg)
        return fxImg
    }

    val mLURL: String by lazy {
        if (get().useMobLyrics && QueleaApp.get().mobileLyricsServer != null) {
            val ip = getIP()
            if (ip != null) return@lazy buildString {
                append("http://")
                append(ip)
                val port = get().mobLyricsPort
                if (port != 80) {
                    append(":")
                    append(port)
                }
            }
        }

        "[" + LabelGrabber.INSTANCE.getLabel("not.started.label") + "]"
    }

    val rCURL by lazy {
        if (get().useRemoteControl && QueleaApp.get().remoteControlServer != null) {
            val ip = getIP()
            if (ip != null) return@lazy buildString {
                append("http://")
                append(ip)
                val port = get().remoteControlPort
                if (port != 80) {
                    append(":")
                    append(port)
                }
            }
        }

        "[" + LabelGrabber.INSTANCE.getLabel("not.started.label") + "]"
    }

    /**
     * {@inheritDoc}
     */
    override fun setupBindings() {
        super.setupBindings()
        editableField.visibleProperty().bind(
            field.editableProperty().and(!field.multilineProperty())
        )
        editableField.textProperty().bindBidirectional(field.userInputProperty())
        editableField.promptTextProperty().bind(field.placeholderProperty())
        editableField.managedProperty().bind(editableField.visibleProperty())
    }

    /**
     * {@inheritDoc}
     */
    override fun setupValueChangedListeners() {
        super.setupValueChangedListeners()
        field.multilineProperty().onChange {
            node.prefHeight = (if (it) 80 else 0).toDouble()
        }

        field.errorMessagesProperty().onChange { _: ObservableList<String?>? ->
            toggleTooltip(editableField)
        }
        editableField.focusedProperty().onChange {
            toggleTooltip(editableField)
        }
    }

    companion object {
        private val LOGGER = LoggerUtils.getLogger()
        private fun getIP(): String? {
            var adress: String? = null
            var interfaces: Enumeration<NetworkInterface>? = null

            try {
                interfaces = NetworkInterface.getNetworkInterfaces()
            } catch (ex: SocketException) {
                LOGGER.log(Level.WARNING, "Socket exception getting ip", ex)

                adress = try {
                    val localHost = InetAddress.getLocalHost()
                    val version = if(localHost is Inet6Address) "v6" else "v4"
                    val hostAddress = localHost.hostAddress

                    LOGGER.warning { "Socket exception, but using $version address as fallback: $hostAddress" }

                    hostAddress
                } catch (ex2: UnknownHostException) {
                    return null
                }
            }

            interfaces?.asSequence()
                ?.filter { it.isRealAndRunning() }
                ?.flatMap { it.inetAddresses.asSequence() }
                ?.ignoreLoopback()
                //for worst case: v6 adress
                ?.onEach {
                    if (it is Inet6Address){
                        adress = adress ?: it.hostAddress?.also {
                            LOGGER.info{ "Storing v6 address, no v4 found yet: $adress" }
                        }
                    }
                }
                //find and return first v4 address
                ?.filterIsInstance<Inet4Address>()
                ?.firstOrNull()
                ?.let {
                    val address = it.hostAddress
                    LOGGER.info{ "Found v4 address: $address" }

                    return address
                }

            //Fallback
            try {
                val fallback = InetAddress.getLocalHost().hostAddress
                LOGGER.info { "Using fallback: $fallback" }
                return fallback
            } catch (ex: UnknownHostException) {
                LOGGER.log(Level.WARNING, "Unknwon host ip", ex)
            }

            //Worst fallback, return ipv6 address
            LOGGER.info { "Falling back to v6 address: $adress" }
            return adress
        }

        private fun NetworkInterface.isRealAndRunning() = try {
            isUp && !isLoopback && !isVirtual &&
                    !displayName.contains("virtual", ignoreCase = true)
        } catch (e : Exception) { false }

        private fun Sequence<InetAddress>.ignoreLoopback() = filterNot { addr->
            val loopBack = addr.isLoopbackAddress
            if (loopBack) LOGGER.info("Ignoring loopback address")

            loopBack
        }
    }
}