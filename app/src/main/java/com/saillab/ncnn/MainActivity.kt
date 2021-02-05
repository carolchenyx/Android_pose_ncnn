package com.saillab.ncnn;

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cheungbh.instance.Injector
import com.cheungbh.yogasdk.domain.BodyPart
import com.cheungbh.yogasdk.domain.Person
import com.cheungbh.yogasdk.view.CameraView
import com.cheungbh.yogasdk.view.CameraViewYolo
import com.cheungbh.yogasdk.view.OverlayViewYolo
import com.cheungbh.yogasdk.view.OverlayViewYolo1
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean


public class MainActivity : AppCompatActivity() {
    private lateinit var cameraView: CameraViewYolo
    private lateinit var overlayView: OverlayViewYolo
    private lateinit var overlayView1: OverlayViewYolo1
    private lateinit var button: Button
    private lateinit var btn_video: Button
    private var USE_GPU = false
    private val REQUEST_PICK_IMAGE = 2
    private val REQUEST_PICK_VIDEO = 3
    private lateinit var resultImageView: ImageView

    private val detectCamera = AtomicBoolean(false)
    private val detectPhoto = AtomicBoolean(false)
    private val detectVideo = AtomicBoolean(false)
    companion object{
        private const val TAG = "MainActivity"
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {
        }

        Injector.setNetLibrary(applicationContext)

//        SimplePose.init(assets, USE_GPU)
        NanoPose.init(assets, USE_GPU)

        setContentView(R.layout.activity_main)
        button = findViewById(R.id.button)
        btn_video = findViewById(R.id.btn_video)
        overlayView = findViewById(R.id.resultView)
        overlayView1 = findViewById(R.id.resultView1)
        resultImageView = findViewById(R.id.imageView )
        cameraView = findViewById<CameraViewYolo>(R.id.cameraView).apply {
            setEventListener(object : CameraViewYolo.EventListener {
                override fun onImageAvailable(image: Image) {
                    val resultBitmap: Bitmap = Injector.getProcessImg().process(
                            image,
                            cameraView.getImgReaderSize(),
                            cameraView.getSensorOrientation()!!,
//                            activity!!.windowManager.defaultDisplay.rotation

                            this@MainActivity.windowManager.defaultDisplay.rotation
                    )

                    var SimplePoseResult: Array<KeyPoint>? = null
                    /** AI interference */
                    if (resultBitmap!= null)
                    {
//                        SimplePoseResult = SimplePose.detect(resultBitmap)
                        SimplePoseResult = NanoPose.detect(resultBitmap,0.3,0.7)
                    }

                    var finalscore: Float = 0.0f
                    var result: Person = Person()
                    var kpts: MutableList<com.cheungbh.yogasdk.domain.KeyPoint> = mutableListOf()
                    if(SimplePoseResult != null && SimplePoseResult.size > 0)
                    {
                        for(i in 0..12)
                        {
                            var kpt = com.cheungbh.yogasdk.domain.KeyPoint()
                            kpt.position.x = SimplePoseResult[0].x[i].toInt()
                            kpt.position.y = SimplePoseResult[0].y[i].toInt()
                            kpt.bodyPart =  BodyPart.values()[i]
                            kpt.score = SimplePoseResult[0].score
                            finalscore += SimplePoseResult[0].score
                            kpts.add(kpt)
                        }
                        result.x0 = SimplePoseResult[0].x0
                        result.y0 = SimplePoseResult[0].y0
                        result.x1 = SimplePoseResult[0].x1
                        result.y1 = SimplePoseResult[0].y1
                    }

                    result.keyPoints = kpts
                    result.score = finalscore/13
                    finalscore = 0.0f



                    /** update UI */
//                    this.runOnUiThread{
//                        //updating code
//                    }
                    //overlayView.drawResult(result, color)
                    if (result != null && result.keyPoints.size>0) {
                        overlayView.drawResult(
                                result,
                                null,
                                null,
                                cameraView.getImgReaderSize().width,
                                cameraView.getImgReaderSize().height
                        )
                    }

                }

                /**
                 * @param width : calculated CameraView's width
                 * @parasdkm height : calculated CameraView's height
                 * */
                override fun onCalculateDimension(width: Int, height: Int) {
                    overlayView.requestLayout(width, height)
                }
            })
        }

        checkCameraPermission()
        button.setOnClickListener(View.OnClickListener {
            val permission = ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        777
                )
            } else {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_PICK_IMAGE)
            }
        })
        btn_video.setOnClickListener(View.OnClickListener {
            val permission = ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        777
                )
            } else {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "video/*"
                startActivityForResult(intent, REQUEST_PICK_VIDEO)
            }
        })

    }
    //    private lateinit var cameraView: CameraView
