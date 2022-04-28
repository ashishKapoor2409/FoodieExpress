package com.example.android.foodieexpress.Remote


import com.example.android.foodieexpress.Model.FCMResponse
import com.example.android.foodieexpress.Model.FCMSendData
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.*

interface IFCMService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key="
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData) : Observable<FCMResponse>
}