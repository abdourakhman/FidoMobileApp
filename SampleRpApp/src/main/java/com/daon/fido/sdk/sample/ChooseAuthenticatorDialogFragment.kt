package com.daon.fido.sdk.sample

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.DialogFragment


class ChooseAuthenticatorDialogFragment(private val authSelectListener: OnAuthSelectListener) :
    DialogFragment() {

    fun interface OnAuthSelectListener {
        fun onAuthSelected(selectedAuth: Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: ")
        val rootView = inflater.inflate(R.layout.choose_authenticator, container)
        val listView = rootView.findViewById<ListView>(R.id.list_view_authenticatorsets)

        val cancel = rootView.findViewById<Button>(R.id.choose_authenticator_cancel)
        cancel.setOnClickListener {
            authSelectListener.onAuthSelected(-1)
            dismiss()
        }

        dialog?.setCanceledOnTouchOutside(false)
        val adapter = context?.let {
            AuthenticatorListAdapter(
                it, (requireActivity().application as CoreApplication).authenticators
            )
        }
        listView.adapter = adapter
        listView.setOnItemClickListener { adapterView, view, position, l ->
            authSelectListener.onAuthSelected(position)
            dismiss()
        }
        return rootView
    }

    override fun onResume() {
        super.onResume()

        //Handling back button
        dialog?.setOnKeyListener(object : DialogInterface.OnKeyListener {
            override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    authSelectListener.onAuthSelected(-1)
                    dismiss()
                    return true
                }
                return false
            }
        })
    }

    companion object {
        private val TAG = ChooseAuthenticatorDialogFragment::class.simpleName
    }
}