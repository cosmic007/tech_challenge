package com.project.techchallenge

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var progressbar: ProgressBar
    private lateinit var countrycode: EditText
    private lateinit var phoneno: EditText

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressbar = findViewById(R.id.progressbar)
        countrycode = findViewById(R.id.countrycode)
        phoneno = findViewById(R.id.phno)

        findViewById<View>(R.id.continuebtn).setOnClickListener {
            val countryCode = countrycode.text.toString().trim()
            val phoneNumber = phoneno.text.toString().trim()
            val fullPhoneNumber = countryCode + phoneNumber

            if (phoneNumber.isNotEmpty()) {
                progressbar.visibility = View.VISIBLE
                makePhoneNumberLoginRequest(fullPhoneNumber)
            } else {
                Toast.makeText(this@MainActivity, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
                progressbar.visibility = View.GONE
            }
        }
    }

    private fun makePhoneNumberLoginRequest(fullPhoneNumber: String) {
        val jsonMediaType = "application/json".toMediaTypeOrNull()
        val requestBody = JSONObject().apply {
            put("number", fullPhoneNumber)
        }.toString().toRequestBody(jsonMediaType)

        val client = OkHttpClient()
        val httpRequest = Request.Builder()
            .url("https://app.aisle.co/V1/users/phone_number_login")
            .post(requestBody)
            .build()

        val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
            // Handle exceptions here
            exception.printStackTrace()
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                progressbar.visibility = View.GONE
            }
        }

        coroutineScope.launch(coroutineExceptionHandler) {
            try {
                val response = withContext(Dispatchers.IO) {
                    client.newCall(httpRequest).execute()
                }
                handlePhoneNumberLoginResponse(response)
            } catch (e: IOException) {
                e.printStackTrace()
                // Handle IO exception
                Toast.makeText(this@MainActivity, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                progressbar.visibility = View.GONE
            }
        }
    }

    private fun handlePhoneNumberLoginResponse(response: okhttp3.Response) {
        progressbar.visibility = View.GONE
        if (response.isSuccessful) {
            Toast.makeText(this@MainActivity, "Success", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, Screen2::class.java).apply {
                putExtra("code", countrycode.text.toString().trim())
                putExtra("number", phoneno.text.toString().trim())
            }
            startActivity(intent)
        } else {
            Toast.makeText(this@MainActivity, "Failed", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        // Cancel all coroutines when the activity is destroyed
        coroutineScope.cancel()
    }
}
