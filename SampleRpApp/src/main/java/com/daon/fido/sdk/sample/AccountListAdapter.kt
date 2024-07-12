package com.daon.fido.sdk.sample

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class AccountListAdapter(context: Context, private val dataSource: Array<String>) :
    ArrayAdapter<String>(context, 0, dataSource) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        //val account: String? = getItem(position)

        val rootView = LayoutInflater.from(context).inflate(R.layout.account_list_item, parent, false)

        val user: TextView = rootView.findViewById(R.id.user_account_id)
        user.text = getItem(position)

        return rootView

    }
}