package com.golubets.ftp
import com.golubets.ftp.gui.FocusTraversalOnArray
import com.golubets.ftp.services.FTPServer
import com.golubets.ftp.services.PreferencesService
import org.slf4j.LoggerFactory
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.IOException
import java.net.InetAddress
import javax.swing.*

class MainWindow @Throws(IOException::class)
constructor() : JFrame() {
    private val userValue: JTextField
    private val passwordValue: JPasswordField
    private val folderValue: JTextField
    private val portValue: JFormattedTextField
    private val folderBtn: JButton
    private val startBtn: JButton
    private val isAnonymous: JCheckBox
    private var ftpServer: FTPServer? = null
    private var isStarted = false
    private val chooser: JFileChooser
    private val preferences = PreferencesService()
    private val connectionStr = JLabel()

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MainWindow::class.java)
        const val DEFAULT_TITLE = "FTP Server"
        const val STARTED_TITLE = "FTP Server STARTED"

    }

    init {
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        isResizable = false
        title = DEFAULT_TITLE
        setBounds(10, 10, 410, 155)

        val dimension = Toolkit.getDefaultToolkit().screenSize

        var x = preferences.get("FTP.X", "0").toDouble()
        var y = preferences.get("FTP.Y", "0").toDouble()
        if (x <= 0 && y <= 0) {
            x = ((dimension.getWidth() - width) / 2)
            y = ((dimension.getHeight() - height) / 2)
        }
        setLocation(x.toInt(), y.toInt())

        chooser = JFileChooser()
        chooser.currentDirectory = java.io.File(".")
        chooser.dialogTitle = "FTP Server - share folder"
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        chooser.isAcceptAllFileFilterUsed = false

        contentPane.layout = null

        val folderLabel = JLabel("Folder")
        folderLabel.setBounds(10, 15, 46, 14)
        contentPane.add(folderLabel)

        folderValue = JTextField()
        folderValue.isEditable = false
        folderValue.setBounds(66, 12, 200, 20)
        contentPane.add(folderValue)
        folderValue.columns = 10

        folderBtn = JButton("...")
        folderBtn.addActionListener {
            if (chooser.showOpenDialog(folderBtn) == JFileChooser.APPROVE_OPTION) {
                folderValue.text = chooser.selectedFile.absolutePath
            }
        }
        folderBtn.setBounds(270, 12, 25, 20)
        contentPane.add(folderBtn)

        val lblPort = JLabel("Port")
        lblPort.setBounds(320, 15, 46, 14)
        contentPane.add(lblPort)

        portValue = JFormattedTextField()
        portValue.horizontalAlignment = SwingConstants.RIGHT
        portValue.text = "21"
        portValue.columns = 5
        portValue.setBounds(350, 12, 46, 20)
        contentPane.add(portValue)

        val userLabel = JLabel("User")
        userLabel.setBounds(10, 46, 46, 14)
        contentPane.add(userLabel)
        userValue = JTextField()
        userValue.text = "user"
        userValue.setBounds(66, 43, 86, 20)
        contentPane.add(userValue)
        userValue.columns = 10

        val passwordLabel = JLabel("Password")
        passwordLabel.setBounds(10, 76, 46, 14)
        contentPane.add(passwordLabel)

        passwordValue = JPasswordField()
        passwordValue.text = "user"
        passwordValue.columns = 10
        passwordValue.setBounds(66, 74, 86, 20)
        contentPane.add(passwordValue)

        isAnonymous = JCheckBox("Anonymous")
        isAnonymous.setBounds(61, 105, 90, 13)
        contentPane.add(isAnonymous)
        isAnonymous.addActionListener {
            if (isAnonymous.isSelected) {
                userValue.isEnabled = false
                passwordValue.isEnabled = false
            } else {
                userValue.isEnabled = true
                passwordValue.isEnabled = true
            }
        }
        loadPreferences()

        connectionStr.setBounds(280, 25, 99, 45)
        contentPane.add(connectionStr)
        connectionStr.addMouseListener(object : MouseAdapter(){
            override fun mouseClicked(p0: MouseEvent?) {
                val cl: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                cl.setContents(StringSelection(connectionStr.text), null)
            }
        })
        setConnectionStr()
//        connectionStr.addMouseListener{}

        startBtn = JButton("Start")
        startBtn.addActionListener {
            if (isStarted) {
                ftpServer!!.stop()
                isStarted = false
                portValue.isEnabled = true
                isAnonymous.isEnabled = true
                if (!isAnonymous.isSelected) {
                    userValue.isEnabled = true
                    passwordValue.isEnabled = true
                }

                folderBtn.isEnabled = true
                startBtn.text = "Start"
                title = DEFAULT_TITLE
                LOGGER.info("Server stopped")
            } else {
                if (isValidFields()) {
                    fillAndStartServer()
                }
            }
        }
        startBtn.setBounds(310, 98, 89, 23)
        contentPane.add(startBtn)


        focusTraversalPolicy = FocusTraversalOnArray(
                arrayOf(contentPane, folderLabel, folderBtn, lblPort, portValue, isAnonymous,
                        userLabel, userValue, passwordLabel, passwordValue, startBtn))

        if (isAnonymous.isSelected) {
            userValue.isEnabled = false
            passwordValue.isEnabled = false
        }
    }

    private fun setConnectionStr() {
        connectionStr.text = "${InetAddress.getLocalHost().hostAddress}:${getPort(portValue.text)}"
    }


    private fun isValidFields(): Boolean {
        if (!isAnonymous.isSelected) {
            if (userValue.text.isEmpty() || passwordValue.password.isEmpty()) {
                JOptionPane.showMessageDialog(contentPane, "user or password is not filled",
                        "error", JOptionPane.ERROR_MESSAGE)
                return false
            }
        }
        if (folderValue.text.isEmpty()) {
            JOptionPane.showMessageDialog(contentPane, "Folder is not filled",
                    "error", JOptionPane.ERROR_MESSAGE)
            return false
        }
        if (getPort(portValue.text) <= 0) {
            return false
        }
        return true
    }

    private fun loadPreferences() {
        portValue.text = preferences.get("FTP.PORT", "21")
        userValue.text = preferences.get("FTP.USER", "user")
        passwordValue.text = preferences.get("FTP.PASSWORD", "user")
        folderValue.text = preferences.get("FTP.FOLDER", "")
        isAnonymous.isSelected = java.lang.Boolean.valueOf(preferences.get("FTP.ANONYMOUS", "false"))
        if (isAnonymous.isSelected) {
            userValue.isEnabled = false
            passwordValue.isEnabled = false
        }
    }

    private fun fillAndStartServer() {
        savePreferences()


        if (getPort(portValue.text) == 0) {
            return
        }
        ftpServer = FTPServer()
        ftpServer!!.setPort(getPort(portValue.text))
        ftpServer!!.isAnonymous = isAnonymous.isEnabled
        ftpServer!!.setUser(userValue.text, passwordValue.password, folderValue.text)
        if (ftpServer!!.start()) {
            setConnectionStr()
            isStarted = true
            portValue.isEnabled = false
            userValue.isEnabled = false
            isAnonymous.isEnabled = false
            passwordValue.isEnabled = false
            folderBtn.isEnabled = false
            startBtn.text = "Stop"
            title = STARTED_TITLE
            LOGGER.info("Server started")
        } else {
            LOGGER.error("Error starting server")
            JOptionPane.showMessageDialog(contentPane,
                    "Error starting server, for mo information read log",
                    "error", JOptionPane.ERROR_MESSAGE)
        }
    }

    private fun getPort(text: String): Int {
        try {
            return Integer.parseInt(text)
        } catch (e: NumberFormatException) {
            JOptionPane.showMessageDialog(contentPane, "Port is not correct",
                    "error", JOptionPane.ERROR_MESSAGE)
        }
        return 0
    }

    private fun savePreferences() {
        preferences["FTP.PORT"] = portValue.text
        preferences["FTP.USER"] = userValue.text
        preferences["FTP.PASSWORD"] = String(passwordValue.password)
        preferences["FTP.FOLDER"] = folderValue.text
        preferences["FTP.ANONYMOUS"] = isAnonymous.isSelected.toString()
        preferences["FTP.X"] = location.getX().toString()
        preferences["FTP.Y"] = location.getY().toString()

        preferences.save()
    }
}