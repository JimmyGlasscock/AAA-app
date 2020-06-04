package tech.jimmyglasscock.aaa;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

public class friendRequestInbox extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request_inbox);

        setTitle("Friend Requests");

        getFriendRequests();
    }

    /*
    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        //Refresh
        getFriendRequests();
    }

     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1000 || resultCode == 1001) {
            //reload friends list
            getFriendRequests();
        }
    }

    public void postRequest(final String postURL, RequestBody postBody){
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
                            //load friends page
                            if(responseString.equals(getString(R.string.server_response_username))){
                                Toast.makeText(getApplicationContext(), getString(R.string.toast_lost_username), Toast.LENGTH_LONG).show();
                            }else{
                                populateRequestsList(responseString);
                            }
                        }
                    });

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void getFriendRequests(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(friendRequestInbox.this);
        String username = prefs.getString("username", "");

        JSONObject myInfo = new JSONObject();
        try{
            myInfo.put("username", username);
        }catch(JSONException e){
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(myInfo.toString(), MediaType.parse("application/json; charset=utf-8"));
        postRequest(getString(R.string.get_friend_requests_page), body);
    }

    public void populateRequestsList(String responseString){
        if(responseString.equals("[]")){
            //show no requests text
            TextView noRequests = (TextView) findViewById(R.id.no_requests);
            noRequests.setVisibility(View.VISIBLE);
            RecyclerView friendsView = findViewById(R.id.friends_view);
            friendsView.setVisibility(View.GONE);
        }else{
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
            friendsView.setVisibility(View.VISIBLE);

            //uses a linear layout manager
            LinearLayoutManager manager = new LinearLayoutManager(this);
            DividerItemDecoration decoration = new DividerItemDecoration(friendsView.getContext(), manager.getOrientation());
            friendsView.setLayoutManager(manager);
            friendsView.addItemDecoration(decoration);

            RecyclerView.Adapter adapter = new FriendsAdapter(this, dataset);
            friendsView.setAdapter(adapter);
        }
    }

}
