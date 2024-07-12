package com.daon.fido.sdk.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.DialogFragment


class ChooseAccountDialogFragment(private val accountSelectListener: OnAccountSelectListener): DialogFragment() {

    fun interface OnAccountSelectListener {
        fun onAccountSelected(selectedAccount: Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.choose_account, container)
        val listView = rootView.findViewById<ListView>(R.id.list_view_user_accounts)

        val cancel = rootView.findViewById<Button>(R.id.choose_user_account_cancel)
        cancel.setOnClickListener {
            //We need to inform Fido SDK that user did not select an account.
            accountSelectListener.onAccountSelected(-1)
            dismiss()
        }

        dialog?.setCanceledOnTouchOutside(false)

        val accounts = arguments?.getSerializable("accounts") as Array<String>
        val adapter = context?.let { AccountListAdapter(it, accounts) }
        listView.adapter = adapter
        listView.setOnItemClickListener {
                adapterView,
                view,
                position,
                l
            ->
            accountSelectListener.onAccountSelected(position)
            dismiss()
        }
        return rootView
    }
}