package com.example.android.foodieexpress.Services

import com.example.android.foodieexpress.Common.Common
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFCMServices : FirebaseMessagingService(){

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Common.updateToken(this,token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val dataRecv =  remoteMessage.data
        if(dataRecv!= null) {
            Common.showNotification(this, Random().nextInt(),
            dataRecv[Common.NOTI_TITLE],
            dataRecv[Common.NOTI_CONTENT],null)
        }
    }

}