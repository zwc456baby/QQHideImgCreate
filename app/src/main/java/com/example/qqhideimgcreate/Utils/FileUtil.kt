package com.example.qqhideimgcreate.Utils

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class FileUtil private constructor() {
    companion object {
        //        这种方式是 kotlin 定义的一种 单例方式
//        使用 by lazy 进行初始化，这样可以保证对象只有在访问的时候才会被创建
//        而且 设置 mode ，这里保证了线程的安全性，确保只创建一次
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            FileUtil()
        }
    }

    fun copyToPathFromStream(input: InputStream, path: String): Boolean {
        if (path.isEmpty()) return false
        val tofile = File(path)
        if (tofile.exists() && tofile.isFile) if (!tofile.delete()) return false
        if (tofile.exists() && tofile.isDirectory) {
            Log.e("tofile exists and tofile is dir,dont not copy ")
            return false
        }
        if (!tofile.createNewFile()) {
            Log.e("create new file error")
            return false
        }
        val tmp = ByteArray(1024)
        var length: Int

        val outputStream: OutputStream = FileOutputStream(tofile)
        do {
            length = input.read(tmp, 0, tmp.size)
            if (!(length > 0)) break
            outputStream.write(tmp, 0, length)
        } while (true)
        outputStream.flush()
        outputStream.close()
        input.close()
        return true
    }
}