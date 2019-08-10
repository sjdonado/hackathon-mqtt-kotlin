package com.example.hackathon.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

import com.example.hackathon.R
import com.example.hackathon.helpers.FragmentHandler
import com.example.hackathon.helpers.HttpClient
import com.example.hackathon.helpers.Utils
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_login.*
import org.json.JSONObject


class LoginFragment : Fragment() {

    companion object {
        fun getInstance(): LoginFragment = LoginFragment()
        val TAG = "LOGIN_FRAGMENT"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHandler = FragmentHandler(activity!! as AppCompatActivity, R.id.main_fragment_container)

        setEditTextValidation(userNameEditText, getString(R.string.fragment_login_username_error))
        setEditTextValidation(passwordEditText, getString(R.string.fragment_login_password_error))

        loginButton.setOnClickListener {
            val data = JSONObject()
            val credentials = JSONObject()
            credentials.put("correo", userNameEditText.editText!!.text.toString())
            credentials.put("password", passwordEditText.editText!!.text.toString())
            data.put("credenciales", credentials)

            HttpClient.login("/usuarios/login", activity!!, data.toString(), true) { err, res ->
                activity!!.runOnUiThread {
                    Utils.setSharedPreferencesStringValue(activity!!, Utils.USER_TOKEN, res)
                    fragmentHandler.add(HomeFragment.getInstance())
                }
            }
        }

        goToSignUpButton.setOnClickListener {
            fragmentHandler.add(SignUpFragment.getInstance(), true)
        }
    }


    private fun setEditTextValidation(editTextLayout: TextInputLayout, errorValue: String){
        editTextLayout.editText!!.afterTextChanged { validateEditText(editTextLayout, editTextLayout.editText!!, errorValue) }
        editTextLayout.editText!!.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateEditText(editTextLayout, editTextLayout.editText!!, errorValue)
            }
        }
    }

    private fun validateEditText(layout: TextInputLayout, editText: EditText, errorValue: String) {
        if (editText.text.isEmpty()) layout.error = errorValue else layout.error = null
    }


    private fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }
        })
    }
}
