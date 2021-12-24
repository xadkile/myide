package com.github.xadkile.p6.message.api.connection.kernel_context.context_object

import java.util.*

/**
 * Provide user name, encryption key, and session id. These are for making message
 */
data class SessionImp internal constructor(
    private val sessionId: String,
    private val systemUsername: String,
    private val key: String
) : Session {
    companion object {
        /**
         * [sessionId] autogenerated
         * [systemUsername] fetched from system
         */
        fun autoCreate(key: String): SessionImp {
            return SessionImp(
                sessionId = UUID.randomUUID().toString(),
                systemUsername = System.getProperty("user.name"),
                key = key
            )
        }
    }

    override fun getSystemUserName(): String {
        return this.systemUsername
    }

    override fun getKey(): String {
        return this.key
    }

    override fun getSessionId(): String {
        return this.sessionId
    }
}
