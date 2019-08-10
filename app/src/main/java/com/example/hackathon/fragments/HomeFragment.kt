package com.example.hackathon.fragments


import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity

import com.example.hackathon.R
import com.example.hackathon.ScanActivity
import com.example.hackathon.helpers.FragmentHandler
import com.example.hackathon.helpers.HttpClient
import com.example.hackathon.helpers.Utils
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
                        saveImageToInternalStorage(bitmap)
//                Toast.makeText(activity!!, "QRCode saved to -> $path", Toast.LENGTH_SHORT).show()
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
    }

    fun saveImage(myBitmap: Bitmap?) {
//        myBitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, out)
//        MediaStore.Images.Media.insertImage(
//            activity!!.contentResolver,
//            myBitmap!!,
//            Calendar.getInstance().timeInMillis.toString() + ".jpg" ,
//            "Cat fat"
//        )
//        File sdCard = Environment!!.getExternalStorageDirectory();
//        @SuppressLint("DefaultLocale") String fileName = String.format("%d.jpg", System.currentTimeMillis());
//        File dir = new File(sdCard.getAbsolutePath() + "/savedImageName");
//        dir.mkdirs();
//        final File myImageFile = new File(dir, fileName); // Create image file
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(myImageFile);
//            Bitmap bitmap = Picasso.get().load(MyUrl).get();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//
//            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            intent.setData(Uri.fromFile(myImageFile));
//            context.sendBroadcast(intent);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                fos.close();
//            } catch (IOException e) {
//                e.printStackTrace()
//            }
//        }
    }

    // Method to save an image to internal storage
    private fun saveImageToInternalStorage(myBitmap: Bitmap? ): Uri {
        val wrapper = ContextWrapper(activity!!.applicationContext)
        var file = wrapper.getDir("images", Context.MODE_PRIVATE)

        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)

            // Compress bitmap
            myBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)

            // Flush the stream
            stream.flush()

            // Close stream
            stream.close()
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }

        // Return the saved image uri
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
}
