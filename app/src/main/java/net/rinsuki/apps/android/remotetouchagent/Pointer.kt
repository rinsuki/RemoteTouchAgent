package net.rinsuki.apps.android.remotetouchagent

import android.view.MotionEvent

class Pointer {

    var upstreamID = ""
    var isPressed = false
    var props = MotionEvent.PointerProperties()
    var coords = MotionEvent.PointerCoords()

    init {
        props.toolType = MotionEvent.TOOL_TYPE_FINGER
        coords.size = 1f
        coords.pressure = 1f
    }
}