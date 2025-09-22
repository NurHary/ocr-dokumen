package com.example.ocrdokumen

import android.graphics.drawable.shapes.Shape
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import com.example.ocrdokumen.ui.theme.OcrDokumenTheme
import org.w3c.dom.Text


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OcrDokumenTheme {
                var currentBluetoothConnection = remember { mutableStateOf("") }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    Box (
                        modifier = Modifier.padding(innerPadding).fillMaxSize()
                    ) {
                        Column (modifier = Modifier.align(Alignment.TopCenter)) {
                            Row() {
                                // ini adalah tombol setting bluetooth
                                OutlinedTextField(value = currentBluetoothConnection.value,
                                    onValueChange = { text -> currentBluetoothConnection.value = text}
                                )

                                Button(
                                    onClick = {
                                    //bluetooth
                                },
                                    shape= RectangleShape,
                                    modifier = Modifier.padding( all = 10.dp)
                                ) {
                                    Text(text = "Connect")
                                }
                            }
                            Text(text = "Bluetooth Tidak Tersambung", modifier = Modifier.padding(top = 5.dp))
                            Row() {
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
                        },
                            modifier = Modifier.align(Alignment.Center),
                            shape = CircleShape){
                            Text(text = "cobak jajal pencetten iki")
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


fun button_click_one(){

}


//@Composable
//fun OpenCamera(){}
//
//@Composable
//fun SendToBluetooth(){}