//    private lateinit var overlayView: OverlayView
    override fun onResume() {
        super.onResume()

        /** Setting Camera Direction */
        //cameraView.setCameraFacing(CameraCharacteristics.LENS_FACING_FRONT)
        //overlayView.setCameraFacing(CameraCharacteristics.LENS_FACING_FRONT)

        cameraView.onResume()
    }
    override fun onPause() {
        super.onPause()
        cameraView.onPause()
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkCameraPermission() {
        var permissionCamera = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)

        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.CAMERA
            )
            ) {
                //show something to tell users why our app need permission
            }else{
                requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        CameraView.REQUEST_CAMERA_PERMISSION
                )
            }
        }else{
            cameraView.permissionGranted()
        }
    }
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        if (requestCode == CameraView.REQUEST_CAMERA_PERMISSION &&
                allPermissionsGranted(grantResults)) {
            cameraView.permissionGranted()
        }else{
            cameraView.permissionDenied()

        }
    }
    private fun allPermissionsGranted(grantResults: IntArray) = grantResults.all {
        it == PackageManager.PERMISSION_GRANTED
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        if (requestCode == REQUEST_PICK_IMAGE) {
            // photo
            runByPhoto(requestCode, resultCode, data)
//        } else if (requestCode == REQUEST_PICK_VIDEO) {
//            // video
//            runByVideo(requestCode, resultCode, data)
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    fun runByPhoto(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null || data.data == null) {
            Toast.makeText(this, "Photo error", Toast.LENGTH_SHORT).show()
            return
        }
        if (detectVideo.get()) {
            Toast.makeText(this, "Video is running", Toast.LENGTH_SHORT).show()
            return
        }

        detectPhoto.set(true)
        val image: Bitmap? = getPicture(data.data)
        if (image == null) {
            Toast.makeText(this, "Photo is null", Toast.LENGTH_SHORT).show()
            return
        }

        NanoPose.init(assets, USE_GPU)

        val thread = Thread(Runnable {

            val start = System.currentTimeMillis()
            val mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true)
            val width = image.width
            val height = image.height
            Log.d(TAG, "runByPhoto: width=$width, height=$height")

//            mutableBitmap = detectAndDraw(mutableBitmap)
            val dur = System.currentTimeMillis() - start
            runOnUiThread {
                    var simplePoseResult: Array<KeyPoint>? = null
                    if (mutableBitmap != null) {
                        //                        SimplePoseResult = SimplePose.detect(resultBitmap)
                        simplePoseResult = NanoPose.detect(mutableBitmap, 0.3, 0.7)
                    }

                Log.d(TAG, "runByPhoto: simplePoseResult=${simplePoseResult?.get(0).toString()}")
                    var finalscore: Float = 0.0f
                    var result: Person = Person()
                    var kpts: MutableList<com.cheungbh.yogasdk.domain.KeyPoint> = mutableListOf()
                    if (simplePoseResult != null && simplePoseResult.size > 0) {
                        for (i in 0..12) {
                            var kpt = com.cheungbh.yogasdk.domain.KeyPoint()
                            kpt.position.x = simplePoseResult[0].x[i].toInt()
                            kpt.position.y = simplePoseResult[0].y[i].toInt()
                            kpt.bodyPart = BodyPart.values()[i]
                            kpt.score = simplePoseResult[0].score
                            finalscore += simplePoseResult[0].score
                            kpts.add(kpt)
                        }
                        result.x0 = simplePoseResult[0].x0
                        result.y0 = simplePoseResult[0].y0
                        result.x1 = simplePoseResult[0].x1
                        result.y1 = simplePoseResult[0].y1
                    }

                    result.keyPoints = kpts
                    result.score = finalscore / 13
                    finalscore = 0.0f

                    if (result != null && result.keyPoints.size > 0) {
                        overlayView1.drawResult(
                                result,
                                null,
                                null,
                                width,
                                height
                        )
                    }

                    resultImageView.setImageBitmap(mutableBitmap)
//                tvInfo.setText(String.format(Locale.CHINESE, "%s\nSize: %dx%d\nTime: %.3f s\nFPS: %.3f",
//                        modelName, height, width, dur / 1000.0, 1000.0f / dur))
            }
        }, "photo detect")
        thread.start()
    }

    fun getPicture(selectedImage: Uri?): Bitmap? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = this.contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
                ?: return null
        cursor.moveToFirst()
        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        val picturePath = cursor.getString(columnIndex)
        cursor.close()
        val bitmap = BitmapFactory.decodeFile(picturePath) ?: return null
        val rotate: Int = readPictureDegree(picturePath)
        return rotateBitmapByDegree(bitmap, rotate)
    }

    fun readPictureDegree(path: String?): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return degree
    }

    fun rotateBitmapByDegree(bm: Bitmap, degree: Int): Bitmap? {
        var returnBm: Bitmap? = null
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.width,
                    bm.height, matrix, true)
        } catch (e: OutOfMemoryError) {
            e.printStackTrace()
        }
        if (returnBm == null) {
            returnBm = bm
        }
        if (bm != returnBm) {
            bm.recycle()
        }
        return returnBm
    }

}

