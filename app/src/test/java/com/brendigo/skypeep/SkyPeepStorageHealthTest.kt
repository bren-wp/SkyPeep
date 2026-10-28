package com.brendigo.skypeep

import org.junit.Assert.*
import org.junit.Test

class SkyPeepStorageHealthTest {
    @Test fun cleanPreferenceReadsPermitExistingSaveFormat() {
        val health = SkyPeepStorageHealth()
        assertTrue(health.canWrite)
    }

    @Test fun oneUnreadableEntryPreventsDestructiveDefaultOverwrite() {
        val health = SkyPeepStorageHealth()
        // A malformed key must not make a previously saved wallet disappear.
        val originalSaved = linkedMapOf("coins" to 2400, "owned_3" to 1)
        val saved = originalSaved.toMutableMap()
        val fallback = linkedMapOf("coins" to 1250, "owned_3" to 0)
        health.onReadFailure()
        if (health.canWrite) saved.putAll(fallback)
        assertFalse(health.canWrite)
        assertEquals(originalSaved, saved)
        // No implicit recovery on a subsequent unrelated successful read.
        assertFalse(health.canWrite)
    }
    @Test fun failedFullFlushIsNotFalselyClearedBySuccessfulPickupWrite() {
        val health = SkyPeepStorageHealth()
        health.onFlushFailure()
        assertTrue(health.canWrite)
        assertTrue(health.needsFullSnapshot)
        assertFalse(health.purchaseReady)
        // Even if a later partial transaction reaches storage, ownership,
        // route scores or options from the earlier save may still be missing.
        health.onFlushSuccess(fullSnapshot = false)
        assertTrue(health.needsFullSnapshot)
        assertFalse(health.purchaseReady)
        health.onFlushSuccess(fullSnapshot = true)
        assertFalse(health.needsFullSnapshot)
        assertTrue(health.purchaseReady)
    }

    @Test fun unreadableOriginalNeverBecomesWritableAfterAnyRetry() {
        val health = SkyPeepStorageHealth()
        health.onReadFailure()
        health.onFlushFailure()
        health.onFlushSuccess(fullSnapshot = true)
        assertFalse(health.canWrite)
        assertFalse(health.purchaseReady)
    }

    @Test fun secondFailedFlushStillRequiresFullRecovery() {
        val health = SkyPeepStorageHealth()
        health.onFlushFailure()
        health.onFlushFailure()
        health.onFlushSuccess(fullSnapshot = false)
        assertTrue(health.needsFullSnapshot)
        health.onFlushSuccess(fullSnapshot = true)
        assertTrue(health.purchaseReady)
    }

}
