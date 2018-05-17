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

    companion object {
        private const val TAG = "MAIN_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_parse.setOnClickListener {
            Log.i(TAG, "Test")
            GetUsfm(usfm_to_txt).execute("https://cdn.door43.org/ne/ulb/v5.2/mat.usfm")
        }
    }

    private inner class GetUsfm(etv: EditText) : AsyncTask<String, Unit, String>() {
        val tv: EditText? = etv
        override fun doInBackground(vararg params: String): String? {
            var usfm: String? = null
            try {
                val url = URL(params[0])
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                // read the response
                val bufferStream = BufferedInputStream(conn.inputStream)
                usfm = convertStreamToString(bufferStream)
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }
            return usfm
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            tv?.setText(result)
        }

        private fun convertStreamToString(inputStream: BufferedInputStream): String? {
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line = ""
            try {
                do {
                    sb.append(reader.readLine())
                    sb.append("\n")
                } while (reader.readLine() != null)
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
    }

}
