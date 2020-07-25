package com.example.sample.activities.kotlin

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.appcompat.app.AppCompatActivity
import com.example.adaptablecurlpage.flipping.enums.FlipSpeed
import com.example.adaptablecurlpage.flipping.views.loadMultiLayoutPages
import com.example.adaptablecurlpage.flipping.views.with
import com.example.sample.R
import com.example.sample.utils.ViewUtils
import kotlinx.android.synthetic.main.flip_activity.*
import kotlinx.android.synthetic.main.item1.view.btn
import java.util.*

class MultiLayoutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.flip_activity)
        val dataList = LinkedList<Pair<Int, MutableMap<Int, *>>>()
        dataList.add(R.layout.item1 with mutableMapOf(R.id.tV to "Let's \n Begin!", R.id.img to R.drawable.google_fun))
        dataList.add(R.layout.item2 with mutableMapOf(R.id.tV to "Get Ready!!", R.id.img to R.drawable.dance))
        dataList.add(R.layout.scroll_text_item with mutableMapOf(R.id.tV to getRubbishText()))
        dataList.add(R.layout.item_simple with mutableMapOf(R.id.img to R.drawable.dance))
        for (i in 0..20) {
            val layout = ListOfLayouts().random()
            if (layout == R.layout.horizontal_image_item)
                dataList.add(layout with getMap5())
            else
                dataList.add(layout with getMap4())
        }
        val speed: FlipSpeed = (intent.getSerializableExtra("speed")
                ?: FlipSpeed.NORMAL) as FlipSpeed
        dynamic_flip_view.setFlipSpeed(speed)
        dynamic_flip_view.loadMultiLayoutPages(dataList) { position, data, layout
            ->
            fun click() {
                btn.setOnClickListener {
                    Toast.makeText(context, "Page of position $position", LENGTH_SHORT).show();
                }
            }

            //Function to populate text and images using the ids from the list
            fun loadData(viewId: Int) {
                val view: View = findViewById(viewId) ?: return
                val dataResult = data.getValue(viewId)
                if (view is ImageView) {
                    ViewUtils.loadImageWithGlide(dataResult as Int, view)
                } else if (view is TextView) {
                    view.text = dataResult as String
                }
            }
            when (layout) {
                R.layout.item1,
                R.layout.item2 -> click()//only item1 & 2 has button to click
                R.layout.item_simple,
                R.layout.scroll_text_item,
                R.layout.horizontal_image_item,
                R.layout.grid_item,
                R.layout.scroll_image_item -> {
//                    TODO("Do anything specific " +
//                            "from (item_simple to scroll_image_item")
                }
            }
            //All View Type has image or txt to update
            for (viewId in data.keys)
                loadData(viewId)
        }
    }

    private fun getRubbishText(): String {
        val largeTxt = StringBuilder()
        for (j in 0..50) {
            largeTxt.append("This Page contain rubbish text of index $j")
        }
        return largeTxt.toString()
    }

    private fun listOfImages() = listOf(
            R.drawable.best_quote, R.drawable.old_man_quote
            , R.drawable.bulb_quote, R.drawable.pencil_quote
            , R.drawable.quote1, R.drawable.quote2
            , R.drawable.double_face, R.drawable.quote2
            , R.drawable.quote3, R.drawable.quote4)

    private fun ListOfLayouts() = listOf(
            R.layout.grid_item
            , R.layout.horizontal_image_item
            , R.layout.scroll_image_item)

    private fun getMap4() = mutableMapOf(
            R.id.img1 to listOfImages().random()
            , R.id.img2 to listOfImages().random()
            , R.id.img3 to listOfImages().random()
            , R.id.img4 to listOfImages().random()
    )

    private fun getMap5() = mutableMapOf(
            R.id.img1 to listOfImages().random()
            , R.id.img2 to listOfImages().random()
            , R.id.img3 to listOfImages().random()
            , R.id.img4 to listOfImages().random()
            , R.id.img5 to listOfImages().random()
    )

    companion object {
        const val TXT_KEY = "txtKey"
    }
}