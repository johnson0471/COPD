package com.example.copd;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

public class Register extends AppCompatActivity {
    EditText edt_regAN, edt_regPW, edt_regPWC, edt_regNAME, edt_regEM, edt_regPN, edt_regHeight, edt_regAge;
    TextView txv_regANC;
    Button btn_sendRegister;

    static Spinner spinner_gender;

    public static final String sys001 = "001",   //註冊成功
            sys002 = "002", //註冊失敗
            sys003 = "003", //帳號已存在
            sys006 = "006"; //帳號可以使用
    String str_Gender = "";

    static Boolean unq = false, gender_select = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        var();
        final String[] Gender = {"男性", "女性"};
        ArrayAdapter<String> GenderList = new ArrayAdapter<String>(Register.this,
                android.R.layout.simple_spinner_dropdown_item, Gender);
        spinner_gender.setAdapter(GenderList);


        spinner_gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                str_Gender = (String) spinner_gender.getItemAtPosition(position);
                if ((str_Gender.equals("男性")) || (str_Gender.equals("女性"))){
                    gender_select = true;
                }else{
                    gender_select = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        final RequestQueue requestQueue = Volley.newRequestQueue(Register.this);

        //隨時監聽Edt有任變動
        //檢查帳號是否重複
        edt_regAN.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edt_regAN.getText().toString().equals("")){
                    Toast.makeText(Register.this, "帳號沒有輸入", Toast.LENGTH_LONG).show();
                }else {
                    String url = classURL.ApplicationURL+"account.php";

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                Gson gson = new Gson();
                                MyJson json = gson.fromJson(response, MyJson.class);

                                if (json.getMyMsg().equals(sys006)){
                                    txv_regANC.setText("該帳號可以使用");
                                    unq = true;
                                }

                                if (json.getMyMsg().equals(sys003)){
                                    txv_regANC.setText("已存在");
                                    unq = false;
                                }

                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(Register.this, error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            //用HashMap来存储请求参数
                            Map<String, String> map = new HashMap<String, String>();
                            map.put("user", edt_regAN.getText().toString());
                            map.put("correct", "0");
                            return map;
                        }
                    };

                    requestQueue.add(stringRequest);
                }
            }
        });



        btn_sendRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (unq){
                    if (gender_select){
                        if ((edt_regAN.getText().toString().equals(""))||(edt_regPW.getText().toString().equals(""))
                                ||(edt_regPWC.getText().toString().equals(""))||(edt_regNAME.getText().toString().equals(""))
                                ||(edt_regHeight.getText().toString().equals(""))||(edt_regAge.getText().toString().equals("")))
                        {
                            Toast.makeText(Register.this, "請確認表單", Toast.LENGTH_LONG).show();
                        }else{

                            if (edt_regPWC.getText().toString().equals(edt_regPW.getText().toString())){

                                String url = classURL.ApplicationURL+"register.php";

                                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try {
                                            Gson gson = new Gson();
                                            MyJson json = gson.fromJson(response, MyJson.class);

                                            if (json.getMyMsg().equals(sys001)){
                                                Toast.makeText(Register.this, "註冊成功", Toast.LENGTH_LONG).show();
                                                Register.this.finish();
                                            }

                                            if (json.getMyMsg().equals(sys002))
                                                Toast.makeText(Register.this, "註冊失敗", Toast.LENGTH_LONG).show();

                                        } catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Toast.makeText(Register.this, "請檢察網路", Toast.LENGTH_LONG).show();
                                    }
                                }) {
                                    @Override
                                    protected Map<String, String> getParams() throws AuthFailureError {
                                        //用HashMap来存储请求参数
                                        Map<String, String> map = new HashMap<String, String>();
                                        map.put("user", edt_regAN.getText().toString());
                                        map.put("passWord", edt_regPW.getText().toString());
                                        map.put("name", edt_regNAME.getText().toString());
                                        map.put("email", edt_regEM.getText().toString());
                                        map.put("phoneNumber", edt_regPN.getText().toString());
                                        map.put("height", edt_regHeight.getText().toString());
                                        map.put("age", edt_regAge.getText().toString());
                                        map.put("gender", str_Gender);
                                        map.put("correct", "1");
                                        return map;
                                    }
                                };

                                requestQueue.add(stringRequest);
                            }
                            else{   //密碼確認不同
                                Toast.makeText(Register.this, "密碼確認與密碼不同", Toast.LENGTH_LONG).show();
                            }
                        }
                    }else{
                        Toast.makeText(Register.this, "性別先進行選擇", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(Register.this, "帳號先進行確認", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void var(){
        edt_regAN = (EditText) findViewById(R.id.edt_regAN);
        edt_regPW = (EditText) findViewById(R.id.edt_regPW);
        edt_regPWC = (EditText) findViewById(R.id.edt_regPWC);
        edt_regNAME = (EditText) findViewById(R.id.edt_regNAME);
        edt_regEM = (EditText) findViewById(R.id.edt_regEM);
        edt_regPN = (EditText) findViewById(R.id.edt_regPN);
        edt_regHeight = (EditText) findViewById(R.id.edt_regHeight);
        edt_regAge = (EditText) findViewById(R.id.edt_regAge);

        txv_regANC = (TextView) findViewById(R.id.txv_regANC);

        btn_sendRegister = (Button) findViewById(R.id.btn_sendRegister);

        //spinner set List
        spinner_gender = (Spinner) findViewById(R.id.spinner_gender);
    }
}