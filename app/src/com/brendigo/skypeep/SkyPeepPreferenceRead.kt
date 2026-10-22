package com.brendigo.skypeep

/** Isolate a single corrupt saved entry rather than aborting all progress. */
internal object SkyPeepPreferenceRead {
    fun integer(
        key: String, fallback: Int,
        lookup: (String, Int) -> Int,
        onFailure: (String, RuntimeException) -> Unit = { _, _ -> }
    ): Int = try {
        lookup(key, fallback)
    } catch (error: RuntimeException) {
        onFailure(key, error)
        fallback
    }

    fun boolean(
        key: String, fallback: Boolean,
        lookup: (String, Boolean) -> Boolean,
        onFailure: (String, RuntimeException) -> Unit = { _, _ -> }
    ): Boolean = try {
        lookup(key, fallback)
    } catch (error: RuntimeException) {
        onFailure(key, error)
        fallback
    }
}
