package com.brendigo.skypeep

/**
 * Score and currency are separately typeset inside original Victory panels.
 * The horizontal gutter is part of the illustrated design, not a touch zone.
 * Rects are in 941x1672 canvas top-left coordinates.
 */
internal object SkyPeepVictoryLayout {
    fun heading(left:Float,width:Float)=
        SkyPeepUiTouch.Rect(left,820f,width,63f)
    fun value(left:Float,width:Float)=
        SkyPeepUiTouch.Rect(left,892f,width,68f)

    val coinHeading=SkyPeepUiTouch.Rect(137f,1008f,666f,62f)
    val coinValue=SkyPeepUiTouch.Rect(137f,1083f,666f,75f)
}
