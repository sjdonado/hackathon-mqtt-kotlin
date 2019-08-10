package com.example.hackathon.fragments

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.example.hackathon.R
import com.example.hackathon.ScanActivity
import com.example.hackathon.helpers.FragmentHandler
import com.example.hackathon.helpers.HttpClient
import com.example.hackathon.helpers.Utils
import com.example.hackathon.lib.MQQTClient
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import kotlinx.android.synthetic.main.fragment_home.*
import org.json.JSONObject
import java.io.*
import java.lang.ref.WeakReference
import java.util.*


class HomeFragment : Fragment() {

    companion object {
        fun getInstance(): HomeFragment = HomeFragment()
        val TAG = "HOME_FRAGMENT"
        val QRcodeWidth = 500
        private val IMAGE_DIRECTORY = "/QRcodeDemonuts"
        private const val REQUEST_CAMERA_PERMISSION = 10
        private val REQUEST_CODE = 1234
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mqqtClient = MQQTClient(activity!!.applicationContext, activity!!)
        val fragmentHandler = FragmentHandler(activity!! as AppCompatActivity, R.id.main_fragment_container)

        logoutButton.setOnClickListener {
            Utils.clearSharedPreferences(activity!!)
            fragmentHandler.add(LoginFragment.getInstance())
        }

        generateQRButton.setOnClickListener {
            HttpClient.getCatFat("https://catfact.ninja/facts?limit=1", activity!!, true) { err, res ->
                activity!!.runOnUiThread {
                    val resJson = JSONObject(res)
                    val data = JSONObject(resJson.getJSONArray("data")[0].toString())
                    try {
                        val bitmap = TextToImageEncode(data.get("fact").toString())
                        qrImageView!!.setImageBitmap(bitmap)
                        val uri = saveImageToExternalStorage(bitmap!!)
                        Toast.makeText(activity!!, "QRCode saved to -> $uri", Toast.LENGTH_SHORT).show()
                    } catch (e: WriterException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        readQRButton.setOnClickListener {
            val intent = Intent(activity!!, ScanActivity::class.java)
            startActivity(intent)
        }

        createThingButton.setOnClickListener {
            HttpClient.createThing("/thing", activity!!, true) { err, res ->
                activity!!.runOnUiThread {
                    if (!err) {
                        Toast.makeText(activity!!, JSONObject(res).get("message").toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        createCredentialsButton.setOnClickListener {
            HttpClient.createThing("/credentials", activity!!, true) { err, res ->
                activity!!.runOnUiThread {
                    if (!err) {
                        Toast.makeText(activity!!, JSONObject(res).get("message").toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        connectButton.setOnClickListener {
            Log.d(TAG, "Connecting...")
            HttpClient.get("/credentials", activity!!, true) { err, res ->
                activity!!.runOnUiThread {
                    if (err) {
                        Toast.makeText(activity!!, JSONObject(res).get("message").toString(), Toast.LENGTH_SHORT).show()
                    } else {
                        val jsonRes = JSONObject(res)
                        Log.d(TAG, jsonRes.toString())

                        val crt = createTempFile()
                        crt.printWriter().use { out ->
                            out.print(jsonRes.get("cert"))
                        }

                        val privateKey = createTempFile()
                        privateKey.printWriter().use { out ->
                            out.print(jsonRes.get("private"))
                        }

                        val caCrtFile = createTempFile()
                        caCrtFile.printWriter().use { out ->
                            out.print("-----BEGIN CERTIFICATE-----\n" +
                                    "MIIDQTCCAimgAwIBAgITBmyfz5m/jAo54vB4ikPmljZbyjANBgkqhkiG9w0BAQsF\n" +
                                    "ADA5MQswCQYDVQQGEwJVUzEPMA0GA1UEChMGQW1hem9uMRkwFwYDVQQDExBBbWF6\n" +
                                    "b24gUm9vdCBDQSAxMB4XDTE1MDUyNjAwMDAwMFoXDTM4MDExNzAwMDAwMFowOTEL\n" +
                                    "MAkGA1UEBhMCVVMxDzANBgNVBAoTBkFtYXpvbjEZMBcGA1UEAxMQQW1hem9uIFJv\n" +
                                    "b3QgQ0EgMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALJ4gHHKeNXj\n" +
                                    "ca9HgFB0fW7Y14h29Jlo91ghYPl0hAEvrAIthtOgQ3pOsqTQNroBvo3bSMgHFzZM\n" +
                                    "9O6II8c+6zf1tRn4SWiw3te5djgdYZ6k/oI2peVKVuRF4fn9tBb6dNqcmzU5L/qw\n" +
                                    "IFAGbHrQgLKm+a/sRxmPUDgH3KKHOVj4utWp+UhnMJbulHheb4mjUcAwhmahRWa6\n" +
                                    "VOujw5H5SNz/0egwLX0tdHA114gk957EWW67c4cX8jJGKLhD+rcdqsq08p8kDi1L\n" +
                                    "93FcXmn/6pUCyziKrlA4b9v7LWIbxcceVOF34GfID5yHI9Y/QCB/IIDEgEw+OyQm\n" +
                                    "jgSubJrIqg0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMC\n" +
                                    "AYYwHQYDVR0OBBYEFIQYzIU07LwMlJQuCFmcx7IQTgoIMA0GCSqGSIb3DQEBCwUA\n" +
                                    "A4IBAQCY8jdaQZChGsV2USggNiMOruYou6r4lK5IpDB/G/wkjUu0yKGX9rbxenDI\n" +
                                    "U5PMCCjjmCXPI6T53iHTfIUJrU6adTrCC2qJeHZERxhlbI1Bjjt/msv0tadQ1wUs\n" +
                                    "N+gDS63pYaACbvXy8MWy7Vu33PqUXHeeE6V/Uq2V8viTO96LXFvKWlJbYK8U90vv\n" +
                                    "o/ufQJVtMVT8QtPHRh8jrdkPSHCa2XV4cdFyQzR1bldZwgJcJmApzyMZFo6IQ6XU\n" +
                                    "5MsI+yMRQ+hDKXJioaldXgjUkK642M4UwtBV8ob2xJNDd2ZhwLnoQdeXeGADbkpy\n" +
                                    "rqXRfboQnoZsG4q5WTP468SQvvG5\n" +
                                    "-----END CERTIFICATE-----\n")
                        }

                        mqqtClient.connect(caCrtFile.path, crt.path, privateKey.path) { topic, message ->
                            Toast.makeText(activity!!, message.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun getTempFile(context: Context, url: String): File? =
        Uri.parse(url)?.lastPathSegment?.let { filename ->
            File.createTempFile(filename, null, context.cacheDir)
        }

    // Method to save an image to external storage
    private fun saveImageToExternalStorage(bitmap: Bitmap): Uri {
        val writeExternalStoragePermission = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (writeExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
        }
        // Get the external storage directory path
        val path = Environment.getExternalStorageDirectory().toString()

        // Create a file to save the image
        val file = File(path, "${UUID.randomUUID()}.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress the bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // Flush the output stream
            stream.flush()

            // Close the output stream
            stream.close()
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }

        // Return the saved image path to uri
        return Uri.parse(file.absolutePath)
    }

    @Throws(WriterException::class)
    private fun TextToImageEncode(Value: String): Bitmap? {
        val bitMatrix: BitMatrix
        try {
            bitMatrix = MultiFormatWriter().encode(
                Value,
                BarcodeFormat.QR_CODE,
                QRcodeWidth, QRcodeWidth, null
            )

        } catch (Illegalargumentexception: IllegalArgumentException) {

            return null
        }

        val bitMatrixWidth = bitMatrix.getWidth()

        val bitMatrixHeight = bitMatrix.getHeight()

        val pixels = IntArray(bitMatrixWidth * bitMatrixHeight)

        for (y in 0 until bitMatrixHeight) {
            val offset = y * bitMatrixWidth

            for (x in 0 until bitMatrixWidth) {

                pixels[offset + x] = if (bitMatrix.get(x, y))
                    resources.getColor(R.color.black)
                else
                    resources.getColor(R.color.white)
            }
        }
        val bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444)

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight)
        return bitmap
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
//            val permissionResult = grantResults.size
//            if (permissionResult > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(applicationContext, "Please grant write permission.", Toast.LENGTH_LONG).show()
//            } else {
//                Toast.makeText(applicationContext, "You denied write permission.", Toast.LENGTH_LONG).show()
//            }
        }
    }


}
