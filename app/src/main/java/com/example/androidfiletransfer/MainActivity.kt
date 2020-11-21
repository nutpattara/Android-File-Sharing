package com.example.androidfiletransfer

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.IntentFilter
import android.net.Uri
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.androidfiletransfer.receiver.WiFiDirectBroadcastReceiver
import com.example.androidfiletransfer.transfer.UriManager
import com.example.androidfiletransfer.view.PeersListAdaptor
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val CHOOSE_FILE_REQUEST_CODE = 111

    private lateinit var manager: WifiP2pManager
    private lateinit var wifiManager: WifiManager
    private lateinit var channel: WifiP2pManager.Channel
    private lateinit var receiver: WiFiDirectBroadcastReceiver
    private lateinit var intentFilter: IntentFilter
    private lateinit var listView: ListView
    private val peers = ArrayList<WifiP2pDevice>()
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

    lateinit var uriManager: UriManager
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

        setWifiText("Please turn on location & wifi for this app")
        val uploadButton: Button = findViewById(R.id.enable_button)
        uploadButton.setOnClickListener {
            val intent = Intent()
                    .setType("*/*")
                    .setAction(Intent.ACTION_GET_CONTENT)
                    .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(Intent.createChooser(intent, "Choose a file"), CHOOSE_FILE_REQUEST_CODE)
        }
        val discoverButton: Button = findViewById(R.id.discover_button)
        discoverButton.setOnClickListener {
            manager.discoverPeers(channel, object : WifiP2pManager.ActionListener {

                override fun onSuccess() {
                    setWifiText("Finding other user...")
                }

                override fun onFailure(reasonCode: Int) {
                    setWifiText("Failed to find other user")
                }
            })
        }
        val resetButton: Button = findViewById(R.id.reset_button)
        resetButton.setOnClickListener {
            resetScreen()
        }

        manager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = manager.initialize(this, mainLooper, null)

        listView = findViewById(R.id.peer_list_view)
        val adaptor = PeersListAdaptor(this, peers)
        listView.adapter = adaptor
        listView.setOnItemClickListener { parent, view, position, id ->
            val selectedPeer = peers[position]
            connectToPeer(selectedPeer)
        }
        uriManager = UriManager()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CHOOSE_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null){
                val dataClipData = data.clipData
                if (dataClipData != null){
                    var allUri = ""
                    for (i in 0 until dataClipData.itemCount){
                        val uri = dataClipData.getItemAt(i).uri
                        uriManager.addUri(uri)
                        allUri += uri
                        allUri += " "
                        setWifiText("Selected " + allUri)
                    }
                } else {
                    val uri = data.data
                    uriManager.addUri(uri)
                    setWifiText("Selected" + uri)
                }
            }
        }
    }

    private fun connectToPeer(device: WifiP2pDevice){
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
            groupOwnerIntent = 0
        }
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(
                    this@MainActivity,
                    "Connect Successful",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(reason: Int) {
                Toast.makeText(
                        this@MainActivity,
                        "Connect failed. Retry.",
                        Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun disconnect(){
        manager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(
                        this@MainActivity,
                        "Disconnected from group",
                        Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(reasonCode: Int) {
            }
        })
    }

    fun resetScreen(){
        disconnect()
        setWifiText("Please turn on location & wifi for this app")
        uriManager.clear()
    }

    private fun updatePeerListChanged(){
        val adaptor = listView.adapter as PeersListAdaptor
        adaptor.notifyDataSetChanged()
    }

    fun setWifiText(text: String){
        // Using this for debug
        val wifiStatus: TextView = findViewById(R.id.wifi_status_text_view)
        wifiStatus.setText(text)
    }

}