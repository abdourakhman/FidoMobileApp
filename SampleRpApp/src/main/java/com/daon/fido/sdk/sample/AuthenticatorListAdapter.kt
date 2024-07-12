package com.daon.fido.sdk.sample

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.daon.fido.client.sdk.core.FidoConstants
import com.daon.fido.client.sdk.model.Authenticator


class AuthenticatorListAdapter(
    context: Context,
    private val dataSource: Array<Array<Authenticator>>
) : ArrayAdapter<Array<Authenticator>>(context, 0, dataSource) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val authenticatorSet: Array<Authenticator>? = getItem(position)
        val rowView =
            LayoutInflater.from(context).inflate(R.layout.authenticator_list_item, parent, false)

        val image: ImageView = rowView.findViewById(R.id.auth_icon)
        val name: TextView = rowView.findViewById(R.id.auth_name)

        val builder = StringBuilder()
        var authCount = 0
        var iconIndex = 0
        if(authenticatorSet != null) {
            for ((index, element) in authenticatorSet.withIndex()) {
                if(element.userVerification == FidoConstants.USER_VERIFY_NONE) {
                    if(authenticatorSet.size == 1) {
                        builder.append(element.title)
                        authCount++
                    }else if (index == 0) {
                        iconIndex = 1
                    }
                }else {
                    if(authCount > 0) {
                        builder.append(" &\n")
                    }
                    builder.append(element.title)
                    authCount++
                }
            }

            name.text = builder.toString()

            val options = BitmapFactory.Options()
            options.inMutable = true
            val commandIndex = authenticatorSet[iconIndex].icon.indexOf(',')
            val imageBase64 = authenticatorSet[iconIndex].icon.substring(commandIndex + 1)
            val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
            image.setImageBitmap(bmp)
        }

        return rowView
    }
}