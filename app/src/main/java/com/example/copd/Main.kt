package com.example.copd

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.media.AudioManager
import android.media.SoundPool
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.copd.databinding.ActivityMainBinding
import com.google.gson.Gson
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.DecimalFormat
import java.util.Timer
import java.util.TimerTask
import java.util.UUID


class Main : AppCompatActivity() {

    //    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler()
    private lateinit var btViewModel: BTViewModel
    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    private var mBTArrayAdapter: ArrayAdapter<String>? = null
    private var mDevicesListView: ListView? = null
    private var mHandler: Handler? = null

    // bluetooth background worker thread to send and receive data
    private var mBTSocket: BluetoothSocket? = null

    // used in bluetooth handler to identify message status
    private var _recieveData = ""
    private var _durationData = "0"
    private var _heartRateData = "0"
    private var _buzzerData = "500"
    private var _stageData = "1"
    private var readMessage_cut = false
    private var readMessage_past = ""
    private val changeColor = -0xffa6b3
    private val FVC: Double? = null
    private val FEV1: Double? = null
    var itemCOPD: ItemCOPD? = null

    //SoundPool
    private var soundPool: SoundPool? = null
    private var hitOfHigh = 0
    private var hitOfLow = 0
    private var endsound = 0
    private var section = 0
    private var pp = 0
    private val tempo = 0f
    private var t = 0f

    //内部類 計算節拍的Timer
    internal inner class MyTimerTask : TimerTask() {
        override fun run() {
            val message = Message()
            message.what = 1
            handler!!.sendMessage(message)
        }
    }

    //Six Minutes Training Timer and Handler
    var handlerSixMinutes: Handler? = null
    var mytimerSixMinutes: Timer? = null
    var sixMinutes = 0
    private val decimalFormat = DecimalFormat(".00")


    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate")


        `var`() //init
        toSixMinutesTrain()

        toBreathTrain()
        //確認藍芽權限
        bluetoothPermission()

        //Timer handler message
        pp = 1

        //SQLite item
        itemCOPD = ItemCOPD(applicationContext)

//        if (itemCOPD.getCount() == 0) {
//            itemCOPD.sample();
//        }

        //get all SQLite Data
        items = itemCOPD!!.all
        val it = intent
        val userName = it.getStringExtra("userName")

        //Volly's function
        val requestQueue = Volley.newRequestQueue(this@Main)

        //get user's age,height,gender and compute step walk length
        getUserData(userName, requestQueue)

        // Register for broadcasts when a device is discovered.
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(blReceiver, filter)


