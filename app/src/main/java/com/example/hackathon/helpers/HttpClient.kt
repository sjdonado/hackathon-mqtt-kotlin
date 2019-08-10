package com.example.hackathon.helpers

import com.example.hackathon.R
import android.app.Activity
import android.app.Dialog
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import android.widget.Toast
import okhttp3.RequestBody


open class HttpClient {

    companion object {
        private const val TAG = "HttpClient"
        private const val PROD_URl = "http://3.224.227.74:3660"
        private const val API_URL = "$PROD_URl/api/v1"
        private val JSON = MediaType.parse("application/json; charset=utf-8")

        fun login(path : String, activity : Activity, json : String, loading: Boolean, callback : (err: Boolean, response: String) -> Unit) {
            val jsonBody = RequestBody.create(JSON, json)
            val request = Request.Builder()
                .url(API_URL + path)
                .post(jsonBody)
                .build()
            executeRequest(request, activity, callback, loading)
        }

        fun signUp(path : String, activity : Activity, json : String, loading: Boolean, callback : (err: Boolean, response: String) -> Unit) {
            val jsonBody = RequestBody.create(JSON, json)
            val request = Request.Builder()
                .url(API_URL + path)
                .post(jsonBody)
                .build()
            executeRequest(request, activity, callback, loading)
        }

        fun getCatFat(path : String, activity : Activity, loading: Boolean, callback : (err: Boolean, response : String) -> Unit) {
            val request = Request.Builder()
                .url(path)
                .build()
            executeRequest(request, activity, callback, loading)
        }

        fun get(path : String, activity : Activity, loading: Boolean, callback : (err: Boolean, response : String) -> Unit) {
            val request = Request.Builder()
                    .header("Authorization", Utils.getSharedPreferencesStringValue(activity, "USER_TOKEN")!!)
                    .url(API_URL + path)
                    .build()
            executeRequest(request, activity, callback, loading)
        }

        fun post(path : String, activity : Activity, json : String, loading: Boolean, callback : (err: Boolean, response: String) -> Unit) {
            val jsonBody = RequestBody.create(JSON, json)
            val request = Request.Builder()
                    .header("Authorization", Utils.getSharedPreferencesStringValue(activity, "USER_TOKEN")!!)
                    .url(API_URL + path)
                    .post(jsonBody)
                    .build()
            executeRequest(request, activity, callback, loading)
        }

        fun put(path : String, activity : Activity, json : String, loading: Boolean, callback : (err: Boolean, response : String) -> Unit) {
            val jsonBody = RequestBody.create(JSON, json)
            val request = Request.Builder()
                    .header("Authorization", Utils.getSharedPreferencesStringValue(activity, "USER_TOKEN")!!)
                    .url(API_URL + path)
                    .put(jsonBody)
                    .build()
            executeRequest(request, activity, callback, loading)
        }

        fun delete(path : String, activity : Activity, json : String, loading: Boolean, callback : (err: Boolean, response: String) -> Unit) {
            val jsonBody = RequestBody.create(JSON, json)
            val request = Request.Builder()
                    .header("Authorization", Utils.getSharedPreferencesStringValue(activity, "USER_TOKEN")!!)
                    .url(API_URL + path)
                    .delete(jsonBody)
                    .build()
            executeRequest(request, activity, callback, loading)
        }


        fun upload(path : String, activity : Activity, multipartBody : MultipartBody, loading: Boolean, callback : (err: Boolean, response : String) -> Unit) {
            val request = Request.Builder()
                    .url(API_URL + path)
                    .header("Authorization", Utils.getSharedPreferencesStringValue(activity, "USER_TOKEN")!!)
                    .post(multipartBody)
                    .build()
            executeRequest(request, activity, callback, loading)
        }

        private fun executeRequest(request: Request, activity: Activity, callback : (err: Boolean, response : String) -> Unit, loading: Boolean) {
            var progressDialog = Dialog(activity)
//             has leaked window DecorView@d3d464c[] that was originally added here -> SERVER DOESN'T SEND DATA OBJECT
            if (loading) {
                try {
                    progressDialog = Utils.showLoading(activity)
                } catch (e: Exception) {
                    Log.e(TAG, e.toString())
                    callback(true, e.message.toString())
                }
            }

            OkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    Log.i(TAG, "${e?.message}")
                    callback(true, e?.message.toString())
                    activity.runOnUiThread {
                        Toast.makeText(activity.applicationContext, e?.message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
                override fun onResponse(call : Call?, response : Response?) {
                    if (loading) {
                        activity.runOnUiThread {
                            progressDialog.hide()
                        }
                    }
                    if(response!!.code() == 200 || response.code() == 201) {
                        callback(false, response.body()!!.string())
                    }else{
                        Log.i(TAG, response.toString())
                        Log.i(TAG, "${response.code()}")
                        callback(true, response.body()!!.string())
//                        activity.runOnUiThread {
//                            Toast.makeText(activity.applicationContext, activity.getString(R.string.internet_server_error), Toast.LENGTH_LONG).show()
//                        }
                    }
                }
            })
        }
    }
}