package com.example.adaptablecurlpage.flipping.adapter

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.annotation.LayoutRes

/**
 * Created by Abu Muhsin on 26/09/2018.
 */
class MultiAdapter<T>(
        private val pages_data: List<Pair<Int, T>>,
        operation: MultiAdapter<T>.(Int, View?, ViewGroup?) -> View
) : BaseAdapter() {
    val mOperation = operation
    private val typeList = ArrayList<@androidx.annotation.LayoutRes Int>()

    override fun getViewTypeCount(): Int {
        for ((i, value) in pages_data.withIndex()) {
            val type = value.first
            if (typeList.isEmpty() || !typeList.contains(type)) {
                typeList.add(type)
            }
        }
        Log.e("MultiAdapter", "this view has ${typeList.size} types")
        return typeList.size;
    }

    override fun getItemViewType(position: Int): Int {
        return pages_data[position].first
    }

    override fun getCount(): Int {
        return pages_data.size
    }

    override fun getItem(i: Int): T {
        return pages_data[i].second
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return mOperation(position, convertView, parent)
    }

}