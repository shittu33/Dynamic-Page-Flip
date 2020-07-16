package com.example.sample.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.sample.R
import com.example.sample.activities.java.custom.MultiLayoutActivity
import com.example.sample.activities.java.custom.SingleLayoutActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onMultiLayoutClick(view: View?) {
//        val intent = Intent(this, com.example.sample.activities.java.custom.MultiLayoutActivity::class.java)
//        val intent = Intent(this, com.example.sample.activities.java.simple.MultiLayoutActivity::class.java)
        val intent = Intent(this, com.example.sample.activities.kotlin.MultiLayoutActivity::class.java)
        startActivity(intent)
    }

    fun onSingleLayoutClick(view: View?) {
//        val intent = Intent(this, com.example.sample.activities.java.custom.SingleLayoutActivity::class.java)
//        val intent = Intent(this, com.example.sample.activities.java.simple.SingleLayoutActivity::class.java)
        val intent = Intent(this, com.example.sample.activities.kotlin.SingleLayoutActivity::class.java)
        startActivity(intent)
    }
}