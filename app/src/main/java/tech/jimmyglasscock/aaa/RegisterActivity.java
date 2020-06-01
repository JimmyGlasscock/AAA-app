package tech.jimmyglasscock.aaa;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //hides action bar
        getSupportActionBar().hide();
    }

    public void register(View v){
        EditText usernameView = findViewById(R.id.username);
        EditText passwordView = findViewById(R.id.password);
        EditText firstnameView = findViewById(R.id.firstname);
        EditText lastnameView = findViewById(R.id.lastname);
        EditText emailView = findViewById(R.id.email);

        String username = usernameView.getText().toString().trim();
        String password = passwordView.getText().toString().trim();
        String firstname = firstnameView.getText().toString().trim();
        String lastname = lastnameView.getText().toString().trim();
        String email = emailView.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(getApplicationContext(), getString(R.string.toast_bad_email_format), Toast.LENGTH_LONG).show();
        }

        if(username.length() == 0 || password.length() == 0 || firstname.length() == 0 || lastname.length() == 0 || email.length() == 0){
            Toast.makeText(getApplicationContext(), getString(R.string.toast_empty_fields), Toast.LENGTH_LONG).show();
        }else{
            JSONObject registrationInfo = new JSONObject();
            try{
                registrationInfo.put("subject", "register");
                registrationInfo.put("username", username);
                registrationInfo.put("password", password);
                registrationInfo.put("firstname", firstname);
                registrationInfo.put("lastname", lastname);
                registrationInfo.put("email", email);
            }catch(JSONException e){
                e.printStackTrace();
            }

            RequestBody body = RequestBody.create(registrationInfo.toString(), MediaType.parse("application/json; charset=utf-8"));
            postRequest(getString(R.string.register_page), body);
        }
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
                                Toast.makeText(getApplicationContext(), getString(R.string.toast_registration_success), Toast.LENGTH_LONG).show();
                                finish();
                            }else if(responseString.equals(getString(R.string.server_response_username))){
                                Toast.makeText(getApplicationContext(), getString(R.string.toast_duplicate_username), Toast.LENGTH_LONG).show();
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
