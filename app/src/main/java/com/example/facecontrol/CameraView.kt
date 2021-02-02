package com.example.facecontrol

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ExifInterface
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.cameraview.*
import java.io.File


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
    private lateinit var mCameraDevice: CameraDevice
    private lateinit var mSession: CameraCaptureSession
    private lateinit var mCaptureRequest :CaptureRequest
    private lateinit var mCaptureRequestBuilder :CaptureRequest.Builder

    //이미지 저장 변수
    private lateinit var mImageReader: ImageReader
    private lateinit var mSize : Size
    private lateinit var mFile : File
    var mBackgroundThread: HandlerThread? = null
    var mBackgroundHandler: Handler? = null

    private var mHeight: Int = 0
    private var mWidth:Int = 0

    //화면 각도
    companion object
    {
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
            R.id.btn_Capture -> {
                getPicture()
            }
        }
    }

    fun initView() {
        with(DisplayMetrics()) {
            windowManager.defaultDisplay.getMetrics(this)
            mHeight = heightPixels
            mWidth = widthPixels
        }

        mSurfaceViewHolder = surfaceView.holder
        mSurfaceViewHolder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                initCameraAndPreview()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mCameraDevice.close()
            }

            override fun surfaceChanged(
                    holder: SurfaceHolder, format: Int,
                    width: Int, height: Int
            ) {

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
        val open_gallery = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(open_gallery, 99)
    }

    fun OpenCamera() {

        try {


            val mCamera = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val setCamera = mCamera.cameraIdList[0] // default 카메라 선택    0 후면 1 전면 2 기타
            val characteristics = mCamera.getCameraCharacteristics(setCamera) //선택한 카메라의 특징 확인

            val Map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            val largestPreviewSize =
                    Map!!.getOutputSizes(ImageFormat.JPEG)[0]
            setAspectRatioTextureView(largestPreviewSize.height, largestPreviewSize.width)

            mImageReader = ImageReader.newInstance(
                    largestPreviewSize.width,
                    largestPreviewSize.height,
                    ImageFormat.JPEG,
                    7
            )

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mCamera.openCamera(setCamera, deviceStateCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {

        }
    }


    fun getPicture() {
        val mCamera = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = mCamera.getCameraCharacteristics(mCameraDevice.id)
        var jpegSize: ArrayList<Size>


        if (mCameraDevice == null) {
            return
        }
    }



    // 카메라 디바이스의 상태가 변경되면 호출되는 CallBack
    private val deviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            mCameraDevice = camera
            try{
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                mCaptureRequestBuilder.addTarget(mSurfaceViewHolder.surface)
                mCameraDevice.createCaptureSession(
                        listOf(mSurfaceViewHolder.surface,mImageReader.surface),mSessionPreviewStateCallback,mBackgroundHandler
                )


            } catch(e: CameraAccessException) {
                //
            }
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.e("연결끊김","")
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
            try {
                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                mSession.setRepeatingRequest(mCaptureRequestBuilder.build(),null,mBackgroundHandler)

            }catch (e:CameraAccessException) {

            }
        }

        override fun onConfigureFailed(session: CameraCaptureSession) {

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




