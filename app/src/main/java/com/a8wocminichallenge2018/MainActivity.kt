package com.a8wocminichallenge2018

import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null

    companion object {
        private const val TAG = "MAIN_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_parse.setOnClickListener {
            Log.i(TAG, "Test")
            //Nepali
            //  GetUsfm(usfm_to_txt).execute("https://cdn.door43.org/ne/ulb/v5.2/mat.usfm")

            //Serbian
            GetUsfm(usfm_to_txt).execute("https://cdn.door43.org/sr-Latn/stf/v1/mat.usfm")
        }
    }

    private inner class GetUsfm(etv: EditText) : AsyncTask<String, Unit, String>() {

        val tv: EditText? = etv

        override fun onPreExecute() {
            super.onPreExecute()
            // Showing progress dialog
            progressDialog = ProgressDialog(tv?.context)
            progressDialog?.let {
                with(it) {
                    setMessage("Please Wait...")
                    setCancelable(false)
                    show()
                }
            }


        }

        override fun doInBackground(vararg params: String): String {

            try {
                val url = URL(params[0])
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                // read the response
                val bufferStream = BufferedInputStream(conn.inputStream)
                return convertStreamToString(bufferStream)
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }
            return "Data Not Found."
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            tv?.setText(result)
            if (progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
        }

        private fun convertStreamToString(inputStream: BufferedInputStream): String {
            var count = 0
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line = ""
            try {
                while (reader.readLine() != null) {
                    count += 1
                    if (count < 3) {
                        continue
                    }
                    sb.append(stripOf(reader.readLine()))
                    sb.append("\n")
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            return sb.toString()
        }

        private fun stripOf(string: String): String {
            val sb = StringBuilder()
            val splitedStringList = string.split(" ")
            for (s in splitedStringList) {
                if (!s.contains("\\")) {
                    sb.append(s)
                    sb.append(" ")
                }

            }
            return sb.toString()
        }
    }

}
