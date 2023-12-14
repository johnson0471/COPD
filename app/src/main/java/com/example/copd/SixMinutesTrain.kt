package com.example.copd

import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.graphics.PorterDuff
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.copd.Main.Companion.sendUsersData
import com.example.copd.databinding.ActivityMainBinding
import com.example.copd.databinding.ActivitySixMinutesTrainBinding
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Timer
import java.util.TimerTask

class SixMinutesTrain : AppCompatActivity() {

    private lateinit var btViewModel: BTViewModel
    private val TAG = "six"
    private var handlerSixMinutes: Handler? = null
    private var mytimerSixMinutes: Timer? = null
    private var requestQueue: RequestQueue? = null
    private var soundPool: SoundPool? = null
    private var hitOfHigh = 0
    private var hitOfLow = 0
    private var endsound = 0
    private val changeColor = -0xffa6b3
    private var sixMinutes = 360
    private val sixCount = 360
    private var userWeight = 0
    private var FVC = 0.0
    private var FEV1 = 0.0
    private var FVCpred = 0.0
    private var FEV1pred = 0.0
    private var strLog: String? = null
    val sys003 = "003"
    val sys004 = "004"
    private val decimalFormat = DecimalFormat("0.00")
    private lateinit var binding: ActivitySixMinutesTrainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySixMinutesTrainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        `var`()
        setInfo()
        startPress()
        stopPress()
//        sixTrain()
        receiveData()
    }

    private fun `var`() {
        soundPool = SoundPool(2, AudioManager.STREAM_MUSIC, 5)

        //載入音訊
        hitOfHigh = soundPool!!.load(this, R.raw.high, 1)
        hitOfLow = soundPool!!.load(this, R.raw.low, 0)
        endsound = soundPool!!.load(this, R.raw.mp3, 1)
        requestQueue = Volley.newRequestQueue(this@SixMinutesTrain)
    }

    //設定個人資訊
    private fun setInfo() {
        val content = SpannableString(userInfo.userName)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        binding.txvUserName.text = content
        val nowDate = SimpleDateFormat("yyyy/MM/dd-HH:mm").format(Date())
        val content2 = SpannableString(nowDate)
        content2.setSpan(UnderlineSpan(), 0, content2.length, 0)
        binding.txvData.text = content2
    }

    private fun receiveData() {
        try {
            btViewModel = ViewModelProvider(this)[BTViewModel::class.java]
            btViewModel.bluetoothSocket.observe(this) { socket ->
                if (socket != null) {
                    Log.d(TAG, "socket = $socket")
                } else {
                    Log.d(TAG, "socket = null")
                }
            }

//            val bufferedReader = BufferedReader(InputStreamReader(inputStream))

//            // 這裡假設你的數據是一行一行地接收的
//            var line: String?
//            while (true) {
//                line = bufferedReader.readLine()
//
//                // 將讀取到的數據發送給 UI 線程進行更新
//                if (line != null) {
//                    val message = handler.obtainMessage(UPDATE_TEXT_VIEW, line)
//                    handler.sendMessage(message)
//                }
//            }
        } catch (e: IOException) {
            Log.e(TAG, "receiveData", e)
        }
    }

    private val handler = Handler(Looper.getMainLooper()) { msg ->
        when (msg.what) {
            UPDATE_TEXT_VIEW -> {
                // 在這裡更新 UI 中的 TextView
                val data = msg.obj as String
                binding.txvDistance.text = data
                true
            }

            else -> false
        }
    }


    private fun updateUI(data: String) {
        // 在這裡更新UI，例如更新TextView的內容
        binding.txvDistance.text = data
    }


