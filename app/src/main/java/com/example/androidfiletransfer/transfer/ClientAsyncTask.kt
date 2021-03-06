package com.example.androidfiletransfer.transfer

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import android.provider.OpenableColumns
import androidx.core.net.toFile
import com.example.androidfiletransfer.MainActivity
import java.io.BufferedOutputStream
import java.io.DataOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.file.Files

class ClientAsyncTask(private val context: Context, private val address: String) : AsyncTask<Void, Void, String>() {

    var pendingFile: String = ""

    @ExperimentalStdlibApi
    override fun doInBackground(vararg params: Void?): String {
        val activity = context as MainActivity
        var uri: Uri? = activity.uriManager.pop()
        while (uri != null){
            pendingFile = getFileName(uri)
            publishProgress()
            sendFile(uri)
            uri = activity.uriManager.pop()
        }
        Thread.sleep(1000)
        sendDone()
        return "Sent file successful! Please wait for other device to be done"
    }

    override fun onPostExecute(result: String) {
        val activity = context as MainActivity
        activity.setWifiText(result)
    }

    override fun onProgressUpdate(vararg values: Void?) {
        val activity = context as MainActivity
        activity.setWifiText("Sending $pendingFile")
    }

    private fun getFileName(uri: Uri): String{
        var result: String? = null
        if (uri.scheme.equals("content")){
            val returnCursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (returnCursor != null && returnCursor.moveToFirst()){
                    result = returnCursor.getString(returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                returnCursor!!.close()
            }
        }
        if (result == null){
            result = uri.path
            val cut = result!!.lastIndexOf("/")
            if (cut != -1){
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    private fun sendDone(): Boolean{
        val message = "FIN:NOTHING"
        val MAX_TRIES = 20
        var tries = 0
        while (true) {
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(address, 8885), 10000)
                val clientOutputStream = BufferedOutputStream(socket.getOutputStream())
                DataOutputStream(clientOutputStream).use { d ->
                    d.writeUTF(message)
                }
                socket.close()
                return true
            } catch (e: IOException) {
                Thread.sleep(500)
                tries += 1
                if (tries == MAX_TRIES) return false
            }
        }
    }

    @ExperimentalStdlibApi
    private fun sendFile(uri: Uri): String{
        val MAX_TRIES = 20
        var tries = 0
        val fileName = getFileName(uri)
        while (true) {
            try {
                val client = Socket()
                client.connect(InetSocketAddress(address, 8885), 10000)
                val clientOutputStream = BufferedOutputStream(client.getOutputStream())
                DataOutputStream(clientOutputStream).use { d ->
                    d.writeUTF("SEN:" + fileName)
                    val f = context.getContentResolver().openInputStream(uri)
                    f!!.copyTo(d)
                    Thread.sleep(1000)
                }
                client.close()
                return fileName
            } catch (e: Exception) {
                Thread.sleep(500)
                tries += 1
                if (tries == MAX_TRIES) return e.toString()
            }
        }
    }

    // TODO
    private fun sendLargeFile(): Boolean{
        return true
    }
}