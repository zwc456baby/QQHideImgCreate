package com.example.qqhideimgcreate.Utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import java.io.File
import java.io.FileInputStream


class ImgMixUtil private constructor() {

    companion object {
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            ImgMixUtil()
        }
        val RECEIVE_IMGMIX_PROGRESS = "com.example.qqhideimgcreate.Utils.ImgMixUtil.receive_imgmix_progress"
        val GET_PROGRESS_KEY = "get_progress_key"
    }

    fun CalculateHiddenImage(context: Context, blackImage: Bitmap, whiteImage: Bitmap): Bitmap {
//        预处理图片信息 ,如果不对图片进行压缩，处理速度非常慢，难以接受，单线程可能需要半小时以上
//        val mRect = Rect()
//        mRect.left = 0
//        mRect.top = 0
//        mRect.right = 1
//        mRect.bottom = 1
//
//        val sOptions = BitmapFactory.Options()
//        sOptions.inPreferredConfig = Bitmap.Config.ARGB_8888
//        val blackInput = BitmapRegionDecoder.newInstance(FileInputStream(File(blackImage)), false);
//        val whiteInput = BitmapRegionDecoder.newInstance(FileInputStream(File(whiteImage)), false);
//
//        val options = BitmapFactory.Options()
//        options.inJustDecodeBounds = false
//        BitmapFactory.decodeFile(blackImage, options)
//        开始读取图片信息
        val b_width = blackImage.width
        val b_height = blackImage.height
//        BitmapFactory.decodeFile(whiteImage, options)
        val w_width = whiteImage.width
        val w_height = whiteImage.height

        //设定最终图片的尺寸
        val f_width = Math.max(b_width, w_width);
        val f_height = Math.max(b_height, w_height);

        val result = Bitmap.createBitmap(f_width, f_height, Bitmap.Config.ARGB_8888);

        //黑色图片距离边缘的距离
        val b_widthOffset = if (b_width == f_width) 0 else (f_width - b_width) / 2;
        val b_heightOffset = if (b_height == f_height) 0 else (f_height - b_height) / 2;

        //白色图片离边缘距离
        val w_widthOffset = if (w_width == f_width) 0 else (f_width - w_width) / 2;
        val w_heightOffset = if (w_height == f_height) 0 else (f_height - w_height) / 2;

        val allByte = f_width.toLong() * f_height.toLong()
        var oldProgress = -1
        for (x in 0 until f_width) {
            for (y in 0 until f_height) {
                oldProgress = sendProgress(context, oldProgress, (x).toLong() * (f_height).toLong(), allByte)
//                mRect.offsetTo(x, y)
                //上下左右交叉排列黑白像素
                val blackPixel = (x + y) % 2 == 0

                var coor_x: Int;
                var coor_y: Int;
                //决定当前像素位置是否对应图一或图二某像素，如果没有，跳过循环
                var validPixel = true;
                if (blackPixel) {
                    coor_x = x - b_widthOffset;
                    if (coor_x > b_width - 1) validPixel = false;
                    coor_y = y - b_heightOffset;
                    if (coor_y > b_height - 1) validPixel = false;
                } else {
                    coor_x = x - w_widthOffset;
                    if (coor_x > w_width - 1) validPixel = false;
                    coor_y = y - w_heightOffset;
                    if (coor_y > w_height - 1) validPixel = false;
                }

                validPixel = validPixel && coor_x >= 0 && coor_y >= 0;
                if (!validPixel) continue;


                val origin: Int =
                    if (blackPixel) {
//                        blackInput.decodeRegion(mRect, sOptions).getPixel(0, 0)
                        blackImage.getPixel(coor_x, coor_y)
                    } else {
//                        whiteInput.decodeRegion(mRect, sOptions).getPixel(0, 0)
                        whiteImage.getPixel(coor_x, coor_y)
                    }

//                将彩色图片转成黑白色图片
                val gray =
                    (Color.red(origin) * 0.3 + Color.green(origin) * 0.59 + Color.blue(origin) * 0.11).toInt()

                //根据颜色计算像素灰度，设定透明度
//                原理就是根据原来的颜色，设定透明度，这样在白色背景和黑色背景下。会有不同的显示效果
                var finalColor: Int
                if (blackPixel) {
                    val tmpGray = 255 - gray
                    finalColor =
                            Color.argb(
                                tmpGray,
                                Color.red(Color.BLACK),
                                Color.green(Color.BLACK),
                                Color.blue(Color.BLACK)
                            )
                } else {
                    val tmpGray = gray
                    finalColor =
                            Color.argb(
                                tmpGray,
                                Color.red(Color.WHITE),
                                Color.green(Color.WHITE),
                                Color.blue(Color.WHITE)
                            )
                }
                result.setPixel(x, y, finalColor);
            }
        }

        return result;
    }

    private fun sendProgress(context: Context, old: Int, current: Long, all: Long): Int {

        val progress = (ArithUtil.div(current.toDouble(), all.toDouble()) * 100).toInt()

        if (progress == old) {
            return old
        }
        val intent = Intent()
        intent.setAction(RECEIVE_IMGMIX_PROGRESS)
        intent.putExtra(GET_PROGRESS_KEY, progress)

        context.sendBroadcast(intent)
        return progress
    }

}