package kr.co.jness.momi.eclean.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader

data class FilterWord(
    val strWord: String,
    val nScore: Int
)
