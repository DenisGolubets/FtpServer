package com.golubets.ftp

import com.golubets.ftp.gui.MainWindow
import com.golubets.ftp.services.FTPServer
import org.slf4j.LoggerFactory
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.UIManager

object Start {
    private val LOGGER = LoggerFactory.getLogger(Start::class.java)
    @JvmStatic
    fun main(args: Array<String>) {

        if (args.isEmpty()) {
            java.awt.EventQueue.invokeLater {
                try {
                    JFrame.setDefaultLookAndFeelDecorated(true)
                    JDialog.setDefaultLookAndFeelDecorated(true)
                    System.setProperty("sun.awt.noerasebackground", "true")
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                    val window = MainWindow()
                    window.isVisible = true
                } catch (e: Exception) {
                    LOGGER.error(e.stackTrace.joinToString(separator = "\n"))
                }
            }
        } else {
            if (args.size == 4) {
                val server = FTPServer()
                server.setPort(Integer.parseInt(args[0]))
                server.setUser(args[1], args[2].toCharArray(), args[3])
                server.start()
            } else {
                LOGGER.error("Invalid arguments\n" +
                        "Use without arguments for GUI or call with:\n" +
                        "<port> <user> <password> <folder>")
            }
        }
    }
}
