@file:Suppress("DEPRECATION", "DEPRECATED_IDENTITY_EQUALS")

package kr.puze.autoparking

import android.content.Context
import android.content.res.Configuration
import android.hardware.Camera
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

class CameraSurfaceView : SurfaceView, SurfaceHolder.Callback {
    var surfaceHolder: SurfaceHolder? = null
    var camera: Camera? = null
    var mContext: Context? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        this.mContext = context
        surfaceHolder = holder
        surfaceHolder!!.addCallback(this)
        isFocusableInTouchMode = true
        isFocusable = true
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder?) {
        camera = Camera.open()
        try {
            val parameters: Camera.Parameters = camera!!.parameters
            if (resources.configuration.orientation !== Configuration.ORIENTATION_LANDSCAPE) {
                parameters.set("orientation", "portrait")
                camera!!.setDisplayOrientation(90)
                parameters.setRotation(90)
            } else {
                parameters.set("orientation", "landscape")
                camera!!.setDisplayOrientation(0)
                parameters.setRotation(0)
            }
            camera!!.parameters = parameters
            camera!!.setPreviewDisplay(surfaceHolder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        this.setOnClickListener { camera!!.autoFocus { b, camera -> } }
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder?, i: Int, i1: Int, i2: Int) {
        camera!!.startPreview()
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder?) {
        camera!!.stopPreview()
        camera!!.release()
        camera = null
    }

    fun capture(callback: Camera.PictureCallback?): Boolean {
        return if (camera != null) {
            camera!!.takePicture(null, null, callback)
            true
        } else {
            false
        }
    }
}