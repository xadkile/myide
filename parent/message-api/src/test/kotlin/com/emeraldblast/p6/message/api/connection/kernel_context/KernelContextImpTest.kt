package com.emeraldblast.p6.message.api.connection.kernel_context

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.github.michaelbull.result.unwrap
import com.emeraldblast.p6.message.api.connection.kernel_context.errors.KernelErrors
import com.emeraldblast.p6.message.di.DaggerMessageApiComponent
import com.emeraldblast.p6.test.utils.TestResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.zeromq.ZContext
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class KernelContextImpTest {
    lateinit var kc: KernelContext
    lateinit var kernelConfig: KernelConfig
    lateinit var zContext: ZContext
    lateinit var ksm: KernelServiceManager

    @BeforeEach
    fun beforeEach() {
        this.zContext = ZContext()
        kernelConfig = TestResources.kernelConfigForTest()
        val dcomponent = DaggerMessageApiComponent.builder()
            .kernelConfig(kernelConfig)
            .kernelCoroutineScope(GlobalScope)
            .networkServiceCoroutineDispatcher(Dispatchers.IO)
            .build()

        kc = dcomponent.kernelContext()
        ksm = dcomponent.kernelServiceManager()
    }

    @AfterEach
    fun afterAll() {
        runBlocking {
            kc.stopAll()
        }
    }

    @Test
    fun testStartAndStopListeners() {
        var start = false
        kc.setKernelStartedListener {
            start = true
        }
        runBlocking {
            kc.startAll()
        }
        assertTrue(start)
        var afterStop = false
        var beforeStop = false
        kc.setOnAfterStopListener {
            afterStop = true
        }
        kc.setOnBeforeStopListener {
            beforeStop = true
        }

        runBlocking {
            kc.stopAll()
            delay(1000)
            assertTrue(afterStop)
            assertTrue(beforeStop)
        }
    }

    @Test
    fun startKernel_FromNotStartedYet() = runBlocking {
        assertTrue(kc.getKernelProcess() is Err)
        val rs = kc.startAll()
        val ksmRs = ksm.startAll()
        assertTrue(rs is Ok, rs.toString())
        assertTrue(ksmRs is Ok, rs.toString())
        assertTrue(kc.isKernelRunning())
        assertTrue(kc.getKernelProcess() is Ok, kc.getKernelProcess().toString())
        assertTrue(kc.getKernelProcess().get()?.isAlive ?: false)
        assertTrue(kc.getConnectionFileContent() is Ok, kc.getConnectionFileContent().toString())
        assertTrue(kc.getChannelProvider() is Ok, kc.getChannelProvider().toString())
        assertTrue(kc.getSession() is Ok, kc.getSession().toString())
        assertTrue(kc.getMsgEncoder() is Ok, kc.getMsgEncoder().toString())
        assertTrue(kc.getMsgIdGenerator() is Ok, kc.getMsgIdGenerator().toString())
        assertTrue(ksm.getHeartBeatServiceRs() is Ok)
        assertTrue(ksm.hbService?.isRunning()?:false)
        assertTrue(ksm.getZmqREPServiceRs() is Ok)
        assertTrue(ksm.zmqREPService?.isRunning()?:false)
        assertTrue(ksm.getIOPubListenerServiceRs() is Ok)
        assertTrue(ksm.ioPubService?.isRunning()?:false)
        assertTrue(Files.exists(Paths.get(kernelConfig.getConnectionFilePath())))
    }

    @Test
    fun startIPython_FromAlreadyStarted() = runBlocking {
        val rs0 = kc.startAll()
        assertTrue(rs0 is Ok)
        val rs = kc.startAll()
        assertTrue(rs is Ok)
    }

    @Test
    fun stopIPython() = runBlocking {
        kc.startAll()
        runBlocking {
            val rs = kc.stopAll()
            assertTrue(rs is Ok)
        }
        assertTrue(kc.isKernelNotRunning())
        assertTrue(kc.getKernelProcess() is Err)
        assertFalse(kc.getKernelProcess().get()?.isAlive ?: false)
        assertTrue(kc.getConnectionFileContent() is Err)
        assertTrue(kc.getSession() is Err)
        assertTrue(kc.getChannelProvider() is Err)
        assertTrue(kc.getMsgEncoder() is Err)
        assertTrue(ksm.getHeartBeatServiceRs() is Err)
        assertTrue(ksm.getZmqREPServiceRs() is Err)
        assertTrue(ksm.getIOPubListenerServiceRs() is Err)
        assertFalse(Files.exists(Paths.get(kernelConfig.getConnectionFilePath())))
    }

    @Test
    fun stopIPython_onAlreadyStopped() = runBlocking {
        kc.startAll()
        runBlocking {
            val rs = kc.stopAll()
            assertTrue(rs is Ok, rs.toString())
            val rs2 = kc.stopAll()
            assertTrue(rs2 is Ok, rs.toString())
        }
    }

    @Test
    fun restartIPython() = runBlocking {
        kc.startAll()
        val oldConnectionFile = kc.getConnectionFileContent().get()
        assertNotNull(oldConnectionFile)
        runBlocking {
            val rs = kc.restartKernel()
            val newConnectionFile = kc.getConnectionFileContent().get()
            assertTrue(rs is Ok, rs.toString())
            assertNotNull(newConnectionFile)
            assertNotEquals(oldConnectionFile, newConnectionFile)
        }
    }

    @Test
    fun restartIPython_OnStopped() = runBlocking {
        kc.startAll()
        runBlocking {
            kc.stopAll()
            val rs = kc.restartKernel()
            assertTrue(rs is Err)
            assertTrue(rs.error.isType(KernelErrors.KernelContextIllegalState.header))
        }
    }
}
