package com.brendigo.skypeep

/**
 * Do not overwrite an existing preference file with fallback/default values
 * after even one saved entry could not be read. Android preference files can
 * contain correctly saved purchases and records alongside a corrupt entry.
 *
 * A new app process can try to read the original file again. No destructive
 * automatic migration or preference clearing is attempted.
 */
internal class SkyPeepStorageHealth {
    private var unreadableSavedData = false

    val canWrite: Boolean get() = !unreadableSavedData

    fun onReadFailure() {
        unreadableSavedData = true
    }
}
