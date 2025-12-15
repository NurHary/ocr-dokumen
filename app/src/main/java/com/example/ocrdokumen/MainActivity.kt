package com.example.ocrdokumen

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.example.ocrdokumen.ui.theme.OcrDokumenTheme

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable


import okhttp3.Call
import okhttp3.Callback

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.format
import java.io.IOException
import java.util.Locale


sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Photo : Screen("photo")
}

class MainActivity : ComponentActivity() {

    @Composable
    private fun AppNav(){
        val memCustomHttpAddress = remember {mutableStateOf("")}
        // Untuk Card Bluetooth
        val cekApakahTelahTerconnect = remember { mutableStateOf(false) }
        val navController = rememberNavController()

        NavHost (navController = navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) {
                PlacesWeCalledHome(
                    memCustomHttpAddress = memCustomHttpAddress,
                    cekApakahTelahTerconnect = cekApakahTelahTerconnect,
                    cameraPreviewUy = {
                        navController.navigate(Screen.Photo.route)
                    }
                )
            }

            composable(Screen.Photo.route) {
                PlacesWeCalledCamera(
                    pencetBack = {navController.popBackStack()}
                )
            }
        }
    }

    // private lateinit var viewBinding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
//

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppNav()
        }
    }
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            }
        }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        Log.d("TAG", "TAKE PHOTO 1")
        val imageCapture = imageCapture ?: return
        Log.d("TAG", "TAKE PHOTO Pass 1")

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ocr-images")
                Log.d("TAG", "TAKE PHOTO 2")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Log.d("TAG", "TAKE PHOTO Finale")
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    private fun captureVideo() {}

    private fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }


    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // // // UI // // //

    @Composable
    fun PlacesWeCalledHome(memCustomHttpAddress : MutableState<String>, cekApakahTelahTerconnect: MutableState<Boolean>, cameraPreviewUy: () -> Unit){
        val cliently = OkHttpClient()

        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }        // Snackbar Setelah melakukan pengambilan foto
        // Untuk Card Foto
        var confirmPhoto = remember { mutableStateOf(false) }

        // For Camera
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val previewView = remember {
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        }

        OcrDokumenTheme {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->


                // Main Layout
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {

                    // SnackBar
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // BLUETOOTH
                        Row(
                            horizontalArrangement = Arrangement.Absolute.Center, modifier = Modifier
                                .padding(
                                    PaddingValues(horizontal = 20.dp,)
                                )
                                .padding(top = 20.dp)
                        ) {
                            // Notes: url
                            OutlinedTextField(
                                value = memCustomHttpAddress.value,
                                onValueChange = { text -> memCustomHttpAddress.value = text },
                                modifier = Modifier
                                    .width(250.dp)
                                    .horizontalScroll(rememberScrollState())
                            )
                            Button(

                                onClick = {

                                    // guard
                                    if (memCustomHttpAddress.value == "") {
                                        scope.launch { snackbarHostState.showSnackbar("Jangan Mengisi Kosong!") }
                                    } else {
                                        val requstly = Request.Builder()
                                            .url(format("http://%s", memCustomHttpAddress)).build()
                                        cliently.newCall(requstly).enqueue(object : Callback {
                                            override fun onFailure(
                                                call: Call,
                                                e: IOException
                                            ) {
                                                cekApakahTelahTerconnect.value = false
                                                scope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        String.format("%s : Tidak Dapat Respon", e)
                                                    )
                                                }
                                            }

                                            override fun onResponse(
                                                call: Call,
                                                response: Response
                                            ) {
                                                cekApakahTelahTerconnect.value = true
                                                scope.launch { snackbarHostState.showSnackbar("Dapat Respon") }
                                            }
                                        }) // TODO: PINDAH
//                                                wifiConnect.value = true
                                    }


                                },
                                shape = RectangleShape,
                                modifier = Modifier.padding(all = 10.dp)
                            ) {
                                Text(text = "Connect HTTP", fontSize = 12.sp)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.Absolute.Center) {
                            if (cekApakahTelahTerconnect.value) {
                                Text(
                                    text = "Telah Tersambung",
                                    modifier = Modifier.padding(top = 5.dp)
                                ) // TODO: ubah ini ke remember
                            } else {
                                Text(
                                    text = "Belum Tersambung",
                                    modifier = Modifier.padding(top = 5.dp)
                                ) // TODO: ubah ini ke remember
                            }
                        }
                    }
                    // // // Kamera
                    Button(
                        onClick = { // button untuk kamera
                            // TODO: Sistem Akses Kamera
                            if (!cekApakahTelahTerconnect.value) {
                                scope.launch { snackbarHostState.showSnackbar("Jangan Mengisi Kosong!") }
                            } else {
                                // Request camera permissions
                                if (allPermissionsGranted()) {
                                    cameraPreviewUy()
                                } else {
                                    requestPermissions()
                                }
                            }

                        },
                        modifier = Modifier.align(Alignment.Center),
                        shape = CircleShape
                    ) {
                        Text(text = "Camera")
                    }

                    // DoubleCheck Photo

                    when {
                        confirmPhoto.value -> { // TODO: &&
                            confirmTakePhoto(
                                onDismissRequest = { confirmPhoto.value = false },
                                onConfirmation = {
                                    confirmPhoto.value = false
                                    // NOTEScekApakahTelahTerconnect.value
                                    // Kirim Ke Bluetooth
                                    println("Mengirim Ke Bluetooth")
                                }
                            )
                        }
                    }
                    Button(
                        onClick = { cekApakahTelahTerconnect.value = true }, modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(30.dp)
                    ) { Text("Debug Pass") }
                }
            }
        }
    }
    @Composable
    fun PlacesWeCalledCamera(
        pencetBack: () -> Unit
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val previewView = remember {
            PreviewView(context).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        }

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                AndroidView({previewView}, modifier = Modifier.fillMaxSize())
                // Back
                Button(
                    onClick = { pencetBack() },
                    modifier = Modifier
                        .align (Alignment.TopStart )
                        .padding(10.dp),
                    shape = CircleShape
                ) { Text("<-") }

                // Capture
                Button(
                    onClick = {takePhoto()},
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp),
                    shape = CircleShape,
                    colors = ButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.White,
                        disabledContentColor = Color.Black,
                        disabledContainerColor = Color.Black
                    )
                ) {}
            }
            LaunchedEffect(Unit) {
                startCamera(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView
                )
            }
        }


    }

}


@Composable
fun confirmTakePhoto(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card (
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)

            )
        {
            Column (
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                // Isi
                Text(text = "Uji Coba", modifier = Modifier.size(20.dp))

                Row(modifier = Modifier.padding(15.dp)) {
                    TextButton(
                        onClick = {
                            // Simpan Foto
                            onDismissRequest()
                        }
                    ) {
                        Text("Ambil ulang Foto")

                    }
                    TextButton(
                        onClick = {
                            // Simpan Foto
                            onConfirmation()
                        }
                    ) {
                        Text("Simpan Foto")

                    }
                }
            }
        }
    }
}







//@Composable
//fun OpenCamera(){}
//
//@Composable
//fun SendToBluetooth(){}