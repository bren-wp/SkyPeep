package com.brendigo.skypeep

/**
 * Battery-aware drawing policy.
 *
 * Animated flight runs continuously. Static illustrated menus, map, shop,
 * settings, pause and result screens draw once on state/input changes.
 * Temporary toasts retain continuous rendering until their timer expires.
 * A paused application does not simulate physics in the background.
 */
internal object SkyPeepRenderPolicy {
    fun continuous(screenName: String, messageSeconds: Float): Boolean =
        screenName == "PLAY" || (messageSeconds.isFinite() && messageSeconds > 0f)
}
