package com.pedroid.mobyfocus.data.mapper

import com.pedroid.mobyfocus.data.local.entity.AppClassificationEntity
import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.domain.model.AppClassification
import org.junit.Assert.assertEquals
import org.junit.Test

class AppClassificationMapperTest {

    private val mapper = AppClassificationMapper()

    @Test
    fun `entity maps to domain resolving category from storage key`() {
        val entity = AppClassificationEntity("com.app.a", "PRODUCTIVE", 42L)

        val domain = mapper.toDomain(entity)

        assertEquals(
            AppClassification("com.app.a", AppCategory.PRODUCTIVE, 42L),
            domain,
        )
    }

    @Test
    fun `entity with unknown category maps to NEUTRAL`() {
        val entity = AppClassificationEntity("com.app.a", "GARBAGE_FROM_CORRUPTION", 42L)

        assertEquals(AppCategory.NEUTRAL, mapper.toDomain(entity).category)
    }

    @Test
    fun `domain maps to entity writing the storage key and passing timestamp through`() {
        val domain = AppClassification("com.app.b", AppCategory.DISTRACTING, 99L)

        val entity = mapper.toEntity(domain)

        assertEquals(
            AppClassificationEntity("com.app.b", "DISTRACTING", 99L),
            entity,
        )
    }
}
