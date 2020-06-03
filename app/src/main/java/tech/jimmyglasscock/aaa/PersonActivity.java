package tech.jimmyglasscock.aaa;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
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

public class PersonActivity extends AppCompatActivity {

    TextView name;
    Button friendRequestButton;
    Button shoutButton;
    Button recordButton;
    Button messagesButton;
    Button removeFriendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        this.overridePendingTransition(R.anim.right_to_left, R.anim.left_to_right);

        name = (TextView) findViewById(R.id.name);
        friendRequestButton = (Button) findViewById(R.id.friendRequestButton);
        shoutButton = (Button) findViewById(R.id.shoutButton);
        recordButton = (Button) findViewById(R.id.recordButton);
        messagesButton = (Button) findViewById(R.id.messagesButton);
        removeFriendButton = (Button) findViewById(R.id.removeFriendButton);

        String accepted_status = getIntent().getStringExtra("accepted");

        String fullname = getIntent().getStringExtra("name");
        name.setText(fullname);


        //If you two are valid friends, then populate with options
        if(accepted_status.equals("1")){
            String firstname = fullname.substring(0, fullname.indexOf(' '));
            String shoutButtonString = getString(R.string.shout_button) + " " + firstname;
            shoutButton.setText(shoutButtonString);
        //if accepted equals 0, request has been sent, but is pending
        }else{
            friendRequestButton.setVisibility(View.VISIBLE);
            shoutButton.setVisibility(View.GONE);
            recordButton.setVisibility(View.GONE);
            messagesButton.setVisibility(View.GONE);
            removeFriendButton.setVisibility(View.GONE);

            if(accepted_status.equals("0")) {
                friendRequestButton.setText(R.string.friends_request_button_sent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.left_to_right2, R.anim.right_to_left2);
    }

    public void sendFriendRequest(View v){
        Button request = (Button) findViewById(R.id.friendRequestButton);
        String currentButtonText = request.getText().toString();

        //if request has not been sent yet
        if(!currentButtonText.equals(getString(R.string.friends_request_button_sent))) {
            //make request here
            String friendID = getIntent().getStringExtra("id");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PersonActivity.this);
            String username = prefs.getString("username", "");
            JSONObject friendInfo = new JSONObject();
            try{
                friendInfo.put("id", friendID);
                friendInfo.put("myUsername", username);
            }catch(JSONException e){
                e.printStackTrace();
            }

            RequestBody body = RequestBody.create(friendInfo.toString(), MediaType.parse("application/json; charset=utf-8"));
            postRequest(getString(R.string.friend_request_page), body);

            request.setText(R.string.friends_request_button_sent);
        }
    }

    public void sendShout(View v){

    }

    public void record(View v){

    }

    public void viewMessages(View v){

    }

    public void removeFriend(View v){
     String friendID = getIntent().getStringExtra("id");
     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(PersonActivity.this);
     String username = prefs.getString("username", "");
     JSONObject friendInfo = new JSONObject();
        try{
            friendInfo.put("id", friendID);
            friendInfo.put("myUsername", username);
        }catch(JSONException e){
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(friendInfo.toString(), MediaType.parse("application/json; charset=utf-8"));
        postRequest(getString(R.string.remove_friend_page), body);
    }

    public void confirmDeleteFriend(final View v){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        removeFriend(v);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.remove_friend_yes_no).setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
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
                            parseWebResponse(responseString);
                        }
                    });

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void parseWebResponse(String responseString){
        if(responseString.equals("removed-friend")){
            Toast.makeText(getApplicationContext(), R.string.friend_removed_successfully, Toast.LENGTH_LONG).show();
            //tells main menu to refresh when done
            Intent intent = new Intent();
            intent.putExtra("adapterPosition", getIntent().getStringExtra("adapterPosition"));
            setResult(777, intent);
            finish();
        }

        if(responseString.equals("request-sent")){
            Toast.makeText(getApplicationContext(), R.string.friends_request_sent, Toast.LENGTH_LONG).show();
            //tells main menu to refresh when done
            Intent intent = new Intent();
            intent.putExtra("adapterPosition", getIntent().getStringExtra("adapterPosition"));
            setResult(888, intent);
            finish();
        }

    }
}