        //定義執行緒 當收到不同的指令做對應的內容
//        mHandler = object : Handler() {
//            override fun handleMessage(msg: Message) {
//                if (msg.what == MESSAGE_READ) { //收到MESSAGE_READ 開始接收資料
//                    var readMessage: String? = null
//                    try {
//                        readMessage = kotlin.String(msg.obj as ByteArray, "UTF-8")
//
//                        //擷取有字元部分的message
//                        readMessage = readMessage.substring(0, readMessage.indexOf(0x00.toChar()))
//
//
//                        //復原被切斷的message
//                        if (readMessage_cut) {
//                            Log.e("Main", "readMessage_past: $readMessage_past")
//                            Log.e("Main", "readMessage: $readMessage")
//                            readMessage = readMessage_past + readMessage
//                            Log.e("Main", "new readMessage: $readMessage")
//                            readMessage_cut = false
//                        }
//
////                        readMessage =  readMessage.substring(0,1);
//                        Log.e("Main", readMessage)
//                        _recieveData = ""
//                        if (readMessage.contains("HC") && readMessage.contains("HCEnd") || readMessage.contains(
//                                "DU"
//                            ) && readMessage.contains("DUEnd") || readMessage.contains("HR") && readMessage.contains(
//                                "HREnd"
//                            ) || readMessage.contains("Stage") && readMessage.contains("SEnd") || readMessage.contains(
//                                "buzzer"
//                            ) && readMessage.contains("buzzerEnd")
//                            || readMessage.contains("Start") || readMessage.contains("Stop")
//                        ) {
//                            if (readMessage.contains("HC") && readMessage.contains("HCEnd")) {
//                                _handCountData = readMessage.substring(
//                                    readMessage.indexOf("HC:") + 3,
//                                    readMessage.indexOf("HCEnd")
//                                )
//                            }
//                            if (readMessage.contains("buzzer") && readMessage.contains("buzzerEnd")) {
//                                _buzzerData = readMessage.substring(
//                                    readMessage.indexOf("buzzer:") + 7,
//                                    readMessage.indexOf("buzzerEnd")
//                                )
//                                t = _buzzerData.toFloat()
//                                if (t != t_past) {
//                                    if (timerUse == true) {
//                                        mytimer!!.cancel()
//                                        soundPool!!.pause(hitOfHigh)
//                                        soundPool!!.pause(hitOfLow)
//                                    }
//                                    t_past = t
//                                    section = 2
//                                    timerUse = true
//                                    mytimer = Timer()
//                                    val tempFloat = t
//                                    mytimer!!.schedule(MyTimerTask(), 0, tempFloat.toLong())
//                                }
//                            }
//                            if (readMessage.contains("DU") && readMessage.contains("DUEnd")) {
//                                _durationData = readMessage.substring(
//                                    readMessage.indexOf("DU:") + 3,
//                                    readMessage.indexOf("DUEnd")
//                                )
//                            }
//                            if (readMessage.contains("HR") && readMessage.contains("HREnd")) {
//                                _heartRateData = readMessage.substring(
//                                    readMessage.indexOf("HR:") + 3,
//                                    readMessage.indexOf("HREnd")
//                                )
//                            }
//                            if (readMessage.contains("Stage") && readMessage.contains("SEnd")) {
//                                _stageData = readMessage.substring(
//                                    readMessage.indexOf("Stage:") + 6,
//                                    readMessage.indexOf("SEnd")
//                                )
//                                val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
//                                val currenttime = Date()
//                                val dts = sdf.format(currenttime)
//                                val item =
//                                    Item(0, "Bok", dts, _durationData, _heartRateData, _stageData)
//                                itemCOPD!!.insert(item)
//                                items = itemCOPD!!.all
//                                upData(
//                                    dts,
//                                    userName,
//                                    requestQueue,
//                                    _durationData,
//                                    _heartRateData,
//                                    _stageData
//                                )
//                            }
//                            //                            if (readMessage.contains("Start")){
////                                mStart.setEnabled(false);
////                                mEnd.setEnabled(true);
////                            }
////                            if (readMessage.contains("Stop")){
////                                mEnd.setEnabled(false);
////                                mStart.setEnabled(true);
////                            }
//                        } else {
//                            readMessage_past = readMessage
//                            readMessage_cut = true
//                            Log.e("Main", "Cut!!!   readMessage: $readMessage")
//                        }
//
//
//                        //取得傳過來字串的第一個字元，其餘為雜訊
////                        _recieveData += readMessage; //拼湊每次收到的字元成字串
//                    } catch (e: UnsupportedEncodingException) {
//                        e.printStackTrace()
//                    }
//                    //將收到的字串呈現在畫面上
//                    mHandCount!!.text = _handCountData

//                        sumWalkLength = _handCountData.toInt() * stepWalkLength
//                    } catch (e: Exception) {
//                    }
//                    mWalkLength!!.text = decimalFormat.format(sumWalkLength)
//                    mDuration!!.text = _durationData
//                    mHeatRate!!.text = _heartRateData
//                    mStage!!.text = _stageData
//                }
//                if (msg.what == CONNECTING_STATUS) {
//                    //收到CONNECTING_STATUS 顯示以下訊息
//                    if (msg.arg1 == 1) {
//                        mBluetoothStatus!!.text = ("已連接至裝置: "
//                                + msg.obj as String)
//                        BTconnected = true
//                    } else mBluetoothStatus!!.text = "連接失敗"
//                }
//            }
//        }
//        handler = object : Handler() {
//            override fun handleMessage(msg: Message) {
//                when (msg.what) {
//                    1 -> {
//                        if (pp == 1) {
//                            // play (int soundID, float leftVolume, float rightVolume, int priority, int loop, float rate)
//                            //播放音频，可以对左右音量分别设置，还可以设置优先级，循环次数以及速率
//
////                            Log.e("Main", "play High");
//                            soundPool!!.play(hitOfHigh, 1.0f, 1.0f, 0, 0, 1.0f)
//                        } else {
////                            Log.e("Main", "play Low");
//                            soundPool!!.play(hitOfLow, 1.0f, 1.0f, 0, 0, 1.0f)
//                        }
//                        if (pp != section) {
//                            pp++
//                        } else {
//                            pp = 1
//                        }
//                    }
//                }
//                super.handleMessage(msg)
//            }
//        }
        if (mBTArrayAdapter == null) {
            // Device does not support Bluetooth
            binding.bluetoothStatus.text = "Bluetooth not found"
            Toast.makeText(this, "Bluetooth device not found!", Toast.LENGTH_SHORT)
                .show()
        } else {
            //定義每個按鍵按下後要做的事情
            binding.btnScan.setOnClickListener { bluetoothOn() }
            binding.btnOff.setOnClickListener { bluetoothOff() }
            binding.btnPaired.setOnClickListener { listPairedDevices() }
            binding.btnDiscover.setOnClickListener { discover() }
        }

        //select to rehabilitation page
        binding.btnHistory.setOnClickListener {
            val intent = Intent()
            intent.setClass(this@Main, History::class.java)
            startActivity(intent)
        }


