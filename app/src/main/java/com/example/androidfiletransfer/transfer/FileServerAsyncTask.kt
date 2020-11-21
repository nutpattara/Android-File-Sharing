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

    override fun doInBackground(vararg params: Void?): String {
        val activity = context as MainActivity
        var info: Array<String> = emptyArray()
        while (true) {
            recvFileNew()
        }
        activity.disconnect()
        return "DONE"
    }

//    override fun doInBackground(vararg params: Void?): String {
//        val activity = context as MainActivity
//        var info: Array<String> = emptyArray()
//        while (true) {
//            info = recvInfo()
//            if (info[0].indexOf("SEND") != -1){
//                recvFile(info)
//            } else if (info[0].indexOf("DONE") != -1){
//                return info[0]
//            } else {
//                return "ERROR: " + info[0]
//            }
//        }
//    }

    override fun onPostExecute(result: String) {
        val activity = context as MainActivity
        activity.setWifiText("Recv: \"$result\"")
    }

//    private fun recvInfo(): Array<String> {
//        try {
//            val serverSocket = ServerSocket(8885)
//            val client = serverSocket.accept()
//            val clientInputStream = client.getInputStream()
//            val message = clientInputStream.bufferedReader().use {
//                it.readLine().toString()
//            }
//            val info = message.split(":").toTypedArray()
//            serverSocket.close()
//            client.close()
//            return info
//        } catch (e: IOException) {
//            return arrayOf(e.toString())
//        }
//    }

//    private fun recvFile(info : Array<String>): Boolean {
//        try {
//            val serverSocket = ServerSocket(8885)
//            val client = serverSocket.accept()
//            val clientInputStream = client.getInputStream()
//            val fileName = "share-file-" + System.currentTimeMillis() + info[1]
//            val f = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
////            val f = File(context.getExternalFilesDir("adhocAndroid"), fileName)
////            val dirs = File(f.parent)
////            if (!dirs.exists()){
////                dirs.mkdirs()
////            }
//            f.createNewFile()
//            val fOutputStream = FileOutputStream(f)
//            clientInputStream.copyTo(fOutputStream)
//            serverSocket.close()
//            client.close()
//            fOutputStream.close()
//            return true
//        } catch (e: Exception) {
//            return false
//        }
//    }

    private fun recvFileNew(): Boolean {
        try {
            val serverSocket = ServerSocket(8885)
            val client = serverSocket.accept()
            val dir = context.getExternalFilesDir("adhocAndroid")
            if (!dir!!.exists()) dir.mkdirs()
            val clientInputStream = BufferedInputStream(client.getInputStream())
            DataInputStream(clientInputStream).use { d ->
                val fileName = d.readUTF()
                if (!fileName.equals("THIS_IS_THE_LAST_FILE")){
                    val f = File(dir, fileName)
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
            return false
        }
    }
}