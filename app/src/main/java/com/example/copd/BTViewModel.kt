package com.example.copd

import android.bluetooth.BluetoothSocket
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.Serializable

class BTViewModel : ViewModel() {
    private val _bluetoothSocket = MutableLiveData<BluetoothSocket?>()
    val bluetoothSocket: LiveData<BluetoothSocket?> = _bluetoothSocket

    fun setBluetoothSocket(socket: BluetoothSocket?) {
        _bluetoothSocket.postValue(socket)
    }
}