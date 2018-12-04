package com.example.qqhideimgcreate

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.example.qqhideimgcreate.Utils.FileUtil
import com.example.qqhideimgcreate.Utils.ImageUtil
import com.example.qqhideimgcreate.Utils.ImgMixUtil
import com.example.qqhideimgcreate.Utils.Log
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_show_img.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var displayImgPath: String? = null
        set(value) {
            val bitmap = checkImgPath(displaySelectIv, value)
            if (bitmap == null) {
                Log.v("path error")
                return
            }
            displaySelectIv.setImageBitmap(bitmap)
            field = value
        }
    private var hideImgPath: String? = null
        set(value) {
            val bitmap = checkImgPath(hideSelectIv, value)
            if (bitmap == null) {
                Log.v("path error")
                return
            }
            hideSelectIv.setImageBitmap(bitmap)
            field = value
        }

    /**
     * 0 : displayImgPath
     * 1 : hideImgPath
     */
    private var selectType = -1

    private var saveBitmap: Bitmap? = null

    private val progressReceive = ProgressBroaddReceive()

    private var currentMixThreadName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setClickEvent()
        registerProgressReceive()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterProgressReceive()
    }

    private fun setClickEvent() {
        Log.v("set click event")
        displaySelectIv.setOnClickListener(this)
        hideSelectIv.setOnClickListener(this)
        mixBtn.setOnClickListener(this)
    }

    private fun registerProgressReceive() {
        val filter = IntentFilter()
        filter.addAction(ImgMixUtil.RECEIVE_IMGMIX_PROGRESS)
        registerReceiver(progressReceive, filter)
    }

    private fun unregisterProgressReceive() {
        try {
            unregisterReceiver(progressReceive)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(p0: View?) {
        Log.v("view click")
        when (p0?.id) {
            R.id.displaySelectIv -> {
                Log.i("display select img click")
                startSelectImg(0)
            }
            R.id.hideSelectIv -> {
                Log.i("hide select img click")
                startSelectImg(1)
            }
            R.id.mixBtn -> mixImg(displayImgPath, hideImgPath)
            R.id.saveBtn -> {
                Log.i("save bitmap btn click")
                saveBitmap?.run {
                    saveToFile(this)
                    saveBitmap = null
                }
                waitDialog?.run {
                    this.dismiss()
                    waitDialog = null
                }
                waitDialogView?.run {
                    waitDialogView = null
                }
            }
            else -> {
                Log.w("not fund view click action")
            }
        }
    }

    private fun checkImgPath(imgView: ImageView, path: String?): Bitmap? {
        if (path.isNullOrEmpty()) return null
        if (!File(path).exists()) return null
        try {
            return ImageUtil.getBitmap(path, imgView.maxHeight, imgView.maxHeight)
        } catch (e: Exception) {
            return null
        }
    }

    private fun startSelectImg(type: Int) {
        selectType = type
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, "Browser Image..."), REQUEST_SELECTIMG_CODE)
    }

    private fun mixImg(displayImgPath: String?, hideImgPath: String?) {
        if (displayImgPath.isNullOrEmpty() || hideImgPath.isNullOrEmpty()) {
            Log.w("请选择两张图片")
            return
        }
        showSaveDialog()


        val mixThead = Thread({
            val name = String(currentMixThreadName.toByteArray())  // 重新标记线程名称和对象
            val disImg: Bitmap = ImageUtil.getBitmap(displayImgPath, 640, 480)
            val hideImg: Bitmap = ImageUtil.getBitmap(hideImgPath, 640, 480)
            saveBitmap = ImgMixUtil.INSTANCE.CalculateHiddenImage(this@MainActivity, disImg, hideImg)
            if (!disImg.isRecycled) disImg.recycle()
            if (!hideImg.isRecycled) hideImg.recycle()

            if (!name.equals(currentMixThreadName))
                return@Thread
            this@MainActivity.runOnUiThread {
                waitDialogView?.apply {
                    this.waitProgress.visibility = View.INVISIBLE
                    this.mixProgressTv.visibility = View.INVISIBLE
                    this.showIv.visibility = View.VISIBLE
                    if (null != saveBitmap) {
                        this.showIv.setImageBitmap(saveBitmap)
                    }
                }
            }
        })
        currentMixThreadName = mixThead.name
        mixThead.start()

    }

    private var waitDialog: AlertDialog? = null
    private var waitDialogView: View? = null

    @SuppressLint("InflateParams")
    private fun showSaveDialog() {
        waitDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_show_img, null)
        waitDialogView?.apply {
            //        view.showIv.setImageBitmap(bitmap)
            this.saveBtn.setOnClickListener(this@MainActivity)
            Log.v("width:%d , height:%d".format(this.showIv.width, this.showIv.height))
            waitDialog = AlertDialog.Builder(this@MainActivity).setView(this).create()
        }

        waitDialog?.show()
    }

    private fun saveToFile(bitmap: Bitmap) {
        try {
            val outputStream: FileOutputStream =
                FileOutputStream(Environment.getExternalStorageDirectory().absolutePath + File.separator + "save.png")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            Log.e("save to file error:%s", Log.get(e))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            data?.data?.apply {
                val inputStream: InputStream? = getContentResolver().openInputStream(this)
                if (inputStream == null) {
                    Log.e("input stream is null ,not select img")
                    return
                }
                var type: String = "tmp"
                when (selectType) {
                    0 -> type = "displayImg"
                    1 -> type = "hideImg"
                }
                val savaImgPath = getImgTmpFilePath(type)
                val result = copyImgToPathFromStream(
                    inputStream,
                    savaImgPath
                )
                if (result) {
                    if (selectType == 0) {
                        displayImgPath = savaImgPath
                    } else if (selectType == 1) {
                        hideImgPath = savaImgPath
                    } else {
                        Log.w("not found select type:%d".format(selectType))
                    }
                }
            }
        }
    }

    private fun copyImgToPathFromStream(input: InputStream, path: String): Boolean {
        Log.v("copy img to path :%s".format(path))
        return FileUtil.INSTANCE.copyToPathFromStream(input, path)
    }

    private fun getImgTmpFilePath(type: String): String {
        val file = this@MainActivity.getExternalFilesDir(LOCAL_IMG_TYPE)
        if (null == file) return Environment.getExternalStorageDirectory().absolutePath + File.separator + type + ".png"
        return file.absolutePath + File.separator + type + ".png"
    }

    companion object {
        val REQUEST_SELECTIMG_CODE = 0
        val LOCAL_IMG_TYPE = "image"
    }

    inner class ProgressBroaddReceive : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val progress = intent?.getIntExtra(ImgMixUtil.GET_PROGRESS_KEY, 0)
            if (null != waitDialog && null != waitDialogView && waitDialogView!!.visibility == View.VISIBLE && waitDialog!!.isShowing) {
                waitDialogView?.mixProgressTv?.setText("当前进度:%d".format(progress))
            }
        }
    }
}
