package dong.duan.project_company.language

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import dong.duan.project_company.base.GenericAdapter
import dong.duan.project_company.databinding.ActivityMainBinding
import dong.duan.project_company.databinding.ItemTextCheckBinding
import okhttp3.*
import java.io.IOException
import java.net.URLEncoder

class MainActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    lateinit var text: String

    data class Text(var text_first: String, var country: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        var arr = ArrayList<Text>()
        arr.add(Text("Xin chào mọi người", "Việt"))
        arr.add(Text("Hi everybody", "Anh"))
        arr.add(Text("大家好", "Trung"))
        arr.add(Text("सबको नमस्ते", "Ấn độ"))
        arr.add(Text("Hola a todos", "Tây ban nha"))
        arr.add(Text("salut tout le monde", "Pháp"))
        arr.add(Text("Привет всем", "Nga"))
        arr.add(Text("Oi pessoal", "Bồ đào nha"))
        arr.add(Text("সবাই কেমন আছেন", "Bengal"))
        arr.add(Text("Hallo allerseits", "German"))
        arr.add(Text("みなさん、こんにちは", "Nhật"))
        arr.add(Text("नमस्कार मंडळी", "Marathi"))
        arr.add(Text("అందరికీ హాయ్", "Telugu"))
        arr.add(Text("Selam millet", "Turkish"))
        arr.add(Text("எல்லோருக்கும் வணக்கம்", "Tamil"))
        arr.add(Text("안녕하세요 여러분", "Korean"))
        arr.add(Text("Ciao a tutti", "Italian"))
        arr.add(Text("สวัสดีทุกคน", "Thailand"))

        binding.txtResult.adapter =
            GenericAdapter(arr, ItemTextCheckBinding::inflate) { itemTextCheckBinding, text, i ->
                itemTextCheckBinding.textFirst.text = text.text_first
                itemTextCheckBinding.textContry.text = text.country
                identifyAndTranslate(text.text_first) {
                    itemTextCheckBinding.textSecond.text = it
                }
                itemTextCheckBinding.root.setOnClickListener {
                    translateValue(text.text_first)
                }

            }
    }

    private fun translateValue(textFirst: String) {

    }

    fun identifyAndTranslate(text: String, callback: (String?) -> Unit) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->

                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(languageCode)
                    .setTargetLanguage(TranslateLanguage.ENGLISH)
                    .build()
                val translation = Translation.getClient(options)
                val conditions = DownloadConditions.Builder()
                    .requireWifi()
                    .build()
                translation.downloadModelIfNeeded(conditions)
                    .addOnSuccessListener {
                        translation.translate(text).addOnSuccessListener {
                            callback(it)
                            translation.close()
                        }
                            .addOnFailureListener {
                                callback(it.message)
                                translation.close()
                            }
                    }
                    .addOnFailureListener { exception ->
                       showToast("Can't not download language")
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