package com.pedroid.mobyfocus.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppCategoryTest {

    @Test
    fun `defines exactly seven categories`() {
        assertEquals(7, AppCategory.entries.size)
    }

    @Test
    fun `storage key equals enum name for every category`() {
        AppCategory.entries.forEach { category ->
            assertEquals(category.name, category.storageKey)
        }
    }

    @Test
    fun `fromStorageKey resolves every known key`() {
        AppCategory.entries.forEach { category ->
            assertEquals(category, AppCategory.fromStorageKey(category.storageKey))
        }
    }

    @Test
    fun `fromStorageKey falls back to NEUTRAL for unknown or corrupted key`() {
        assertEquals(AppCategory.NEUTRAL, AppCategory.fromStorageKey("CORRUPTED_VALUE"))
        assertEquals(AppCategory.NEUTRAL, AppCategory.fromStorageKey(""))
        assertEquals(AppCategory.NEUTRAL, AppCategory.fromStorageKey("productive"))
    }
}
