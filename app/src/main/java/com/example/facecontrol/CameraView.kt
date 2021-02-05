package com.example.facecontrol

import android.Manifest
import android.R.attr
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.hardware.Camera
import android.hardware.camera2.*
import android.media.ExifInterface
import android.media.Image
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.*
import android.view.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.cameraview.*
import org.bytedeco.javacpp.RealSense
import java.io.*


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
    var mForegroundHandler: Handler? = null

    private var mHeight: Int = 0
    private var mWidth:Int = 0
    private var mCameraID = CAMERA_BACK // 기본 후면카메라

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
                mCameraDevice.close()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

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

            mImageReader = ImageReader.newInstance(
                    largestPreviewSize.width,
                    largestPreviewSize.height,
                    ImageFormat.JPEG,
                    7
            )

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mCamera.openCamera(mCameraID, deviceStateCallback, mBackgroundHandler)
        } catch (e: CameraAccessException) {

        }
    }

    // 음??
    fun getPicture() {

        val mCamera = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = mCamera.getCameraCharacteristics(mCameraID) //선택한 카메라의 특징 확인

        val rotation = windowManager.defaultDisplay.rotation
        mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))

        var file = File(Environment.getExternalStorageDirectory().toString())
        val readerListener = object : ImageReader.OnImageAvailableListener {
            override fun onImageAvailable(reader: ImageReader?) {
                var image: Image? = null

                try {
                    image = mImageReader!!.acquireLatestImage()

                    val buffer = image!!.planes[0].buffer
                    val bytes = ByteArray(buffer.capacity())
                    buffer.get(bytes)

                    var output: OutputStream? = null
                    try {
                        output = FileOutputStream(file)
                        output.write(bytes)
                    } finally {
                        output?.close()

                        var uri = Uri.fromFile(file)
                        Log.d("사진테스트", "uri 제대로 잘 바뀌었는지 확인 ${uri}")

                        // 프리뷰 이미지에 set 해줄 비트맵을 만들어준다
                        var bitmap: Bitmap = BitmapFactory.decodeFile(file.path)

                        // 비트맵 사진이 90도 돌아가있는 문제를 해결하기 위해 rotate 해준다
                        var rotateMatrix = Matrix()
                        rotateMatrix.postRotate(90F)
                        var rotatedBitmap: Bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, rotateMatrix, false)

                        // 90도 돌아간 비트맵을 이미지뷰에 set 해준다
//                        img_previewImage.setImageBitmap(rotatedBitmap)
                        val setimage = Intent(this@CameraView,ImageView::class.java)
                        setimage.putExtra("image", rotatedBitmap)
                        startActivity(setimage)

                    }
                } catch (e:FileNotFoundException) {
                    e.printStackTrace()
                } catch (e:IOException) {
                    e.printStackTrace()
                } finally {
                    image?.close()
                }
            }
        }
    }




    // 카메라 디바이스의 상태가 변경되면 호출되는 CallBack
    private val deviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            mCameraDevice = camera
            try{
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW) //미리보기 객체
                mCaptureRequestBuilder.addTarget(mSurfaceViewHolder.surface)
                mCameraDevice.createCaptureSession(
                        listOf(mSurfaceViewHolder.surface, mImageReader.surface), mSessionPreviewStateCallback, mBackgroundHandler
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
            try {
                mCaptureRequestBuilder.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                mSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler)

            }catch (e: CameraAccessException) {
                e.printStackTrace()
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




