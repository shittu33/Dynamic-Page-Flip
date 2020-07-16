package com.example.adaptablecurlpage.flipping.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

/**
 * Created by Abu Muhsin on 26/09/2018.
 */
class SingleAdapter<T>(
        private val pages_data: List<T>,
        operation: SingleAdapter<T>.(Int, View?, ViewGroup?) -> View
) : BaseAdapter() {
    var mOperation = operation;

    override fun getViewTypeCount(): Int {
        return 1
    }
    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun getCount(): Int {
        return pages_data.size
    }

    override fun getItem(i: Int): T {
        return pages_data[i]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return mOperation(position, convertView, parent)
    }

}