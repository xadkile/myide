package com.emeraldblast.p6.message.api.connection.service.errors

import com.emeraldblast.p6.common.exception.error.ErrorHeader
import com.emeraldblast.p6.common.exception.error.ErrorReport

object ServiceErrors {
    private const val prefix = "ServiceErrors_"

    object ServiceNull {
        val header = ErrorHeader("${prefix}1", "Service is null")
        class Data(val serviceName: String)
        fun report(detail:String): ErrorReport{
            return header.setDescription(detail).toErrorReport()
        }
    }

    object ServiceNotRunning {
        val header = ErrorHeader("${prefix}2", "Service is not running")
        fun report(detail:String): ErrorReport{
            return header.setDescription(detail).toErrorReport()
        }
    }
}