//    private fun receiveData(socket: BluetoothSocket) {
//        try {
//            // 獲得藍牙Socket的輸入流
//            val inputStream = socket.inputStream
//            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
//
//            // 讀取數據
//            val data = bufferedReader.readLine()
//
//            // 在這裡處理接收到的數據
//            // 例如，你可以顯示數據在UI上
//            runOnUiThread {
//                // TODO: 在UI上處理接收到的數據
//            }
//
//        } catch (e: IOException) {
//            // 處理例外情況
//            e.printStackTrace()
//        }
//    }


    private fun startPress() {
        binding.btnSixstart.setOnClickListener {
            if (Main.BTConnected) {
                val view =
                    LayoutInflater.from(this@SixMinutesTrain).inflate(R.layout.dialog_weight, null)
                val alertDialog = AlertDialog.Builder(this@SixMinutesTrain)
                    .setTitle("請輸入你的體重")
                    .setView(view)
                    .setPositiveButton("確定") { _, _ ->
                        val editText = view.findViewById<View>(R.id.edt_weight) as EditText
                        userWeight = editText.text.toString().toInt()
                        Toast.makeText(
                            this@SixMinutesTrain,
                            "userWeight: $userWeight",
                            Toast.LENGTH_SHORT
                        ).show()
                        predCol()
                        if (Main.mConnectedThread != null) { //First check to make sure thread created
                            Main.mConnectedThread!!.write("/") //reset the Arduino and delay 0.5s to wait
                            try {
                                // delay 1 second
                                Thread.sleep(1000)
                                soundPool!!.play(hitOfHigh, 1.0f, 1.0f, 0, 0, 1.0f)

                                //按下後 顏色變深
                                binding.btnSixstart.background.setColorFilter(
                                    changeColor,
                                    PorterDuff.Mode.MULTIPLY
                                )
                                binding.btnSixstart.isEnabled = false
                                binding.btnStop.background.setColorFilter(
                                    changeColor,
                                    PorterDuff.Mode.MULTIPLY
                                )
                                binding.btnStop.isEnabled = false
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                            sendUsersData(Main.mConnectedThread)
                            Main.mConnectedThread!!.write("-")
                        }
                        if (Main.timerUse == true) {
                            Main.mytimer!!.cancel()
                            soundPool!!.pause(hitOfHigh)
                            soundPool!!.pause(hitOfLow)
                            Main.t_past = 0f
                        }
                        mytimerSixMinutes = Timer()
                        mytimerSixMinutes!!.schedule(MyTimerSixMinutesTask(), 0, 1000)
                    }
                    .show()
            } else Toast.makeText(this@SixMinutesTrain, "請先連結藍芽裝置", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun stopPress() {
        binding.btnStop.setOnClickListener {
            init()
            binding.txvDistance.text = ""
            binding.txvStep.text = ""
            if (Main.mConnectedThread != null) Main.mConnectedThread!!.write("/")
            if (Main.timerUse) {
                Main.mytimer!!.cancel()
                soundPool!!.pause(hitOfHigh)
                soundPool!!.pause(hitOfLow)
                Main.t_past = 0f
            }
        }
    }

    private fun init() {
        sixMinutes = sixCount
        mytimerSixMinutes!!.cancel()
        binding.btnSixstart.text = "Start"
        binding.btnSixstart.isEnabled = true
        binding.btnSixstart.background.colorFilter = null
        binding.btnSixback.isEnabled = true
        binding.btnSixback.background.colorFilter = null
    }

//    private fun sixTrain() {
//        handlerSixMinutes = object : Handler() {
//            override fun handleMessage(msg: Message) {
//                when (msg.what) {
//                    1 -> if (sixMinutes > 0) {
//                        sixMinutes -= 1
//                        binding.btnSixstart.text = sixMinutes.toString()
//                        binding.txvDistance.text = decimalFormat.format(Main.sumWalkLength)
//                        binding.txvStep.text = Main._handCountData
//                    } else {
//                        soundPool!!.play(endsound, 1.0f, 1.0f, 0, 0, 1.0f)
//                        val view = LayoutInflater.from(this@SixMinutesTrain)
//                            .inflate(R.layout.dialog_weight, null)
//                        val alertDialog = AlertDialog.Builder(this@SixMinutesTrain)
//                            .setTitle("請問您知道您走了多遠嗎?(公尺)")
//                            .setView(view)
//                            .setPositiveButton("確定") { dialog, which ->
//                                val editText = view.findViewById<View>(R.id.edt_weight) as EditText
//                                Main.sumWalkLength =
//                                    java.lang.Float.valueOf(editText.text.toString()).toDouble()
//                                postCol()
//                            }
//                            .setNegativeButton("不知道") { dialog, which -> postCol() }
//                            .show()
//                        init()
//                        mytimerSixMinutes!!.cancel()
//                    }
//                }
//                super.handleMessage(msg)
//            }
//        }
//    }

    private fun upDataSix(dts: String) {
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
                    strLog =
                        if (json.myMsg == sys003) "上傳成功" else if (json.myMsg == sys004) "上傳失敗" else "Wrong"
                    Toast.makeText(this@SixMinutesTrain, strLog, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener {
                Toast.makeText(
                    this@SixMinutesTrain,
                    "請檢察網路",
                    Toast.LENGTH_LONG
                ).show()
            }) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                //用HashMap將資料透過Post傳到PHP
                val map: MutableMap<String, String> = HashMap()
                map["userName"] = userInfo.Account
                map["Time"] = dts
                map["Walks"] = Main.stepWalkLength.toString()
                map["WalkLength"] = Main.sumWalkLength.toString()
                map["FVC"] = FVC.toString()
                map["FEV1"] = FEV1.toString()
                return map
            }
        }

        //將要求加入隊列
        requestQueue!!.add(stringRequest)
    }

    private inner class MyTimerSixMinutesTask : TimerTask() {
        override fun run() {
            val message = Message()
            message.what = 1
            handlerSixMinutes!!.sendMessage(message)
        }
    }

    private fun predCol() {
        if (userInfo.userGender == "男性") {
            if (userInfo.userAge in 20..24) {
                FVCpred = -6.8865 + 0.0590 * userInfo.userHeight + 0.0739 * userInfo.userAge
                Log.e("FVCpred", "-6.8865+0.0590*userInfo.userHeight+0.0739*userInfo.userAge; ")
                FEV1pred = -6.1181 + 0.0519 * userInfo.userHeight + 0.0636 * userInfo.userAge
                Log.e("FEV1pred", "-6.1181+0.0519*userInfo.userHeight+0.0636*userInfo.userAge; ")
                Log.e("分區", "1" + userInfo.userGender)
            } else {
                FVCpred = -8.7818 + 0.0844 * userInfo.userHeight - 0.0298 * userInfo.userAge
                Log.e("FVCpred", "-8.7818+0.0844*userInfo.userHeight-0.0298*userInfo.userAge; ")
                FEV1pred = -6.5147 + 0.0665 * userInfo.userHeight - 0.0292 * userInfo.userAge
                Log.e("FEV1pred", "-6.5147+0.0665*userInfo.userHeight-0.0292*userInfo.userAge; ")
                Log.e("分區", "2" + userInfo.userGender)
            }
        } else if (userInfo.userGender == "女性") {
            if (userInfo.userAge in 20..69) {
                FVCpred = -3.1947 + 0.0444 * userInfo.userHeight - 0.0169 * userInfo.userAge
                Log.e("FVCpred", "-3.1947+0.0444*userInfo.userHeight-0.0169*userInfo.userAge; ")
                FEV1pred = -1.821 + 0.0332 * userInfo.userHeight - 0.019 * userInfo.userAge
                Log.e("FEV1pred", "-1.821+0.0332*userInfo.userHeight-0.019*userInfo.userAge;")
                Log.e("分區", "3" + userInfo.userGender)
            } else {
                FVCpred = -0.1889 + 0.0313 * userInfo.userHeight - 0.0296 * userInfo.userAge
                Log.e("FVCpred", "-0.1889+0.0313*userInfo.userHeight-0.0296*userInfo.userAge;")
                FEV1pred = 2.6539 + 0.0143 * userInfo.userHeight - 0.0397 * userInfo.userAge
                Log.e("FEV1pred", "2.6539+0.0143*userInfo.userHeight-0.0397*userInfo.userAge; ")
                Log.e("分區", "4" + userInfo.userGender)
            }
        } else {
            Log.e("Error", "Error")
        }
        binding.txvFEV1pred.text = decimalFormat.format(FEV1pred)
        binding.txvFVCpred.text = decimalFormat.format(FVCpred)
    }

    private fun postCol() {
        FVC = -4.249 + 2.255 * Main.stepWalkLength + 0.031 * userInfo.userHeight
        FEV1 = -0.453 + 0.002 * Main.sumWalkLength + 0.020 * userWeight
        //Toast.makeText(this, "1", Toast.LENGTH_SHORT).show();
        binding.txvFVCpost.text = decimalFormat.format(FVC)
        binding.txvFEV1post.text = decimalFormat.format(FEV1)
        val result = FEV1 / FVC
        val result2 = FEV1 / FEV1pred
        val result3 = FVC / FVCpred
        binding.txvResult1.text = decimalFormat.format(result)
        binding.txvResult2.text = decimalFormat.format(result2)
        binding.txvResult3.text = decimalFormat.format(result3)
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val currentTime = Date()
        val dts = sdf.format(currentTime)
        upDataSix(dts)
    }

    companion object {
        const val UPDATE_TEXT_VIEW = 1
    }


//    override fun onDestroy() {
//        super.onDestroy()
//        try {
//            serverSocket.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
}