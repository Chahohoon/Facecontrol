package com.example.facecontrol

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.ExifInterface
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.media.ImageReader.newInstance
import android.os.*
import android.util.*
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.cameraview.*
import java.io.*
import java.nio.ByteBuffer
import java.util.*


/**
SurfaceView 및 액티비티 수명 주기
액티비티가 시작되면 이 순서대로 콜백을 수신합니다.

onCreate()
onResume()
surfaceCreated()
surfaceChanged()

뒤로를 클릭하면 다음이 표시됩니다.

onPause()
surfaceDestroyed()

 **/

class CameraView: AppCompatActivity(), View.OnClickListener {

    //카메라 변수
    private lateinit var mSurfaceViewHolder: SurfaceHolder
    private lateinit var mTextureView: TextureView.SurfaceTextureListener
    private lateinit var mCameraDevice: CameraDevice // 카메라 디바이스
    private lateinit var mSession: CameraCaptureSession  //캡쳐 세션
    private lateinit var mCaptureRequest: CaptureRequest
    private lateinit var mCaptureRequestBuilder: CaptureRequest.Builder
    //이미지 저장 변수

    private lateinit var mImageReader: ImageReader
    private lateinit var mSize: Size
    private lateinit var mFile: File

    var mBackgroundThread: HandlerThread? = null  // Preview 정상적으로 동작하게 하기 위해서 사용하는 Background Thread
    var mBackgroundHandler: Handler? = null // Background 에서 동작하는 핸들러
    var mForegroundHandler: Handler? = null  //UI Thread에서 동작하는 핸들러

    private var mHeight: Int = 0
    private var mWidth: Int = 0
    private var mCameraID = CAMERA_BACK // 기본 후면카메라

