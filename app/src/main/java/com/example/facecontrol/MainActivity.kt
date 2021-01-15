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

    private val CAMERA_PEMISSION  = arrayOf(Manifest.permission.CAMERA)
    private val STORAGE_PEMISSION  = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val FLAG_PERM_CAMERA = 98
    val FLAG_PERM_STORAGE = 99
    val FALG_REQ_CAMERA = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.testbutton-> {
                if(getPermission(CAMERA_PEMISSION)) {
                    openCamera()
                } else {
                    ActivityCompat.requestPermissions(this,CAMERA_PEMISSION,FLAG_PERM_CAMERA)
                }
            }
        }
    }

    //
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
//            FLAG_PERM_STORAGE-> {
//                var permissionCheckable =  true
//                for(grant in grantResults) {
//                    if (grant != PackageManager.PERMISSION_GRANTED) {
//                        permissionCheckable = false
//                        break
//                    }
//                }
//                if(permissionCheckable) {
//                    //카메라 실행
//                }
//            }

            FLAG_PERM_CAMERA->{
                var permissionCheckable =  true
                for(grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) {
                        permissionCheckable = false
                        break
                    }
                }
                if(permissionCheckable) {
                    openCamera()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                FALG_REQ_CAMERA-> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    resultImage.setImageBitmap(bitmap)
                }
            }
        }
    }

    fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent,FALG_REQ_CAMERA)
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