package com.emeraldblast.p6.message.api.message.sender.shell

import com.emeraldblast.p6.common.exception.error.ErrorReport
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelContextReadOnly
import com.emeraldblast.p6.message.api.connection.kernel_context.KernelServiceManager
import com.emeraldblast.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.emeraldblast.p6.message.api.message.protocol.JPMessage
import com.emeraldblast.p6.message.api.message.protocol.data_interface_definition.Shell
import com.emeraldblast.p6.message.api.message.sender.MsgSender
import com.emeraldblast.p6.message.api.message.sender.PCSender
import com.github.michaelbull.result.*
import javax.inject.Inject

typealias ExecuteReply = JPMessage<Shell.Execute.Reply.MetaData, Shell.Execute.Reply.Content>

typealias ExecuteRequest = JPMessage<Shell.Execute.Request.MetaData, Shell.Execute.Request.Content>

interface ExecuteSender : MsgSender<ExecuteRequest,
        Result<ExecuteReply, ErrorReport>>

/**
 * Send a code execution request. It is noted that the result returned by this sender is not the code execution result, only the sending status.
 * @return a reply.
 */
class ExecuteSenderImp @Inject constructor(
    private val kernelContext: KernelContextReadOnly,
    private val kernelServiceManager: KernelServiceManager,
) : ExecuteSender {

    override suspend fun send(
        message: ExecuteRequest,
    ): Result<ExecuteReply, ErrorReport> {
        if (this.kernelContext.isKernelNotRunning()) {
            return Err(
                KernelErrors.KernelDown.report("ExecuteSenderImp can't send message because the kernel is down. Message header is:\n" + "${message.header}")
            )
        }
        val rt = kernelContext.getSocketProvider().map { it.shellSocket() }
            .andThen { shellSocket ->
                kernelContext.getMsgEncoder().andThen { msgEncoder ->
                    kernelServiceManager.getHeartBeatServiceRs().andThen { hbs ->
                        val pcSender = PCSender<ExecuteRequest, ExecuteReply>(
                            shellSocket,
                            msgEncoder,
                            hbs,
                            kernelContext.zContext(),
                            kernelContext.kernelConfig.timeOut.messageTimeOut
                        )
                        pcSender.send2<Shell.Execute.Reply.MetaData, Shell.Execute.Reply.Content>(message)
                    }
                }
            }
        return rt
//        val pcSender = PCSender<ExecuteRequest, ExecuteReply>(
//            kernelContext.getSocketProvider().unwrap().shellSocket(),
//            kernelContext.getMsgEncoder().unwrap(),
//            kernelContext.getHeartBeatService().unwrap(),
//            kernelContext.zContext(),
//            kernelContext.kernelConfig.timeOut.messageTimeOut
//        )
//        val rt: Result<ExecuteReply, ErrorReport> =
//            pcSender.send2<Shell.Execute.Reply.MetaData, Shell.Execute.Reply.Content>(message)
//        return rt
    }
}
