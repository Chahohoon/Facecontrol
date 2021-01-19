package com.example.facecontrol

import android.hardware.Camera
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.cameraview.*
import java.io.IOException


class CameraView(private var mCamera: Camera) : AppCompatActivity(), SurfaceHolder.Callback {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cameraview)

        val holder = surfaceView.holder
        holder.addCallback(this)
        surfaceCreated(holder)

    }

    @JvmName("surfaceCreated1")
    fun surfaceCreated(holder: SurfaceHolder?) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        mCamera = Camera.open()
        mCamera.setDisplayOrientation(90)
        try {
            mCamera.setPreviewDisplay(holder)
        } catch (e: IOException) {
            mCamera.release()
//            mCamera = null
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        TODO("Not yet implemented")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        TODO("Not yet implemented")
    }

}


