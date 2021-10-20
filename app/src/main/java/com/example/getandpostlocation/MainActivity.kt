package com.example.getandpostlocation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.*
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var nameEditText: EditText
    lateinit var locationEditText: EditText
    lateinit var nameGetEditText: EditText
    lateinit var namesTextView: TextView
    lateinit var addButton: Button
    lateinit var getLocationButton: Button
    lateinit var getButton: Button
    lateinit var getLocationLinearLayout: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nameEditText = findViewById(R.id.edt_name)
        locationEditText = findViewById(R.id.edt_location)
        nameGetEditText = findViewById(R.id.edt_getNameLocation)
        namesTextView = findViewById(R.id.tv_names)
        namesTextView.movementMethod = ScrollingMovementMethod()
        addButton = findViewById(R.id.btn_addEntry)
        getLocationButton = findViewById(R.id.btn_getLocation)
        getButton = findViewById(R.id.btn_get)
        getLocationLinearLayout = findViewById(R.id.ll_getLocation)

        addButton.setOnClickListener {
            if (nameEditText.text.trim().isNotEmpty()) {
                if (locationEditText.text.trim().isNotEmpty()) {
                    addName(nameEditText.text.toString(), locationEditText.text.toString())
                } else {
                    Toast.makeText(this, "please enter a location", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        getLocationLinearLayout.isVisible = false
        getLocationButton.setOnClickListener {
            getLocationLinearLayout.isVisible = !getLocationLinearLayout.isVisible
        }

        getButton.setOnClickListener {
            if (nameGetEditText.text.isNotEmpty()) {
                getLocation(nameGetEditText.text.toString())
            } else {
                Toast.makeText(this, "please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

    }



    private fun addName(name: String, location: String) {
        CoroutineScope(Dispatchers.IO).launch {

            val jsonObject = JSONObject()
            try {
                jsonObject.put("name", name)
                jsonObject.put("location", location)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            val client = OkHttpClient()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonObject.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("https://dojo-recipes.herokuapp.com/test/")
                .post(requestBody)
                .build()

            var response: Response? = null
            try {
                response = client.newCall(request).execute()
                if (response.code == 201) {
                    withContext(Dispatchers.Main) {
                        nameEditText.text.clear()
                        locationEditText.text.clear()
                        Toast.makeText(this@MainActivity, "added successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "something went wrong", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

    }

    private fun getLocation(name :String) {
        namesTextView.text = ""
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val okHttpClient = OkHttpClient()
                val request = Request.Builder()
                    .url("https://dojo-recipes.herokuapp.com/test/")
                    .build()
                val response = okHttpClient.newCall(request).execute()

                if (response != null) {
                    if (response.code == 200) {
                        val jsonArray = JSONArray(response.body!!.string())
                        Log.d("HELP", jsonArray.toString())
                        for (index in 0 until jsonArray.length()) {
                            val nameObj = jsonArray.getJSONObject(index)
                            val userName = nameObj.getString("name")
                            val userLocation = nameObj.getString("location")
                            withContext(Main){
                                if(name.lowercase() == userName.lowercase()){
                                    namesTextView.text = "${namesTextView.text}${userName}: ${userLocation}\n"
                                }
                            }
                        }
                        withContext(Main){
                            if(namesTextView.text.isEmpty()){
                                namesTextView.text = "name can not be found!"
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("TextViewActivity", e.message.toString())
            }
        }
    }
}