        //Send start general rehabilitation signal
        binding.btnDiwt.setOnClickListener {
            if (BTConnected) {
                resetValues()
                if (mConnectedThread != null) { //First check to make sure thread created
                    mConnectedThread!!.write("/")
                    try {
                        setIsBtn(false)
                        //按下後 顏色變深
                        binding.btnDiwt.background.setColorFilter(
                            changeColor,
                            PorterDuff.Mode.MULTIPLY
                        )

                        // delay 1 second
                        Thread.sleep(1000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    sendUsersData(mConnectedThread)
                    mConnectedThread!!.write(".")
                }
                //傳送將輸入的資料出去
            } else Toast.makeText(this@Main, "請先連結藍芽裝置", Toast.LENGTH_SHORT).show()
        }


        //Send stop rehabilitation signal
        binding.btnEnd.setOnClickListener { stopFunction() }

        //Send Sum the walk length function
        binding.btnMedical.setOnClickListener { makeCall("0923836186") }
        binding.btnDevice.setOnClickListener { makeCall("0929587022") }
        soundPool = SoundPool(2, AudioManager.STREAM_MUSIC, 5)

        //載入音訊
        hitOfHigh = soundPool!!.load(this, R.raw.high, 1)
        hitOfLow = soundPool!!.load(this, R.raw.low, 0)
        endsound = soundPool!!.load(this, R.raw.mp3, 1)
    }


    private fun `var`() {
        //connect Layout's component
        mBTArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        //mBTAdapter = BluetoothAdapter.getDefaultAdapter()
        // get a handle on the bluetooth radio
        mDevicesListView = findViewById<View>(R.id.devicesListView) as ListView
        mDevicesListView?.adapter = mBTArrayAdapter // assign model to view
        mDevicesListView?.onItemClickListener = mDeviceClickListener
    }

    //檢查藍芽權限
    private fun bluetoothPermission() {
        val requestBluetooth =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    //granted
                    Log.d(TAG, "Result = " + result.resultCode.toString())
                } else {
                    showAlertDialog("請開啟藍芽權限")
                }
            }

