package tech.jimmyglasscock.aaa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {

    private ArrayList<JSONObject> mDataset;
    private LayoutInflater mInflater;

    public FriendsAdapter(Context c, ArrayList<JSONObject> dataset){
        mDataset = dataset;
        mInflater = LayoutInflater.from(c);
    }

    @Override
    public FriendsAdapter.FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        //create new view
        View v = mInflater.inflate(R.layout.friend_view, parent, false);
        return new FriendsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FriendsViewHolder holder, int position){
        try {
            String fullname = mDataset.get(position).getString("firstname") + " " + mDataset.get(position).getString("lastname");
            String id = mDataset.get(position).getString("id");

            holder.name.setText(fullname);
            holder.id.setText(id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount(){
        return mDataset.size();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{
        public TextView name;
        public TextView id;

        public FriendsViewHolder(View v){
            super(v);
            name = v.findViewById(R.id.friend_name);
            id = v.findViewById(R.id.friend_id);
        }
    }
}