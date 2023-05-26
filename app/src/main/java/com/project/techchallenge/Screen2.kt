package com.project.techchallenge

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class Screen2 : AppCompatActivity() {

    private lateinit var otpField: EditText
    private lateinit var continueButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen2)

        val phonenumber: TextView = findViewById(R.id.phoneno)
        val code = intent.getStringExtra("code")
        val number = intent.getStringExtra("number")
        val phoneNo = code + number
        phonenumber.text = phoneNo

        otpField = findViewById(R.id.otp_field)
        continueButton = findViewById(R.id.Continuebtn)

        continueButton.setOnClickListener {
            val otp = otpField.text.toString().trim()

            if (otp.isNotEmpty()) {
                val verifyOTPRequest = VerifyOTPRequest(phoneNo, otp)
                makeVerifyOTPRequest(verifyOTPRequest)
            } else {
                Toast.makeText(this@Screen2, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun makeVerifyOTPRequest(request: VerifyOTPRequest) {
        val jsonMediaType = "application/json".toMediaTypeOrNull()
        val requestBody = JSONObject().apply {
            put("number", request.number)
            put("otp", request.otp)
        }.toString().toRequestBody(jsonMediaType)

        val client = OkHttpClient()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(buildVerifyOTPRequest(requestBody)).execute()
                handleVerifyOTPResponse(response)
            } catch (e: IOException) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Screen2, "Failed to make OTP verification request", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun buildVerifyOTPRequest(requestBody: okhttp3.RequestBody): Request {
        return Request.Builder()
            .url("https://app.aisle.co/V1/users/verify_otp")
            .post(requestBody)
            .build()
    }

    private fun handleVerifyOTPResponse(response: okhttp3.Response) {
        val responseBody = response.body?.string()
        if (response.isSuccessful && responseBody != null) {
            val responseObject = JSONObject(responseBody)
            val authToken = responseObject.optString("auth_token")

            if (authToken.isNotEmpty()) {
                navigateToScreen3(authToken)
            } else {
                runOnUiThread {
                    Toast.makeText(this@Screen2, "OTP verification failed", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            runOnUiThread {
                Toast.makeText(this@Screen2, "Failed to make OTP verification request", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToScreen3(authToken: String) {
        runOnUiThread {
            val intent = Intent(this@Screen2, Screen3::class.java)
            intent.putExtra("authToken", authToken)
            startActivity(intent)
        }
    }

    data class VerifyOTPRequest(val number: String, val otp: String)
}
