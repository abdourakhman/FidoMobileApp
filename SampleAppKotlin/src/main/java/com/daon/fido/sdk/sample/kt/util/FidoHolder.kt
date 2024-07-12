package com.daon.fido.sdk.sample.kt.util

import android.content.Context
import android.os.Bundle
import com.daon.fido.client.sdk.IXUAF
import com.daon.fido.client.sdk.IXUAFService
import com.daon.sdk.fido.service.rpsa.IXUAFRPSAService

class FidoHolder private constructor(private val context: Context) {
    var fido: IXUAF
    var rpsaServer: IXUAFService

    init {
        rpsaServer = getServer()
        fido = IXUAF(context, rpsaServer)
    }

    private fun getServer(): IXUAFService {
        val rpsaParams = Bundle()
        rpsaParams.putString("server_url", "http://srv-idx.gemadec.ma:8000")

        //
        return IXUAFRPSAService(context, rpsaParams)

        //REST
        /*val restParams = Bundle()
        restParams.putString("appId", "mobileteamfido")
        restParams.putString("regPolicy", "reg")
        restParams.putString("authPolicy", "auth")
        //Basic Auth
        restParams.putString("username", "admin2")
        restParams.putString("password", "admin2")
        restParams.putString("server_url", "https://us-dev-env4.identityx-cloud.com:443/fido")
        restParams.putString("rest_path", "IdentityXServices/rest/v1")
        return IXUAFRestService(context, restParams)*/
    }

    companion object {
        private var instance: FidoHolder? = null
        fun getInstance(context: Context): FidoHolder {
            if (instance != null) {
                return instance as FidoHolder
            }
            instance = FidoHolder(context)
            return instance as FidoHolder
        }
    }

}