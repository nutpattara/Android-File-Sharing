package com.example.androidfiletransfer.view

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.androidfiletransfer.R

public class PeersListAdaptor(private val context: Context,
                              private val dataSource: ArrayList<WifiP2pDevice>) : BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.list_item_peer, parent, false)

        val nameTextView = rowView.findViewById(R.id.nameTextView) as TextView

        val device = getItem(position) as WifiP2pDevice
        nameTextView.text = device.deviceName

        // Not done

        return rowView
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return dataSource.size
    }

    fun updatePeerList(){
        notifyDataSetChanged()
    }

}