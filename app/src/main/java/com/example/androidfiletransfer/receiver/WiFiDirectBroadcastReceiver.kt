package com.example.androidfiletransfer.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import com.example.androidfiletransfer.MainActivity
import com.example.androidfiletransfer.R
import com.example.androidfiletransfer.transfer.ClientAsyncTask
import com.example.androidfiletransfer.transfer.FileServerAsyncTask

class WiFiDirectBroadcastReceiver(
    private val mManager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val peerListListener: WifiP2pManager.PeerListListener,
    private val activity: MainActivity) : BroadcastReceiver() {

    private val connectionListener = WifiP2pManager.ConnectionInfoListener { info ->

        val groupOwnerAddress: String = info.groupOwnerAddress.hostAddress
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
            activity.setWifiText("You are receiver")
            FileServerAsyncTask(activity).execute()
        } else if (info.groupFormed) {
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
            activity.setWifiText("You are sender")
            ClientAsyncTask(activity, groupOwnerAddress).execute()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action: String? = intent.action
        when (action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                when (state) {
                    WifiP2pManager.WIFI_P2P_STATE_ENABLED -> {
                        // Wifi P2P is enabled
                    }
                    else -> {
                        // Wi-Fi P2P is not enabled
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                mManager.requestPeers(channel, peerListListener)
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                mManager.let { manager ->
                    val networkInfo: NetworkInfo? = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO) as NetworkInfo
                    if (networkInfo?.isConnected == true) {
                        // We are connected with the other device, request connection
                        // info to find group owner IP
                        manager.requestConnectionInfo(channel, connectionListener)
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // Respond to this device's wifi state changing
            }
        }
    }
}