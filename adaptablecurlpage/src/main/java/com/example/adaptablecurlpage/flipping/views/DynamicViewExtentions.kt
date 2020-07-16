package com.example.adaptablecurlpage.flipping.views

import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import com.example.adaptablecurlpage.flipping.adapter.MultiAdapter
import com.example.adaptablecurlpage.flipping.adapter.SingleAdapter

fun <T> DynamicFlipView.loadSingleLayoutPages(
        @LayoutRes layoutResources: Int,
        dataList: List<T>,
        operation: View.(Int, T) -> Unit
) {
    adapter = SingleAdapter(dataList) { pos, recycledView, parent
        ->
        var v = recycledView
        if (v == null)
            v = LayoutInflater.from(parent!!.context).inflate(layoutResources, parent, false)
        v!!.operation(pos, getItem(pos))
        v
    }
}

fun <T> DynamicFlipView.loadMultiLayoutPages(
        dataList: List<Pair<Int, T>>,
        operation: View.(Int, T, Int) -> Unit
) {
    adapter = MultiAdapter(dataList) { pos, recycledView, parent
        ->
        var v = recycledView
        val viewType = getItemViewType(pos)
        if (v == null)
            v = LayoutInflater.from(parent!!.context).inflate(viewType, parent, false)
        v!!.operation(pos, getItem(pos), viewType)
        v
    }
}

infix fun <A, B> A.with(that: B): Pair<A, B> = Pair(this, that)

