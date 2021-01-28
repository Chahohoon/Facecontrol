package com.example.facecontrol

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Bundle
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.cameraview.*
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

class CameraView: AppCompatActivity(), SurfaceHolder.Callback {


    private lateinit var mImageReader: ImageReader
    private lateinit var mCameraDevice: CameraDevice
    private lateinit var mPreviewBuilder: CaptureRequest.Builder
    private lateinit var mSession: CameraCaptureSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cameraview)

        OpenCamera()
    }


    fun OpenCamera() {

        val mCamera = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val setCamera = mCamera.cameraIdList[0] // default 카메라 선택
        val characteristics = mCamera.getCameraCharacteristics(setCamera) //선택한 카메라의 특징 확인

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mCamera.openCamera(setCamera,deviceStateCallback,null)
        if (mCamera.cameraIdList.isEmpty()) {
            return
        }


    }


    //Camera Device
    private val deviceStateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            mCameraDevice = camera
            try {
                mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
                mPreviewBuilder.addTarget(SurfaceView.holder.surface)
                mCameraDevice.createCaptureSession(listOf(SurfaceView.holder.surface), mSessionPreviewStateCallback, null)

            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()

        }



        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
        }

    }

    private val mSessionPreviewStateCallback = object : CameraCaptureSession.StateCallback() {
        override fun onConfigured(session: CameraCaptureSession) {
            mSession = session
            try {
                // Key-Value 구조로 설정
                // 오토포커싱이 계속 동작
                mPreviewBuilder.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                //필요할 경우 플래시가 자동으로 켜짐
                mPreviewBuilder.set(
                        CaptureRequest.CONTROL_AE_MODE,
                        CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
                )
                mSession.setRepeatingRequest(mPreviewBuilder.build(), null, null)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }

        }

        override fun onConfigureFailed(session: CameraCaptureSession) {
//            TODO("Not yet implemented")
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
//        TODO("Not yet implemented")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//        TODO("Not yet implemented")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
//        TODO("Not yet implemented")
    }
}



