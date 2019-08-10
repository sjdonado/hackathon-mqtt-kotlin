package com.example.hackathon

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.FragmentManager
import com.example.hackathon.fragments.HomeFragment
import com.example.hackathon.fragments.LoginFragment
import com.example.hackathon.helpers.FragmentHandler
import com.example.hackathon.helpers.Utils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var fragmentHandler: FragmentHandler
    private lateinit var actionBar : ActionBar
    private lateinit var fragmentManager: FragmentManager
    private var userId: String? = null
    var reading = false

    companion object {
        val TAG = "MAIN_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        setContentView(R.layout.activity_main)

        setSupportActionBar(main_toolbar)
        actionBar = supportActionBar!!

        fragmentManager = supportFragmentManager
        fragmentHandler = FragmentHandler(this, R.id.main_fragment_container)

        val token = Utils.getSharedPreferencesStringValue(this, Utils.USER_TOKEN)

        if (token == null) {
           fragmentHandler.add(LoginFragment.getInstance())
        } else {
            fragmentHandler.add(HomeFragment.getInstance())
        }

//        if (currentUser == null) {
//
//        } else {
//            fragmentHandler.add(HomeFragment.getInstance())
//        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1)
            super.onBackPressed()
        else
            this.finish()
    }
    //    private val navigationBackPressListener = View.OnClickListener { fragmentManager.popBackStack() }
}
