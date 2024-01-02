package com.example.ocrlanguagetranslatorapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.ocrlanguagetranslatorapp.databinding.ActivityMainBinding
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.lang.Exception
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var binding: ActivityMainBinding
    private lateinit var fromSpinner: Spinner
    private val PICK_IMAGE_REQUEST_CODE = 123
    lateinit var recognizer: TextRecognizer
    val latinScript = Data.LatinScript
    val chineseLanguages = Data.chineseLanguages
    val devanagariLanguages = Data.devanagariLanguages
    val japaneseLanguages = Data.japaneseLanguages
    val koreanLanguages = Data.koreanLanguage
    val languages = Data.languages

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val toSpinner: Spinner = findViewById(R.id.TOspinner)
        fromSpinner = findViewById(R.id.FROMSpinner)
        hideProgressBar()

        textToSpeech = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.getDefault()
            } else {
                Toast.makeText(this@MainActivity, "Text-to-speech initialization failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up spinner adapters and listeners
        val options = arrayOf("AFRIKAANS", "ARABIC", "BELARUSIAN", "BULGARIAN", "BENGALI", "CATALAN", "CZECH", "WELSH", "DANISH", "GERMAN",
            "GREEK", "ENGLISH", "ESPERANTO", "SPANISH", "ESTONIAN", "PERSIAN", "FINNISH", "FRENCH", "IRISH", "GALICIAN",
            "GUJARATI", "HEBREW", "HINDI", "CROATIAN", "HAITIAN", "HUNGARIAN", "INDONESIAN", "ICELANDIC", "ITALIAN",
            "JAPANESE", "GEORGIAN", "KANNADA", "KOREAN", "LITHUANIAN", "LATVIAN", "MACEDONIAN", "MARATHI", "MALAY",
            "MALTESE", "DUTCH", "NORWEGIAN", "POLISH", "PORTUGUESE", "ROMANIAN", "RUSSIAN", "SLOVAK", "SLOVENIAN",
            "ALBANIAN", "SWEDISH", "SWAHILI", "TAMIL", "TELUGU", "THAI", "TAGALOG", "TURKISH", "UKRAINIAN", "URDU",
            "VIETNAMESE", "CHINESE", "others"

        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        fromSpinner.adapter = adapter
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        toSpinner.adapter = adapter2

        var fromLanguage = ""
        var toLanguage = ""

        fromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fromLanguage = options[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        toSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                toLanguage = languages[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        // Handle user interactions
        binding.apply {
            btnScan.setOnClickListener {
                etTranslate.text.clear()
                if ( fromLanguage != "others")
                {
                    setupRecognizer(fromLanguage)
                    openGallery()
                }
                else{
                    Toast.makeText(this@MainActivity,"TO SCAN YOU HAVE TO CHOOSE A PARTICULAR LANGUAGE",Toast.LENGTH_LONG).show()
                }
            }
            btnTranslate.setOnClickListener {
                showProgressBar()
                val textToTranslate = etTranslate.text.toString()
                if (textToTranslate.isNotEmpty()) {
                    if ( fromLanguage == "others")
                    {
                        identifyLanguage(binding.etTranslate.text.toString()) { identifiedLang ->
                            val flag : Boolean = devanagariLanguages.containsKey(identifiedLang)
                            Log.d("SAHIL"," $flag")

                            fromLanguage = findScript(identifiedLang)

                            setupRecognizer(fromLanguage)
                            translateText(fromLanguage, toLanguage, binding.etTranslate.text.toString())
                        }
                    }
                    else{
                        translateText(fromLanguage, toLanguage, textToTranslate)
                    }

                } else {
                    Toast.makeText(this@MainActivity, "Please enter text to translate", Toast.LENGTH_SHORT).show()
                }
            }
            btnRead.setOnClickListener {
                val text = tvResult.text.toString()
                if (text.isEmpty()) {
                    Toast.makeText(this@MainActivity, "No text to read", Toast.LENGTH_SHORT).show()
                } else {
                    textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                }
            }
        }

        // Lifecycle coroutine to handle translation based on language selections
//        lifecycleScope.launch {
//            if (fromLanguage != "others") {
//                setupRecognizer(fromLanguage)
//                Log.d("SAHIL"," $fromLanguage")
//                translateText(fromLanguage, toLanguage, binding.etTranslate.text.toString())
//            } else {
//                val inputText = binding.etTranslate.text.toString()
//                val identifiedLang = identifyLanguage(inputText)
//                fromLanguage = findScript(identifiedLang)
//                Log.d("SAHIL"," $fromLanguage")
//                setupRecognizer(fromLanguage)
//                translateText(identifiedLang, toLanguage, inputText)
//            }
//        }
    }

    private fun speakText(text: String) {
        if (text.isNotBlank()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
        } else {
            Toast.makeText(this, "No text to speak", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE

    }

    private fun setupRecognizer(language: String) {
        recognizer = when (language) {
            "JAPANESE" -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            "HINDI" -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            "KOREAN" -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            "CHINESE" -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            "MARATHI" -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            "NEPALI"-> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            else -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        }
    }

        private fun openGallery() {
        //to take image from gallery
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)

    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if ((data != null) && (data.data != null)) {
                val imageUri: Uri = data.data!!
                binding.image.setImageURI(imageUri)
                performTextRecognitionFromUri(imageUri)
            }
        }
    }
    private fun performTextRecognitionFromUri(imageUri: Uri) {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(this, imageUri)
            processImageForText(image)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
    private fun processImageForText(image: InputImage) {
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                handleTextRecognitionSuccess(visionText)
            }
            .addOnFailureListener { e ->
                handleTextRecognitionFailure(e)
            }
    }
    private fun handleTextRecognitionFailure(e: Exception) {
        e.printStackTrace()
    }

    private fun handleTextRecognitionSuccess(visionText: Text) {
        val resultText = visionText.text
        for (block in visionText.textBlocks) {
            val blockText = block.text
            val blockCornerPoints = block.cornerPoints
            val blockFrame = block.boundingBox
            for (line in block.lines) {
                val lineText = line.text
                val lineCornerPoints = line.cornerPoints
                val lineFrame = line.boundingBox
                for (element in line.elements) {
                    val elementText = element.text
                    val elementCornerPoints = element.cornerPoints
                    val elementFrame = element.boundingBox
                }
            }
        }
        binding.apply {
            etTranslate.setText(resultText)
        }
    }
    // Other utility functions (openGallery, onActivityResult, performTextRecognitionFromUri, etc.)

    private fun translateText(from: String, to: String, text: String) {

        val f = when(from)
        {
            "ENGLISH" -> TranslateLanguage.ENGLISH
            "GERMAN" -> TranslateLanguage.GERMAN
            "HINDI" -> TranslateLanguage.HINDI
            "MARATHI" -> TranslateLanguage.MARATHI
            "AFRIKAANS" -> TranslateLanguage.AFRIKAANS
            "ARABIC" -> TranslateLanguage.ARABIC
            "BELARUSIAN" -> TranslateLanguage.BELARUSIAN
            "BULGARIAN" -> TranslateLanguage.BULGARIAN
            "BENGALI" -> TranslateLanguage.BENGALI
            "CATALAN" -> TranslateLanguage.CATALAN
            "CZECH" -> TranslateLanguage.CZECH
            "WELSH" -> TranslateLanguage.WELSH
            "DANISH" -> TranslateLanguage.DANISH
            "GREEK" -> TranslateLanguage.GREEK
            "ESPERANTO" -> TranslateLanguage.ESPERANTO
            "SPANISH" -> TranslateLanguage.SPANISH
            "ESTONIAN" -> TranslateLanguage.ESTONIAN
            "PERSIAN" -> TranslateLanguage.PERSIAN
            "FINNISH" -> TranslateLanguage.FINNISH
            "FRENCH" -> TranslateLanguage.FRENCH
            "IRISH" -> TranslateLanguage.IRISH
            "GALICIAN" -> TranslateLanguage.GALICIAN
            "GUJARATI" -> TranslateLanguage.GUJARATI
            "HEBREW" -> TranslateLanguage.HEBREW
            "CROATIAN" -> TranslateLanguage.CROATIAN
            "HAITIAN" -> TranslateLanguage.HAITIAN_CREOLE
            "HUNGARIAN" -> TranslateLanguage.HUNGARIAN
            "INDONESIAN" -> TranslateLanguage.INDONESIAN
            "ICELANDIC" -> TranslateLanguage.ICELANDIC
            "ITALIAN" -> TranslateLanguage.ITALIAN
            "JAPANESE" -> TranslateLanguage.JAPANESE
            "GEORGIAN" -> TranslateLanguage.GEORGIAN
            "KANNADA" -> TranslateLanguage.KANNADA
            "KOREAN" -> TranslateLanguage.KOREAN
            "LITHUANIAN" -> TranslateLanguage.LITHUANIAN
            "LATVIAN" -> TranslateLanguage.LATVIAN
            "MACEDONIAN" -> TranslateLanguage.MACEDONIAN
            "MALAY" -> TranslateLanguage.MALAY
            "MALTESE" -> TranslateLanguage.MALTESE
            "DUTCH" -> TranslateLanguage.DUTCH
            "NORWEGIAN" -> TranslateLanguage.NORWEGIAN
            "POLISH" -> TranslateLanguage.POLISH
            "PORTUGUESE" -> TranslateLanguage.PORTUGUESE
            "ROMANIAN" -> TranslateLanguage.ROMANIAN
            "RUSSIAN" -> TranslateLanguage.RUSSIAN
            "SLOVAK" -> TranslateLanguage.SLOVAK
            "SLOVENIAN" -> TranslateLanguage.SLOVENIAN
            "ALBANIAN" -> TranslateLanguage.ALBANIAN
            "SWEDISH" -> TranslateLanguage.SWEDISH
            "SWAHILI" -> TranslateLanguage.SWAHILI
            "TAMIL" -> TranslateLanguage.TAMIL
            "TELUGU" -> TranslateLanguage.TELUGU
            "THAI" -> TranslateLanguage.THAI
            "TAGALOG" -> TranslateLanguage.TAGALOG
            "TURKISH" -> TranslateLanguage.TURKISH
            "UKRAINIAN" -> TranslateLanguage.UKRAINIAN
            "URDU" -> TranslateLanguage.URDU
            "VIETNAMESE" -> TranslateLanguage.VIETNAMESE
            "CHINESE" -> TranslateLanguage.CHINESE
            else -> {
                TranslateLanguage.ENGLISH
            }
        }
        val t = when (to) {
            "ENGLISH" -> TranslateLanguage.ENGLISH
            "GERMAN" -> TranslateLanguage.GERMAN
            "HINDI" -> TranslateLanguage.HINDI
            "MARATHI" -> TranslateLanguage.MARATHI
            "AFRIKAANS" -> TranslateLanguage.AFRIKAANS
            "ARABIC" -> TranslateLanguage.ARABIC
            "BELARUSIAN" -> TranslateLanguage.BELARUSIAN
            "BULGARIAN" -> TranslateLanguage.BULGARIAN
            "BENGALI" -> TranslateLanguage.BENGALI
            "CATALAN" -> TranslateLanguage.CATALAN
            "CZECH" -> TranslateLanguage.CZECH
            "WELSH" -> TranslateLanguage.WELSH
            "DANISH" -> TranslateLanguage.DANISH
            "GREEK" -> TranslateLanguage.GREEK
            "ESPERANTO" -> TranslateLanguage.ESPERANTO
            "SPANISH" -> TranslateLanguage.SPANISH
            "ESTONIAN" -> TranslateLanguage.ESTONIAN
            "PERSIAN" -> TranslateLanguage.PERSIAN
            "FINNISH" -> TranslateLanguage.FINNISH
            "FRENCH" -> TranslateLanguage.FRENCH
            "IRISH" -> TranslateLanguage.IRISH
            "GALICIAN" -> TranslateLanguage.GALICIAN
            "GUJARATI" -> TranslateLanguage.GUJARATI
            "HEBREW" -> TranslateLanguage.HEBREW
            "CROATIAN" -> TranslateLanguage.CROATIAN
            "HAITIAN" -> TranslateLanguage.HAITIAN_CREOLE
            "HUNGARIAN" -> TranslateLanguage.HUNGARIAN
            "INDONESIAN" -> TranslateLanguage.INDONESIAN
            "ICELANDIC" -> TranslateLanguage.ICELANDIC
            "ITALIAN" -> TranslateLanguage.ITALIAN
            "JAPANESE" -> TranslateLanguage.JAPANESE
            "GEORGIAN" -> TranslateLanguage.GEORGIAN
            "KANNADA" -> TranslateLanguage.KANNADA
            "KOREAN" -> TranslateLanguage.KOREAN
            "LITHUANIAN" -> TranslateLanguage.LITHUANIAN
            "LATVIAN" -> TranslateLanguage.LATVIAN
            "MACEDONIAN" -> TranslateLanguage.MACEDONIAN
            "MALAY" -> TranslateLanguage.MALAY
            "MALTESE" -> TranslateLanguage.MALTESE
            "DUTCH" -> TranslateLanguage.DUTCH
            "NORWEGIAN" -> TranslateLanguage.NORWEGIAN
            "POLISH" -> TranslateLanguage.POLISH
            "PORTUGUESE" -> TranslateLanguage.PORTUGUESE
            "ROMANIAN" -> TranslateLanguage.ROMANIAN
            "RUSSIAN" -> TranslateLanguage.RUSSIAN
            "SLOVAK" -> TranslateLanguage.SLOVAK
            "SLOVENIAN" -> TranslateLanguage.SLOVENIAN
            "ALBANIAN" -> TranslateLanguage.ALBANIAN
            "SWEDISH" -> TranslateLanguage.SWEDISH
            "SWAHILI" -> TranslateLanguage.SWAHILI
            "TAMIL" -> TranslateLanguage.TAMIL
            "TELUGU" -> TranslateLanguage.TELUGU
            "THAI" -> TranslateLanguage.THAI
            "TAGALOG" -> TranslateLanguage.TAGALOG
            "TURKISH" -> TranslateLanguage.TURKISH
            "UKRAINIAN" -> TranslateLanguage.UKRAINIAN
            "URDU" -> TranslateLanguage.URDU
            "VIETNAMESE" -> TranslateLanguage.VIETNAMESE
            "CHINESE" -> TranslateLanguage.CHINESE
            else -> TranslateLanguage.ENGLISH // Default language if not found
        }

//        Log.d("SAHIL BHAGAT", "$f,  $t")
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(f)
            .setTargetLanguage(t)
            .build()
        val translator = Translation.getClient(options)
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        hideProgressBar()
                        binding.tvResult.text = translatedText
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Translation failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Model download failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun identifyLanguage(resultText: String, callback: (String) -> Unit) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(resultText)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    Toast.makeText(this, "UNKNOWN LANGUAGE", Toast.LENGTH_SHORT).show()
                    callback("") // Passing empty string when language is unknown
                } else {
                    callback(languageCode) // Pass the identified language code to the callback
                }
            }
            .addOnFailureListener {
                // Handle failure if the language identification process encounters an error
                // For example: Toast.makeText(this, "Identification failed: ${it.message}", Toast.LENGTH_SHORT).show()
                // Notify the callback about the failure condition if needed
                callback("") // Passing empty string in case of failure
            }
    }


    private fun findScript(langCode: String): String {
        val cleanLangCode = langCode.trim().toLowerCase(Locale.ROOT)
        if (devanagariLanguages.containsValue(cleanLangCode)) {
            Log.d("SAHIL", "inside devanagari")
            recognizer = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
            val language = devanagariLanguages.entries.find { it.value == cleanLangCode }?.key
            return language ?: ""
        }
        if ( chineseLanguages.containsKey(cleanLangCode))
        {
            Log.d("SAHIL","inside chinese")
            recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            val language = chineseLanguages.entries.find { it.value == langCode }?.key
            return language ?: ""
        }
        if ( japaneseLanguages.containsKey(cleanLangCode))
        {
            Log.d("SAHIL","inside japanese")
            recognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            val language = japaneseLanguages.entries.find { it.value == langCode }?.key
            return language ?: ""
        }
        if ( koreanLanguages.containsKey(cleanLangCode))
        {
            Log.d("SAHIL","inside korean")
            recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            val language = koreanLanguages.entries.find { it.value == langCode }?.key
            return language ?: ""
        }
        else{
            Log.d("SAHIL","inside latin")
            recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val language = latinScript.entries.find { it.value == langCode }?.key
            return language ?: ""
        }
    }
}
