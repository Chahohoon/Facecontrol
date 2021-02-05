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
import kotlinx.android.synthetic.main.imageview.*
import java.security.Permission


class ImageView : AppCompatActivity(), View.OnClickListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.imageview)

        getImage()
    }

    override fun onClick(v: View?) {
    }

    fun getImage() {

        var intent = intent
        if(intent.hasExtra("image"))
        imageView.setImageBitmap(intent.getParcelableExtra<Bitmap>("image"))
    }
}