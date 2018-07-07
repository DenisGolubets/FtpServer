package com.golubets.ftp.services

import org.apache.ftpserver.ftplet.*
import org.apache.ftpserver.usermanager.AnonymousAuthentication
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission
import org.apache.ftpserver.usermanager.impl.WritePermission
import org.slf4j.LoggerFactory
import java.util.*

class UserManager : org.apache.ftpserver.ftplet.UserManager {
    private var user: BaseUser? = null

    companion object {
        private val LOGGER = LoggerFactory.getLogger(UserManager::class.java)

    }

    fun setUser(user: BaseUser) {
        this.user = user
        if (user.authorities == null || user.authorities.isEmpty()) {
            val authorities: ArrayList<Authority> = ArrayList()
            authorities.add(WritePermission())
            authorities.add(ConcurrentLoginPermission(10, 10))
            user.authorities = authorities
        }
    }

    @Throws(AuthenticationFailedException::class)
    override fun authenticate(auth: Authentication?): User? {
        if (auth != null && auth is UsernamePasswordAuthentication) {
            val userAuth = auth as UsernamePasswordAuthentication?
            if (user!!.name == userAuth!!.username && user!!.password == userAuth.password) {
                LOGGER.info("${user!!.name} connected")
                return user
            }
        }
        if (AnonymousAuthentication::class.java.isAssignableFrom(auth!!.javaClass)) {
            LOGGER.info("${user!!.name} connected")
            return if (user!!.enabled) user else null
        }
        LOGGER.error("Authenticate failed: $auth")
        return null
    }

    @Throws(FtpException::class)
    override fun delete(login: String) {
    }

    @Throws(FtpException::class)
    override fun doesExist(login: String): Boolean {
        return user!!.name == login
    }

    @Throws(FtpException::class)
    override fun getAdminName(): String {
        return user!!.name
    }

    @Throws(FtpException::class)
    override fun getAllUserNames(): Array<String> {
        return arrayOf(user!!.name)
    }

    @Throws(FtpException::class)
    override fun getUserByName(login: String): User? {
        return user
    }

    @Throws(FtpException::class)
    override fun isAdmin(login: String): Boolean {
        return user!!.name == login
    }

    @Throws(FtpException::class)
    override fun save(login: User) {
    }
}
