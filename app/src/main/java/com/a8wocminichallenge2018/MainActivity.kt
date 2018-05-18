package com.a8wocminichallenge2018

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.user_input_form.*
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
        val theme = getSharedPref()
        if (theme == "dark") {
            setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.AppTheme)
        }
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
        btn_or.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.user_input_form, null)

            val alertDialog = AlertDialog.Builder(this, if (getSharedPref() == "dark") R.style.AlerDarkTheme else 0)
            alertDialog.setView(view)
            alertDialog.setTitle("Fill in the info")
            alertDialog.setCancelable(false)
            val bookNameEt = view.findViewById<View>(R.id.book_name) as EditText
            val chapterNumberEt = view.findViewById<View>(R.id.chapter_number) as EditText
            val startVerseEt = view.findViewById<View>(R.id.start_verse) as EditText
            val endVerseEt = view.findViewById<View>(R.id.end_verse) as EditText

            alertDialog.setPositiveButton("View", DialogInterface.OnClickListener { dialog, whichButton ->
                val bookName = bookNameEt.text.toString()
                val chapterNumber = chapterNumberEt.text.toString()
                val startVerse = startVerseEt.text.toString()
                val endVerse = endVerseEt.text.toString()
                val uriBuilder = Uri.Builder()
                uriBuilder.scheme("https")
                        .authority("cdn.door43.org")
                        .appendPath("ne")
                        .appendPath("ulb")
                        .appendPath("v5.2")
                if (bookName.isNotEmpty() && bookName.isNotBlank()) {
                    uriBuilder.appendPath(getIdentifier(bookName))
                    if (chapterNumber.isNotEmpty() && chapterNumber.isNotBlank()) {
                    }
                    if (startVerse.isNotEmpty() && startVerse.isNotBlank()) {

                    }
                    if (endVerse.isNotEmpty() && endVerse.isNotBlank()) {

                    }
                    GetUsfm(usfm_to_txt).execute(uriBuilder.toString())
                } else {
                    Toast.makeText(this, "Book name cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            })
            alertDialog.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, whichButton ->
                dialog.dismiss()
            })

            alertDialog.create().show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.dark_theme -> {
                writeSharedPref("dark")
                this.recreate()
            }
            R.id.light_theme -> {
                writeSharedPref("light")
                this.recreate()
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
            var str = "Data Not Found."
            try {
                val url = URL(params[0])
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                // read the response
                val bufferStream = BufferedInputStream(conn.inputStream)
                str = convertStreamToString(bufferStream)
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }
            return str
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            tv?.setText(result)
            if (progressDialog!!.isShowing) {
                progressDialog!!.dismiss()
            }
            urlEditTv!!.visibility = View.GONE
            btnView!!.visibility = View.GONE
            btn_or!!.visibility = View.GONE
        }

        private fun convertStreamToString(inputStream: BufferedInputStream): String {
            var count = 0
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sb = StringBuilder()
            var line: String? = null
            try {
                while ({ line = reader.readLine(); line }() != null) {
                    if (count > 6) {
                        if (line == "") continue
                        sb.append(stripOf(line!!))
                        if (sb.toString().last() == '\n') continue
                        sb.append('\n')
                    } else {
                        count += 1
                        continue
                    }
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

    private fun writeSharedPref(theme_name: String) {
        val sharedPref = getSharedPreferences("theme", 0)
        val editor = sharedPref.edit()
        editor.putString("theme_name", theme_name)
        editor.commit()
    }

    private fun getSharedPref(): String {
        val sharedPref = getSharedPreferences("theme", 0)
        return sharedPref.getString("theme_name", "")
    }

    private fun getIdentifier(bookName: String): String {
        var identifier = ""
        val bookname = bookName.toLowerCase()
        val list = identifierList()
        if (bookname in list.keys) {
            identifier = list.getValue(bookname)
        }
        return identifier.plus(".usfm")
    }

    private fun identifierList(): Map<String, String> {
        return mapOf(
                "mathew" to "mat",
                "mark" to "mrk",
                "luke" to "luk",
                "john" to "jhn",
                "acts" to "act"
        )
    }
}
