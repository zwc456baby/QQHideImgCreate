package com.example.qqhideimgcreate

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import com.example.qqhideimgcreate.Utils.ImageUtil
import com.example.qqhideimgcreate.Utils.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity(), View.OnClickListener {

    var displayImgPath: String? = null
        set(value) {
            val bitmap = checkImgPath(displaySelectIv, value)
            if (bitmap == null) {
                Log.v("path error")
                return
            }
            displaySelectIv.setImageBitmap(bitmap)
            field = value
        }
    var hideImgPath: String? = null
        set(value) {
            val bitmap = checkImgPath(hideSelectIv, value)
            if (bitmap == null) {
                Log.v("path error")
                return
            }
            hideSelectIv.setImageBitmap(bitmap)
            field = value
        }

    var selectType = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setClickEvent()
    }

    fun setClickEvent() {
        Log.v("set click event")
        displaySelectIv.setOnClickListener(this)
        hideSelectIv.setOnClickListener(this)
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
            else -> {
                Log.w("not fund view click action")
            }
        }
    }

    fun checkImgPath(imgView: ImageView, path: String?): Bitmap? {
        if (path.isNullOrEmpty()) return null
        if (!File(path).exists()) return null
        try {
            return ImageUtil.getBitmap(path, imgView.maxHeight, imgView.maxHeight)
        } catch (e: Exception) {
            return null
        }

//        旧的实现
//        if (path.isNullOrEmpty()) return null
//        if (!File(path).exists()) return null
//        val bitmap: Bitmap
//        try {
//            bitmap = BitmapFactory.decodeFile(path)
//        } catch (e: Exception) {
//            return null
//        }
//        return bitmap
    }

    fun startSelectImg(type: Int) {
        selectType = type
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent, "Browser Image..."), REQUEST_SELECTIMG_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            data?.data?.apply {
                if (selectType == 0) {
                    displayImgPath = getRealPathFromURI(this@MainActivity, this)
                } else if (selectType == 1) {
                    hideImgPath = getRealPathFromURI(this@MainActivity, this)
                }
            }

//            旧的实现
//            val uri: Uri? = data?.getData()
//            val options: BitmapFactory.Options = BitmapFactory.Options()
//            options.inJustDecodeBounds = false
//            try {
//                if (uri == null) {
//                    Log.v("uri is null")
//                    return
//                }
//                val inputStream: InputStream? = getContentResolver().openInputStream(uri)
//                if (inputStream == null) {
//                    Log.v("input stream is null")
//                    return
//                }
//                BitmapFactory.decodeStream(inputStream, null, options)
//                inputStream.close()
//                val height: Int = options.outHeight
//                val width: Int = options.outWidth
//                var sampleSize = 1
//                val max: Int = Math.max(height, width)
//                val MAX_SIZE = 769
//                if (max > MAX_SIZE) {
//                    val nw: Int = width / 2
//                    val nh: Int = height / 2
//                    while ((nw / sampleSize) > MAX_SIZE || (nh / sampleSize) > MAX_SIZE) {
//                        sampleSize *= 2
//                    }
//                }
//                options.inSampleSize = sampleSize
//                options.inJustDecodeBounds = false
//                val selectdBitmap =
//                    BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, options)
//                if (selectType == 0)
//                    displaySelectIv.setImageBitmap(selectdBitmap)
//                else if (selectType == 1)
//                    hideSelectIv.setImageBitmap(selectdBitmap)
//                else
//                    Log.v("not found select type :%d".format(selectType))
//            } catch (ioe: Exception) {
//                Log.e("select img error:%s".format(Log.get(ioe)))
//            }
        }
    }

    fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        Log.v(contentUri)
        val cursor = context.getContentResolver().query(contentUri, null, null, null, null)
        if (cursor == null) {
            cursor?.close()
            return contentUri.path
        } else {
            cursor.moveToFirst()
            val index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            val realPath = cursor.getString(index)
            cursor.close()
            Log.v(realPath)
            return realPath
        }
    }

    companion object {
        val REQUEST_SELECTIMG_CODE = 0
    }

}
