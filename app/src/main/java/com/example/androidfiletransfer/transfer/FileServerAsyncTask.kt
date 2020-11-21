package com.example.androidfiletransfer.transfer

import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import com.example.androidfiletransfer.MainActivity
import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Paths

class FileServerAsyncTask(private val context: Context) : AsyncTask<Void, Void, String>() {

    var pendingFile: String = ""

    override fun doInBackground(vararg params: Void?): String {
        val activity = context as MainActivity
        while (true) {
            val res = recvFile()
            if (!res) break
        }
        activity.disconnect()
        return "Receive All Files"
    }

    override fun onPostExecute(result: String) {
        val activity = context as MainActivity
        activity.setWifiText(result)
    }

    override fun onProgressUpdate(vararg values: Void?) {
        val activity = context as MainActivity
        activity.setWifiText("Downloading $pendingFile")
    }

    private fun recvFile(): Boolean {
        try {
            val serverSocket = ServerSocket(8885)
            val client = serverSocket.accept()
            val dir = context.getExternalFilesDir("adhocAndroid")
            if (!dir!!.exists()) dir.mkdirs()
            val clientInputStream = BufferedInputStream(client.getInputStream())
            DataInputStream(clientInputStream).use { d ->
                val info = d.readUTF().split(":")
                val fileName = info[1]
                if (info[0].indexOf("FIN") == -1){
                    pendingFile = fileName
                    publishProgress()
                    val saveName = "share-file-" + System.currentTimeMillis() + fileName
                    val f = File(dir, saveName)
                    val fOutputStream = FileOutputStream(f)
                    clientInputStream.copyTo(fOutputStream)
                    fOutputStream.close()
                    client.close()
                    return true
                }
            }
            client.close()
            return false
        } catch (e: Exception) {
            return true
        }
    }
}