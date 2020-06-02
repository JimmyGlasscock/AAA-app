package tech.jimmyglasscock.aaa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        loadFriendsList();
    }

    @Override
    public void onBackPressed() {
        //do nothing, back disabled
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuItemSelected = item.getItemId();
        if(menuItemSelected == R.id.action_log_out){
            Context context = MainMenuActivity.this;
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    public void logout(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainMenuActivity.this);
        prefs.edit().putString("username", "").apply();
        prefs.edit().putString("password", "").apply();

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 777) {
            //reload friends list
            loadFriendsList();
        }
    }

    public void loadFriendsList(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainMenuActivity.this);
        String username = prefs.getString("username", "");

        JSONObject myInfo = new JSONObject();
        try{
            myInfo.put("username", username);
        }catch(JSONException e){
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(myInfo.toString(), MediaType.parse("application/json; charset=utf-8"));
        postRequest(getString(R.string.get_friends_page), body);
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
                            if(responseString.equals(getString(R.string.server_response_username))){
                                Toast.makeText(getApplicationContext(), getString(R.string.toast_lost_username), Toast.LENGTH_LONG).show();
                            }else{
                                addFriendsToScreen(responseString);
                            }
                        }
                    });

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void addFriendsToScreen(String responseString){
        ArrayList<JSONObject> dataset = new ArrayList<JSONObject>();

        try {
            JSONArray response = new JSONArray(responseString);
            for(int i = 0; i < response.length(); i++){
                dataset.add(response.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RecyclerView friendsView = findViewById(R.id.friends_view);
        //uses a linear layout manager
        LinearLayoutManager manager = new LinearLayoutManager(this);
        DividerItemDecoration decoration = new DividerItemDecoration(friendsView.getContext(), manager.getOrientation());
        friendsView.setLayoutManager(manager);
        friendsView.addItemDecoration(decoration);

        RecyclerView.Adapter adapter = new FriendsAdapter(this, dataset);
        friendsView.setAdapter(adapter);
    }
}
