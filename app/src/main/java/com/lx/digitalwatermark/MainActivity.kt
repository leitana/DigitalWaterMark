package com.lx.digitalwatermark

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.lx.digitalwatermark.RealPathFromUriUtils.getPath
import com.watermark.androidwm.WatermarkBuilder
import com.watermark.androidwm.WatermarkDetector
import com.watermark.androidwm.bean.WatermarkText
import com.watermark.androidwm.listener.BuildFinishListener
import com.watermark.androidwm.listener.DetectFinishListener
import com.watermark.androidwm.task.DetectionReturnValue
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.Matisse.obtainPathResult
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.internal.entity.CaptureStrategy
import kotlinx.android.synthetic.main.activity_main.*
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_CHOOSE = 1
    private val REQUEST_PERMISSION_SETTING: Int = 2
    private lateinit var bitMap: Bitmap
    private lateinit var filePath: String

    val permissionArray = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.CAMERA)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(this, permissionArray, REQUEST_PERMISSION_SETTING)

        chooseImg.setOnClickListener {
            Matisse.from(this@MainActivity)
                .choose(MimeType.ofAll()) // 选择 mime 的类型
                .countable(true)
                .capture(true)
                .captureStrategy(
                    CaptureStrategy(true, "com.lx.digitalwatermark.fileprovider")
                )
                .maxSelectable(1) // 图片选择的最多数量
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f) // 缩略图的比例
                .imageEngine(GlideEngine()) // 使用的图片加载引擎
                .forResult(REQUEST_CODE_CHOOSE) // 设置作为标记的请求码

        }

        //压缩图片
        compressImg.setOnClickListener {
            val file = File(GetImagePathUtil.getRootDir(this) + File.separator + "compressPic")
            if (!file.exists()) {
                file.mkdirs()
            }
            Luban.with(this)
                .load(filePath)
                .setTargetDir(file.path)
                .setCompressListener(object : OnCompressListener{
                    override fun onStart() {
                        //压缩前开始调用
                    }

                    override fun onSuccess(file: File?) {
                        file?.let { filePath = file.path }
                        bitMap = BitmapFactory.decodeFile(file?.path)
                        val uri = Uri.parse(
                            MediaStore.Images.Media.insertImage(
                                contentResolver,
                                bitMap,
                                null,
                                null
                            )
                        )
                        ImageLoaderUtils.display(this@MainActivity,image, uri.toString())
                        Toast.makeText(this@MainActivity, "压缩图片成功", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(e: Throwable?) {
                        Toast.makeText(this@MainActivity, e?.message, Toast.LENGTH_SHORT).show()
                    }
                }).launch()
        }

        //添加水印
        addWaterMark.setOnClickListener {
            val watermarkText = WatermarkText("测试水印128391237")
                .setPositionX(0.5) // 横坐标
                .setPositionY(0.5) // 纵坐标
                .setTextAlpha(100) // 透明度
                .setTextColor(Color.WHITE) // 文字水印文字颜色

            WatermarkBuilder
                .create(this, bitMap)
                .loadWatermarkText(watermarkText)
                .setInvisibleWMListener(true, object : BuildFinishListener<Bitmap?> {
                    override fun onSuccess(bitmap: Bitmap?) {
                        if (bitmap != null) {
                            Toast.makeText(this@MainActivity, "添加水印成功成功", Toast.LENGTH_SHORT).show()
                            bitMap = bitmap
                            val fileMid = File(GetImagePathUtil.getRootDir(this@MainActivity) + File.separator + "watermark")
                            if (!fileMid.exists()) {
                                fileMid.mkdir()
                            }
                            val date = Date()
                            val timestamp = date.time.toString()
                            val file = File(GetImagePathUtil.getRootDir(this@MainActivity) + File.separator + "watermark" + File.separator + timestamp + ".jpg")
                            filePath = file.path
                            BitMapUtils.saveBitmap(bitMap, file)

                        }
                    }

                    override fun onFailure(message: String) {
                        // do something...
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    }
                })
        }

        readWaterMark.setOnClickListener {
            WatermarkDetector
                .create(bitMap, true)
                .detect(object : DetectFinishListener{
                    override fun onSuccess(returnValue: DetectionReturnValue?) {
//                        val watermarkImage = returnValue!!.watermarkBitmap
                        val watermarkString = returnValue!!.watermarkString
                        Toast.makeText(this@MainActivity, watermarkString, Toast.LENGTH_SHORT).show()
                    }

                    override fun onFailure(message: String?) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "解析水印失败", Toast.LENGTH_SHORT).show()
                        }
                    }

                })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            if (Matisse.obtainResult(data).size > 0) {
                for (uri in Matisse.obtainResult(data)) {
                    ImageLoaderUtils.display(this, image, uri.toString())
                    bitMap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    filePath = getPath(this@MainActivity, uri)
                }
            }
        }
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            requestPermissions(
                this@MainActivity,
                permissionArray,
                REQUEST_PERMISSION_SETTING
            )
        }
    }
}