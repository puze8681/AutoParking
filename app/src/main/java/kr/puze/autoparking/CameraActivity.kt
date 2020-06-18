@file:Suppress("DEPRECATION")

package kr.puze.autoparking

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.googlecode.tesseract.android.TessBaseAPI
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CameraActivity : AppCompatActivity() {

    private var tessBaseAPI: TessBaseAPI = TessBaseAPI()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        button.setOnClickListener {
            capture()
        }
        val dir = "$filesDir/tesseract"
        if (checkLanguageFile("$dir/tessdata"))
            tessBaseAPI.init(dir, "kor")
    }

    private fun checkLanguageFile(dir: String): Boolean {
        val file = File(dir)
        if (!file.exists() && file.mkdirs()) createFiles(dir) else if (file.exists()) {
            val filePath = "$dir/kor.traineddata"
            val langDataFile = File(filePath)
            if (!langDataFile.exists()) createFiles(dir)
        }
        return true
    }

    private fun createFiles(dir: String) {
        val assetMgr: AssetManager = this.assets
        try {
            val inputStream = assetMgr.open("kor.traineddata")
            val destFile = "$dir/kor.traineddata"
            val outputStream = FileOutputStream(destFile)
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            inputStream.close()
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun capture() {
        Log.d("LOGTAG", "capture")
        surfaceView!!.capture(Camera.PictureCallback { bytes, camera ->
            Log.d("LOGTAG", "PictureCallback")
            val options: BitmapFactory.Options = BitmapFactory.Options()
            options.inSampleSize = 8
            var bitmap: Bitmap? = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            bitmap = getRotatedBitmap(bitmap, 90)
            imageView!!.setImageBitmap(bitmap)
            button.isEnabled = false
            button.text = "텍스트 인식중..."
            AsyncTess(tessBaseAPI, this@CameraActivity, textView, button).execute(bitmap)
            Log.d("LOGTAG", "startPreview")
            camera.startPreview()
        })
    }

    @Synchronized
    fun getRotatedBitmap(bitmap: Bitmap?, degrees: Int): Bitmap? {
        var mBitmap: Bitmap? = bitmap
        if (degrees != 0 && mBitmap != null) {
            val m = Matrix()
            m.setRotate(
                degrees.toFloat(),
                mBitmap.width.toFloat() / 2,
                mBitmap.height.toFloat() / 2
            )
            try {
                val b2: Bitmap = Bitmap.createBitmap(
                    mBitmap,
                    0,
                    0,
                    mBitmap.width,
                    mBitmap.height,
                    m,
                    true
                )
                if (mBitmap !== b2) {
                    mBitmap = b2
                }
            } catch (ex: OutOfMemoryError) {
                ex.printStackTrace()
            }
        }
        return mBitmap
    }

    class AsyncTess(tessBaseAPI: TessBaseAPI, context: Context, textView: TextView, button: Button) : AsyncTask<Bitmap?, Int?, String?>() {
        private var mTessBaseAPI = tessBaseAPI
        private var mContext = context
        private var mTextView = textView
        private var mButton = button
        override fun doInBackground(vararg mRelativeParams: Bitmap?): String {
            mTessBaseAPI.setImage(mRelativeParams[0])
            return mTessBaseAPI.utF8Text
        }

        override fun onPostExecute(result: String?) {
            mTextView.text = result
            Toast.makeText(mContext, "" + result, Toast.LENGTH_LONG).show()
            mButton.isEnabled = true
            mButton.text = "텍스트 인식"
        }
    }
}
