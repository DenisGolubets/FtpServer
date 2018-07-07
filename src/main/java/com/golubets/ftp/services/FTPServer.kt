package com.golubets.ftp.services

import org.apache.ftpserver.ConnectionConfigFactory
import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.FtpException
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.slf4j.LoggerFactory

class FTPServer {
    private var port: Int = 0
    private val userManager: UserManager
    private var server: FtpServer? = null
    var isAnonymous = false

    companion object {
        private val LOGGER = LoggerFactory.getLogger(FTPServer::class.java)
        private const val ANONYMOUS = "anonymous"
    }

    init {
        port = 21
        userManager = UserManager()
    }

    fun setPort(port: Int) {
        this.port = port
    }

    fun setUser(login: String, password: CharArray?, home: String) {
        if (!isAnonymous || ANONYMOUS.equals(login, ignoreCase = true)) {
            val anonymous = BaseUser()
            anonymous.homeDirectory = home
            anonymous.enabled = true
            anonymous.name = ANONYMOUS
            userManager.setUser(anonymous)
        } else {
            val user = BaseUser()
            user.name = login
            if (password != null && password.isNotEmpty()) {
                user.password = String(password)
            }
            user.homeDirectory = home
            user.enabled = true
            userManager.setUser(user)
        }
    }

    fun stop() {
        if (server != null && !server!!.isStopped) {
            server!!.stop()
            server = null
        }
    }

    fun start(): Boolean {
        try {
            stop()

            val configFactory = ConnectionConfigFactory()
            configFactory.isAnonymousLoginEnabled = isAnonymous
            configFactory.maxAnonymousLogins = 15
            configFactory.maxLoginFailures = 5
            configFactory.loginFailureDelay = 30
            configFactory.maxThreads = 10
            configFactory.maxLogins = 10

            val factory = ListenerFactory()
            factory.port = port
            factory.idleTimeout = 60

            val serverFactory = FtpServerFactory()
            serverFactory.addListener("default", factory.createListener())
            serverFactory.userManager = userManager
            serverFactory.connectionConfig = configFactory.createConnectionConfig()

            server = serverFactory.createServer()
            server!!.start()
        } catch (ex: FtpException) {
            LOGGER.error("Error start server: ", ex)
            return false
        }

        return true
    }
}
