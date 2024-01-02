package com.example.ocrlanguagetranslatorapp

class Data {
    companion object{
        val LatinScript= mapOf(
            "af" to "Afrikaans",
            "sq" to "Albanian",
            "ca" to "Catalan",
            "zh" to "Chinese",
            "hr" to "Croatian",
            "cs" to "Czech",
            "da" to "Danish",
            "nl" to "Dutch",
            "en" to "English",
            "et" to "Estonian",
            "fil" to "Filipino",
            "fi" to "Finnish",
            "fr" to "French",
            "de" to "German",
            "hu" to "Hungarian",
            "is" to "Icelandic",
            "id" to "Indonesian",
            "it" to "Italian",
            "ja" to "Japanese",
            "ko" to "Korean",
            "lv" to "Latvian",
            "lt" to "Lithuanian",
            "ms" to "Malay",
            "mr" to "Marathi",
            "ne" to "Nepali",
            "no" to "Norwegian",
            "pl" to "Polish",
            "pt" to "Portuguese",
            "ro" to "Romanian",
            "sr-Latn" to "Serbian",
            "sk" to "Slovak",
            "sl" to "Slovenian",
            "es" to "Spanish",
            "sv" to "Swedish",
            "tr" to "Turkish",
            "vi" to "Vietnamese"
        )
        val chineseLanguages = mapOf(
            "Chinese" to "zh" // Hans/Hant; supported in v2
        )
        val devanagariLanguages = mapOf(
            "Hindi" to "hi",
            "Marathi" to "mr",
            "Nepali" to "ne"
        )
        val japaneseLanguages = mapOf(
            "Japanese" to "ja"
        )
        val languagesUpperCase = arrayOf(
            "Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Danish", "German",
            "Greek", "English", "Esperanto", "Spanish", "Estonian", "Persian", "Finnish", "French", "Irish", "Galician",
            "Gujarati", "Hebrew", "Hindi", "Croatian", "Haitian", "Hungarian", "Indonesian", "Icelandic", "Italian",
            "Japanese", "Georgian", "Kannada", "Korean", "Lithuanian", "Latvian", "Macedonian", "Marathi", "Malay",
            "Maltese", "Dutch", "Norwegian", "Polish", "Portuguese", "Romanian", "Russian", "Slovak", "Slovenian",
            "Albanian", "Swedish", "Swahili", "Tamil", "Telugu", "Thai", "Tagalog", "Turkish", "Ukrainian", "Urdu",
            "Vietnamese", "Chinese"
        )
        val options = arrayOf("Afrikaans", "Arabic", "Belarusian", "Bulgarian", "Bengali", "Catalan", "Czech", "Welsh", "Danish", "German",
            "Greek", "English", "Esperanto", "Spanish", "Estonian", "Persian", "Finnish", "French", "Irish", "Galician",
            "Gujarati", "Hebrew", "Hindi", "Croatian", "Haitian", "Hungarian", "Indonesian", "Icelandic", "Italian",
            "Japanese", "Georgian", "Kannada", "Korean", "Lithuanian", "Latvian", "Macedonian", "Marathi", "Malay",
            "Maltese", "Dutch", "Norwegian", "Polish", "Portuguese", "Romanian", "Russian", "Slovak", "Slovenian",
            "Albanian", "Swedish", "Swahili", "Tamil", "Telugu", "Thai", "Tagalog", "Turkish", "Ukrainian", "Urdu",
            "Vietnamese", "Chinese","others")
        val options1 = languagesUpperCase.map { it.uppercase() }.toTypedArray()

        val languages = languagesUpperCase.map { it.uppercase() }.toTypedArray()


        val koreanLanguage = mapOf(
            "Korean" to "ko"
        )


    }


}