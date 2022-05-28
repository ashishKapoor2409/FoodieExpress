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
        "Authorization:key=AAAADwdGgR4:APA91bGtuEZkl9ubDf-GvAnudq3Uh6Pj2aRqn9JmX1-r7ZUVAPkBr_2f72OgBxtN1R1PFI82AIiJLCeXMQ_SRPyiSUrxnYeoVqsOnoU10BrAK2iTC5Ee9yXi6dD-v8CcWG-lmqKrqPq2"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData) : Observable<FCMResponse>
}