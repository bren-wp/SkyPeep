package com.brendigo.skypeep

/**
 * Guard the existing local preference keys without destructive recovery.
 *
 * One unreadable entry can coexist with valid wallet/ownership records:
 * forbid all writes rather than replacing that record with defaults.
 *
 * A failed flush is different: in-memory data may still be valid, but a later
 * three-key pickup or two-key score write cannot prove that an earlier full
 * snapshot reached disk. Retry the full snapshot before claiming recovery.
 */
internal class SkyPeepStorageHealth {
    private var unreadableSavedData = false
    private var fullSnapshotPending = false

    val canWrite: Boolean get() = !unreadableSavedData
    val needsFullSnapshot: Boolean get() = fullSnapshotPending

    fun onReadFailure() {
        unreadableSavedData = true
    }

    fun onFlushFailure() {
        fullSnapshotPending = true
    }

    /** A partial successful flush must never clear a previous failed write. */
    fun onFlushSuccess(fullSnapshot: Boolean) {
        if (fullSnapshot) fullSnapshotPending = false
    }

    /** Cosmetic transactions must be blocked while recovery is unconfirmed. */
    val purchaseReady: Boolean get() = canWrite && !fullSnapshotPending
}