    //화면 각도
    companion object {
        const val CAMERA_BACK = "0"
        const val CAMERA_FRONT = "1"

        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(ExifInterface.ORIENTATION_NORMAL, 0)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_90, 90)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_180, 180)
            ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_270, 270)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //화면 켜짐 유지
        window.setFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setContentView(R.layout.cameraview)

        initView()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            //캡쳐
            R.id.btn_Capture -> {
                getPicture()
            }
            //갤러리
            R.id.b -> {
                getGallery()
            }
            //카메라 전환
            R.id.a -> {
                if (mCameraID == "0") {
                    mCameraID = CAMERA_FRONT
                    mCameraDevice.close()
                    OpenCamera()
                } else {
                    mCameraID = CAMERA_BACK
                    mCameraDevice.close()
                    OpenCamera()
                }
            }
        }
    }

    fun initView() {
        //해상도
        with(DisplayMetrics()) {
            windowManager.defaultDisplay.getRealMetrics(this)
            mHeight = heightPixels
            mWidth = widthPixels
        }

        mSurfaceViewHolder = surfaceView.holder
        mSurfaceViewHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                initCameraAndPreview()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                holder.removeCallback(this)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                try {
                    mImageReader = newInstance(mWidth, mHeight, ImageFormat.JPEG, 2)
                    mImageReader.setOnImageAvailableListener(mImageCaptureListener, mBackgroundHandler)
                    Log.i("현재사이즈 : ", surfaceView.width.toString() + "x" + surfaceView.height.toString())

                } catch (e: CameraAccessException) {

                }
            }
        })
    }


    fun initCameraAndPreview() {
        val handlerThread = HandlerThread("CAMERA2")
        handlerThread.start()
        mBackgroundHandler = Handler(handlerThread.looper)

        OpenCamera()
    }

    //갤러리 열기
    fun getGallery() {
        var open_gallery = Intent(Intent.ACTION_PICK)
        open_gallery.type = "image/*"
        open_gallery.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(open_gallery, 99)
    }

    //카메라 열기
    fun OpenCamera() {

        try {
            val mCamera = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val characteristics = mCamera.getCameraCharacteristics(mCameraID) //선택한 카메라의 특징 확인

            val Map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            val largestPreviewSize =
                    Map!!.getOutputSizes(ImageFormat.JPEG)[0]
            setAspectRatioTextureView(largestPreviewSize.height, largestPreviewSize.width)

            mImageReader = newInstance(
                    largestPreviewSize.width,
                    largestPreviewSize.height,
                    ImageFormat.JPEG,
                    7
            )
            //권한체크
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mCamera.openCamera(mCameraID, deviceStateCallback, mBackgroundHandler)

        } catch (e: CameraAccessException) {
            Log.e("opencamera","에러")
        }
    }

    // 음?
    fun getPicture2() {
        if (mSession != null) {
            try {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                mCaptureRequestBuilder.addTarget(mImageReader.surface)
                try {
                    // 캡쳐 세션에 콜백을 Attach 하지 않았으므로 handler를 null처리할 수 있다
                    mSession.capture(mCaptureRequestBuilder.build(), null, null)
                } catch (e: CameraAccessException) {
                    Log.e("CAMERA2 Picture", "Failed to file actual capture request", e)
                }
            } catch (e: CameraAccessException) {
                Log.e("CAMERA2 Picture", "Failed to file actual capture request", e)
            }
        } else {
            Log.e("CAMERA2 Picture", "User attempted to perform a capture outside our session")
        }
        //카메라로 부터 JPEG이미지를 받게 되면 Callback 이 호출된다
        //여기서 JPEG 이미지를 못받아옴 2021.02.10
        mImageReader.setOnImageAvailableListener(object : ImageReader.OnImageAvailableListener {
            override fun onImageAvailable(reader: ImageReader?) {
                mBackgroundHandler?.post(CapturedImageSaver(reader!!.acquireLatestImage()))
            }
        }, null)
    }

    fun getPicture() {
        //jpeg를 받으면 인텐트 해줘야하는곳 
    }

    val mImageCaptureListener = OnImageAvailableListener { reader ->
        // Save the image once we get a chance
        mBackgroundHandler!!.post(CapturedImageSaver(reader.acquireNextImage()))

    }


    // 카메라 디바이스의 상태가 변경되면 호출되는 CallBack
    private val deviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            mCameraDevice = camera
            try {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW) //미리보기 객체
                mCaptureRequestBuilder.addTarget(mSurfaceViewHolder.surface)
                mCameraDevice.createCaptureSession(
                        listOf(mSurfaceViewHolder.surface, mImageReader.surface),
                        mSessionPreviewStateCallback,
                        mBackgroundHandler
                )


            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.e("연결끊김", "")
            camera.close()

        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
        }

    }

    //    백그라운드 Thread에서 화면을 캡쳐하는 Session에 변경사항이 있을때 호출되는 Callback
    private val mSessionPreviewStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            mSession = session
            if (mSurfaceViewHolder != null) {
                //화면에 뿌려줄 프리뷰 정보를 생성
                try {
                    mCaptureRequestBuilder.set(
                            CaptureRequest.CONTROL_AF_MODE,
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
                    try {
                        mSession.setRepeatingRequest(
                                mCaptureRequestBuilder.build(),
                                null,
                                null
                        )
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                } catch (e: CameraAccessException) {

                }
            }
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.e("CAMERA2", "Configuration error on device '" + mCameraDevice.getId())
        }


    }

    private fun setAspectRatioTextureView(ResolutionWidth: Int, ResolutionHeight: Int) {
        if (ResolutionWidth > ResolutionHeight) {
            val newWidth = mWidth
            val newHeight = mWidth * ResolutionWidth / ResolutionHeight
            updateTextureViewSize(newWidth, newHeight)

        } else {
            val newWidth = mWidth
            val newHeight = mWidth * ResolutionHeight / ResolutionWidth
            updateTextureViewSize(newWidth, newHeight)
        }

    }


    private fun updateTextureViewSize(viewWidth: Int, viewHeight: Int) {
        Log.d("ViewSize", "TextureView Width : $viewWidth TextureView Height : $viewHeight")
        surfaceView.layoutParams = FrameLayout.LayoutParams(viewWidth, viewHeight)


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 99 갤러리
        when (requestCode) {
            99 -> {
                if (requestCode == Activity.RESULT_OK && requestCode == 99) {
                    //이미지뷰.setimageURl(data?.data)
                }
            }

        }
    }

}

internal class CapturedImageSaver(private val mCapture: Image) : Runnable {
    override fun run() {
        try {
            // 파일명 지정
            val file = File.createTempFile(
                    "Test", ".jpg",
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES
                    )
            )
            try {
                FileOutputStream(file).use { ostream ->
                    Log.i(
                            "CAMERAA2", "Retrieved image is" +
                            (if (mCapture.format == ImageFormat.JPEG) "" else "n't") + " a JPEG"
                    )
                    val buffer: ByteBuffer = mCapture.planes[0].buffer
                    Log.i(
                            "CAMERAA2", "Captured image size: " +
                            mCapture.width + 'x' + mCapture.height
                    )
                    // 이미지를 파일에 쓰기
                    val jpeg = ByteArray(buffer.remaining())
                    buffer.get(jpeg)
                    ostream.write(jpeg)
                }
            } catch (ex: FileNotFoundException) {
                Log.e("CAMERAA2", "Unable to open output file for writing", ex)
            } catch (ex: IOException) {
                Log.e("CAMERAA2", "Failed to write the image to the output file", ex)
            }
        } catch (ex: IOException) {
            Log.e("CAMERAA2", "Unable to create a new output file", ex)
        } finally {
            mCapture.close()
        }
    }
}




