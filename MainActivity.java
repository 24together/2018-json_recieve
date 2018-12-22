package com.andrstudy.a2_finalproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    ImageButton alert; //팝업버튼선언
    private TextView textView;
    public String myJSON = "";
    private TextView TextView_id;
    private TextView TextView_price;
    private Button button;
    private ListView listView;
    private static String ip = null;
    private static final String TAG_RESULTS="result";
    private static final String TAG_MENU = "menuName";
    private static final String TAG_PRICE = "price";
    private static final String TAG_SPECIAL = "isSpecial";
    private SharedPreferences preferences;

    JSONArray menus = null;
    private ArrayList<HashMap<String, String>> menuList;

@Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    menuList = new ArrayList<HashMap<String, String>>();

    listView=(ListView)findViewById(R.id.listView);

    ////팝업버튼 설정
    alert = (ImageButton) findViewById(R.id.alert);
    alert.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder request = new AlertDialog.Builder(MainActivity.this);
            request.setTitle("ip설정");//팝업창 타이틀바
            request.setMessage("ip를 입력해주세요.");//팝업창 내용

            //EditText삽입
            final EditText et = new EditText(MainActivity.this);
            request.setView(et);

            //확인 버튼 설정
            request.setPositiveButton("입력", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    //Text값 받기
                    ip = et.getText().toString();
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("ip", ip);
                    editor.commit();
                    getData("http://"+ip+"/201802/menu.php");
                }

            });
            // 취소 버튼 설정
            request.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();     //닫기
                }
            });


            request.show();

        }//end of onclick()
    });//end of alert.onclicklistener

    textView = (TextView) findViewById(R.id.textView);
    SharedPreferences preference = getSharedPreferences("namePref", Activity.MODE_PRIVATE);
    String checkAddress = preference.getString("ip", null);
//데이터 가져오기
    preferences = getSharedPreferences("ip",MODE_PRIVATE);
    String getIp = preferences.getString("ip", null);
//다시 들어 올 경우 제공하는 ip
    if(getIp !=null)
        getData("http://"+getIp+"/201802/menu.php");

}//end of oncreate

    //php 출력물을 가져와서 string 값으로 쓸 수 있게 한다.
    public void getData(String url) {
        class GetDataJson extends AsyncTask<String, Void, String>{

            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];//예외처리
                if(params==null || params.length<1)
                    return null;
                BufferedReader bufferedReader = null;//json 한글자씩 불러옴
                try{
                    //url 접속 및 빌더생성
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setDoInput(true);
                    con.setUseCaches(false);
                    con.setDefaultUseCaches(false);
                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while((json = bufferedReader.readLine())!=null){
                        sb.append(json+"\n");
                    }

                    return sb.toString().trim();
                }catch(Exception e){
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String result){
                myJSON=result;
                Log.i("myJSON", myJSON);
                showList();
            }

        }
        GetDataJson g = new GetDataJson();
        g.execute(url);
    }// end of getdata
    protected void showList(){
        try{
            JSONObject jsonObj = new JSONObject(myJSON);
            menus = jsonObj.getJSONArray(TAG_RESULTS);//TAG_RESULTs 응답해서 받아온 키 값

            TextView_id = (TextView)findViewById(R.id.textView_id);
            TextView_price = (TextView)findViewById(R.id.textView_price);
            //리스트뷰 출력
            for(int i=0; i<menus.length(); i++){
                JSONObject c = menus.getJSONObject(i);
                String menuName = c.getString(TAG_MENU);
                String price = c.getString(TAG_PRICE);
                String isSpecial = c.getString(TAG_SPECIAL);

                HashMap<String, String> menusArray = new HashMap<String,String>();
                //오늘의 메뉴
                if(isSpecial == "true") {
                    TextView_id.setText(menuName);
                    TextView_price.setText(price);
                }
                //리스트뷰 추가
                menusArray.put(TAG_MENU, menuName);
                menusArray.put(TAG_PRICE, price);
                menuList.add(menusArray);
            }

            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, menuList, R.layout.second_main,
                    new String[]{TAG_MENU, TAG_PRICE}, //들어가는 데이터의 key 값
                    new int[]{R.id.id, R.id.price,} //데이터가 들어가는 position 역할을 하는 textView
            );
            listView.setAdapter(adapter);
        }catch(JSONException e){
            e.printStackTrace();
        }
    }//end of showList
}//end of class