package com.example.drawingappcourseandroid12

import android.app.Dialog
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var brushSize: ImageButton? = null

    private var recyclerView: RecyclerView? = null
    private var colorAdapter: ColorsSelectorAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        drawingView = findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(20.toFloat())

        brushSize = findViewById(R.id.ib_brush)

        brushSize?.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        setupRecyclerView()


    }


    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.rv_colors)
        colorAdapter = ColorsSelectorAdapter()
        recyclerView?.adapter = colorAdapter

        recyclerView?.setHasFixedSize(true)
        val colors = resources.getIntArray(R.array.colors)
        colorAdapter?.setColors(colors.toList())

        colorAdapter?.setOnColorSelectedListener {
            drawingView?.setColor(it)
        }

    }

    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")
        val smallBtn = brushDialog.findViewById(R.id.ib_small_brush) as ImageButton
        val mediumBtn = brushDialog.findViewById(R.id.ib_medium_brush) as ImageButton
        val largeBtn = brushDialog.findViewById(R.id.ib_large_brush) as ImageButton
        smallBtn.setOnClickListener {
            drawingView?.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }
        mediumBtn.setOnClickListener {
            drawingView?.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        }
        largeBtn.setOnClickListener {
            drawingView?.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

}