package com.example.ocrdokumen

import android.app.Dialog
import android.graphics.drawable.shapes.Shape
import android.icu.text.CaseMap
import android.os.Bundle
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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Snackbar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.example.ocrdokumen.ui.theme.OcrDokumenTheme
import org.w3c.dom.Text

import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            OcrDokumenTheme {

                var currentBluetoothConnection = remember { mutableStateOf("") }
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                var confirmPhoto = remember { mutableStateOf(false) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->


                    // Main Layout
                    Box (
                        modifier = Modifier.padding(innerPadding).fillMaxSize()
                    ) {
                        // SnackBar
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.align(Alignment.BottomCenter),
                            )
                        Column (modifier = Modifier.align(Alignment.TopCenter)) {
                            Row() {
                                // ini adalah tombol setting bluetooth
                                OutlinedTextField(value = currentBluetoothConnection.value,
                                    onValueChange = { text -> currentBluetoothConnection.value = text}
                                )

                                Button(
                                    onClick = {
                                    //bluetooth
                                        scope.launch { snackbarHostState.showSnackbar("Bluetooth") }
                                },
                                    shape= RectangleShape,
                                    modifier = Modifier.padding( all = 10.dp)
                                ) {
                                    Text(text = "Connect")
                                }
                            }
                            Text(text = "Bluetooth Tidak Tersambung", modifier = Modifier.padding(top = 5.dp))
                            Row( modifier = Modifier.padding(all = 10.dp)) {
                                Button(
                                    shape = RectangleShape,
                                    onClick = {
                                        // mencoba Pairing lagi
                                    })
                                {
                                    Text(text = "Pair Ulang")
                                }
                                Button(
                                    shape = RectangleShape,
                                    onClick = {
                                        // Refresh Koneksi
                                    }
                                ) {
                                    Text(text = "Refresh")
                                }
                            }
                        }
                        // // // Kamera
                        Button(
                            onClick = { // button untuk kamera
                                confirmPhoto.value = true

                        },
                            modifier = Modifier.align(Alignment.Center),
                            shape = CircleShape){
                            Text(text = "Camera")
                        }

                        // DoubleCheck Photo
                        when {
                            confirmPhoto.value -> {
                                confirmTakePhoto(
                                    onDismissRequest = {confirmPhoto.value = false},
                                    onConfirmation = {
                                        confirmPhoto.value = false
                                        // Kirim Ke Bluetooth
                                        println("Mengirim Ke Bluetooth")

                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonUi(modifier: Modifier = Modifier) {

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