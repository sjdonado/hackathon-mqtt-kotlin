package com.example.hackathon.helpers

import android.app.Activity
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.view.View
import android.widget.ProgressBar
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

object Utils {
    private const val PREFERENCES_NAME = "HACKATHON_DATA"
    const val USER_TOKEN = "USER_TOKEN"
    const val USER_OBJECT = "USER_OBJECT"

    private fun mergeTwoJSONObject(j1: JSONObject, j2: JSONObject) {
        val keys = j1.keys()
        var obj1: Any
        var obj2: Any
        while (keys.hasNext()) {
            val next = keys.next()
            if (j1.isNull(next)) continue
            obj1 = j1.get(next)
            if (!j2.has(next)) j2.putOpt(next, obj1)
            obj2 = j2.get(next)
            if (obj1 is JSONObject && obj2 is JSONObject) {
                mergeTwoJSONObject(obj1, obj2)
            }
        }
    }

    private fun merge(objects: List<JSONObject>): JSONObject {
        var i = 0
        var j = 1
        while (i < objects.size - 1) {
            mergeTwoJSONObject(objects[i], objects[j])
            i++
            j++
        }
        return objects[objects.size - 1]
    }

    fun getSharedPreferencesStringValue(activity: Activity, keyName: String) : String? {
        val sp = activity.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        return sp.getString(keyName, null)
    }

    fun setSharedPreferencesStringValue(activity: Activity, keyName : String, data: String) {
        val sp = activity.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(keyName, data)
        editor.apply()
    }

    fun updateSharedPreferencesObjectValue(activity: Activity, keyName: String, jsonObject: JSONObject) {
        val sp = activity.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        val oldJsonObjectValue = sp.getString(keyName, null)

        if(oldJsonObjectValue != null) {
            val newJsonObject = merge(arrayListOf(JSONObject(oldJsonObjectValue), jsonObject))
            setSharedPreferencesStringValue(activity, keyName, newJsonObject.toString())
        } else {
            setSharedPreferencesStringValue(activity, keyName, jsonObject.toString())
        }
    }

    fun getCircularProgressDrawable(activity: Activity): CircularProgressDrawable {
        val circularProgressDrawable = CircularProgressDrawable(activity)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    fun clearSharedPreferences(activity: Activity){
        val sp = activity.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
        sp.edit().clear().apply()
    }

    fun showLoading(activity: Activity): Dialog {
        val progressDialog = Dialog(activity)
        val dialog = ProgressBar(activity)
        dialog.isIndeterminate = true
        dialog.visibility = View.VISIBLE
        dialog.isClickable = false
        progressDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        progressDialog.setContentView(dialog)
        progressDialog.show()
        return progressDialog
    }

    fun showSnackbar(rootLayout : View, text : String ) {
        Snackbar.make(
                rootLayout,
                text,
                Snackbar.LENGTH_SHORT
        ).show()
    }
}