/*
 * SkyPeep gameplay, visual integration and menus.
 * Derived from the original user-supplied FlappyBird Android/libGDX starter.
 * Original 2018 Kotlin starter copyright Konstantinos Drakonakis (Apache-2.0).
 * Reference illustrations supplied by the user; no online image assets.
 */
package com.brendigo.skypeep

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ExtendViewport
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/** All locations refer to the original 941x1672 portrait artwork. No scrolling. */
class SkyPeepGame(private val shareText: ((String) -> Unit)? = null,
                  private val qaWorld: Int = 0,
                  private val qaSkin: Int = -1,
                  private val qaVictory: Boolean = false,
                  private val qaPillars: Boolean = false,
                  private val qaScore: Int = -1,
                  private val qaMapUnlocked: Int = -1,
                  private val qaLoss: Boolean = false,
                  private val qaBackgroundMotion: Boolean = false,
                  private val qaDifficulty: Int = -1) : ApplicationAdapter() {
    companion object {
        private const val W = 941f
        private const val H = 1672f
        private const val BIRD_X = 172f
        private val BIRD_W = SkyPeepBirdLayout.WIDTH
        private val BIRD_H = SkyPeepBirdLayout.HEIGHT
        // Rendered pillar assets occupy the full width, including their caps.
        private const val GATE_WIDTH = 275f
        private const val MAX_LEVEL = 6
        private val LEVEL_TITLES = arrayOf("Sky Start","Cloud Drift","Crystal Drop",
            "Wind Gate","Moon Glow","Sun Peak")
        private val SCORE_STROKE_OFFSETS = floatArrayOf(-2.8f,0f,2.8f)
        private val SCORE_OUTLINE = Color.valueOf("10264B")
        // Original winged source badge shows white digits with a navy edge.
        private val SCORE_INK = Color.WHITE
        private val TRAIL_TINTS = arrayOf(Color.WHITE, Color.valueOf("43DBFF"),
            Color.valueOf("FFD145"), Color.valueOf("C078FF"))
        private val SHOP_SHADE = Color(.04f,.13f,.30f,.72f)
        private val SETTINGS_SHADE = Color(0f,.07f,.17f,.68f)
        private val RESULT_SHADE = Color(.06f,.16f,.32f,.29f)
        private val UNLIT_STAR = Color(.38f,.44f,.56f,.85f)
        private val VICTORY_HEADLINE_COLOR = Color.valueOf("6B3D26")
        private val SHOP_NAME_COLOR = Color.valueOf("5B3526")
        private val SHOP_PRICE_COLOR = Color.valueOf("734428")
        private val SETTINGS_NAMES = arrayOf("MUSIC","SOUND EFFECTS","VIBRATION","DIFFICULTY")
        private val TEXT_ON_CREAM = Color.valueOf("593D28")
        private val RESULT_VALUE_COLOR = Color.valueOf("673A22")
        private val BIRD_TINTS = arrayOf(Color.WHITE, Color(.83f,.98f,1f,1f),
            Color(.50f,1f,.55f,1f), Color(.65f,.55f,1f,1f),
            Color(1f,.91f,.76f,1f))
        private val SHIELD_TINT = Color(.85f,.95f,1f,.85f)
        private val EMPTY_HEART_TINT = Color(.30f,.36f,.48f,.38f)
    }

    private enum class Screen { SPLASH, MENU, MAP, PLAY, PAUSE, RESULT, SKINS, SHOP, SETTINGS }
    private data class Gate(var x: Float, var centerY: Float, var scored: Boolean = false,
                            var coinTaken: Boolean = false, var gemTaken: Boolean = false,
                            val gem: Boolean = false,
                            var precisionSampled: Boolean = false,
                            var preciseAtCrossing: Boolean = false,
                            val heart: Boolean = false, var heartTaken: Boolean = false)

    private lateinit var batch: SpriteBatch
    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: ExtendViewport
    private var visibleFrame = SkyPeepScreenFrame.forPixels(941,1672)
    private lateinit var prefs: Preferences
    private val storageHealth = SkyPeepStorageHealth()
    private var lastSaveFailed = false
    private val pictures = mutableMapOf<String, Texture>()
    private var loadedScreenArt: String? = null
    private var loadedLevelArt = 0
    private lateinit var fontSmall: BitmapFont
    private lateinit var fontLarge: BitmapFont
    private lateinit var solid: Texture
    private val chrome = mutableMapOf<String, com.badlogic.gdx.graphics.g2d.NinePatch>()
    private val chromeTextures = mutableMapOf<String, Texture>()
    private var costumes: Map<Int, Texture> = emptyMap()
    private var victoryStar: Texture? = null
    private var pillarStem: Texture? = null
    private var trailTexture: Texture? = null
    private var heartTexture: Texture? = null
    private var selectedTrail = 0
    private val ownedTrails = BooleanArray(4)
    private var music: Music? = null
    private val sounds = mutableMapOf<String, Sound>()
    private val tmp = GlyphLayout()
    private val fontColorScratch = Color()
    private val hudText = SkyPeepHudTextCache()
    private val flightHud = SkyPeepFlightHudState()
    private val backgroundCrop = SkyPeepBackgroundMotion.MutableCrop()
    private val touch = Vector2()
    private var lastContinuousRendering: Boolean? = null

    private var screen = Screen.SPLASH
    private var lastLoggedScreen: Screen? = null
    private var level = 1
    private var unlocked = MAX_LEVEL
    private val worldCoins = IntArray(MAX_LEVEL)
    private var resultWon = false
    private var score = 0
    private var best = 0
    private var coins = 1250
    private var gems = 75
    private var earnedCoins = 0
    private var selectedSkin = 0
    private val ownedSkins = BooleanArray(5)
    private val stars = IntArray(MAX_LEVEL)
    // Route progress is independent for BREEZE, EXPLORER and TEMPEST.
    // Existing stars and global best remain intact for save compatibility.
    private var difficulty = SkyDifficulty.EXPLORER
    private val routeStars = Array(SkyDifficulty.entries.size) { IntArray(MAX_LEVEL) }
    private val routeBests = IntArray(SkyDifficulty.entries.size)
    private var precisionPasses = 0
    private var lives = SkyPeepLives.MAX
    private var shieldSeconds = 0f
    private var healingFeedback = 0f
    private var spawnedGateCount = 0
    private var lastHeartSpawnTime = -SkyPeepLives.HEART_INTERVAL_SECONDS
    private var earnedStars = 0
    private var musicEnabled = true
    private var effectsEnabled = true
    private var vibrationEnabled = false
    private var birdY = 815f
    private var verticalSpeed = 0f
    private var flapTimer = 0f
    private var readyForFirstTap = false
    private var animationTime = 0f
    // Separate motion clock freezes scenery when paused or in a menu.
    private var flightBackgroundTime = 0f
    // Independent elapsed physics clock: gust sampling must not depend on
    // a phone's rendering refresh rate, UI redraw, or a paused animation.
    private var flightPhysicsTime = 0f
    private var lastBackgroundLogSecond = -1
    private var lastPhysicsLogSample = -1
    private var nextGate = 1.5f
    private var lastGateCenter: Float? = null
    private val gates = mutableListOf<Gate>()
    private var message = ""
    private var messageTimer = 0f

    override fun create() {
        batch = SpriteBatch()
        camera = OrthographicCamera()
        viewport = ExtendViewport(W, H, camera)
        resize(Gdx.graphics.width, Gdx.graphics.height)
        batch.projectionMatrix = camera.combined
        prefs = Gdx.app.getPreferences("SkyPeepSettingsAndProgress")
        best = readInt("best", 0).coerceAtLeast(0)
        unlocked = MAX_LEVEL
        level = readInt("level", 1).coerceIn(1, MAX_LEVEL)
        for (i in worldCoins.indices)
            worldCoins[i] = SkyPeepCoinGoal.collected(
                readInt("collected_world_${i+1}", 0))
        coins = readInt("coins", 1250).coerceAtLeast(0)
        gems = readInt("gems", 75).coerceAtLeast(0)
        selectedSkin = readInt("skin", 0).coerceIn(0, 4)
        musicEnabled = readBool("music", true)
        effectsEnabled = readBool("effects", true)
        vibrationEnabled = readBool("vibration", false)
        ownedSkins[0] = true
        for (i in 1..4) ownedSkins[i] = readBool("owned_$i", false)
        if (!ownedSkins[selectedSkin]) selectedSkin = 0
        for (i in 0 until MAX_LEVEL) stars[i] = readInt("stars_${i+1}", 0).coerceIn(0, 3)
        difficulty = SkyDifficulty.fromStored(
            readInt("difficulty", SkyDifficulty.EXPLORER.ordinal))
        for (mode in SkyDifficulty.entries) {
            routeBests[mode.ordinal] = readInt(
                "route_best_${mode.ordinal}",
                if (mode == SkyDifficulty.EXPLORER) best else 0).coerceAtLeast(0)
            for (i in 0 until MAX_LEVEL) {
                val fallback = if (mode == SkyDifficulty.EXPLORER) stars[i] else 0
                routeStars[mode.ordinal][i] = readInt(
                    "route_stars_${mode.ordinal}_${i+1}", fallback).coerceIn(0, 3)
            }
        }
        // QA may select a different license but never opens locked worlds in release APKs.
        if (qaDifficulty in SkyDifficulty.entries.indices)
            difficulty = SkyDifficulty.fromStored(qaDifficulty)
        Gdx.app.log("SkyPeepDifficulty", "loaded=${difficulty.title}")
        ownedTrails[0] = true
        for (i in 1..3) ownedTrails[i] = readBool("trail_owned_$i",false)
        selectedTrail = readInt("trail_selected",0).coerceIn(0,3)
        if (!ownedTrails[selectedTrail]) selectedTrail = 0
        if (!storageHealth.canWrite) {
            Gdx.app.error("SkyPeepStorage", "Saved entries could not be read; writes disabled to preserve existing progress")
            toast("SAVE UNAVAILABLE — PROGRESS PROTECTED")
        }
        Gdx.app.log("SkyPeepShop",
            "loaded=${SkyPeepTrailCatalog.all[selectedTrail].name}")

        // Screen textures are loaded on demand, rather than keeping every
        // full-portrait menu illustration in GPU memory at the same time.
        syncScreenArt()
        // Only retain textures for the selected level. Preloading all six worlds
        // multiplied the texture-memory footprint on lower-end Android devices.
        loadLevelArt(level)
        for (pose in arrayOf("idle", "up", "down")) load("bird_$pose")
        load("coin_asset")
        load("gem_asset")
        fontSmall = BitmapFont(Gdx.files.internal("ui_36.fnt"))
        fontLarge = BitmapFont(Gdx.files.internal("ui_62.fnt"))
        val pixel = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixel.setColor(Color.WHITE)
        pixel.fill()
        solid = SkyPeepManagedTexture.fromPixmap(pixel)
        for ((name, texture) in SkyPeepUiArtwork.createChrome()) {
            chromeTextures[name] = texture
            chrome[name] = com.badlogic.gdx.graphics.g2d.NinePatch(
                texture, 32, 32, 32, 32)
        }
        costumes = SkyPeepBirdCostumes.prepare()
        victoryStar = SkyPeepUiArtwork.createVictoryStar()
        trailTexture = SkyPeepTrailArtwork.create()
        heartTexture = SkyPeepHeartArt.create()
        Gdx.app.log("SkyPeepLifecycle",
            "generatedManaged=" + generatedTexturesManaged())
        for (kind in arrayOf("flap", "coin", "win", "fail", "click")) {
            try { sounds[kind] = Gdx.audio.newSound(Gdx.files.internal("$kind.wav")) }
            catch (_: Exception) { /* audio is optional on unsupported devices */ }
        }
        try {
            music = Gdx.audio.newMusic(Gdx.files.internal("music.ogg"))
            music?.isLooping = true
            music?.volume = .40f
            if (musicEnabled) music?.play()
        } catch (_: Exception) { music = null }
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        Gdx.input.setCatchKey(Input.Keys.ESCAPE, true)
        if (qaMapUnlocked in 1..MAX_LEVEL) {
            // Debug-only screenshot fixture: a freshly unlocked world must
            // never be shown as completed before its first successful flight.
            unlocked = MAX_LEVEL
            level = qaMapUnlocked
            for (i in 0 until MAX_LEVEL) {
                stars[i] = if (i < unlocked - 1) 3 else 0
                routeStars[difficulty.ordinal][i] = stars[i]
            }
            screen = Screen.MAP
            Gdx.app.log("SkyPeepMap","map_loaded unlocked=$unlocked selected=$level")
        }
        if (qaWorld in 1..MAX_LEVEL) {
            unlocked = MAX_LEVEL
            if(qaSkin in 0..4) {
                selectedSkin = qaSkin
                ownedSkins[qaSkin] = true
                Gdx.app.log("SkyPeepSkin","skin=" + SkyPeepSkinCatalog.get(qaSkin).name)
            }
            begin(qaWorld)
            if (qaBackgroundMotion) readyForFirstTap = false
            if (qaScore >= 0) {
                score = qaScore.coerceAtMost(999999)
                Gdx.app.log("SkyPeepHUD","debug_score=$score level=$level")
            }
            if (qaVictory) {
                worldCoins[level-1] = SkyPeepCoinGoal.REQUIRED
                finish(true)
                Gdx.app.log("SkyPeepQA","victory level=" + level)
            } else if (qaLoss) {
                finish(false)
                Gdx.app.log("SkyPeepQA","loss level=" + level)
            }
        }
    }

    /** A malformed saved entry should not crash startup or hide other valid data. */
    private fun readInt(key: String, fallback: Int): Int =
        SkyPeepPreferenceRead.integer(key, fallback,
            { name, default -> prefs.getInteger(name, default) },
            { name, cause ->
                storageHealth.onReadFailure()
                Gdx.app.error("SkyPeepStorage", "Cannot read integer preference: $name", cause)
            })

    private fun readBool(key: String, fallback: Boolean): Boolean =
        SkyPeepPreferenceRead.boolean(key, fallback,
            { name, default -> prefs.getBoolean(name, default) },
            { name, cause ->
                storageHealth.onReadFailure()
                Gdx.app.error("SkyPeepStorage", "Cannot read boolean preference: $name", cause)
            })

    private fun loadLevelArt(newLevel: Int) {
        require(newLevel in 1..MAX_LEVEL)
        if (loadedLevelArt == newLevel) return
        // Stage ALL incoming world textures before retiring old ones. If
        // decoding fails, the previous world remains renderable: no blank
        // scene, partial pillar set or half-replaced HUD is exposed.
        val staged = mutableMapOf<String, Texture>()
        var newStem: Texture? = null
        try {
            for (name in levelArtNames(newLevel)) {
                staged[name] = makeTexture(
                    name, if (name.startsWith("bg_")) "jpg" else "png")
            }
            newStem = SkyPeepPillarStem.create(newLevel)
        } catch (e: Exception) {
            staged.values.forEach { it.dispose() }
            newStem?.dispose()
            throw e
        }
        val priorWorld = loadedLevelArt
        val previousStem = pillarStem
        pictures.putAll(staged)
        pillarStem = newStem
        loadedLevelArt = newLevel
        if (priorWorld != 0) {
            for (name in levelArtNames(priorWorld))
                pictures.remove(name)?.dispose()
        }
        previousStem?.dispose()
    }

    private fun levelArtNames(world: Int): List<String> =
        listOf("bg_$world", "pillar_${world}_top", "pillar_${world}_bottom") +
            listOf("left", "score", "right", "pause").map { "hud_${it}_$world" }

    private fun syncScreenArt() {
        // Reuse the current illustration while in the same screen, and dispose
        // the previous full-screen JPG before switching to another UI page.
        // Victory must never render the source illustration's baked-in GAME OVER text.
        val required = if (screen == Screen.RESULT && resultWon) null
                       else SkyPeepAssets.screenFor(screen.name)
        if (required == loadedScreenArt) return
        // Commit the incoming menu art first. Switching screens must not
        // briefly leave the texture map empty while an old JPG is disposed.
        if (required != null && !pictures.containsKey(required))
            pictures[required] = makeTexture(required, "jpg")
        val previous = loadedScreenArt
        loadedScreenArt = required
        if (previous != null && previous != required)
            pictures.remove(previous)?.dispose()
    }

    private fun makeTexture(name: String, suffix: String): Texture =
        Texture(Gdx.files.internal("$name.$suffix")).also {
            it.setFilter(Texture.TextureFilter.Linear,Texture.TextureFilter.Linear)
        }

    private fun load(name: String, suffix: String = "png") {
        pictures[name] = makeTexture(name,suffix)
    }

    private fun image(name: String, x: Float, y: Float, width: Float, height: Float) {
        pictures[name]?.let { batch.draw(it, x, y, width, height) }
    }

    override fun resize(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        visibleFrame = SkyPeepScreenFrame.forPixels(width, height)
        // ExtendViewport consumes the entire device display. Recenter its
        // expanded world around the original reference art rather than
        // shifting the buttons, HUD, collision world or tap targets.
        viewport.update(width, height, false)
        camera.position.set(W*.5f, H*.5f, 0f)
        camera.update()
        Gdx.app.log("SkyPeepViewport",
            "device=${width}x${height} frame=" +
            "${visibleFrame.left},${visibleFrame.bottom}.." +
            "${visibleFrame.right},${visibleFrame.top}")
    }

    override fun render() {
        val delta = SkyPeepPhysicsStep.boundedDelta(Gdx.graphics.deltaTime)
        animationTime += delta
        if (screen == Screen.PLAY && !readyForFirstTap) flightBackgroundTime += delta
        if (messageTimer > 0f) messageTimer -= delta
        handleInput()
        if (screen == Screen.PLAY) updateGame(delta)
        syncScreenArt()
        if (screen != lastLoggedScreen) {
            Gdx.app.log("SkyPeepNav","screen=" + screen.name)
            lastLoggedScreen = screen
        }
        Gdx.gl.glClearColor(.16f,.67f,.96f,1f)
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT)
        viewport.apply()
        batch.projectionMatrix = camera.combined
        batch.begin()
        drawFullBleedEdges()
        when (screen) {
            Screen.SPLASH -> image("screen_splash", 0f, 0f, W, H)
            Screen.MENU -> { image("screen_menu", 0f, 0f, W, H); drawMenuWallet() }
            Screen.MAP -> { image("screen_map", 0f, 0f, W, H); drawMapProgress() }
            Screen.PLAY -> drawGameplay()
            Screen.PAUSE -> { image("screen_pause", 0f, 0f, W, H); drawPauseToggles() }
            Screen.RESULT -> {
                if (resultWon) drawVictory() else {
                    image("screen_over", 0f, 0f, W, H)
                    drawResult()
                }
            }
            Screen.SKINS -> { image("screen_skins", 0f, 0f, W, H); drawSkins() }
            Screen.SHOP -> { image(panelBackdropName(),0f,0f,W,H); drawShop() }
            Screen.SETTINGS -> { image(panelBackdropName(), 0f, 0f, W, H); drawSettings() }
        }
        if (messageTimer > 0 && screen != Screen.PLAY) {
            badge("dark",110f,1375f,W-220f,103f)
            fitted(message,1437f,fontSmall,1.4f,W-290f)
        }
        batch.end()
        updateRenderingPolicy()
    }

    /**
     * Toggle continuous GL rendering only when changing between animated
     * flight/temporary messages and otherwise static illustrated screens.
     * Touch input and Android lifecycle events still request a fresh frame.
     */
    private fun updateRenderingPolicy() {
        val required = SkyPeepRenderPolicy.continuous(screen.name,messageTimer)
        if (required != lastContinuousRendering) {
            Gdx.graphics.setContinuousRendering(required)
            lastContinuousRendering = required
            Gdx.app.log("SkyPeepPower",
                "screen=${screen.name} continuous=$required")
        }
    }

    /**
     * Paint the *real artwork's outer edge pixels* into ExtendViewport's
     * additional display area. The illustrated 941x1672 UI stays undistorted
     * and every original touch rectangle remains at its reference position.
     * No bars are left blank on tall phones or wide tablets.
     */
    private fun drawFullBleedEdges() {
        val name = when(screen) {
            Screen.PLAY -> "bg_$level"
            Screen.RESULT -> if(resultWon) "bg_$level" else "screen_over"
            Screen.SHOP, Screen.SETTINGS -> panelBackdropName()
            Screen.SPLASH -> "screen_splash"
            Screen.MENU -> "screen_menu"
            Screen.MAP -> "screen_map"
            Screen.PAUSE -> "screen_pause"
            Screen.SKINS -> "screen_skins"
        }
        val texture = pictures[name] ?: return
        val f=visibleFrame
        // Sample broad ORIGINAL scenery regions for overscan. Stretching a
        // four-pixel sliver created visibly empty, flat strips on tall devices.
        // Artwork and all UI elements inside the 941x1672 authored canvas stay
        // at 1:1 proportions, while only the safe overscan area is extended.
        val safeScenery = SkyPeepFullBleed.usesScenery(
            screen.name, resultWon)
        // For UI screens use only the true outer 12px SKY/SCENERY ROWS,
        // not a tall 1/8-image slice that could repeat the logo or wallet.
        // Always sample their FULL WIDTH: stretching just 12 source columns
        // across 941 logical pixels created the user's flat horizontal bars.
        val bottomBand = SkyPeepFullBleed.horizontalBand(
            texture.width, texture.height, safeScenery, bottom = true)
        val topBand = SkyPeepFullBleed.horizontalBand(
            texture.width, texture.height, safeScenery, bottom = false)
        val sideSlice=SkyPeepFullBleed.sample(texture.width)
        // Menu/level/skin illustrations contain wallet controls and a logo in
        // their full-width top bands. Sampling those bands would duplicate
        // clickable-looking foreground graphics outside the authored canvas.
        // Only use the outermost sky strip for non-gameplay overscan.
        val menuSafeWidth=SkyPeepFullBleed.safeUiStrip(texture.width)
        val horizontalSourceWidth=if(safeScenery) sideSlice else menuSafeWidth
        if(f.extraY>.01f) {
            batch.draw(texture,0f,-f.extraY,W,f.extraY,
                bottomBand.x,bottomBand.y,
                bottomBand.width,bottomBand.height,false,false)
            batch.draw(texture,0f,H,W,f.extraY,
                topBand.x,topBand.y,
                topBand.width,topBand.height,false,false)
        }
        if(f.extraX>.01f) {
            batch.draw(texture,-f.extraX,-f.extraY,
                f.extraX,H+2f*f.extraY,
                0,0,horizontalSourceWidth,texture.height,false,false)
            batch.draw(texture,W,-f.extraY,
                f.extraX,H+2f*f.extraY,
                texture.width-horizontalSourceWidth,0,
                horizontalSourceWidth,texture.height,false,false)
        }
        // Shop and Settings darken their staged WORLD scene behind their
        // parchment panels. Extend the SAME shade into extra margins:
        // otherwise tall devices show a bright rectangular seam around the
        // centre panel while its photographed background is dimmed.
        val shade=when(screen) {
            Screen.SHOP -> SHOP_SHADE
            Screen.SETTINGS -> SETTINGS_SHADE
            Screen.RESULT -> if(resultWon) RESULT_SHADE else null
            else -> null
        }
        if(shade!=null) {
            if(f.extraY>.01f) {
                drawRect(0f,-f.extraY,W,f.extraY,shade)
                drawRect(0f,H,W,f.extraY,shade)
            }
            if(f.extraX>.01f) {
                drawRect(-f.extraX,-f.extraY,f.extraX,
                    H+2f*f.extraY,shade)
                drawRect(W,-f.extraY,f.extraX,
                    H+2f*f.extraY,shade)
            }
        }
    }

    private fun handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK) || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            when (screen) {
                Screen.PLAY -> screen = Screen.PAUSE
                Screen.PAUSE -> screen = Screen.PLAY
                Screen.SPLASH -> screen = Screen.MENU
                else -> screen = Screen.MENU
            }
            playSound("click")
            return
        }
        if (!Gdx.input.justTouched()) return
        touch.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        viewport.unproject(touch)
        if (touch.x < visibleFrame.left || touch.x > visibleFrame.right ||
            touch.y < visibleFrame.bottom || touch.y > visibleFrame.top) return
        // Extra viewport strips are scenery only on menu/dialogue pages.
        // During gameplay tapping any visible sky should still flap.
        if (screen != Screen.PLAY &&
            (touch.x < 0f || touch.x > W ||
             touch.y < 0f || touch.y > H)) return
        val x = touch.x
        val yTop = H - touch.y
        when (screen) {
            Screen.SPLASH -> { screen = Screen.MENU; playSound("click") }
            Screen.MENU -> clickMenu(x, yTop)
            Screen.MAP -> clickMap(x, yTop)
            Screen.PLAY -> {
                if (SkyPeepHudLayout.pauseContains(visibleFrame,x,yTop)) {
                    screen = Screen.PAUSE
                    playSound("click")
                }
                else flap()
            }
            Screen.PAUSE -> clickPause(x,yTop)
            Screen.RESULT -> clickResult(x,yTop)
            Screen.SKINS -> clickSkins(x,yTop)
            Screen.SHOP -> clickShop(x,yTop)
            Screen.SETTINGS -> clickSettings(x,yTop)
        }
    }

    private fun hit(x: Float,y: Float, l: Float,t: Float,w: Float,h: Float): Boolean =
        x >= l && x <= l+w && y >= t && y <= t+h

    private fun clickMenu(x: Float, y: Float) {
        // Shared artwork hitboxes avoid the former six-pixel menu row overlap.
        when (SkyPeepMenuTouch.actionAt(x, y)) {
            SkyPeepMenuTouch.Action.PLAY -> { playSound("click"); begin(level) }
            SkyPeepMenuTouch.Action.LEVELS -> { playSound("click"); screen = Screen.MAP }
            SkyPeepMenuTouch.Action.SKINS -> { playSound("click"); screen = Screen.SKINS }
            SkyPeepMenuTouch.Action.SHOP -> { playSound("click"); openWorldPanel(Screen.SHOP) }
            SkyPeepMenuTouch.Action.SETTINGS -> { playSound("click"); openWorldPanel(Screen.SETTINGS) }
            SkyPeepMenuTouch.Action.COINS -> { playSound("click"); toast("COLLECT COINS IN FLIGHT") }
            SkyPeepMenuTouch.Action.GEMS -> { playSound("click"); toast("COLLECT GEMS IN FLIGHT") }
            null -> Unit
        }
    }

    /**
     * Selecting an island on MAP only updates the selected-world preference.
     * When opening Shop/Settings later, load its scenery BEFORE switching
     * screens. If decoding fails the last working world remains renderable.
     */
    private fun openWorldPanel(next: Screen) {
        require(next == Screen.SHOP || next == Screen.SETTINGS)
        if (SkyPeepPanelArtwork.needsStage(level, loadedLevelArt)) {
            try {
                loadLevelArt(level)
            } catch (error: Exception) {
                Gdx.app.error("SkyPeepAssets",
                    "Panel world $level could not be loaded", error)
                toast("WORLD BACKGROUND UNAVAILABLE")
            }
        }
        screen = next
        Gdx.app.log("SkyPeepAssets",
            "panel=${next.name} selected=$level loaded=$loadedLevelArt")
    }

    /** Never paint a background texture that has not been loaded yet. */
    private fun panelBackdropName(): String =
        SkyPeepPanelArtwork.backgroundFor(loadedLevelArt) ?: "bg_$level"

    private fun clickMap(x: Float, y: Float) {
        when {
            SkyPeepMapLayout.back.contains(x,y) -> {
                playSound("click")
                screen = Screen.MENU
            }
            SkyPeepMapLayout.play.contains(x,y) -> {
                playSound("click")
                begin(level)
            }
            else -> {
                val tapped=SkyPeepMapLayout.worldAt(x,y) ?: return
                playSound("click")
                // All six illustrated islands are immediately selectable.
                level=tapped
                save()
                Gdx.app.log("SkyPeepMap","selected_world=$level")
                toast("SELECTED: ${levelTitle(level)}")
            }
        }
    }

    private fun clickPause(x: Float, y: Float) {
        // Blank gutters and the darkened background remain inert.
        val action = SkyPeepUiTouch.pauseActionAt(x, y) ?: return
        playSound("click")
        when (action) {
            SkyPeepUiTouch.PauseAction.RESUME -> screen = Screen.PLAY
            SkyPeepUiTouch.PauseAction.RESTART -> begin(level)
            SkyPeepUiTouch.PauseAction.HOME -> { screen = Screen.MENU; save() }
            SkyPeepUiTouch.PauseAction.MUSIC -> setMusic(!musicEnabled)
            SkyPeepUiTouch.PauseAction.SFX -> {
                effectsEnabled = !effectsEnabled
                save()
            }
        }
    }

    private fun clickResult(x: Float, y: Float) {
        // A tap in the card's empty space or between drawn buttons is inert.
        // Touch geometry is kept outside rendering code for unit testing.
        when (SkyPeepResultTouch.actionAt(x, y, resultWon)) {
            SkyPeepResultTouch.Action.HOME -> {
                playSound("click")
                screen = Screen.MENU
                save()
            }
            SkyPeepResultTouch.Action.RETRY_OR_NEXT -> {
                playSound("click")
                val nextLevel = if (resultWon && level < MAX_LEVEL) level + 1 else level
                begin(nextLevel)
            }
            SkyPeepResultTouch.Action.SHARE -> {
                playSound("click")
                val msg = "SkyPeep | ${levelTitle(level)} | ${difficulty.title} | Score $score | Best ${routeBests[difficulty.ordinal]}"
                if (shareText != null) shareText.invoke(msg)
                else Gdx.app.clipboard.contents = msg
                toast("SHARE RESULT")
            }
            null -> Unit
        }
    }

    private fun clickSkins(x: Float, y: Float) {
        if (y < 140f && x in 240f..355f) {
            playSound("click")
            toast("EARN COINS IN FLIGHT")
            return
        }
        if (y < 140f && x in 825f..935f) {
            playSound("click")
            toast("COLLECT GEMS IN FLIGHT")
            return
        }
        if (SkyPeepUiTouch.skinsBack(x,y)) {
            playSound("click")
            screen = Screen.MENU
            return
        }
        if (SkyPeepUiTouch.defaultEquipAt(x,y)) {
            if (selectedSkin != 0 && !ensurePurchaseStorageReady()) return
            playSound("click")
            if (selectedSkin != 0) {
                selectedSkin = 0
                save()
                if (lastSaveFailed) {
                    toast("EQUIP CHANGE NOT SAVED")
                    return
                }
            }
            Gdx.app.log("SkyPeepSkin", "equipped=0")
            toast("DEFAULT EQUIPPED")
            return
        }
        // Card art and prices are informational, only the BUY/EQUIP control is actionable.
        val skin = SkyPeepUiTouch.skinPurchaseAt(x,y) ?: return
        if (!ensurePurchaseStorageReady()) return
        playSound("click")
        // Single-charge wallet operation, including repeated EQUIP taps.
        val choice = SkyPeepSkinCatalog.get(skin)
        val purchase = SkyPeepSkinCatalog.purchase(skin, ownedSkins[skin], coins, gems)
        if (!purchase.success) {
            toast("NOT ENOUGH ${if (choice.currency == SkinCurrency.GEMS) "GEMS" else "COINS"}")
            playSound("fail")
            return
        }
        coins = purchase.coins
        gems = purchase.gems
        ownedSkins[skin] = true
        selectedSkin = skin
        save()
        if (lastSaveFailed) {
            toast("PURCHASE NOT SAVED - CHECK STORAGE")
            return
        }
        Gdx.app.log("SkyPeepSkin", "equipped=$skin")
        toast(if (purchase.alreadyOwned) "${skinName(skin)} EQUIPPED"
              else "${skinName(skin)} UNLOCKED")
        if (!purchase.alreadyOwned) playSound("win")
    }

    private fun clickShop(x:Float,yTop:Float) {
        if(SkyPeepUiTouch.shopBack(x,yTop)) {
            playSound("click")
            screen=Screen.MENU
            save()
            return
        }
        // A purchase must originate from the visible BUY/EQUIP badge.
        val offerId = SkyPeepUiTouch.shopPurchaseAt(x,yTop) ?: return
        if (!ensurePurchaseStorageReady()) return
        val offer = SkyPeepTrailCatalog.all[offerId]
        val purchased=SkyPeepTrailCatalog.purchase(
            offer.id,ownedTrails[offer.id],coins,gems)
        if (!purchased.success) {
            toast("NOT ENOUGH " + (if(offer.currency==TrailCurrency.GEMS) "GEMS" else "COINS"))
            playSound("fail")
            return
        }
        coins=purchased.coins
        gems=purchased.gems
        ownedTrails[offer.id]=true
        selectedTrail=offer.id
        save()
        if (lastSaveFailed) {
            toast("PURCHASE NOT SAVED - CHECK STORAGE")
            return
        }
        toast(if(purchased.alreadyOwned) "${offer.name} EQUIPPED"
              else "${offer.name} UNLOCKED")
        // Avoid writing wallet balances into globally accessible diagnostic logs.
        Gdx.app.log("SkyPeepShop","selected=${offer.name}")
        playSound(if(purchased.alreadyOwned) "click" else "win")
    }

    /**
     * Do not charge the wallet or change ownership when existing stored data
     * was unreadable or a previous full snapshot may not have reached disk.
     * Retry the complete known in-memory state before any new transaction.
     */
    private fun ensurePurchaseStorageReady(): Boolean {
        if (storageHealth.canWrite && storageHealth.needsFullSnapshot) save()
        if (storageHealth.purchaseReady && !lastSaveFailed) return true
        toast("SAVE UNAVAILABLE - PURCHASE BLOCKED")
        Gdx.app.error("SkyPeepStorage", "Blocked cosmetic change until storage recovers")
        return false
    }

    private fun clickSettings(x: Float, y: Float) {
        if (SkyPeepUiTouch.settingsBack(x,y)) {
            playSound("click")
            screen=Screen.MENU
            save()
            return
        }
        // Non-interactive gutters stay silent, and the visible fourth row
        // alone cycles the player's route license.
        val row = SkyPeepUiTouch.settingsRowAt(x,y) ?: return
        playSound("click")
        when (row) {
            0 -> setMusic(!musicEnabled)
            1 -> { effectsEnabled=!effectsEnabled; save() }
            2 -> {
                vibrationEnabled = !vibrationEnabled
                save()
                Gdx.app.log("SkyPeepSettings", "vibration=$vibrationEnabled")
            }
            3 -> {
                difficulty = difficulty.next()
                save()
                toast("SKY ROUTE: ${difficulty.title}")
                Gdx.app.log("SkyPeepDifficulty","selected=${difficulty.title}")
            }
        }
    }

    private fun setMusic(enabled:Boolean) {
        musicEnabled=enabled
        updateMusicPlayback(enabled)
        save()
    }

    private fun updateMusicPlayback(playing:Boolean) {
        // Audio focus/device changes must never crash an otherwise offline
        // flight. The sound resource is optional and will be disposed at exit.
        try {
            if (playing) music?.play() else music?.pause()
        } catch (_: Exception) {
            Gdx.app.log("SkyPeepAudio","Music unavailable during playback transition")
        }
    }

    private fun begin(chosen: Int) {
        val targetLevel = chosen.coerceIn(1, MAX_LEVEL)
        // A corrupt/missing world texture must not terminate the app or
        // mutate selected progress. loadLevelArt stages incoming textures
        // before retiring the previous world.
        try {
            loadLevelArt(targetLevel)
        } catch (e: Exception) {
            Gdx.app.error("SkyPeepAssets", "Failed loading world $targetLevel", e)
            toast("WORLD COULD NOT BE LOADED")
            return
        }
        level = targetLevel
        screen = Screen.PLAY
        score=0
        earnedCoins=0
        flightHud.reset()
        resultWon=false
        precisionPasses=0
        earnedStars=0
        lives = SkyPeepLives.MAX
        shieldSeconds = 0f
        healingFeedback = 0f
        spawnedGateCount = 0
        lastHeartSpawnTime = -SkyPeepLives.HEART_INTERVAL_SECONDS
        nextGate=2.0f
        gates.clear()
        lastGateCenter = if (qaPillars) 1050f else null
        if (qaPillars) gates.add(Gate(W - 300f,1050f))
        verticalSpeed=0f
        birdY=840f
        flapTimer=0f
        animationTime=0f
        flightBackgroundTime=0f
        flightPhysicsTime=0f
        lastBackgroundLogSecond=-1
        lastPhysicsLogSample=-1
        readyForFirstTap=true
        Gdx.app.log("SkyPeepLives","run_start lives=$lives")
        save()
    }

    private fun flap() {
        readyForFirstTap=false
        verticalSpeed=690f
        flapTimer=.13f
        playSound("flap")
    }

    private fun updateGame(dt: Float) {
        // Keep gameplay contact checks stable on slower Android devices:
        // 50ms rendering frames are integrated as up to six smaller steps.
        // Bird motion and moving gates therefore cannot skip over contact
        // regions merely because a frame took longer to render.
        val frameSeconds = SkyPeepPhysicsStep.boundedDelta(dt)
        val steps = SkyPeepPhysicsStep.substepCount(frameSeconds)
        if (steps == 0) return
        val stepSeconds = frameSeconds / steps
        repeat(steps) {
            // A contact or completed route ends this render frame immediately.
            if (screen != Screen.PLAY) return
            updateGameStep(stepSeconds)
        }
    }

    private fun updateGameStep(dt: Float) {
        // Debug-only GL fixture: observe active background movement without
        // collisions/gravity terminating the run during emulator capture.
        if (qaBackgroundMotion && qaWorld in 1..MAX_LEVEL) return
        if (readyForFirstTap) return
        shieldSeconds = SkyPeepLives.tickShield(shieldSeconds,dt)
        healingFeedback = max(0f,healingFeedback-dt)
        flapTimer = max(0f,flapTimer-dt)
        val gravity=SkyRouteRules.gravity(difficulty)
        val skyCurrent=SkyPeepWindClock.windAtStepMidpoint(
            level,difficulty,flightPhysicsTime,dt)
        flightPhysicsTime = SkyPeepWindClock.advance(flightPhysicsTime,dt)
        // Debug-only real-physics QA: log while a live flight updates, never
        // during the frozen-background visual screenshot fixture.
        if (qaDifficulty >= 0 && qaWorld in 1..MAX_LEVEL) {
            val sample=(flightBackgroundTime * 5f).toInt()
            if(sample!=lastPhysicsLogSample) {
                lastPhysicsLogSample=sample
                Gdx.app.log("SkyPeepPhysics",
                    "world=$level mode=${difficulty.title} wind=${skyCurrent.toInt()} " +
                    "gravity=${gravity.toInt()} gap=${SkyRouteRules.gap(level,difficulty).toInt()} " +
                    "speed=${SkyRouteRules.speed(level,difficulty).toInt()}")
            }
        }
        // Preserve Explorer's original -900 px/s terminal velocity, while
        // making Breeze gentler and Tempest's descent more demanding.
        verticalSpeed = max(-900f * difficulty.gravityFactor,
            verticalSpeed + (skyCurrent - gravity)*dt)
        birdY += verticalSpeed * dt
        var birdBody=SkyPeepContact.body(BIRD_X,birdY,BIRD_W,BIRD_H)
        if (SkyPeepContact.hitsWorldEdge(birdBody,
                visibleFrame.bottom,visibleFrame.top)) {
            // Recompute the collision body after recovering to the painted
            // edge: the stale pre-bounce ellipse could cause a false stone
            // hit or miss a real coin/gem/heart pickup during the same step.
            val recovery=SkyPeepWorldBounds.recover(
                birdY,visibleFrame.bottom,visibleFrame.top,BIRD_H)
            birdY=recovery.birdY
            verticalSpeed=recovery.speedY
            birdBody=SkyPeepContact.body(BIRD_X,birdY,BIRD_W,BIRD_H)
            if (takeDamage()) return
        }
        val gap = SkyRouteRules.gap(level,difficulty)
        val moveSpeed = SkyRouteRules.speed(level,difficulty)
        nextGate -= dt
        if (nextGate <= 0f) {
            nextGate=SkyPeepGatePass.nextSpawnCountdown(
                nextGate, SkyRouteRules.spawnSeconds(level,difficulty))
            val center = SkyPeepGatePlacement.next(
                lastGateCenter, level, difficulty, MathUtils.random())
            lastGateCenter = center
            spawnedGateCount++
            val withHeart = SkyPeepLives.spawnHeart(
                lives,flightPhysicsTime,lastHeartSpawnTime,spawnedGateCount)
            if (withHeart) lastHeartSpawnTime = flightPhysicsTime
            gates.add(Gate(SkyPeepScreenFrame.pillarSpawnX(visibleFrame),
                center,gem=(score>0 && !withHeart && MathUtils.random(0,9)==0),
                heart=withHeart))
        }
        val iterator=gates.iterator()
        while (iterator.hasNext()) {
            val gate=iterator.next()
            gate.x -= moveSpeed * dt
            val gateWidth=GATE_WIDTH
            val topLimit=gate.centerY+gap*.5f
            val bottomLimit=gate.centerY-gap*.5f
            if (shieldSeconds <= 0f && SkyPeepContact.hitsGate(
                    birdBody,gate.x,gateWidth,bottomLimit,topLimit)) {
                if (takeDamage()) return
            }
            // Same centre and 37-unit radius as drawCoin below. Only actual
            // torso/pickup overlap earns a coin, never a broad invisible halo.
            if (!gate.coinTaken && SkyPeepPickup.overlaps(
                birdBody, gate.x + 118f, gate.centerY + 9f,
                SkyPeepPickup.COIN_RADIUS
            )) {
                gate.coinTaken=true
                // One physically collected coin = one world-goal pickup.
                // Spending wallet currency cannot reduce world progress.
                coins = SkyPeepEconomy.credit(coins,5)
                earnedCoins = SkyPeepEconomy.credit(earnedCoins,5)
                flightHud.collectCoin()
                val beforePickup=worldCoins[level-1]
                worldCoins[level-1] = SkyPeepCoinGoal.increment(beforePickup)
                playSound("coin")
                if (SkyPeepCoinGoal.justCompleted(
                    beforePickup,worldCoins[level-1])) {
                    finish(true)
                    return
                }
                // Persist only the wallet and this world's collection;
                // rewriting all route stats/skins on every pickup stalls GL.
                savePickup()
            }
            // Gem centre and 41-unit radius exactly follow drawDiamond.
            if (gate.gem && !gate.gemTaken &&
                SkyPeepPickup.overlaps(birdBody,
                    gate.x + 113f, gate.centerY + 93f,
                    SkyPeepPickup.GEM_RADIUS)) {
                gate.gemTaken=true
                gems = SkyPeepEconomy.credit(gems,1)
                flightHud.collectGem()
                playSound("coin")
                savePickup() // Durably commit the actual gem pickup.
            }
            if (gate.heart && !gate.heartTaken && SkyPeepPickup.overlaps(
                    birdBody,gate.x+118f,gate.centerY-100f,
                    SkyPeepLives.HEART_RADIUS)) {
                gate.heartTaken = true
                val restored = SkyPeepLives.heal(lives)
                if (restored > lives) {
                    lives = restored
                    healingFeedback = 1.4f
                    playSound("win")
                }
            }
            // Precision belongs to the moment the gate MIDPOINT crosses the
            // bird. Sampling on the score screen's later trailing-edge pass
            // penalised flaps that happened safely after crossing the opening.
            if (!gate.precisionSampled &&
                SkyPeepGatePass.reachedBirdCentre(gate.x,gateWidth,birdBody.cx)) {
                gate.precisionSampled=true
                gate.preciseAtCrossing=SkyRouteRules.isPrecisionPass(
                    birdBody.cy,gate.centerY,gap)
            }
            if (!gate.scored && SkyPeepGatePass.hasClearedBody(
                gate.x, gateWidth, birdBody.left, SkyPeepContact.CONTACT_GRACE)) {
                gate.scored=true
                if (score < Int.MAX_VALUE) score++
                val oldBest = best
                val oldRouteBest = routeBests[difficulty.ordinal]
                best=max(best,score)
                routeBests[difficulty.ordinal]=max(oldRouteBest,score)
                // Preserve new records even when Android stops the ongoing
                // flight before an ordinary pause/result full-save.
                if (best != oldBest || routeBests[difficulty.ordinal] != oldRouteBest)
                    saveRecord()
                // Precision affects stars only, never bonus coins.
                if (gate.precisionSampled && gate.preciseAtCrossing)
                    precisionPasses++
            }
            if (gate.x < SkyPeepScreenFrame.pillarDespawnX(visibleFrame))
                iterator.remove()
        }
    }

    /**
     * One hit costs ONE life; a shield covers the full length of a pipe so a
     * single obstacle cannot repeatedly consume lives at low frame rates.
     */
    private fun takeDamage(): Boolean {
        val damage = SkyPeepLives.hit(lives,shieldSeconds)
        if (!damage.tookDamage) return false
        lives = damage.remaining
        if (lives == 0) {
            finish(false)
            return true
        }
        shieldSeconds = SkyPeepLives.SHIELD_SECONDS
        playSound("fail")
        if (vibrationEnabled) try { Gdx.input.vibrate(30) } catch (_:Exception) { }
        return false
    }

    private fun finish(won: Boolean) {
        resultWon = won
        screen = Screen.RESULT
        best=max(best,score)
        routeBests[difficulty.ordinal]=max(routeBests[difficulty.ordinal],score)
        if (won) {
            earnedStars=SkyRouteRules.starsOnCompletion(precisionPasses,score)
            routeStars[difficulty.ordinal][level-1]=max(
                routeStars[difficulty.ordinal][level-1],earnedStars)
            stars[level-1]=max(stars[level-1],earnedStars)
            unlocked=MAX_LEVEL
            playSound("win")
        } else {
            playSound("fail")
            if (vibrationEnabled) try { Gdx.input.vibrate(40) } catch (_:Exception) { }
        }
        save()
    }

    private fun drawGameplay() {
        // Keep the illustrated composition edge-to-edge while introducing
        // subtle depth. No wrap seam, external image, or gameplay collision shift.
        pictures["bg_$level"]?.let { background ->
            // Reuse the same pair of crop coordinates on every GL frame.
            // Camera movement and the initial 1.4s fade remain unchanged.
            SkyPeepBackgroundMotion.cropInto(
                background.width, flightBackgroundTime,
                !readyForFirstTap, backgroundCrop)
            if (qaBackgroundMotion && qaWorld in 1..MAX_LEVEL) {
                val second = flightBackgroundTime.toInt()
                if (second != lastBackgroundLogSecond) {
                    Gdx.app.log("SkyPeepBackground", "second=$second offset_px=${backgroundCrop.sourceX} world=$level")
                    lastBackgroundLogSecond = second
                }
            }
            batch.draw(background,0f,0f,W,H,
                backgroundCrop.sourceX,0,backgroundCrop.sourceWidth,
                background.height,false,false)
        }
        val gap = SkyRouteRules.gap(level,difficulty)
        // Resolve illustrated pillar textures once per rendered frame, not
        // once for every active gate. The full asset set was checked at load.
        val top = pictures["pillar_${level}_top"] ?: return
        val bottom = pictures["pillar_${level}_bottom"] ?: return
        for (gate in gates) {
            val topY = gate.centerY+gap*.5f
            val bottomY = gate.centerY-gap*.5f
            val pillarWidth=GATE_WIDTH
            // The pole stems are rendered behind the photographed caps as scalable 2D shapes.
            // Continuous, textured shafts sit behind the original illustrated caps.
            val stemX = gate.x + PillarGeometry.STEM_INSET
            val stemW = PillarGeometry.STEM_WIDTH
            drawStoneStem(stemX,stemW,
                PillarGeometry.topTailStart(topY,top.height.toFloat()),
                visibleFrame.top)
            drawStoneStem(stemX,stemW,visibleFrame.bottom,
                PillarGeometry.bottomTailHeight(bottomY,bottom.height.toFloat()))
            batch.draw(top,gate.x,topY,pillarWidth,top.height.toFloat())
            batch.draw(bottom,gate.x,bottomY-bottom.height,
                pillarWidth,bottom.height.toFloat())
            if (!gate.coinTaken) {
                drawCoin(gate.x+118f,gate.centerY+9f,SkyPeepPickup.COIN_RADIUS)
            }
            if (gate.gem && !gate.gemTaken) {
                drawDiamond(gate.x+113f,gate.centerY+93f,SkyPeepPickup.GEM_RADIUS)
            }
            if (gate.heart && !gate.heartTaken) {
                drawHeart(gate.x+118f,gate.centerY-100f,SkyPeepLives.HEART_RADIUS)
            }
        }
        drawFlightTrail()
        drawBird()
        drawHUD()
        if (healingFeedback > 0f)
            fitted("+1 LIFE",(birdY+BIRD_H+52f).coerceAtMost(H-236f),
                fontSmall,1.0f,202f,BIRD_X+BIRD_W*.5f)

    }

    private fun drawStoneStem(x:Float,width:Float,start:Float,end:Float) {
        val tile=pillarStem ?: return
        var y=max(visibleFrame.bottom,start)
        val stop=min(visibleFrame.top,end)
        while(y<stop-0.5f) {
            val size=min(144f,stop-y)
            // Crop the final partial repeat instead of distorting its texture.
            // Its trailing source rows meet the previous full repeat cleanly.
            val sourceHeight = kotlin.math.ceil(size.toDouble()).toInt().coerceIn(1, 144)
            val sourceY = PillarGeometry.trailingSourceY(size)
            batch.draw(tile,x,y,width,size,0,sourceY,128,sourceHeight,false,false)
            y+=size
        }
    }

    private fun drawFlightTrail() {
        if (readyForFirstTap) return
        val tx=trailTexture ?: return
        val tint=TRAIL_TINTS[selectedTrail.coerceIn(0,TRAIL_TINTS.lastIndex)]
        batch.color=tint
        val drift=kotlin.math.sin(animationTime*9f)*7f
        batch.draw(tx,BIRD_X-158f,birdY+41f+drift,180f,129f)
        batch.color=Color.WHITE
    }

    private fun drawBird() {
        val pose=if (flapTimer>0 || ((animationTime*8f).toInt()%3==1)) "up"
                 else if ((animationTime*8f).toInt()%3==2) "down" else "idle"
        // Preserve the exact original 2D source pixels; slight angular bob through vertical physics.
        val tx=pictures["bird_$pose"] ?: return
        val alt=if (shieldSeconds>0f) SHIELD_TINT
                else BIRD_TINTS[selectedSkin.coerceIn(0,BIRD_TINTS.lastIndex)]
        batch.color=alt
        // Each bird pose is cropped differently in the source sprites.
        // Use its own natural aspect ratio without shifting SkyPeep's centre.
        batch.draw(tx,
            BIRD_X + SkyPeepBirdLayout.leftFor(tx.width,tx.height),
            birdY,
            SkyPeepBirdLayout.widthFor(tx.width,tx.height),BIRD_H)
        batch.color=Color.WHITE
        // Separate costume layer remains crisp across each of the 3 wing poses.
        if (selectedSkin != 0) costumes[selectedSkin]?.let { costume ->
            batch.draw(costume,
                BIRD_X + SkyPeepBirdLayout.COSTUME_X,
                birdY + BIRD_H - SkyPeepBirdLayout.COSTUME_Y_FROM_TOP,
                SkyPeepBirdLayout.COSTUME_WIDTH,
                SkyPeepBirdLayout.COSTUME_HEIGHT)
        }
    }

    private fun drawHUD() {
        // Exact cropped reference HUD layers. Dynamic figures replace reference mockup values.
        // On tall displays HUD art belongs to the PHYSICAL top of the
        // expanded world, never floating below an unused top inset.
        val top=visibleFrame.top
        val left = SkyPeepHudLayout.coinX(visibleFrame)
        val right = SkyPeepHudLayout.gemX(visibleFrame)
        val pause = SkyPeepHudLayout.pauseX(visibleFrame)
        // Source-illustrated coin, gemstone, pause and winged score artwork.
        // The rebuilt score texture keeps its native ratio instead of a
        // vertically squashed banner; preserve its top position.
        // Match reconstructed original PNG proportions (269:113 and
        // 191:115) while preserving their previous top-edge anchors.
        image("hud_left_$level", left, top-109f,
            SkyPeepHudLayout.COIN_PLATE_WIDTH,
            SkyPeepHudLayout.COIN_PLATE_HEIGHT)
        image("hud_right_$level", right, top-111f,
            SkyPeepHudLayout.GEM_PLATE_WIDTH,
            SkyPeepHudLayout.GEM_PLATE_HEIGHT)
        image("hud_pause_$level", pause, top-129f, 125f, 122f)
        image("hud_score_$level", 313f, top-188f,
            SkyPeepHudLayout.SCORE_WIDTH,
            SkyPeepHudLayout.SCORE_HEIGHT)
        // Three lives and the per-world goal occupy the LEFT status lane.
        // They must never overlay the original central winged score ribbon.
        for (i in 0 until SkyPeepLives.MAX) {
            if (i >= lives) batch.color = EMPTY_HEART_TINT
            drawHeart(SkyPeepHudLayout.heartCenterX(visibleFrame,i),
                SkyPeepHudLayout.heartCenterY(visibleFrame),21f)
            batch.color = Color.WHITE
        }
        // Flight HUD numbers are real pickups for THIS RUN, not spendable
        // wallet totals from Menu/Skins/Shop. Preserve wallet and progress
        // Preferences separately, with no migration or economy change.
        fitted(hudText.coins(flightHud.coinsCollected),top-52f,
            fontSmall,1.27f,108f,left+171f)
        fitted(hudText.gems(flightHud.gemsCollected),top-51f,
            fontSmall,1.25f,70f,right+122f)
        drawWingedScore()
        // Real collected pickups (not spendable wallet balance), now off
        // the banner and never competing with the centre score numerals.
        val goalX=SkyPeepHudLayout.goalX(visibleFrame)
        val goalY=SkyPeepHudLayout.goalBottom(visibleFrame)
        badge("dark",goalX,goalY,
            SkyPeepHudLayout.GOAL_WIDTH,SkyPeepHudLayout.GOAL_HEIGHT)
        fitted(hudText.progress(worldCoins[level-1]),
            goalY+35f,fontSmall,.84f,SkyPeepHudLayout.GOAL_WIDTH-14f,
            goalX+SkyPeepHudLayout.GOAL_WIDTH/2f,
            bounds=SkyPeepUiTouch.Rect(goalX,H-goalY-SkyPeepHudLayout.GOAL_HEIGHT,
                SkyPeepHudLayout.GOAL_WIDTH,SkyPeepHudLayout.GOAL_HEIGHT))
        // Never silently show a healthy wallet when local storage is no
        // longer safely readable or flush() failed. Only the failure state
        // adds this strip; ordinary gameplay stays source-art compatible.
        if (!storageHealth.canWrite || lastSaveFailed) {
            badge("dark",W/2f-260f,visibleFrame.bottom+90f,520f,68f)
            fitted("PROGRESS NOT SAVED",visibleFrame.bottom+138f,
                fontSmall,.95f,495f,W/2f)
        }
    }

    private fun drawWingedScore() {
        val text=hudText.score(score)
        // The score is part of the bright illustrated winged HUD, not a
        // separate dark overlay. Keep the large reference-style outlined
        // numerals centred and automatically fit three-plus digit results.
        val baseline=visibleFrame.top-54f
        val size=1.38f
        val available=160f
        val center=W/2f
        val outline=SCORE_OUTLINE
        val ink=SCORE_INK
        for (dx in SCORE_STROKE_OFFSETS)
            for (dy in SCORE_STROKE_OFFSETS)
                if (dx!=0f || dy!=0f)
                    fitted(text,baseline+dy,fontLarge,size,available,center+dx,outline)
        fitted(text,baseline,fontLarge,size,available,center,ink)
    }

    private fun drawCoin(x:Float,y:Float,r:Float) {
        image("coin_asset",x-r,y-r,r*2f,r*2f)
    }

    private fun drawDiamond(x:Float,y:Float,r:Float) {
        image("gem_asset",x-r,y-r,r*2f,r*2f)
    }

    private fun drawHeart(x:Float,y:Float,r:Float) {
        heartTexture?.let { batch.draw(it,x-r,y-r,r*2f,r*2f) }
    }

    private fun drawMenuWallet() {
        // Cover only screenshot mockup's sample numeric labels, retaining the original visual buttons.
        // At the reference values preserve the screenshot pixels exactly.
        // Reference artwork already paints 1,250 coins and 75 gems.
        // Preserve those exact pixels until genuine saved balances differ.
        if (SkyPeepReferenceWallet.menuNeedsCoinOverlay(coins)) {
            badge("dark",119f,H-103f,136f,65f)
            fitted(SkyPeepHudNumbers.compact(coins),H-54f,fontSmall,1.70f,127f,186f,
                bounds=SkyPeepUiTouch.Rect(119f,38f,136f,65f))
        }
        if (SkyPeepReferenceWallet.menuNeedsGemOverlay(gems)) {
            badge("dark",743f,H-103f,76f,65f)
            fitted(SkyPeepHudNumbers.compact(gems),H-54f,fontSmall,1.70f,69f,781f,
                bounds=SkyPeepUiTouch.Rect(743f,38f,76f,65f))
        }
    }

    private fun drawMapProgress() {
        // World map artwork already includes the flags, islands, names and
        // star rows. Drawing six extra coin panels obscured those names.
        // One selected-world counter uses the empty lower toolbar gutter.
        fitted(SkyPeepMapUi.footer(level,worldCoins[level-1]),
            92f,fontSmall,.93f,352f,387f)
        for (world in 1..MAX_LEVEL) {
            val marker=SkyPeepMapLayout.marker(world)
            val selected=world==level
            val done=SkyPeepCoinGoal.isComplete(worldCoins[world-1])
            // Each small status face covers only a reference-painted LOCK,
            // never the numbered island badge, world name or star row.
            // World 1 has no painted lock and needs no extra overlay.
            SkyPeepMapUi.lockChip(world)?.let { chip ->
                badge(if (selected || done) "green" else "blue",
                    chip.left, H-chip.top-chip.height, chip.width, chip.height)
                fitted(SkyPeepMapUi.status(world,level,worldCoins[world-1]),
                    H-chip.top-chip.height/2f+12f, fontSmall, .90f,
                    chip.width-8f, chip.left+chip.width/2f,bounds=chip)
            }
            if(world==2) {
                // The supplied illustrated map has CLOUD PASS printed
                // directly into the parchment plaque. Cover *only* those
                // obsolete letters, not the world number, art or star row.
                badge("cream",marker.x-119f,H-marker.y-89f,238f,49f)
                fitted("CLOUD DRIFT",H-marker.y-56f,fontSmall,.93f,
                    218f,marker.x,TEXT_ON_CREAM,
                    bounds=SkyPeepUiTouch.Rect(marker.x-119f,marker.y+40f,238f,49f))
            }
            val earned=routeStars[difficulty.ordinal][world-1].coerceIn(0,3)
            if(earned>0) {
                val starRow=SkyPeepMapLayout.starRow(world)
                for(i in 0 until earned) victoryStar?.let { star ->
                    batch.draw(star,starRow.x-79f+59f*i,
                        H-starRow.y-25f,51f,51f)
                }
            }
        }
    }

    private fun drawVictory() {
        // Winner screen deliberately has its own composition: no sad bird or
        // baked GAME OVER heading from the loss-only reference artwork.
        image("bg_$level",0f,0f,W,H)
        drawRect(0f,0f,W,H,RESULT_SHADE)
        image("bird_up",366f,1375f,212f,185f)
        if (selectedSkin != 0) costumes[selectedSkin]?.let {
            batch.draw(it,412f,1468f,120f,72f)
        }
        badge("wood",120f,1180f,701f,155f)
        fitted("LEVEL COMPLETE",1284f,fontLarge,1.32f,644f)
        fitted(levelTitle(level).uppercase(Locale.ROOT),1190f,fontSmall,1.55f,580f)
        // Difficulty is already selected in Settings and shown in sharing;
        // putting it here overlapped the large world name on real emulator.
        // A genuine three-star rating based on how accurately the player
        // navigated each sky route, not an unconditional triple gold reward.
        for (i in 0..2) victoryStar?.let {
            if(i >= earnedStars) batch.color=UNLIT_STAR
            batch.draw(it,291f+i*120f,1009f,115f,115f)
            batch.color=Color.WHITE
        }
        badge("cream",99f,448f,743f,551f)
        fitted("FLIGHT COMPLETED",924f,fontSmall,1.55f,675f,W/2f,VICTORY_HEADLINE_COLOR)
        badge("dark",137f,706f,313f,146f)
        badge("dark",492f,706f,311f,146f)
        fitted("SCORE",826f,fontSmall,1.30f,275f,294f)
        fitted("BEST",826f,fontSmall,1.30f,275f,648f)
        fitted(score.toString(),781f,fontLarge,1.12f,272f,294f)
        fitted(routeBests[difficulty.ordinal].toString(),781f,fontLarge,1.12f,272f,648f)
        badge("wood",137f,508f,666f,156f)
        fitted("COINS COLLECTED",643f,fontSmall,1.28f,608f)
        fitted(SkyPeepCoinGoal.label(worldCoins[level-1]),
            586f,fontLarge,.96f,608f,W/2f,Color.WHITE)
        // Touch zones match the illustrated arcade buttons, not the card.
        SkyPeepResultTouch.victoryButtons.forEach { button ->
            val surface = when (button.action) {
                SkyPeepResultTouch.Action.HOME -> "blue"
                SkyPeepResultTouch.Action.RETRY_OR_NEXT -> "green"
                SkyPeepResultTouch.Action.SHARE -> "wood"
            }
            badge(surface, button.left, H - button.top - button.height,
                button.width, button.height)
        }
        val victoryButtons=SkyPeepResultTouch.victoryButtons
        fitted("HOME",303f,fontSmall,1.72f,231f,185f,
            bounds=victoryButtons[0].let { SkyPeepUiTouch.Rect(it.left,it.top,it.width,it.height) })
        fitted(if(level<MAX_LEVEL) "NEXT" else "REPLAY",303f,fontSmall,1.72f,251f,471f,
            bounds=victoryButtons[1].let { SkyPeepUiTouch.Rect(it.left,it.top,it.width,it.height) })
        fitted("SHARE",303f,fontSmall,1.72f,232f,757f,
            bounds=victoryButtons[2].let { SkyPeepUiTouch.Rect(it.left,it.top,it.width,it.height) })
    }

    private fun drawResult() {
        // Static illustration labels remain; the actual run values replace sample values.
        badge("cream",201f,H-947f,153f,90f)
        badge("cream",402f,H-947f,160f,90f)
        badge("cream",665f,H-947f,122f,90f)
        fitted(score.toString(),H-874f,fontLarge,1f,142f,277f,RESULT_VALUE_COLOR)
        fitted(routeBests[difficulty.ordinal].toString(),H-874f,fontLarge,1f,148f,483f,RESULT_VALUE_COLOR)
        fitted(earnedCoins.toString(),H-874f,fontLarge,1f,112f,726f,RESULT_VALUE_COLOR)
        val progress=SkyPeepCoinGoal.fraction(worldCoins[level-1])
        badge("dark",293f,H-1100f,463f,57f)
        if(progress>.02f) badge("blue",299f,H-1095f,450f*progress,47f)
        fitted(SkyPeepCoinGoal.label(worldCoins[level-1]),
            H-1058f,fontSmall,1.28f,409f,526f)
        if(resultWon) {
            badge("green",315f,H-1432f,316f,176f)
            fitted(if(level<MAX_LEVEL) "NEXT" else "REPLAY",H-1350f,fontSmall,1.65f,300f,475f)
        }
    }

    private fun drawPauseToggles() {
        toggle(565f,H-1218f,musicEnabled)
        toggle(565f,H-1324f,effectsEnabled)
    }

    private fun drawSkins() {
        // Dynamic saved values always replace the fixed reference mockup.
        // The SKINS source plate paints 1,250 coins and 89 gems, whereas
        // Main Menu source paints 1,250 coins and 75 gems. Repaint only
        // when actual saved data differs from the corresponding illustration.
        if (SkyPeepReferenceWallet.skinsNeedsCoinOverlay(coins)) {
            badge("wallet",120f,H-103f,164f,65f)
            fitted(SkyPeepHudNumbers.compact(coins),H-35f,fontSmall,1.28f,153f,199f,
                bounds=SkyPeepUiTouch.Rect(120f,38f,164f,65f))
        }
        if (SkyPeepReferenceWallet.skinsNeedsGemOverlay(gems)) {
            badge("wallet",733f,H-103f,111f,65f)
            fitted(SkyPeepHudNumbers.compact(gems),H-35f,fontSmall,1.28f,104f,788f,
                bounds=SkyPeepUiTouch.Rect(733f,38f,111f,65f))
        }
        // DEFAULT labels the illustrated default bird, not the currently worn
        // costume. Preserve the painted plaque. When another skin is worn,
        // replace ONLY the outdated baked EQUIPPED caption with live EQUIP.
        if (selectedSkin != 0) {
            val button = SkyPeepUiTouch.defaultEquipRect
            badge("green", button.left,
                H-button.top-button.height, button.width, button.height)
            fitted("EQUIP", H-button.top-button.height/2f+13f,
                fontSmall,1.14f,button.width-10f,
                button.left+button.width/2f,Color.WHITE,bounds=button)
        }
        for (i in 1..4) {
            val caption = SkyPeepSkinUi.dynamicCaption(
                i, selectedSkin, ownedSkins[i]) ?: continue
            // The original screenshot already paints its four BUY buttons.
            // Repainting unchanged BUY art degraded 1:1 lower-card pixels.
            // Draw only the dynamic EQUIP or EQUIPPED state, using the same
            // rectangle that handles the actual button tap.
            val button=SkyPeepUiTouch.skinPurchaseRect(i)
            badge("green",
                button.left,H-button.top-button.height,button.width,button.height)
            fitted(caption,
                H-button.top-button.height/2f+13f,fontSmall,1.14f,
                button.width-12f,button.left+button.width/2f,Color.WHITE,bounds=button)
        }
    }

    /** Reuse the real illustrated aviator (and equipped costume) as a
     * decorative mascot on screens lacking a supplied baked-in bird panel.
     * Unlike a screenshot overlay, this stays a proper transparent sprite.
     */
    private fun drawMenuMascot(x:Float,y:Float,width:Float) {
        val bird=pictures["bird_idle"] ?: return
        val scale=width/BIRD_W
        val height=width*bird.height/bird.width
        batch.draw(bird,x,y,width,height)
        if(selectedSkin>0) costumes[selectedSkin]?.let { costume ->
            batch.draw(costume,
                x+SkyPeepBirdLayout.COSTUME_X*scale,
                y+height-SkyPeepBirdLayout.COSTUME_Y_FROM_TOP*scale,
                SkyPeepBirdLayout.COSTUME_WIDTH*scale,
                SkyPeepBirdLayout.COSTUME_HEIGHT*scale)
        }
    }

    private fun drawShop() {
        // Dedicated store over existing world scenery, not menu buttons.
        drawRect(0f,0f,W,H,SHOP_SHADE)
        badge("cream",72f,180f,797f,1320f)
        badge("wood",117f,1365f,708f,122f)
        // Native bird illustration gives the SHOP its own SkyPeep identity,
        // as in the supplied PAUSE/SKINS design rather than a generic dialog.
        drawMenuMascot(383f,1481f,175f)
        fitted("FLIGHT TRAILS",1444f,fontLarge,1.28f,657f,
            bounds=SkyPeepUiTouch.Rect(117f,H-1487f,708f,122f))
        badge("dark",118f,1252f,338f,90f)
        badge("dark",483f,1252f,338f,90f)
        fitted("COINS  ${SkyPeepHudNumbers.compact(coins)}",1312f,fontSmall,1.38f,305f,288f,
            bounds=SkyPeepUiTouch.Rect(118f,H-1342f,338f,90f))
        fitted("GEMS  ${SkyPeepHudNumbers.compact(gems)}",1312f,fontSmall,1.38f,305f,653f,
            bounds=SkyPeepUiTouch.Rect(483f,H-1342f,338f,90f))
        val tx=trailTexture
        for(offer in SkyPeepTrailCatalog.all) {
            val y=1060f-offer.id*195f
            badge("cream",99f,y,743f,174f)
            badge("wood",119f,y+24f,110f,122f)
            if(tx!=null) {
                batch.color=TRAIL_TINTS[offer.id.coerceIn(0,TRAIL_TINTS.lastIndex)]
                batch.draw(tx,132f,y+48f,90f,74f)
                batch.color=Color.WHITE
            }
            fitted(offer.name.uppercase(Locale.ROOT),y+121f,fontSmall,1.10f,322f,410f,SHOP_NAME_COLOR,
                bounds=SkyPeepUiTouch.Rect(240f,H-y-174f,334f,174f))
            fitted(SkyPeepTrailCatalog.priceLabel(offer),y+66f,
                fontSmall,1.05f,322f,410f,SHOP_PRICE_COLOR,
                bounds=SkyPeepUiTouch.Rect(240f,H-y-174f,334f,174f))
            val buyButton = SkyPeepUiTouch.shopPurchaseRect(offer.id)
            // All active offers use a bright green legible control; state is
            // expressed in its BUY/EQUIP/EQUIPPED caption, not low contrast.
            badge("green",
                buyButton.left,H-buyButton.top-buyButton.height,buyButton.width,buyButton.height)
            val caption=when {
                selectedTrail==offer.id -> "EQUIPPED"
                ownedTrails[offer.id] -> "EQUIP"
                else -> "BUY"
            }
            fitted(caption,y+103f,fontSmall,1.18f,207f,706f,Color.WHITE,
                bounds=buyButton)
        }
        badge("green",279f,245f,384f,137f)
        fitted("BACK",337f,fontLarge,1.22f,340f)
    }

    private fun drawSettings() {
        // Branded parchment and wood over world scenery, no ghosted menu buttons.
        drawRect(0f,0f,W,H,SETTINGS_SHADE)
        badge("cream",91f,348f,759f,1020f)
        badge("wood",132f,1214f,677f,139f)
        drawMenuMascot(381f,1350f,179f)
        fitted("SETTINGS",1340f,fontLarge,1.55f,627f,
            bounds=SkyPeepUiTouch.Rect(132f,H-1353f,677f,139f))
        for(i in SETTINGS_NAMES.indices) {
            val yy=1010f-i*143f
            badge("wood",137f,yy,666f,119f)
            fitted(SETTINGS_NAMES[i],yy+77f,fontSmall,1.12f,
                362f,350f,Color.WHITE,
                bounds=SkyPeepUiTouch.Rect(137f,H-yy-119f,666f,119f))
            if(i<3) {
                toggle(657f,yy+28f,when(i) {
                    0 -> musicEnabled
                    1 -> effectsEnabled
                    else -> vibrationEnabled
                })
            } else fitted(difficulty.title,yy+77f,fontSmall,.94f,
                214f,686f,Color.WHITE,
                bounds=SkyPeepUiTouch.Rect(137f,H-yy-119f,666f,119f))
        }
        badge("blue",137f,421f,310f,116f)
        fitted("BEST " + routeBests[difficulty.ordinal],494f,fontSmall,1.1f,285f,292f)
        badge("green",492f,421f,313f,116f)
        fitted("WORLDS 6/6",494f,fontSmall,1.3f,290f,649f)
        badge("green",185f,243f,575f,129f)
        fitted("BACK",352f,fontLarge,1.30f,531f,
            bounds=SkyPeepUiTouch.Rect(185f,H-372f,575f,129f))
    }

    /** Rounded, antialiased-like gradient chrome rendered once, reused on each screen. */
    private fun badge(name:String,x:Float,y:Float,width:Float,height:Float) {
        if(width<=0f || height<=0f) return
        if(width>=64f && height>=64f) chrome[name]?.draw(batch,x,y,width,height)
        else chromeTextures[name]?.let { batch.draw(it,x,y,width,height) }
    }

    private fun toggle(x:Float,y:Float,enabled:Boolean) {
        badge(if(enabled) "green" else "red",x,y,112f,61f)
        badge("cream",x+(if(enabled) 57f else 7f),y+7f,48f,47f)
    }

    private fun fitted(value:String,baseline:Float,font:BitmapFont,size:Float,
                       available:Float,centerX:Float=W/2f,tint:Color=Color.WHITE,
                       bounds:SkyPeepUiTouch.Rect?=null) {
        val savedScale=font.data.scaleX
        fontColorScratch.set(font.color)
        font.data.setScale(size)
        tmp.setText(font,value)
        if(tmp.width>available && tmp.width>0f)
            font.data.setScale(size*available/tmp.width)
        tmp.setText(font,value)
        if(bounds!=null) {
            // Button artwork has BOTH horizontal and vertical boundaries.
            // Width-only fitting let tall captions overflow wallet panels.
            val factor=SkyPeepTextBounds.scaleLimit(tmp.width,tmp.height,bounds)
            if(factor<1f) {
                font.data.setScale(font.data.scaleX*factor)
                tmp.setText(font,value)
            }
        }
        val placement=if(bounds==null) null else SkyPeepTextBounds.position(
            bounds,centerX,baseline,tmp.width,tmp.height,H)
        font.color=tint
        font.draw(batch,value,
            (placement?.centerX ?: centerX)-tmp.width/2f,
            placement?.baseline ?: baseline)
        font.color=fontColorScratch
        font.data.setScale(savedScale)
    }

    private fun drawRect(x:Float,y:Float,w:Float,h:Float,color:Color) {
        if (w<=0f || h<=0f) return
        batch.color=color
        batch.draw(solid,x,y,w,h)
        batch.color=Color.WHITE
    }

    private fun toast(value:String) {
        message=value
        messageTimer=2.3f
    }
    private fun levelTitle(i:Int)=LEVEL_TITLES[i.coerceIn(1,MAX_LEVEL)-1]
    private fun skinName(i:Int)=SkyPeepSkinCatalog.get(i).name
    private fun playSound(kind:String) {
        if (effectsEnabled) try { sounds[kind]?.play(.45f) } catch (_:Exception) { }
    }
    /** Records are durable as soon as the illustrated pillar is passed. */
    private fun saveRecord() {
        persist {
            SkyPeepRecordSave.write(difficulty.ordinal, best,
                routeBests[difficulty.ordinal]) { key, value ->
                prefs.putInteger(key, value)
            }
        }
    }

    /** A real pickup changes only these three saved values. */
    private fun savePickup() {
        persist {
            SkyPeepPickupSave.write(level, worldCoins[level-1], coins, gems) {
                    key, value -> prefs.putInteger(key, value)
            }
        }
    }

    /**
     * Keep synchronous durability while isolating write failures. A full
     * save remains mandatory for settings, scores, stars and shop ownership.
     * On I/O failure, the in-memory state is retained for the next retry.
     */
    private fun persist(fullSnapshot: Boolean = false, write: () -> Unit) {
        if (!::prefs.isInitialized || !SkyPeepQaSession.shouldPersist(
                world = qaWorld, mapUnlocked = qaMapUnlocked,
                skin = qaSkin, victory = qaVictory, pillars = qaPillars,
                score = qaScore, loss = qaLoss,
                backgroundMotion = qaBackgroundMotion,
                difficulty = qaDifficulty)) return
        if (!storageHealth.canWrite) {
            lastSaveFailed = true
            return
        }
        // Partial pickup/record writes cannot repair an earlier failed full
        // save that also included costume ownership, stars and preferences.
        if (!fullSnapshot && storageHealth.needsFullSnapshot) {
            save()
            return
        }
        try {
            write()
            prefs.flush()
            storageHealth.onFlushSuccess(fullSnapshot)
            lastSaveFailed = storageHealth.needsFullSnapshot
        } catch (e: RuntimeException) {
            storageHealth.onFlushFailure()
            lastSaveFailed = true
            // A full-storage/permission fault should not crash Android
            // during a flight, onPause or dispose. Do not claim it saved.
            Gdx.app.error("SkyPeepStorage", "Unable to save local progress", e)
            if (screen != Screen.PLAY) toast("SAVE FAILED - CHECK STORAGE")
        }
    }

    private fun save() {
        persist(fullSnapshot = true) {
            prefs.putInteger("best",best)
            prefs.putInteger("difficulty",difficulty.ordinal)
            for (mode in SkyDifficulty.entries) {
                prefs.putInteger("route_best_${mode.ordinal}",routeBests[mode.ordinal])
                for (i in 0 until MAX_LEVEL)
                    prefs.putInteger("route_stars_${mode.ordinal}_${i+1}",
                        routeStars[mode.ordinal][i])
            }
            prefs.putInteger("unlocked",MAX_LEVEL)
            for (i in worldCoins.indices)
                prefs.putInteger("collected_world_${i+1}",SkyPeepCoinGoal.collected(worldCoins[i]))
            prefs.putInteger("level",level)
            prefs.putInteger("coins",coins)
            prefs.putInteger("gems",gems)
            prefs.putInteger("skin",selectedSkin)
            prefs.putInteger("trail_selected",selectedTrail)
            for (i in 1..3) prefs.putBoolean("trail_owned_$i",ownedTrails[i])
            prefs.putBoolean("music",musicEnabled)
            prefs.putBoolean("effects",effectsEnabled)
            prefs.putBoolean("vibration",vibrationEnabled)
            for (i in 0..4) prefs.putBoolean("owned_$i",ownedSkins[i])
            for (i in 0 until MAX_LEVEL) prefs.putInteger("stars_${i+1}",stars[i])
        }
    }

    override fun pause() {
        // App backgrounding must never leave a live run progressing behind a system UI.
        if (screen == Screen.PLAY) screen = Screen.PAUSE
        save()
        updateMusicPlayback(false)
    }
    private fun generatedTexturesManaged(): Boolean =
        ::solid.isInitialized && solid.isManaged &&
        chromeTextures.isNotEmpty() && chromeTextures.values.all { it.isManaged } &&
        costumes.isNotEmpty() && costumes.values.all { it.isManaged } &&
        victoryStar?.isManaged == true &&
        pillarStem?.isManaged == true &&
        trailTexture?.isManaged == true &&
        heartTexture?.isManaged == true

    override fun resume() {
        // Ensure one fresh frame after Android recreates the surface. Static
        // menu/PAUSE scenes remain event-driven instead of running the GPU
        // continuously after a Home/Resume round trip.
        Gdx.graphics.requestRendering()
        Gdx.app.log("SkyPeepLifecycle",
            "resume generatedManaged=" + generatedTexturesManaged())
        if (musicEnabled) updateMusicPlayback(true)
    }
    override fun dispose() {
        save()
        music?.dispose()
        sounds.values.forEach { it.dispose() }
        pictures.values.forEach { it.dispose() }
        if (::fontSmall.isInitialized)fontSmall.dispose()
        if (::fontLarge.isInitialized)fontLarge.dispose()
        if (::solid.isInitialized)solid.dispose()
        chromeTextures.values.forEach { it.dispose() }
        costumes.values.forEach { it.dispose() }
        victoryStar?.dispose()
        pillarStem?.dispose()
        trailTexture?.dispose()
        heartTexture?.dispose()
        if (::batch.isInitialized)batch.dispose()
    }
}