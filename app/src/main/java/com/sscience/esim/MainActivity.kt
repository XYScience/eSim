package com.sscience.esim

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.util.Log
import android.telephony.euicc.EuiccManager
import android.R.attr.action
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.telephony.TelephonyManager
import android.telephony.euicc.DownloadableSubscription
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        val ACTIVATION_CODE = "LPA:1\$rsp.truphone.com$57F9937E8B4C4F77A48D8B449C60BAA4"
        const val ACTION_DOWNLOAD_SUBSCRIPTION = "download_subscription"
        const val ACTION_SWITCH_TO_SUBSCRIPTION = "switch_to_subscription"
    }

    private var euiccManager: EuiccManager? = null
    private var callBackIntentDownload: PendingIntent? = null
    private var callBackIntentSwitch: PendingIntent? = null

    @SuppressLint("WrongConstant", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val subId = SubscriptionManager.getDefaultSubscriptionId()
        tvSubscriptionId.text = "DefaultSubscriptionId: $subId"

        euiccManager = getSystemService(Context.EUICC_SERVICE) as EuiccManager

        val isEnable = euiccManager?.isEnabled
        Log.e(">>>>>>>>>", "isEnable: $isEnable")

        registerReceiver(receiverDownload, IntentFilter(ACTION_DOWNLOAD_SUBSCRIPTION))
        registerReceiver(receiverSwitch, IntentFilter(ACTION_SWITCH_TO_SUBSCRIPTION))

        btn_download.setOnClickListener {
            val intent = Intent(ACTION_DOWNLOAD_SUBSCRIPTION)
            callBackIntentDownload = PendingIntent.getBroadcast(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val sub: DownloadableSubscription = DownloadableSubscription.forActivationCode(ACTIVATION_CODE)
            euiccManager?.downloadSubscription(sub, true, callBackIntentDownload)
        }

        btn_switch.setOnClickListener {
            val intent = Intent(ACTION_SWITCH_TO_SUBSCRIPTION)
            callBackIntentSwitch = PendingIntent.getBroadcast(this, 12, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            euiccManager?.switchToSubscription(10, callBackIntentSwitch)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e(">>>>>>>>>", "requestCode: $requestCode, resultCode: $resultCode")
    }

    private var receiverDownload: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_DOWNLOAD_SUBSCRIPTION != intent.action) {
                return
            }
            val detailedCode = intent.getIntExtra(
                EuiccManager.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE, -1 /* defaultValue*/
            )
            val resultCode = resultCode
            if (resultCode == EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_RESOLVABLE_ERROR) {
                euiccManager?.startResolutionActivity(this@MainActivity, 11, intent, callBackIntentDownload)
            }
            Log.e(">>>>>>>>>", "receiverDownload detailedCode: $detailedCode, resultCode: $resultCode")
        }
    }

    private var receiverSwitch: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_SWITCH_TO_SUBSCRIPTION != intent.action) {
                return
            }
            val detailedCode = intent.getIntExtra(
                EuiccManager.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE, -1 /* defaultValue*/
            )
            val resultCode = resultCode
            if (resultCode == EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_RESOLVABLE_ERROR) {
                euiccManager?.startResolutionActivity(this@MainActivity, 11, intent, callBackIntentSwitch)
            }
            Log.e(">>>>>>>>>", "receiverSwitch detailedCode: $detailedCode, resultCode: $resultCode")
        }
    }
}
