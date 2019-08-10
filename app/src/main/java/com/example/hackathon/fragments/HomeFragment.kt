package com.example.hackathon.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.hackathon.R
import com.example.hackathon.helpers.FragmentHandler
import com.example.hackathon.helpers.Utils
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment() {

    companion object {
        fun getInstance(): HomeFragment = HomeFragment()
        val TAG = "HOME_FRAGMENT"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentHandler = FragmentHandler(activity!! as AppCompatActivity, R.id.main_fragment_container)

        logoutButton.setOnClickListener {
            Utils.clearSharedPreferences(activity!!)
            fragmentHandler.add(LoginFragment.getInstance())
        }
    }
}
