package com.example.facecontrol

import android.content.Intent
import android.hardware.Camera
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


class CameraView : AppCompatActivity() {
    val FALG_REQ_CAMERA = 101  //카메라 열기
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cameraview)

//        openCamera()

    }

    private fun startCamera() {
    }

//    fun openCamera() {
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        startActivityForResult(intent,FALG_REQ_CAMERA)
//    }
}


