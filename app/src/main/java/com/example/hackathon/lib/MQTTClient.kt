package com.example.hackathon.lib

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.hackathon.helpers.SSLUtil
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*


class MQTTClient(private val context: Context, private val activity: Activity) {

    companion object {
        const val TAG = "MqttClient"
        const val URI = "ssl://a1cf0ig3498foo-ats.iot.us-east-1.amazonaws.com:8883"
    }

    val client by lazy {
        val clientId = MqttClient.generateClientId()
        MqttAndroidClient(context, URI, clientId)
    }

    fun connect(caCrtFilePath: String, crtPath: String, privateKeyPath: String,
                messageCallBack: ((topic: String, message: MqttMessage) -> Unit)? = null) {
        try {
            activity.runOnUiThread {
                client.setCallback(object : MqttCallbackExtended {
                    override fun connectComplete(reconnect: Boolean, serverURI: String) {
//                        topics?.forEach {
//                            subscribeTopic(it)
//                        }
                        Toast.makeText(context, "Connected to: $serverURI", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "Connected to: $serverURI")
                        client.subscribe("test", 0)
                    }

                    override fun connectionLost(cause: Throwable) {
                        Log.d(TAG, "The Connection was lost.")
                    }

                    @Throws(Exception::class)
                    override fun messageArrived(topic: String, message: MqttMessage) {
                        Log.d(TAG, "Incoming message from $topic: " + message.toString())
                        messageCallBack?.invoke(topic, message)
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken) {

                    }
                })
                val options = MqttConnectOptions()
                options.connectionTimeout = 60
                options.keepAliveInterval = 60
                options.socketFactory =  SSLUtil.getSocketFactory(caCrtFilePath, crtPath, privateKeyPath)
                client.connect(options)
            }
        } catch (e: MqttException) {
            Log.d(TAG, e.message)
            e.printStackTrace()
        }
    }

    fun publishMessage(topic: String, msg: String) {

        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            client.publish(topic, message.payload, 0, true)
            Log.d(TAG, "$msg published to $topic")
        } catch (e: MqttException) {
            Log.d(TAG, "Error Publishing to $topic: " + e.message)
            e.printStackTrace()
        }

    }
//
    fun subscribeTopic(topic: String, qos: Int = 0) {
        client.subscribe(topic, qos).actionCallback = object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                Log.d(TAG, "Subscribed to $topic")
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Log.d(TAG, "Failed to subscribe to $topic")
                exception.printStackTrace()
            }
        }
    }
//
    fun close() {
        client.apply {
            unregisterResources()
            close()
        }
    }
}