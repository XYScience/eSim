package com.sscience.esim

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.telephony.euicc.DownloadableSubscription
import android.telephony.euicc.EuiccManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity>>>"
        val ACTIVATION_CODE = "LPA:1\$rsp.truphone.com\$2T-1GW21-1MEX2U4"
        const val ACTION_DOWNLOAD_SUBSCRIPTION = "download_subscription"
        const val ACTION_SWITCH_TO_SUBSCRIPTION = "switch_to_subscription"
        const val ACTION_DELETE_SUBSCRIPTION = "delete_subscription"
    }

    private var euiccManager: EuiccManager? = null
    private var callBackIntent: PendingIntent? = null

    @SuppressLint("WrongConstant", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val subId = SubscriptionManager.getDefaultSubscriptionId()
        tvSubscriptionId.text = "DefaultSubscriptionId: $subId"

        euiccManager = getSystemService(Context.EUICC_SERVICE) as EuiccManager

        val isEnable = euiccManager?.isEnabled
        Log.e(TAG, "isEnable: $isEnable")
        try {
            val eid = euiccManager?.eid
            Log.e(TAG, "eid: $eid")
        } catch (e: Exception) {
            Log.e(TAG, ">>>>>: $e")
        }

        registerReceiver(receiver, IntentFilter(ACTION_DOWNLOAD_SUBSCRIPTION))
        registerReceiver(receiver, IntentFilter(ACTION_SWITCH_TO_SUBSCRIPTION))
        registerReceiver(receiver, IntentFilter(ACTION_DELETE_SUBSCRIPTION))


        btn_download.setOnClickListener {
            Log.e(TAG, "onCLick: $ACTION_DOWNLOAD_SUBSCRIPTION")
            val intent = Intent(ACTION_DOWNLOAD_SUBSCRIPTION)
            callBackIntent = PendingIntent.getBroadcast(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val sub: DownloadableSubscription = DownloadableSubscription.forActivationCode(ACTIVATION_CODE)
            Log.e(TAG, "ac: ${sub.encodedActivationCode}")
            euiccManager?.downloadSubscription(sub, true, callBackIntent)
        }

        btn_switch.setOnClickListener {
            Log.e(TAG, "onCLick: $ACTION_SWITCH_TO_SUBSCRIPTION")
            val intent = Intent(ACTION_SWITCH_TO_SUBSCRIPTION)
            callBackIntent = PendingIntent.getBroadcast(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val subscriptionId: String = et_switch_subscription_id.text.toString()
            euiccManager?.switchToSubscription(subscriptionId.toInt(), callBackIntent)
        }

        btn_delete.setOnClickListener {
            Log.e(TAG, "onCLick: $ACTION_DELETE_SUBSCRIPTION")
            val intent = Intent(ACTION_DELETE_SUBSCRIPTION)
            callBackIntent = PendingIntent.getBroadcast(this, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val subscriptionId: String = et_delete_subscription_id.text.toString()
            euiccManager?.deleteSubscription(subscriptionId.toInt(), callBackIntent)
        }

        val tm: TelephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val cp = tm.hasCarrierPrivileges()
        Log.e(TAG, "hasCarrierPrivileges: $cp")

        val packageInfo: PackageInfo
        try {
            packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in packageInfo.signatures) {
                val md = MessageDigest.getInstance("SHA-1")
                val certHash: ByteArray = md.digest(signature.toByteArray())
                Log.e(">>>>>>>>>", "signature hash: ${Arrays.toString(certHash)}")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            throw IllegalArgumentException("Unknown package: $packageName", e)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e(TAG, "onActivityResult requestCode: $requestCode, resultCode: $resultCode")
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            if (ACTION_DOWNLOAD_SUBSCRIPTION != intent.action
                && ACTION_SWITCH_TO_SUBSCRIPTION != intent.action
                && ACTION_DELETE_SUBSCRIPTION != intent.action
            ) return

            Log.e(TAG, "receiver: ${intent.action}")
            val detailedCode = intent.getIntExtra(
                EuiccManager.EXTRA_EMBEDDED_SUBSCRIPTION_DETAILED_CODE, -1 /* defaultValue*/
            )
            val resultCode = resultCode
            if (resultCode == EuiccManager.EMBEDDED_SUBSCRIPTION_RESULT_RESOLVABLE_ERROR) {
                euiccManager?.startResolutionActivity(this@MainActivity, 11, intent, callBackIntent)
            }
            Log.e(TAG, "onReceive detailedCode: $detailedCode, resultCode: $resultCode")
        }

    }
}
