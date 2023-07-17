package dong.duan.project_company

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import dong.duan.project_company.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
  lateinit  var text:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnTranslate.setOnClickListener {
            var txt_translate= binding.editText.text.toString()
            identifyAndTranslate(txt_translate){
                binding.txtResult.text=it
            }
        }



        binding.editText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                text=""
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                text=p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
                identifyAndTranslate(p0.toString()){
                    binding.txtResult.text=it
                }
            }

        })
    }

    fun identifyAndTranslate(text: String, callback: (String) -> Unit) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(languageCode)
                    .setTargetLanguage(TranslateLanguage.ENGLISH)
                    .build()
                val englishGermanTranslator = Translation.getClient(options)
                englishGermanTranslator.downloadModelIfNeeded()
                    .addOnSuccessListener {
                        englishGermanTranslator.translate(text)
                            .addOnSuccessListener { translatedText ->
                                callback(translatedText)
                            }
                            .addOnFailureListener { exception ->
                            }
                            .addOnCompleteListener {
                                englishGermanTranslator.close()
                            }
                    }
                    .addOnFailureListener { exception ->
                        englishGermanTranslator.close()
                    }
            }
            .addOnFailureListener {
                showToast(it.message.toString())
            }
    }

    fun showToast(mess:Any){
        Toast.makeText(applicationContext,mess.toString(),Toast.LENGTH_LONG).show()
    }
}