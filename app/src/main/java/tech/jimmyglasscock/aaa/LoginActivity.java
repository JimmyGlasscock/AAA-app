package tech.jimmyglasscock.aaa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private String username = "";
    private String password = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //hides action bar
        getSupportActionBar().hide();
    }

    public void mainMenu(View v){
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
    }

    public void login(View v){
        EditText usernameView = findViewById(R.id.username);
        EditText passwordView = findViewById(R.id.password);

        username = usernameView.getText().toString().trim();
        password = passwordView.getText().toString().trim();

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

    public void postRequest(String postURL, RequestBody postBody){
        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder().url(postURL).post(postBody).header("Accept", "application/json").header("Content-Type", "application/json").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
                Log.d("FAIL", e.getMessage());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), getString(R.string.toast_failed_server_connection), Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try{
                    final String responseString = response.body().string().trim();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(responseString.equals(getString(R.string.server_response_success))){
                                //store username and password
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
                                prefs.edit().putString("username", username).commit();
                                prefs.edit().putString("password", password).commit();

                                //Move to logged in activity
                                mainMenu(getCurrentFocus());

                            }else if(responseString.equals(getString(R.string.server_response_username))){
                                Toast.makeText(getApplicationContext(), getString(R.string.toast_bad_username), Toast.LENGTH_LONG).show();
                            }else if(responseString.equals(getString(R.string.server_response_password))){
                                Toast.makeText(getApplicationContext(), getString(R.string.toast_bad_password), Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getApplicationContext(), getString(R.string.toast_error_generic), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
