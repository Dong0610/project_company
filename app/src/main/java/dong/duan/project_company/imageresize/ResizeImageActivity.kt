package dong.duan.project_company.imageresize

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.renderscript.ScriptIntrinsicConvolve3x3
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import dong.duan.project_company.databinding.ActivityResizeImageBinding
import java.io.FileNotFoundException

interface changeScale {
    fun setSize(w: Int, h: Int)

}

@Suppress("DEPRECATION")
class ResizeImageActivity : AppCompatActivity(), changeScale {
    private val binding by lazy {
        ActivityResizeImageBinding.inflate(layoutInflater)
    }

    public lateinit var list_ev: changeScale

    lateinit var bitmap: Bitmap
    lateinit var uriImage: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        list_ev = this
        binding.chooseImg.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            this.startActivityForResult(intent, 100)
        }

        binding.addWtms.setOnClickListener {
            binding.imgView.setImageBitmap(
                (if (uriImage == null) null else processImage(
                    uriImage, applicationContext
                )) as Bitmap?
            )
        }
    }


    var sizeImage = 0

    @SuppressLint("Recycle")
    fun sizeImage(choosen: Uri, context: Context): Int {
        val inputStream = context.contentResolver.openInputStream(choosen)
        val fileSizeInBytes = inputStream?.available()?.toLong() ?: 0L
        val sizeInKB = fileSizeInBytes / 1024
        sizeImage = sizeInKB.toInt()
        return sizeInKB.toInt()
    }



    fun processImage(inputPath: Uri, context: Context): Bitmap {
        val inputStream = inputPath.let { context.contentResolver.openInputStream(it) }
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        val fileSizeInKB = sizeImage(inputPath, context)
        if (fileSizeInKB < 512) {
            return scaleImage(originalBitmap)
        } else {
            return thumnailImage(originalBitmap)!!
        }

    }
    fun scaleImage(bitmap: Bitmap): Bitmap {
        val width = bitmap.width * 2
        val height = bitmap.height * 2
        setSize(width, height)
        var targetW = 0f
        var targetH = 0f
        if (width < height) {
            targetH = 768f
            targetW = (768f / height) * width
        } else if (width == height) {
            targetH = 768f
            targetW = 768f
        } else {
            targetH = (768f / width) * height
            targetW = 768f
        }
        setSize(targetW.toInt(), targetH.toInt())
        val matrix = Matrix()
        matrix.postScale(
            targetW.toFloat() / bitmap.width,
            targetH.toFloat() / bitmap.height
        )
        val scaledBitmap = Bitmap.createBitmap(targetW.toInt(), targetH.toInt(), bitmap.config)
        val canvas = Canvas(scaledBitmap)
        canvas.drawBitmap(bitmap, matrix, null)
        setSize(scaledBitmap.width, scaledBitmap.height)
        return scaledBitmap
    }

    private fun thumnailImage(bitmap: Bitmap):Bitmap? {
        val width=bitmap.width
        val height= bitmap.height
        setSize(width,height)
        val targetW: Float
        val targetH: Float

        if(width<height){
            targetH=768f
            targetW= 768f*(width.toFloat()/height)
        }
        else if(width==height){
            targetW=768f
            targetH=768f
        }
        else{
            targetW=768f
            targetH=768f*(height.toFloat()/width.toFloat())
        }
        val matrix = Matrix()
        matrix.postScale(
            targetW / bitmap.width,
            targetH / bitmap.height
        )
        val scaledBitmap = Bitmap.createBitmap(targetW.toInt(), targetH.toInt(), bitmap.config)
        val canvas = Canvas(scaledBitmap)
        canvas.drawBitmap(bitmap, matrix, null)
        setSize(scaledBitmap.width, scaledBitmap.height)
        return scaledBitmap
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 100) {
            try {
                uriImage = data?.data!!
                binding.txtInfo.text =
                    sizeImage(data.data!!, applicationContext).toString()
                binding.imgView.setImageURI(uriImage)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    override fun setSize(w: Int, h: Int) {
        var text = binding.txtInfo.text.toString()
        text += "\n$w -- $h\n"
        binding.txtInfo.text = text
    }
}