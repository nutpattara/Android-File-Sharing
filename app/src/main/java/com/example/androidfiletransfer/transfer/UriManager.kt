package com.example.androidfiletransfer.transfer

import android.net.Uri

class UriManager {

    private var uris = ArrayList<Uri>()

    fun clear(){
        uris.clear()
    }

    fun addUri(uri: Uri?){
        if (uri != null){
            uris.add(uri)
        }
    }

    @ExperimentalStdlibApi
    fun pop(): Uri?{
        try {
            val uri = uris.removeFirst()
            return uri
        } catch (e: NoSuchElementException) {
            return null
        }

    }
}