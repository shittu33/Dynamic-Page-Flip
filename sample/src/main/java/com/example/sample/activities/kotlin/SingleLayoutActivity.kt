package com.example.sample.activities.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.adaptablecurlpage.flipping.views.loadSingleLayoutPages
import com.example.sample.R
import kotlinx.android.synthetic.main.flip_activity.*
import kotlinx.android.synthetic.main.item1.view.tV
import java.util.*

class SingleLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flip_activity)
        val pagesData = LinkedList<String>()
        for (i in 1..15) {
            val largeTxt = StringBuilder()
            for (j in 0..50) {
                largeTxt.append("This is Page $i of rubbish text $j")
            }
            pagesData.add(largeTxt.toString())
        }
        dynamic_flip_view.loadSingleLayoutPages(R.layout.scroll_text_item, pagesData) { position, data
            ->
            tV.setText(data)
        }
    }
}