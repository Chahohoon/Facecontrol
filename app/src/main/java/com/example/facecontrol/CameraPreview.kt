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


/** A basic Camera preview class */
class CameraPreview(context: Context, private val mCamera : Camera) : SurfaceView(context), SurfaceHolder.Callback {

    private val mHolder: SurfaceHolder = holder.apply {
        addCallback(this@CameraPreview)
    }



    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {

    }
}
