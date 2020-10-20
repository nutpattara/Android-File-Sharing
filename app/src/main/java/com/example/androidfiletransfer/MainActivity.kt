package com.example.androidfiletransfer

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.example.androidfiletransfer.receiver.WiFiDirectBroadcastReceiver

class MainActivity : AppCompatActivity() {

    private lateinit var manager: WifiP2pManager
    private lateinit var wifiManager: WifiManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: WiFiDirectBroadcastReceiver
    private lateinit var intentFilter: IntentFilter
    private val peers = mutableListOf<WifiP2pDevice>()
    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        if (refreshedPeers != peers) {
            peers.clear()
            peers.addAll(refreshedPeers)

            // If an AdapterView is backed by this data, notify it
            // of the change. For instance, if you have a ListView of
            // available peers, trigger an update.
            // (listAdapter as WiFiPeerListAdapter).notifyDataSetChanged()
            updatePeerListChanged()

            // Perform any other updates needed based on the new list of
            // peers connected to the Wi-Fi P2P network.
        }

        if (peers.isEmpty()) {
            //Log.d(TAG, "No devices found")
            return@PeerListListener
        }
    }

    private var isWifiP2pEnabled: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        wifiManager = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }

        //
        setWifiText("Please turn location on for this app")
        val wifiButton: Button = findViewById(R.id.enable_button)
        wifiButton.setOnClickListener {
            if (!wifiManager.isWifiEnabled) {
                wifiManager.setWifiEnabled(true)
                setWifiText("Enable Wifi")
            } else {
                setWifiText("Wifi already enable")
            }
        }
        val discoverButton: Button = findViewById(R.id.discover_button)
        discoverButton.setOnClickListener {
            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {

                override fun onSuccess() {
                    // The onSuccess() method only notifies you that the discovery process succeeded
                    // and does not provide any information about the actual peers that it discovered, if any
                    setWifiText("Finding other user...")
                }

                override fun onFailure(reasonCode: Int) {
                    setWifiText("Failed to find other user")
                }
            })
        }
        //

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)
    }

    public override fun onResume() {
        super.onResume()
        receiver = WiFiDirectBroadcastReceiver(manager, channel, peerListListener, this)
        registerReceiver(receiver, intentFilter)
    }

    public override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    private fun updatePeerListChanged(){
        for (device in peers){
            setWifiText("Found " + device.deviceName)
        }
    }

    fun setWifiText(text: String){
        // Using this for debug
        val wifiStatus: TextView = findViewById(R.id.wifi_status_text_view)
        wifiStatus.setText(text)
    }

}