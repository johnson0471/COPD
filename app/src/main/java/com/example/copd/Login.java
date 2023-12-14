package com.example.copd;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    Button loginBtn, regBtn;
    EditText edt_userName, edt_password;

    String strLog,url;
    static RequestQueue requestQueue;
    static final String sys001 = "001", sys002 = "002";
    SharedPreferences setting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activate_login);
        var();
        //取得先前紀錄的資料
        setting = getSharedPreferences("Login", MODE_PRIVATE);
        edt_userName.setText(setting.getString("PREF_USERID", ""));
        edt_password.setText(setting.getString("PREF_USERPWD", ""));
        if (!edt_userName.getText().toString().equals(""))
            Login_button();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Login_button();
            }
        });

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toReg = new Intent();
                toReg.setClass(Login.this, Register.class);
                startActivity(toReg);

            }
        });

    }

    private void var(){
        requestQueue = Volley.newRequestQueue(Login.this);
        edt_userName = (EditText) findViewById(R.id.edt_userName);
        edt_password = (EditText) findViewById(R.id.edt_password);
        loginBtn = (Button) findViewById(R.id.btn_login);
        regBtn = (Button) findViewById(R.id.btn_reg);
        url=classURL.ApplicationURL+"login.php";

    }

    private void Login_button(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    //透過Gson來解析JsonObject物件
                    Gson gson = new Gson();
                    MyJson json = gson.fromJson(response, MyJson.class);

                    if (json.getMyMsg().equals(sys001)) {	//登入成功並跳轉
                        strLog = "Successfully";
                        userInfo.Account=edt_userName.getText().toString();
                        userInfo.userName=json.getUserName();
                        userInfo.userHeight=Integer.valueOf(json.getMyHeight());
                        userInfo.userAge=Integer.valueOf(json.getMyAge());
                        userInfo.userGender=json.getMyGender();

                        setting.edit()
                                .putString("PREF_USERID", edt_userName.getText().toString())
                                .commit();
                        setting.edit()
                                .putString("PREF_USERPWD", edt_password.getText().toString())
                                .commit();

                        //如果成功前往主介面
                        Intent toMain = new Intent();
                        toMain.setClass(Login.this, Main.class);
                        toMain.putExtra("userName", edt_userName.getText().toString());   //將註冊的帳號裝入bundle
                        startActivity(toMain);
                    }
                    else if (json.getMyMsg().equals(sys002))
                        strLog = "Error";
                    else
                        strLog = "Wrong";


                    Toast.makeText(Login.this, "Login Result is " + strLog, Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(Login.this, "請檢查網路", Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //用HashMap將資料透過Post傳到PHP
                Map<String,String> map = new HashMap<String,String>();
                map.put("userName", edt_userName.getText().toString());
                map.put("password", edt_password.getText().toString());
                return map;
            }
        };

        //將要求加入隊列
        requestQueue.add(stringRequest);
    }

}