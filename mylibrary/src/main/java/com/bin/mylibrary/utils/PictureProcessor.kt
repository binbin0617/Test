//package com.bin.mylibrary.utils
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.graphics.Matrix
//import android.media.ExifInterface
//import java.io.File
//import java.io.FileOutputStream
//import kotlin.math.max
//import kotlin.math.roundToInt
//
///**
// * 图片预处理
// */
//data class  PictureProcessor(var name:String) {
//
//
//    fun compress(file: File, cacheDir: String, sizeLimitKB: Int = 500): File {
//        var src = file
//        if (src.length() <= sizeLimitKB * 1024) return src
//
//        val degree = getPictureRotateDegree(src)
//        val bitmap = getBitmap(src, degree, cacheDir) {
//            src = it
//        }
//
//        if (src.length() <= sizeLimitKB * 1024) return src
//
//        val options = getOptions(src)
//        val width = if (options.outWidth < options.outHeight) options.outWidth else options.outHeight
//        val quality = when (width > 1800) {
//            true -> 60
//            false -> 70
//        }
//
//        return qualityCompress(bitmap, cacheDir, quality).apply {
//            if (src.name != file.name) src.delete()
//        }
//    }
//
//    private fun getBitmap(src: File, degree: Int, cacheDir: String, onResize: (File) -> Unit): Bitmap {
//        val srcBitmap = BitmapFactory.decodeFile(src.absolutePath, getOptions(src, 2800))
//        return if (degree != 0) {
//            getRotateBitmap(srcBitmap, degree.toFloat()).apply {
//                srcBitmap.recycle()
//
//                val dir = File(cacheDir).apply {
//                    if (!this.exists()) this.mkdirs()
//                }
//                val outImg = File(dir, "${System.currentTimeMillis()}.jpg")
//                val fileOutputStream = FileOutputStream(outImg)
//
//                this.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
//
//                onResize(outImg)
//            }
//        } else {
//            srcBitmap
//        }
//    }
//
//    private fun getOptions(src: File, reqWidth: Int = 0): BitmapFactory.Options {
//        val options = BitmapFactory.Options()
//        options.inJustDecodeBounds = true
//        BitmapFactory.decodeFile(src.absolutePath, options)
//        options.inJustDecodeBounds = false
//
//        var inSampleSize = 1
//        if (reqWidth != 0) {
//
//            val width = if (options.outWidth < options.outHeight) options.outWidth else options.outHeight
//
//            if (width > reqWidth) {
//                inSampleSize = max((width.toFloat() / reqWidth.toFloat()).roundToInt(), 2)
//            }
//        }
//
//        return options.apply { this.inSampleSize = inSampleSize }
//    }
//
//    private fun getPictureRotateDegree(srcImg: File): Int {
//        var degree = 0
//        /* 获取其EXIF信息 */
//        val exifInterface = ExifInterface(srcImg.absolutePath)
//        /* 获取图片的旋转信息 */
//        val orientation = exifInterface.getAttributeInt(
//            ExifInterface.TAG_ORIENTATION,
//            ExifInterface.ORIENTATION_NORMAL
//        )
//        when (orientation) {
//            ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
//            ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
//            ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
//        }
//        return degree
//    }
//
//    private fun getRotateBitmap(b: Bitmap, rotateDegree: Float): Bitmap {
//        return try {
//            val matrix = Matrix()
//            matrix.postRotate(rotateDegree)
//
//            val afterRotate = Bitmap.createBitmap(b, 0, 0, b.width, b.height, matrix, false)
//            b.recycle()
//
//            afterRotate
//        } catch (e: Exception) {
//            e.printStackTrace()
//            b
//        }
//    }
//
//    private fun qualityCompress(
//        srcImg: Bitmap,
//        cacheDir: String,
//        quality: Int
//    ): File {
//
//        /* 设置图片质量输出结果 */
//        val outImg = File(cacheDir, "${System.currentTimeMillis()}.jpg")
//        val fileOutputStream = FileOutputStream(outImg)
//        srcImg.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream)
//        /* 回收图片资源关闭输出流 */
//        fileOutputStream.close()
//        return outImg
//    }
//}
