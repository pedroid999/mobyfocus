package com.pedroid.mobyfocus.data.repository

import android.app.AppOpsManager
import android.content.Context
import android.os.Process
import com.pedroid.mobyfocus.domain.model.UsageAccessPermissionStatus
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Verifies the AppOps mode → [UsageAccessPermissionStatus] mapping.
 *
 * In a pure-JVM test `Build.VERSION.SDK_INT` defaults to 0, so the pre-Q
 * `checkOpNoThrow` branch is exercised — which is exactly the mode-mapping logic
 * under test.
 */
class UsageAccessRepositoryImplTest {

    private val context = mockk<Context>(relaxed = true)
    private val appOps = mockk<AppOpsManager>()
    private lateinit var repository: UsageAccessRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic(Process::class)
        every { Process.myUid() } returns 1000
        every { context.getSystemService(Context.APP_OPS_SERVICE) } returns appOps
        every { context.packageName } returns "com.pedroid.mobyfocus"
        repository = UsageAccessRepositoryImpl(context)
    }

    @After
    fun tearDown() {
        unmockkStatic(Process::class)
    }

    @Test
    fun `returns GRANTED when op mode is allowed`() {
        every { appOps.checkOpNoThrow(any(), any(), any()) } returns AppOpsManager.MODE_ALLOWED

        assertEquals(UsageAccessPermissionStatus.GRANTED, repository.getPermissionStatus())
    }

    @Test
    fun `returns NOT_GRANTED when op mode is not allowed`() {
        every { appOps.checkOpNoThrow(any(), any(), any()) } returns AppOpsManager.MODE_IGNORED

        assertEquals(UsageAccessPermissionStatus.NOT_GRANTED, repository.getPermissionStatus())
    }
}
