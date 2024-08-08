package com.wash.washandroid.presentation.fragment.study

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.wash.washandroid.R

class CardAdapter(private val context: Context, private val data: List<String>) : BaseAdapter() {

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.study_item_card, parent, false)
        val textView: TextView = view.findViewById(R.id.tv_study_card)
        textView.text = data[position]
        return view
    }
}