        val requestMultiplePermissions =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                permissions.entries.forEach {
                    Log.d(TAG, "Key = ${it.key}, Value${it.value}")
                }
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        } else {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)

        }
    }

    //取代startActivityResult()方法
    //註冊callback回來處理的API
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "藍芽已啟用成功 !", Toast.LENGTH_SHORT).show()
                binding.bluetoothStatus.text = "請連結上藍芽裝置"
            } else {
                Toast.makeText(this, "藍芽啟用未成功，請再試一次", Toast.LENGTH_SHORT).show()
                showAlertDialog("請連接藍芽功能")
            }
        }

    private fun bluetoothOn() {
        val bluetoothAdapter =
            (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        //如果藍芽沒開啟
        if (bluetoothAdapter == null) {
            // 檢查設備有無支援藍芽，有則跳else
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_DENIED
            ) {
                Toast.makeText(this, "此設備不支援藍芽", Toast.LENGTH_SHORT).show()
            }
        } else {
            // 檢查藍芽是否已經啟用
            if (!bluetoothAdapter.isEnabled) {
                // 如果藍芽未啟用，則啟動一個 Intent 來要求用戶啟用藍芽
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                Log.e(TAG, "bluetoothAdapter is not Enabled")
                //開啟藍芽確認介面
                resultLauncher.launch(enableBtIntent)
                binding.bluetoothStatus.text = "藍芽正在開啟中..."
            } else {
                // 藍芽已經啟用
                Toast.makeText(
                    this, "藍芽已啟用", Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun showAlertDialog(message: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)

        // 設定對話框的標題
        alertDialogBuilder.setTitle("系統通知")

        // 設定對話框的訊息
        alertDialogBuilder.setMessage(message)

        // 設定按鈕的行為
        alertDialogBuilder.setPositiveButton("確定") { dialog, _ ->
            // 使用者按下"確定"按鈕後的處理
            dialog.dismiss() // 關閉對話框
        }
        // 創建並顯示AlertDialog
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        // 取得Positive Button
        val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)

        // 設定字型大小和顏色
        positiveButton.textSize = 16F // 設定字型大小為18sp
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            positiveButton.setTextColor(
                resources.getColor(
                    R.color.colorPrimary,
                    resources.newTheme()
                )
            )
        }
    }

    private fun bluetoothOff() {
        val bluetoothAdapter =
            (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        } else {
            bluetoothAdapter?.disable() // 關閉藍芽
            mBTArrayAdapter?.clear()    // 將資訊欄清除
            binding.bluetoothStatus.text = "藍芽狀態顯示未連線"
            Toast.makeText(
                applicationContext, "藍芽已關閉",
                Toast.LENGTH_SHORT
            ).show()
            binding.btnPaired.isEnabled = true
        }

    }

    private fun discover() {
        // Check if the device is already discovering
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val bluetoothAdapter =
            (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

        if (bluetoothAdapter?.isDiscovering == true) { //如果已經找到裝置
            bluetoothAdapter.cancelDiscovery() //取消尋找
            Toast.makeText(applicationContext, "Discovery stopped", Toast.LENGTH_SHORT).show()
        } else {
            if (bluetoothAdapter!!.isEnabled) { //如果沒找到裝置且已按下尋找
                mBTArrayAdapter!!.clear() // clear items
                bluetoothAdapter.startDiscovery() //開始尋找
                Toast.makeText(
                    applicationContext, "Discovery started",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                Toast.makeText(
                    applicationContext, "Bluetooth not on",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    //建立一個BroadcastReceiver 來接收藍芽裝置資訊
    private val blReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address
                    Log.d(TAG, "deviceName:$device?.name,deviceHardwareAddress:$device?.address")

                    // add the name to the list
                    mBTArrayAdapter?.add(
                        """
                                    $deviceName
                                    $deviceHardwareAddress
                                    """.trimIndent()
                    )
                    mBTArrayAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    //列出已連接的裝置和MAC位址
    private fun listPairedDevices() {
        if (bluetoothAdapter?.isEnabled == true) {
            // put it's one to the adapter
            mBTArrayAdapter?.clear() // clear items
            val pairedDevice: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
            if (pairedDevice != null) {
                for (device in pairedDevice)
                    mBTArrayAdapter!!.add(device.name + "\n" + device.address)

                Toast.makeText(
                    applicationContext, "顯示已配對的裝置",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else Toast.makeText(
            applicationContext, "藍芽未開啟",
            Toast.LENGTH_SHORT
        ).show()
    }


    private val mDeviceClickListener = OnItemClickListener { _, view, _, _ ->
        val bluetoothAdapter =
            (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        if (!bluetoothAdapter!!.isEnabled) {
            Toast.makeText(
                baseContext, "藍芽未開啟",
                Toast.LENGTH_SHORT
            ).show()
            return@OnItemClickListener
        } else {
            binding.bluetoothStatus.text = "連接中..."
            // Get the device MAC address, which is the last 17 chars in the View
            val info = (view as TextView).text.toString()
            val address = info.substring(info.length - 17)
            val name = info.substring(0, info.length - 17)
            Log.d(TAG, "name: $name,address: $address")

            // Spawn a new thread to avoid blocking the GUI one
            object : Thread() {
                @SuppressLint("MissingPermission")
                override fun run() {
                    var fail = false
                    //取得裝置MAC找到連接的藍芽裝置
                    val device = bluetoothAdapter.getRemoteDevice(address)
                    try {
                        mBTSocket = createBluetoothSocket(device)
                        Log.d(TAG, "device = $device")
                        //建立藍芽socket
                    } catch (e: IOException) {
                        Log.e(TAG, "Socket's create() method failed", e)
                        fail = true
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket?.connect() //建立藍芽連線

                        btViewModel = ViewModelProvider(this@Main)[BTViewModel::class.java]
                        btViewModel.setBluetoothSocket(mBTSocket)

                        Log.d(TAG, mBTSocket.toString())
                        Log.d(TAG, "藍芽連線完成")
                        binding.bluetoothStatus.text = "藍芽已連線"
                        BTConnected = true
                        //連接完成，藍芽列表就被清除
                        handler.postDelayed({
                            mBTArrayAdapter?.clear()
                            Toast.makeText(
                                this@Main, "藍芽連線完成",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.btnPaired.isEnabled = false
                        }, 1000)
                    } catch (e: IOException) {
                        try {
                            fail = true
                            mBTSocket?.close() //關閉socket
                            //開啟執行緒 顯示訊息
                            mHandler?.obtainMessage(CONNECTING_STATUS, 1, -1)
                                ?.sendToTarget()
                            Log.e(TAG, "Could not connect the client socket", e)
                            val list = device.uuids
                            Log.d(TAG, list.toString())
                            handler.post {
                                Toast.makeText(
                                    this@Main, "請檢查是否已開啟六分鐘APP",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            binding.bluetoothStatus.text = "藍芽連線失敗，請再試一次"
                        } catch (e2: IOException) {
                            Log.e(TAG, "Could not close the client socket", e2)
                        }
                    }
                    if (!fail) {
                        //開啟執行緒用於傳輸及接收資料
                        mConnectedThread = ConnectedThread(mBTSocket)
                        mConnectedThread!!.start()
                        //開啟新執行緒顯示連接裝置名稱
                        mHandler?.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                            ?.sendToTarget()
                        Log.d(TAG, "連接位置: $device")

                        mHandler = object : Handler(Looper.getMainLooper()) {
                            override fun handleMessage(msg: Message) {
                                when (msg.what) {
                                    MESSAGE_READ -> {
                                        val bytes = msg.arg1
                                        val buffer = msg.obj as ByteArray
                                        val data = String(buffer, 0, bytes)
                                        val formattedData = data.replace("(\\d)(?=(\\d{3})+\$)".toRegex(), "$1,")
                                        val dataList = formattedData.split(",")
                                        Log.d(TAG, dataList.toString())
                                        try {
                                            processReceivedData(dataList)
                                        } catch (e: IOException) {
                                            Log.e(TAG, "received is fail", e)
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }.start()
        }
    }

    //透過確認裝置資訊來回傳UUID，建立藍芽通道
    @Throws(IOException::class)
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID)
    }

//    private fun receiveData() {
//        try {
//            val mmInputStream = mBTSocket!!.inputStream
//            val mmBuffer = ByteArray(1024)
//            val bytesRead: Int = mmInputStream.read(mmBuffer)
//            val incomingMessage = String(mmBuffer, 0, bytesRead)
//
//            processReceivedData(incomingMessage)
//        } catch (e: IOException) {
//            Log.e(TAG, "processReceivedData", e)
//        }
//    }

    private fun processReceivedData(dataList: List<String>) {
        binding.handCount.text = dataList[0]
        binding.walkLength.text = dataList[1]
    }

//    private val mDeviceClickListener = OnItemClickListener { _, view, _, _ ->
//        val bluetoothAdapter =
//            (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
//        if (!bluetoothAdapter!!.isEnabled) {
//            Toast.makeText(
//                baseContext, "藍芽未開啟",
//                Toast.LENGTH_SHORT
//            ).show()
//            return@OnItemClickListener
//        } else {
//            binding.bluetoothStatus.text = "連接中..."
//            // Get the device MAC address, which is the last 17 chars in the View
//            val info = (view as TextView).text.toString()
//            val address = info.substring(info.length - 17)
//            val name = info.substring(0, info.length - 17)
//            Log.d(TAG, "name: $name,address: $address")
//
//            // Spawn a new thread to avoid blocking the GUI one
//            object : Thread() {
//                override fun run() {
//                    var fail = false
//                    //取得裝置MAC找到連接的藍芽裝置
//                    val device = bluetoothAdapter.getRemoteDevice(address)
//                    try {
//                        mBTSocket = createBluetoothSocket(device)
//                        Log.d(TAG, "device = $device")
//                        //建立藍芽socket
//                    } catch (e: IOException) {
//                        Log.e(TAG, "Socket's create() method failed", e)
//                        fail = true
//                        Toast.makeText(
//                            baseContext, "Socket creation failed",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                    // Establish the Bluetooth socket connection.
//                    try {
//                        mBTSocket?.connect() //建立藍芽連線
//                    } catch (e: IOException) {
//                        try {
//                            Log.e(TAG, "Could connect the client socket", e)
//                            fail = true
//                            mBTSocket!!.close() //關閉socket
//                            //開啟執行緒 顯示訊息
//                            mHandler?.obtainMessage(CONNECTING_STATUS, 1, -1)
//                                ?.sendToTarget()
//                        } catch (e2: IOException) {
//                            Log.e(TAG, "Could not close the client socket", e2)
//                            //insert code to deal with this
//                            Toast.makeText(
//                                baseContext, "Socket creation failed",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//                    if (!fail) {
//                        //開啟執行緒用於傳輸及接收資料
//                        mConnectedThread = ConnectedThread(mBTSocket)
//                        mConnectedThread!!.start()
//                        //開啟新執行緒顯示連接裝置名稱
//                        mHandler?.obtainMessage(CONNECTING_STATUS, 1, -1, name)
//                            ?.sendToTarget()
//                        Log.d(TAG, "fail = $device")
//                    }
//                }
//            }.start()
//        }
//    }

//    private val mDeviceClickListener = OnItemClickListener { _, view, _, _ ->
//        val bluetoothAdapter = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
//        if (!bluetoothAdapter!!.isEnabled) {
//            Toast.makeText(
//                baseContext, "藍芽未開啟",
//                Toast.LENGTH_SHORT
//            ).show()
//            return@OnItemClickListener
//        }
//        binding.bluetoothStatus.text = "連接中..."
//        // Get the device MAC address, which is the last 17 chars in the View
//        val info = (view as TextView).text.toString()
//        Log.d(TAG,info.length.toString())
//        val address = info.substring(info.length - 17)
//        val name = info.substring(0, info.length - 17)
//        Log.d(TAG, "name: $name,address: $address")
//
//        // Spawn a new thread to avoid blocking the GUI one
//        object : Thread() {
//            override fun run() {
//                var fail = false
//                //取得裝置MAC找到連接的藍芽裝置
//                val device = bluetoothAdapter.getRemoteDevice(address)
//                try {
//                    mBTSocket = createBluetoothSocket(device)
//                    Log.d(TAG,"device = $device")
//                    //建立藍芽socket
//                } catch (e: IOException) {
//                    fail = true
//                    Toast.makeText(
//                        baseContext, "Socket creation failed",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                // Establish the Bluetooth socket connection.
//                try {
//                    mBTSocket?.connect() //建立藍芽連線
//                } catch (e: IOException) {
//                    try {
//                        Log.d(TAG, e.toString())
//                        fail = true
//                        mBTSocket!!.close() //關閉socket
//                        //開啟執行緒 顯示訊息
//                        mHandler?.obtainMessage(CONNECTING_STATUS, -1, -1)
//                            ?.sendToTarget()
//                    } catch (e2: IOException) {
//                        //insert code to deal with this
//                        Toast.makeText(
//                            baseContext, "Socket creation failed",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//                if (!fail) {
//                    //開啟執行緒用於傳輸及接收資料
//                    mConnectedThread = ConnectedThread(mBTSocket)
//                    mConnectedThread!!.start()
//                    //開啟新執行緒顯示連接裝置名稱
//                    mHandler?.obtainMessage(CONNECTING_STATUS, 1, -1, name)
//                        ?.sendToTarget()
//                }
//            }
//        }.start()
//    }


    private inner class MyTimerSixMinutesTask : TimerTask() {
        override fun run() {
            val message = Message()
            message.what = 1
            handlerSixMinutes!!.sendMessage(message)
        }
    }

    inner class ConnectedThread(private val mmSocket: BluetoothSocket?) : Thread() {
        private val mmInStream: InputStream? = mmSocket?.inputStream
        private val mmOutStream: OutputStream? = mmSocket?.outputStream

        override fun run() {
            var bytes: Int // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    val buffer = ByteArray(1024) // buffer store for the stream
                    bytes = mmInStream!!.available()
                    if (bytes != 0) {
                        SystemClock.sleep(200)
                        //pause and wait for rest of data
                        bytes = mmInStream.available()
                        // how many bytes are ready to be read?
                        bytes = mmInStream.read(buffer, 0, bytes)
                        // record how many bytes we actually read
                        mHandler!!.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget() // Send the obtained bytes to the UI activity
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "ConnectedThread is failed", e)
                    break
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        fun write(input: String) {
            val bytes = input.toByteArray() //converts entered String into bytes
            try {
                mmOutStream!!.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)
            }
        }

        /* Call this from the main activity to shutdown the connection */
        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }

//    inner class ConnectedThread(private val mmSocket: BluetoothSocket?) : Thread() {
//        private val mmInStream: InputStream?
//        private val mmOutStream: OutputStream?
//
//        init {
//            var tmpIn: InputStream? = null
//            var tmpOut: OutputStream? = null
//
//            // Get the input and output streams, using temp objects because
//            // member streams are final
//            try {
//                tmpIn = mmSocket!!.inputStream
//                tmpOut = mmSocket.outputStream
//            } catch (e: IOException) {
//
//            }
//            mmInStream = tmpIn
//            mmOutStream = tmpOut
//        }
//
//        override fun run() {
////            byte[] buffer = new byte[1024];  // buffer store for the stream
//            var bytes: Int // bytes returned from read()
//            // Keep listening to the InputStream until an exception occurs
//            while (true) {
//                try {
//                    // Read from the InputStream
//                    val buffer = ByteArray(256) // buffer store for the stream
//                    bytes = mmInStream!!.available()
//                    if (bytes != 0) {
//                        SystemClock.sleep(200)
//                        //pause and wait for rest of data
//                        bytes = mmInStream.available()
//                        // how many bytes are ready to be read?
//                        bytes = mmInStream.read(buffer, 0, bytes)
//                        // record how many bytes we actually read
//                        mHandler!!.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
//                            .sendToTarget() // Send the obtained bytes to the UI activity
//                    }
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                    break
//                }
//            }
//        }
//
//        /* Call this from the main activity to send data to the remote device */
//        fun write(input: String) {
//            val bytes = input.toByteArray() //converts entered String into bytes
//            try {
//                mmOutStream!!.write(bytes)
//            } catch (e: IOException) {
//                Log.e(TAG, "Error occurred when sending data", e)
//            }
//        }
//
//        /* Call this from the main activity to shutdown the connection */
//        fun cancel() {
//            try {
//                mmSocket!!.close()
//            } catch (e: IOException) {
//                Log.e(TAG, "Could not close the connect socket", e)
//            }
//        }
//    }


    //將復健的資料上傳於雲端
    fun upData(
        dts: String,
        userName: String?,
        requestQueue: RequestQueue,
        durationData: String,
        heartRateData: String,
        stageData: String
    ) {
        //用Volley內的StringRequest來跟PHP進行溝通
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,
            url,
            Response.Listener { response ->
                try {
                    Log.e("rehabilitation", "response: $response")

                    //透過Gson來解析JsonObject物件
                    val gson = Gson()
                    val json = gson.fromJson(response, MyJson::class.java)
                    if (json.myMsg == sys003) strLog =
                        "上傳成功" else if (json.myMsg == sys004) strLog = "上傳失敗" else strLog =
                        "Wrong"
                    Toast.makeText(this@Main, strLog, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this@Main, "請檢察網路", Toast.LENGTH_LONG).show()
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                //用HashMap將資料透過Post傳到PHP
                val map: MutableMap<String, String> = HashMap()
                map["userName"] = userName!!
                map["Time"] = dts
                map["Duration"] = durationData
                map["HeartRate"] = heartRateData
                map["Stage"] = stageData
                return map
            }
        }

        //將要求加入隊列
        requestQueue.add(stringRequest)
    }

    //將six的資料上傳於雲端
    fun upDataSix(
        dts: String,
        userName: String,
        requestQueue: RequestQueue,
        Walks: String,
        WalkLength: String,
        FVC: String,
        FEV1: String
    ) {
        //用Volley內的StringRequest來跟PHP進行溝通
        val sixUrl = classURL.ApplicationURL + "six.php"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,
            sixUrl,
            Response.Listener { response ->
                try {
                    Log.e("rehabilitation", "response: $response")

                    //透過Gson來解析JsonObject物件
                    val gson = Gson()
                    val json = gson.fromJson(response, MyJson::class.java)
                    if (json.myMsg == sys003) strLog =
                        "上傳成功" else if (json.myMsg == sys004) strLog = "上傳失敗" else strLog =
                        "Wrong"
                    Toast.makeText(this@Main, strLog, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                Toast.makeText(this@Main, "請檢察網路", Toast.LENGTH_LONG).show()
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                //用HashMap將資料透過Post傳到PHP
                val map: MutableMap<String, String> = HashMap()
                map["userName"] = userName
                map["Time"] = dts
                map["Walks"] = Walks
                map["WalkLength"] = WalkLength
                map["FVC"] = FVC
                map["FEV1"] = FEV1
                return map
            }
        }

        //將要求加入隊列
        requestQueue.add(stringRequest)
    }

    //透過帳號將身高、年齡、性別 抓回來
    private fun getUserData(userName: String?, requestQueue: RequestQueue) {
        val urlGetUserData = classURL.ApplicationURL + "personal.php"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST,
            urlGetUserData,
            Response.Listener { response ->
                try {
                    val gson = Gson()
                    val json = gson.fromJson(response, MyJson::class.java)
                    userAge = json.myAge
                    userHeight = json.myHeight
                    userGender = json.myGender
                    stepWalkLength = computeWalkLength()
                    Log.d(
                        TAG,
                        "userAge: $userAge userHeight: $userHeight userGender: $userGender"
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(
                    this@Main,
                    error.message,
                    Toast.LENGTH_LONG
                ).show()
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                //用HashMap来存储请求参数
                val map: MutableMap<String, String> = HashMap()
                map["user"] = userName!!
                return map
            }
        }
        requestQueue.add(stringRequest)
    }

    private fun resetValues() {
        binding.handCount.text = "0" //將收到的字串呈現在畫面上
        binding.duration.text = "0"
        binding.heartRate.text = "0"
        binding.stage.text = "1"
        _handCountData = "0"
        _durationData = "0"
        _heartRateData = "0"
        _stageData = "1"
    }

    //透過演算法得知使用者的步數
    fun computeWalkLength(): Double {
        val intUserAge = userAge.toInt()
        val intUserHeight = userHeight.toInt()
        val m =
            if (userGender == "男性") {
                if (intUserAge in 25..99) {
                    0.289 + 0.153 * (-6.5147 + 0.0665 * intUserHeight + 0.0292 * intUserAge)
                } else {
                    0.289 + 0.153 * (-6.1181 + 0.0519 * intUserHeight + 0.0636 * intUserAge)
                }
            } else {
                if (intUserAge in 70..98) {
                    0.289 + 0.153 * (2.6539 + 0.0143 * intUserHeight - 0.0397 * intUserAge)
                } else {
                    0.289 + 0.153 * (-1.8210 + 0.0332 * intUserHeight - 0.0190 * intUserAge)
                }
            }
        return m
    }

    private fun setIsBtn(b: Boolean) {
        binding.btnSixMinutesTrain.isEnabled = b
        binding.btnDiwt.isEnabled = b
        binding.btnBreath.isEnabled = b
        binding.btnHistory.isEnabled = b
    }

    private fun initBtnColor() {
        binding.btnDiwt.background.colorFilter = null
        binding.btnBreath.background.colorFilter = null
        binding.btnSixMinutesTrain.background.colorFilter = null
    }

    private fun initSix() {
        if (sixMinutes != 0) {
            sixMinutes = 0
            mytimerSixMinutes!!.cancel()
            binding.btnSixMinutesTrain.text = "six"
        }
    }

    private fun stopFunction() {
        setIsBtn(true)
        initBtnColor()
        initSix()
        if (mConnectedThread != null) mConnectedThread!!.write("/")
        if (timerUse == true) {
            mytimer!!.cancel()
            soundPool!!.pause(hitOfHigh)
            soundPool!!.pause(hitOfLow)
            t_past = 0f
        }
    }

    private fun makeCall(number: String) {
        val phoneIntent = Intent(Intent.ACTION_CALL)
        phoneIntent.data = Uri.parse("tel:$number")
        if (ContextCompat.checkSelfPermission(
                this@Main,
                Manifest.permission.CALL_PHONE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@Main, arrayOf(Manifest.permission.CALL_PHONE),
                MY_PERMISSIONS_REQUEST_CALL_PHONE
            )

            // MY_PERMISSIONS_REQUEST_CALL_PHONE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            //You already have permission
            try {
                startActivity(phoneIntent)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    private fun toSixMinutesTrain() {
        binding.btnSixMinutesTrain.setOnClickListener {
            if (BTConnected) {
                val toSix = Intent()
                toSix.setClass(this@Main, SixMinutesTrain::class.java)
                startActivity(toSix)
            } else {
                Toast.makeText(this@Main, "請先連結藍芽裝置", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun toBreathTrain() {
        binding.btnBreath.setOnClickListener {
            val toBreath = Intent()
            toBreath.setClass(this@Main, BreathTrain::class.java)
            startActivity(toBreath)
            /*
                     if (BTconnected){
                         resetValues();
                         if(mConnectedThread != null) { //First check to make sure thread created
                             mConnectedThread.write("/"); //reset the Arduino and delay 0.5s to wait
                             try{
                                 // delay 1 second
                                 setIsBtn(false);
                                 //按下後 顏色變深
                                 breathBtn.getBackground().setColorFilter(changeColor,android.graphics.PorterDuff.Mode.MULTIPLY);
                                 Thread.sleep(1000);
    
                             } catch(InterruptedException e){
                                 e.printStackTrace();
    
                             }
    
                             sendUsersData(mConnectedThread);
                             mConnectedThread.write("-");
                         }
    
                         if (timerUse == true){
                             mytimer.cancel();
                             soundPool.pause(hitOfHigh);
                             soundPool.pause(hitOfLow);
                             t_past = 0;
                         }
                     }else
                         Toast.makeText(Main.this, "請先連結藍芽裝置", Toast.LENGTH_SHORT).show();*/
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart");
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause");
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume");
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(blReceiver)
        Log.d(TAG, "onDestroy");
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_CALL_PHONE = 0
        const val sys003 = "003"
        const val sys004 = "004"
        var strLog: String? = null
        val url = classURL.ApplicationURL + "rehabilitation.php"

        // Our main handler that will receive callback notifications
        @JvmField
        var mConnectedThread: ConnectedThread? = null

        // bi-directional client-to-client data path
        private val BTMODULEUUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // "random" unique identifier

        // #defines for identifying shared types between calling functions
        private const val REQUEST_ENABLE_BT = 1

        private const val BLUETOOTH_PERMISSION_REQUEST_CODE = 2 // 用於請求藍芽權限的請求碼

        // used to identify adding bluetooth names
        private const val MESSAGE_READ = 2

        // used in bluetooth handler to identify message update
        private const val CONNECTING_STATUS = 3

        @JvmField
        var _handCountData = "0"
        const val TAG = "Main"

        // 儲存所有記事本的List物件
        @JvmField
        var items: List<Item>? = null

        @JvmField
        var t_past = 0f

        //提醒音Timer的Handler
        var handler: Handler? = null

        @JvmField
        var mytimer: Timer? = null

        @JvmField
        var timerUse = false

        //else parameters
        var userAge = ""
        var userHeight = ""
        var userGender = "" //user's data

        @JvmField
        var stepWalkLength = 0.0

        @JvmField
        var sumWalkLength = 0.0
        private const val userWeight = 0

        @JvmField
        var BTConnected = false

        @JvmStatic
        fun sendUsersData(c: ConnectedThread?) {
            c!!.write(" ")
            c.write(userAge)
            c.write(" ")
            c.write(userHeight)
            c.write(" ")
            if (userGender == "男性") {
                c.write("1")
            } else {
                c.write("0")
            }
        }
    }
}