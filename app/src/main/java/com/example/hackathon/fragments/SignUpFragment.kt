package com.example.hackathon.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.hackathon.R
import com.example.hackathon.helpers.FragmentHandler
import com.example.hackathon.helpers.HttpClient
import com.example.hackathon.helpers.Utils
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.fragment_sign_up.*
import org.json.JSONObject


class SignUpFragment : Fragment() {

    companion object {
        fun getInstance(): SignUpFragment = SignUpFragment()
        val TAG = "SIGN_UP_FRAGMENT"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHandler = FragmentHandler(activity!! as AppCompatActivity, R.id.main_fragment_container)

        setEditTextValidation(signUpFirstNameEditText, getString(R.string.fragment_sign_up_first_name_error))
        setEditTextValidation(signUpLastNameEditText, getString(R.string.fragment_sign_up_last_name_error))
        setEditTextValidation(signUpEmailEditText, getString(R.string.fragment_sign_up_email_error))
        setEditTextValidation(signUpPasswordEditText, getString(R.string.fragment_sign_up_password_error))

        signUpButton.setOnClickListener {
            val data = JSONObject()
            val user = JSONObject()
            user.put("nombre", signUpFirstNameEditText.editText!!.text.toString())
            user.put("apellidos", signUpLastNameEditText.editText!!.text.toString())
            user.put("correo", signUpEmailEditText.editText!!.text.toString())
            user.put("password", signUpPasswordEditText.editText!!.text.toString())
            data.put("usuario", user)

            HttpClient.signUp("/usuarios/registro", activity!!, data.toString(), true) { err, res ->
                activity!!.runOnUiThread {
                    val jsonRes = JSONObject(res)
                    if (err) {
                        Toast.makeText(activity!!, jsonRes.get("message").toString(), Toast.LENGTH_LONG).show()
                    } else {
                        Log.d(TAG, jsonRes.toString())
                        Toast.makeText(activity!!, jsonRes.get("message").toString(), Toast.LENGTH_LONG).show()
                        fragmentHandler.add(LoginFragment.getInstance())
                    }
                }
            }
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
