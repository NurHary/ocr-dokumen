package com.example.ocrdokumen

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.os.Bundle
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.Context
import android.location.LocationManager
import android.provider.Settings
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.example.ocrdokumen.ui.theme.OcrDokumenTheme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.TextButton
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

const val REQUEST_ENABLE_BLUETOOTH: Int = 255091
const val REQUEST_ENABLE_GPS: Int = 255555

class MainActivity : ComponentActivity() {
    var GBluetoothAvailableDevices = mutableStateListOf<BluetoothDevice?>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            OcrDokumenTheme {

                val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
                val locationManager: LocationManager = getSystemService(LocationManager::class.java)

                val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter;
                var isGpsEnabled : Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)


//                var bt_activity_launcher: ActivityResultLauncher<Intent> = registerForActivityResult(
//                    ActivityResultContracts.StartActivityForResult(),
//                    ActivityResultCallback<ActivityResult>(){
//                        @Override
//                        fun onActivityResult(result: ActivityResult){
//
//                        }
//                    }

//                )

                val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                registerReceiver(receiver, filter)

                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }        // Snackbar Setelah melakukan pengambilan foto
                // Untuk Card Foto
                var confirmPhoto = remember { mutableStateOf(false) }
                // Untuk Card Bluetooth
                var wifiConnect = remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->


                    // Main Layout
                    Box (
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {

                        // SnackBar
                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier.align(Alignment.BottomCenter),
                            )
                        Column (modifier = Modifier.align(Alignment.TopCenter)) {
                            // BLUETOOTH
                            Row() {
                                Button(

                                    onClick = {
                                        // Cek apabila Hp Support Bluetooth
                                            // Hp Tidak Kentang
                                        if (bluetoothAdapter != null) {
                                            if (bluetoothAdapter?.isEnabled == false) {
                                                scope.launch { snackbarHostState.showSnackbar("Bluetooth Tidak Diizinkan") }
                                                val enableBtIntent =
                                                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                                // TODO: Minta Izin Bluetooth
                                                startActivityForResult(
                                                    enableBtIntent, // Ignore Errors
                                                    REQUEST_ENABLE_BLUETOOTH
                                                )

                                            } else {
                                                // scope.launch { snackbarHostState.showSnackbar("Bluetooth Diizinkan")
                                                // TODO: Pindahkan layar / timpa dan kasih opsi untuk membenarkan Bluetooth

                                                // Perangkat Yang Telah Disambungkaval
//                                                val requestCode = 1;
//                                                val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
//                                                   putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
//                                                }
//                                                startActivityForResult(discoverableIntent, requestCode)
                                                // ini untuk menjadikan hp ini yang discoverable
                                                if (!isGpsEnabled){
                                                    val mapIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                                    startActivityForResult(mapIntent, REQUEST_ENABLE_GPS)
                                                }
                                                wifiConnect.value = true
                                            }
                                        }

                                    //bluetooth
                                },
                                    shape= RectangleShape,
                                    modifier = Modifier.padding( all = 10.dp)
                                ) {
                                    Text(text = "Connect Perangkat")
                                }
                                when {
                                    wifiConnect.value -> { // TODO: &&
                                        BluetoothConnectToDevice(
                                            onDismissRequest = {wifiConnect.value = false},
                                            onConfirmation = {
                                                wifiConnect.value = false
                                                // NOTES
                                                // Kirim Ke Bluetooth
                                                println("Mengirim Ke Bluetooth")

                                            },
                                            bluetoothAdapter,
                                            GBluetoothAvailableDevices
                                        )
                                    }
                                }
                            }
                            Text(text = "Bluetooth Tidak Tersambung", modifier = Modifier.padding(top = 5.dp)) // TODO: ubah ini ke remember
                        }
                        // // // Kamera
                        Button(
                            onClick = { // button untuk kamera
                                // TODO: Sistem Akses Kamera
                                confirmPhoto.value = true

                        },
                            modifier = Modifier.align(Alignment.Center),
                            shape = CircleShape){
                            Text(text = "Camera")
                        }

                        // DoubleCheck Photo
                        when {
                            confirmPhoto.value -> { // TODO: &&
                                confirmTakePhoto(
                                    onDismissRequest = {confirmPhoto.value = false},
                                    onConfirmation = {
                                        confirmPhoto.value = false
                                        // NOTES
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
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action
            when(action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    GBluetoothAvailableDevices.add(device)
                    val deviceName = device?.name
                    println(deviceName)
                    val deviceHardwareAddress = device?.address // MAC address
                    println(deviceHardwareAddress)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
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

@Composable
fun BluetoothConnectToDevice(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    btadapter: BluetoothAdapter?,
    btdevice: SnapshotStateList<BluetoothDevice?>,
){
//    val pairedDevices: Set<BluetoothDevice>? = btadapter?.bondedDevices
//    pairedDevices?.forEach { device ->
//        val deviceName = device.name
//        val deviceHardwareAddress = device.address // MAC address
//    }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card (
            modifier = Modifier
                .fillMaxWidth()
                .height(750.dp)
                .width(750.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)

        )
        {
            // Pemilihan
            Column (modifier = Modifier
                .padding(15.dp)
                .height(600.dp)
                .width(750.dp)
                .verticalScroll(
                    rememberScrollState()
                ))
            {
                // Jaringan Terkoneksi
                Text(text= "Koneksi Bluetooth Saat Ini   ···················", modifier = Modifier.fillMaxWidth(), color = Color.DarkGray)
                // Jaringan Tidak Terkoneksi
                Text(text= "Koneksi Bluetooth Tersedia   ·················", modifier = Modifier.fillMaxWidth(), color = Color.DarkGray)
                btdevice?.forEach { device -> bluetooth_box(device!!.name, device!!.address, {}) }
                for (i in 1..12){
                    bluetooth_box("Bahlil", "Etanol", {jajal2()})
                }
            }
            Row(modifier = Modifier
                .padding(15.dp)
                .fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Absolute.SpaceEvenly) {
                Button(shape = RoundedCornerShape(4.dp), onClick = {}) { Text("Refresh", fontSize = 12.sp)}
                TextButton(
                    onClick = {
                        // Simpan Foto
                        onDismissRequest()
                    }
                ) {
                    Text("Batal")

                }
                TextButton(
                    onClick = {
                        // Simpan Foto
                        onConfirmation()
                    }
                ) {
                    Text("Selesai")

                }
            }
        }
    }

}

@Composable
fun discover_devices(){}

@Composable
fun bluetooth_box(name: String, address: String, clickFn: () -> Unit
){
    Column (
        // verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .clickable(enabled = true, onClick = clickFn)
            .height(80.dp)
            .fillMaxWidth()
            .padding(20.dp)
    ){
        Row(
            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(text = name, color = Color.Black, fontSize = 10.sp)
            // Spacer(modifier = Modifier.width(60.dp))
            Text(text="Tidak Tersambung", color = Color.Black, fontSize = 10.sp)
        }
        Text(text = address, color = Color.Black, fontSize = 10.sp)
    }
}

fun jajal2(){
    println("Farhan Memek")
}

//@Composable
//fun OpenCamera(){}
//
//@Composable
//fun SendToBluetooth(){}