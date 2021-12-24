package com.github.xadkile.p6.message.api.connection.kernel_context

import com.github.michaelbull.result.Result
import java.io.InputStream
import java.io.OutputStream

/**
 * TODO need to add something to watch for unexpected kill of IPython
 * Interface for managing kernel process, provide information to work with the kernel.
 * There is a risk of memory leak here. It is crucial that consumers of instances of this interface must not cache any derivative objects
 */
interface KernelContext : KernelContextReadOnly {
    /**
     * startCore() + startServices
     */
    suspend fun startAll(): Result<Unit, Exception>

    /**
     * Start IPython process and read connection file.
     *
     * It is guarantee that once IPython start, components objects are available for use. They include: IPython Process, connection file object, session object, channel provider, sender factory.
     *
     * Call [startKernel] on an already running context does not change the state of this manager, return Ok result.
     *
     * It must be guaranteed that connection file is created and read, and process (jpython + zmq) is on and ready to accept command.
     */
    suspend fun startKernel():Result<Unit,Exception>

    /**
     * start services
     */
    suspend fun startServices():Result<Unit,Exception>

    /**
     * Kill the current kernel process and delete the current connection file.
     *
     * Stop an already stopped manager does nothing, return Ok result.
     *
     * It must be guaranteed that connection file is deleted, process is completely killed after calling stop.
     */
    suspend fun stopAll(): Result<Unit, Exception>

    suspend fun stopServices():Result<Unit,Exception>

    suspend fun stopKernel():Result<Unit,Exception>
//
    /**
     * Terminate the current process and launch a new IPython Process.
     *
     * Connection file content is also updated.
     *
     * This function can only be used on already running manager. Attempt to call it on stopped manager must be prohibited.
     */
    suspend fun restartKernel(): Result<Unit, Exception>

    fun getKernelProcess(): Result<Process, Exception>

    /**
     * Return input stream of the current IPython process
     */
    fun getKernelInputStream():Result<InputStream,Exception>

    /**
     * Return output stream of the current IPython process
     */
    fun getKernelOutputStream():Result<OutputStream,Exception>

    /**
     * add a listener that is invoked before a legal/normal stopping of a process
     */
    fun setOnBeforeStopListener(listener: OnKernelContextEvent)

    /**
     * remove the legal/normal on-before-process-stop listener
     */
    fun removeBeforeStopListener()

    /**
     * add a listener that is invoked after a legal/normal stopping of a process
     */
    fun setOnAfterStopListener(listener: OnKernelContextEvent)

    /**
     * remove the legal/normal on-after-process-stop listener
     */
    fun removeAfterStopListener()

    fun setKernelStartedListener(listener: OnKernelContextEvent)

    fun removeOnProcessStartListener()
}

