package com.pedroid.mobyfocus.data.mapper

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppUsageMapperTest {

    private val context = mockk<Context>()
    private val packageManager = mockk<PackageManager>()
    private lateinit var mapper: AppUsageMapper

    @Before
    fun setUp() {
        every { context.packageManager } returns packageManager
        mapper = AppUsageMapper(context)
    }

    @Test
    fun `resolves display name from package manager`() {
        val info = mockk<ApplicationInfo>()
        every { packageManager.getApplicationInfo("com.app.a", 0) } returns info
        every { packageManager.getApplicationLabel(info) } returns "App A"

        val result = mapper.toAppUsageList(mapOf("com.app.a" to 5_000L))

        assertEquals("App A", result.single().displayName)
        assertEquals(5_000L, result.single().foregroundTimeMillis)
    }

    @Test
    fun `falls back to package name when app is not installed`() {
        every { packageManager.getApplicationInfo("com.gone", 0) } throws
            PackageManager.NameNotFoundException()

        val result = mapper.toAppUsageList(mapOf("com.gone" to 3_000L))

        assertEquals("com.gone", result.single().displayName)
    }

    @Test
    fun `drops entries with zero usage`() {
        val info = mockk<ApplicationInfo>()
        every { packageManager.getApplicationInfo(any(), 0) } returns info
        every { packageManager.getApplicationLabel(info) } returns "Whatever"

        val result = mapper.toAppUsageList(mapOf("com.zero" to 0L))

        assertTrue(result.isEmpty())
    }
}
