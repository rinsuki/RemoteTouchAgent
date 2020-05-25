package net.rinsuki.apps.android.remotetouchagent

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.hardware.display.DisplayManager
import android.hardware.input.InputManager
import android.os.SystemClock
import android.view.InputDevice
import android.view.InputEvent
import android.view.MotionEvent
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.net.Socket

object KtAgent {
    @ExperimentalStdlibApi
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            mainProcess(args)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("PrivateApi")
    @ExperimentalStdlibApi
    fun mainProcess(args: Array<String>) {
        println("Hello!")
        val classDisplayManagerGlobal = Class.forName("android.hardware.display.DisplayManagerGlobal")
        val displayManagerGlobal = classDisplayManagerGlobal.getMethod("getInstance").invoke(null)
        val displayIds: IntArray = classDisplayManagerGlobal.getMethod("getDisplayIds").invoke(displayManagerGlobal) as IntArray
        val displayInfo = classDisplayManagerGlobal.getMethod("getDisplayInfo", Int::class.java).invoke(displayManagerGlobal, displayIds[0])
        val displayWidth = displayInfo.javaClass.getField("logicalWidth").get(displayInfo) as Int
        val displayHeight = displayInfo.javaClass.getField("logicalHeight").get(displayInfo) as Int

        val socket = Socket(args[0], args[1].toInt())
        val stream = BufferedReader(InputStreamReader(socket.getInputStream()))

        var pointerMaxID = -1
        val pointers = mutableListOf<Pointer>()
        var freeIds = mutableListOf<Int>()
        var tappedCount = 0

        while (true) {
            val line = stream.readLine()
            val taps = if (line.length == 0) emptyList() else line.split(",")
            pointers.forEach { it.isPressed = false }
            val currentTime = SystemClock.uptimeMillis()

            taps.forEachIndexed { index, tap ->
                val tapComponents = tap.split(":")
                println(tapComponents)
                val tapID = tapComponents[0]
                val tapX = tapComponents[1].toFloat()
                val tapY = tapComponents[2].toFloat()

                var pointer = pointers.find { it.upstreamID == tapID }
                val isTouchStart = pointer == null
                if (pointer == null) {
                    tappedCount++
                    pointer = Pointer()
                    if (freeIds.size > 0) {
                        pointer.props.id = freeIds.removeFirst()
                    } else {
                        pointerMaxID++
                        pointer.props.id = pointerMaxID
                    }
                    pointer.upstreamID = tapID
                    pointers.add(pointer)
                }
                pointer.isPressed = true
                pointer.coords.x = tapX * displayWidth
                pointer.coords.y = tapY * displayHeight

                if (isTouchStart) {
                    var action = if (pointerMaxID == 0) MotionEvent.ACTION_DOWN else MotionEvent.ACTION_POINTER_DOWN
                    action = (pointer.props.id shl 8) or action
                    val event = MotionEvent.obtain(
                            currentTime, currentTime,
                            action,
                            pointers.count(), pointers.map { it.props }.toTypedArray(), pointers.map { it.coords }.toTypedArray(),
                            0, 0,
                            1f, 1f, 0,
                            0, 0, 0
                    )
                    event.source = InputDevice.SOURCE_TOUCHSCREEN
                    injectInputEvent(event)
                }
            }

            val event = MotionEvent.obtain(
                    currentTime, currentTime,
                    MotionEvent.ACTION_MOVE,
                    pointers.count(), pointers.map { it.props }.toTypedArray(), pointers.map { it.coords }.toTypedArray(),
                    0, 0,
                    1f, 1f, 0,
                    0, 0, 0
            )
            event.source = InputDevice.SOURCE_TOUCHSCREEN
            injectInputEvent(event)

            pointers.filter { it.isPressed.not() }.forEach { pointer ->
                tappedCount--
                var action = if (tappedCount == 0) MotionEvent.ACTION_UP else MotionEvent.ACTION_POINTER_UP
                action = (pointer.props.id shl 8) or action
                val event = MotionEvent.obtain(
                        currentTime, currentTime,
                        action,
                        pointers.count(), pointers.map { it.props }.toTypedArray(), pointers.map { it.coords }.toTypedArray(),
                        0, 0,
                        1f, 1f, 0,
                        0, 0, 0
                )
                event.source = InputDevice.SOURCE_TOUCHSCREEN
                injectInputEvent(event)
                freeIds.add(0, pointer.props.id)
                pointers.remove(pointer)
            }

            if (pointers.size == 0) {
                pointerMaxID = -1
                freeIds = mutableListOf()
            }
        }
    }

    var im: InputManager? = null;

    fun injectInputEvent(event: InputEvent, mode: Int = 0) {
        if (im == null) im = InputManager::class.java.getMethod("getInstance").invoke(InputManager::class) as InputManager;
        System.out.println(im);
        im?.javaClass
            ?.getMethod("injectInputEvent", InputEvent::class.java, Integer.TYPE)
            ?.invoke(im, event, mode)
    }
}