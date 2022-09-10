package com.example.drawingappcourseandroid12

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ColorsSelectorAdapter() : RecyclerView.Adapter<ColorsSelectorAdapter.ColorViewHolder>() {

    private var colors = listOf<Int>()

    private var selectedColor = 0

    private var onColorSelected: ((Int) -> Unit)? = null

    fun setColors(colors: List<Int>) {

        this.colors = colors

        notifyDataSetChanged()

    }

    fun setOnColorSelectedListener(onColorSelected: (Int) -> Unit) {

        this.onColorSelected = onColorSelected

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_colors, parent, false)

        return ColorViewHolder(view)

    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {

        holder.bind(colors[position])

    }

    override fun getItemCount(): Int = colors.size

    inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val colorView = itemView.findViewById<View>(R.id.iv_color)

        fun bind(color: Int) {

            colorView.setBackgroundColor(color)

            colorView.isSelected = color == selectedColor

            colorView.setOnClickListener {

                selectedColor = color

                onColorSelected?.invoke(color)

                notifyDataSetChanged()

            }

        }

    }

}