package dong.duan.project_company

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import dong.duan.project_company.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import okhttp3.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    lateinit var text: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnTranslate.setOnClickListener {
            var txt_translate = binding.editText.text.toString()
            if(txt_translate==""){
                binding.txtResult.text = ""
            }
            else{
            identifyAndTranslate(txt_translate) { result ->
                binding.txtResult.text = result
            }}

        }
    }


    fun identifyAndTranslate(text: String, callback: (String?) -> Unit) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                translateText(text, languageCode) {
                    callback(it)
                }
            }
            .addOnFailureListener { languageException ->
                callback(null)
            }
    }

    fun translateText(sourceText: String, code: String, callback: (String) -> Unit) {

        val targetLanguage = "en"
        val apiKey = "ea91b4c242msh3354b2440ddf4b9p13b289jsn534bceec3cd6"
        val encodedSourceText = URLEncoder.encode(sourceText, "UTF-8")
        val url =
            "https://api.mymemory.translated.net/get?q=$encodedSourceText&langpair=$code|$targetLanguage&X-RapidAPI-Key=$apiKey"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val translatedText = responseBody?.let {
                    it.substringAfter("\"translatedText\":\"").substringBefore("\"")
                }
                callback(translatedText ?: "Can't translate")
            }

            override fun onFailure(call: Call, e: IOException) {
                callback(e.message.toString())
            }
        })
    }

    fun showToast(mess: Any) {
        Toast.makeText(applicationContext, mess.toString(), Toast.LENGTH_LONG).show()
    }
}