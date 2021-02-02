package com.example.facecontrol

import android.content.Context
import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.lang.Exception


/** A basic Camera preview class */
class CameraPreview(context: Context, private val mCamear : Camera) : SurfaceView(context), SurfaceHolder.Callback {

    private val mHolder: SurfaceHolder = holder.apply {
        addCallback(this@CameraPreview)
        setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mCamear.apply {
            try{
                setDisplayOrientation(90)
                setPreviewDisplay(holder)
                startPreview()
            } catch (e:IOException) {

            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if(mHolder.surface == null) {
            return
        }

        try {
            mCamear.stopPreview()
        } catch (e:Exception) {
        }

        mCamear.apply {
            try{
                setPreviewDisplay(mHolder)
                startPreview()
            } catch (e:Exception) {

            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
    }
}
