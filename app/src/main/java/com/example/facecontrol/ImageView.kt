package com.example.facecontrol

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.SparseIntArray
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import kotlinx.android.synthetic.main.cameraview.*
import kotlinx.android.synthetic.main.imageview.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ImageView : AppCompatActivity(), View.OnClickListener {

    val ALBUM = "facedraw"
    var albumPath: String? = null
    private var saveToDisk = false
    private var mOrientation = 0f
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
        when(v?.id) {
            R.id.btn3 -> {
                saveToDisk = true
                getAlbum()
            }
        }
    }
    fun getImage() {
        ByteImage = intent.getByteArrayExtra("image")!!
        val bitmap = BitmapFactory.decodeByteArray(ByteImage, 0, ByteImage.size)

                Glide.with(this).asBitmap().load(bitmap).into(imageView)
        }

    private fun updataGallery(file: File) {
        var contentUri = Uri.fromFile(file)
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri)
        applicationContext.sendBroadcast(mediaScanIntent)
    }

    private fun getAlbum() {
        // Check for SD card storage
        ByteImage = intent.getByteArrayExtra("image")!!
        val status = Environment.getExternalStorageState()
        if (status != Environment.MEDIA_MOUNTED) {
        }

        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM
            ), ALBUM
        )
        if(!file.mkdirs()) {
            albumPath = file.absolutePath
        } else {
            albumPath = file.absolutePath
        }
        saveImage(ByteImage)
    }

    private fun saveImage(byteArray: ByteArray) {
        var captureTime = System.currentTimeMillis().toString()
        var pictureName = captureTime + ".JPG"
        var file = File(albumPath, pictureName)
        try {

            // Local storage
            var fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(byteArray)
            fileOutputStream.close()

            saveToDisk = false

            updataGallery(file)
            Toast.makeText(this, "사진이 저장되었습니다", Toast.LENGTH_LONG).show()

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
        }
    }
}