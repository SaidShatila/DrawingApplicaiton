package com.example.drawingappcourseandroid12

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var brushSize: ImageButton? = null
    private var imageButton: ImageButton? = null
    private var imageView: ImageView? = null
    private var imageButtonClear: ImageButton? = null
    private var imageButtonUndo: ImageButton? = null
    private var imageButtonRedo: ImageButton? = null
    private var imageButtonSave: ImageButton? = null
    private var imageButtonShare: ImageButton? = null

    var customProgressBarDialog: Dialog? = null
    private var recyclerView: RecyclerView? = null
    private var colorAdapter: ColorsSelectorAdapter? = null

    var imagePath: String? = null
    private val cameraResultLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

            if (isGranted) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }

        }
    private val galleryResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val data = result.data
                if (data != null) {
                    val uri = data.data
                    imageView?.setImageURI(uri)
                }
            }
        }
    private val cameraAndLocationResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permission = it.key
                val isGranted = it.value

                if (permission == Manifest.permission.CAMERA && isGranted) {
                    Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
                } else if (permission == Manifest.permission.READ_EXTERNAL_STORAGE && isGranted) {
                    val intent = Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    intent.type = "image/*"
                    galleryResultLauncher.launch(intent)
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        drawingView = findViewById(R.id.drawing_view)
        imageButton = findViewById(R.id.ib_image_pick)
        imageView = findViewById(R.id.iv_background)
        imageButtonClear = findViewById(R.id.ib_clear)
        imageButtonUndo = findViewById(R.id.ib_undo)
        imageButtonRedo = findViewById(R.id.ib_redo)
        imageButtonSave = findViewById(R.id.ib_save)
        imageButtonShare = findViewById(R.id.ib_share)
        drawingView?.setSizeForBrush(20.toFloat())

        brushSize = findViewById(R.id.ib_brush)

        brushSize?.setOnClickListener {
            showBrushSizeChooserDialog()
//        customProgressDialog()
        }

        imageButton?.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                    Manifest.permission.CAMERA
                )
            ) {
                shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
            } else {
                cameraAndLocationResultLauncher.launch(
                    arrayOf(
                        Manifest.permission.CAMERA,
                        /* Manifest.permission.ACCESS_FINE_LOCATION,
                         Manifest.permission.ACCESS_COARSE_LOCATION,*/
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                )
                requestStoragePermission()
            }
        }
        imageButtonClear?.setOnClickListener {
            drawingView?.clear()
        }

        imageButtonUndo?.setOnClickListener {
            drawingView?.undo()
        }

        imageButtonRedo?.setOnClickListener {
            drawingView?.redo()
        }

        imageButtonSave?.setOnClickListener {
            if (isReadStorageAllowed()) {
                lifecycleScope.launch {

                    showCustomProgressDialog()

                    val bitmap = withContext(Dispatchers.Default) {
                        getBitmapView()
                    }
                    saveBitmapFile(bitmap)
                }

            }


        }
        imageButtonShare?.setOnClickListener {
            if (isReadStorageAllowed() && imagePath != null) {
                lifecycleScope.launch {
                    imagePath?.let { it1 -> shareImage(it1) }
                }

            } else {
                Toast.makeText(this, "Please save the image first", Toast.LENGTH_SHORT).show()
            }
        }
        setupRecyclerView()


    }


    private fun isReadStorageAllowed(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapView(): Bitmap {
        val bitmap = Bitmap.createBitmap(
            drawingView?.width!!,
            drawingView?.height!!,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        val bgDrawable = drawingView?.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(ContextCompat.getColor(this, R.color.white))
        }
        drawingView?.draw(canvas)
        return bitmap
    }

    private suspend fun saveBitmapFile(bitmap: Bitmap): String {
        var result = ""
        withContext(Dispatchers.IO) {
            if (bitmap != null) {
                try {
                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    val file = File(
                        externalCacheDir?.absoluteFile.toString() + File.separator + "DrawingApp"
                                + System.currentTimeMillis() / 1000 + ".png"
                    )

                    val fileOutputStream = FileOutputStream(file)
                    fileOutputStream.write(bytes.toByteArray())
                    fileOutputStream.close()
                    result = file.absolutePath

                    runOnUiThread {
                        cancelCustomProgressDialog()
                        if (result.isNotEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "File Saved Successfully $result",
                                Toast.LENGTH_SHORT
                            ).show()
                            imagePath = result
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Something went wrong while saving the file.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                } catch (e: Exception) {
                    result = " "
                    e.printStackTrace()
                }
            }

        }
        return result
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            cameraResultLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
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

    private fun alertDialogFunction() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to exit?")
        builder.setPositiveButton("Yes") { dialog, which ->
            finish()
        }
        builder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }


    private fun customDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_custom)
        dialog.findViewById<AppCompatButton>(R.id.btn_positive).setOnClickListener {
            Toast.makeText(this, "Positive", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.findViewById<AppCompatButton>(R.id.btn_negative).setOnClickListener {
            Toast.makeText(this, "Negative", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }



        dialog.show()
    }

    private fun showCustomProgressDialog() {
        customProgressBarDialog = Dialog(this)
        customProgressBarDialog?.setContentView(R.layout.custom_progress_dialog)
        //set to true to cancel dialog on outside touch
        customProgressBarDialog?.setCancelable(false)
        customProgressBarDialog?.show()
    }

    private fun cancelCustomProgressDialog() {
        if (customProgressBarDialog != null) {
            customProgressBarDialog?.dismiss()
            customProgressBarDialog = null
        }
    }

    private fun shareImage(result: String) {
        MediaScannerConnection.scanFile(this, arrayOf(result), null) { path, uri ->
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type = "image/png"
            startActivity(Intent.createChooser(shareIntent, "Share"))
        }
    }
}