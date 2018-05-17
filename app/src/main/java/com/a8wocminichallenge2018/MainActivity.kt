package com.a8wocminichallenge2018

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    private var progressDialog: ProgressDialog? = null
    private var urlEditTv: EditText? = null
    private var btnView: Button? = null

    companion object {
        private const val TAG = "MAIN_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        btnView = btn_view
        urlEditTv = url

        btn_view.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(url.windowToken, 0)

            //Nepali
            //  GetUsfm(usfm_to_txt).execute("https://cdn.door43.org/ne/ulb/v5.2/mat.usfm")

            //Serbian
            // GetUsfm(usfm_to_txt).execute("https://cdn.door43.org/sr-Latn/stf/v1/mat.usfm")

            val url = urlEditTv?.text.toString()

            if (url.isNotEmpty()) {
                if (Patterns.WEB_URL.matcher(url).matches()) {
                    GetUsfm(usfm_to_txt).execute(url)
                } else {
                    Toast.makeText(this, "Invalid Url.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Url cannot be empty.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> item.setOnMenuItemClickListener {
                startActivity(Intent(this, MainActivity::class.java))
                true
            }

        }
        return super.onOptionsItemSelected(item)
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
            urlEditTv!!.visibility = View.GONE
            btnView!!.visibility = View.GONE
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
