package com.example.facecontrol

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.transition.Transition
import android.util.Log
import android.util.SparseIntArray
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.imageview.*
import java.net.URL
import java.security.Permission


class ImageView : AppCompatActivity(), View.OnClickListener {

    private lateinit var ByteImage : ByteArray
    private val ORIENTATIONS = SparseIntArray()
    init {
        ORIENTATIONS.append(ExifInterface.ORIENTATION_NORMAL, 0)
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_90, 90)
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_180, 180)
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_270, 270)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.imageview)

        getImage()
    }

    override fun onClick(v: View?) {
    }

    fun getImage() {
        ByteImage = intent.getByteArrayExtra("image")!!
        val bitmap = BitmapFactory.decodeByteArray(ByteImage, 0, ByteImage.size)
        imageView.setImageBitmap(bitmap)
//        Glide.with(this).asBitmap().load(bitmap).into(object : CustomViewTarget<ImageView,Bitmap>(imageView) {
//            override fun onLoadFailed(errorDrawable: Drawable?) {
//            }
//
//            override fun onResourceReady(
//                resource: Bitmap,
//                transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
//            ) {
//                imageView.setImageBitmap(resource)
//            }
//
//            override fun onResourceCleared(placeholder: Drawable?) {
//            }
//
//        })
}
}