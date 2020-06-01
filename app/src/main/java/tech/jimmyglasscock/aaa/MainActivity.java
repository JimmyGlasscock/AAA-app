package tech.jimmyglasscock.aaa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.spec.ECField;
import java.util.Scanner;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //hides action bar
        getSupportActionBar().hide();

        checkSavedCrentials();
    }

    public void login(View v){
        Intent intent = new Intent(this, LoginActivity.class);
        //activity will return weather or not it was successful
        startActivityForResult(intent,2);
    }

    public void register(View v){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    public void mainMenu(View v){
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
    }

    public void checkSavedCrentials(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String username = prefs.getString("username", "");
        String password = prefs.getString("password", "");

        login(username, password);
    }

    public void login(String username, String password){
        JSONObject loginInfo = new JSONObject();
        try{
            loginInfo.put("username", username);
            loginInfo.put("password", password);
        }catch(JSONException e){
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(loginInfo.toString(), MediaType.parse("application/json; charset=utf-8"));
        postRequest(getString(R.string.login_page), body);
    }

    public void postRequest(String postURL, RequestBody postBody) {
        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder().url(postURL).post(postBody).header("Accept", "application/json").header("Content-Type", "application/json").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
                Log.d("FAIL", e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    final String responseString = response.body().string().trim();

                    runOnUiThread(new Runnable() {
                        @Override
                        public int hashCode() {
                            return super.hashCode();
                        }

                        @Override
                        public void run() {
                            if(responseString.equals(getString(R.string.server_response_success))) {
                                mainMenu(getCurrentFocus());
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
