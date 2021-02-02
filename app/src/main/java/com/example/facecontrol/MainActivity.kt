package com.example.facecontrol

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.security.Permission


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val  CAMERA_PEMISSION = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//    private val  STORAGE_PEMISSION = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val FLAG_PERM_STORAGE = 97 // 저장소 권한
    val FLAG_PERM_CAMERA = 98 //카메라 권한
    val FALG_REQ_CAMERA = 101  //카메라 열기


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_cameraopen -> {
                if (getPermission(CAMERA_PEMISSION)) {
                    startActivity(Intent(this, CameraView::class.java))
                } else {
                    ActivityCompat.requestPermissions(this,CAMERA_PEMISSION,FLAG_PERM_CAMERA)
                }
            }
        }
    }

    // 권한체크
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {

            FLAG_PERM_CAMERA -> {
                var permissionCheckable = true
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        permissionCheckable = false
                        break
                    }
                }
                if (permissionCheckable) {
                    startActivity(Intent(this, CameraView::class.java))
                }
            }

            FLAG_PERM_STORAGE -> {
                var permissionCheckable = true
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        permissionCheckable = false
                        break
                    }
                }
                if (permissionCheckable) {
                    startActivity(Intent(this, CameraView::class.java))
                }
            }
        }
    }



    fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent,FALG_REQ_CAMERA)
        finish()
    }

    //퍼미션 체크함수
    fun getPermission(permissions: Array<String>) : Boolean {
        for(permission in permissions) {
            val result = ContextCompat.checkSelfPermission(this,permission)
            if ( result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

}