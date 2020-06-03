package tech.jimmyglasscock.aaa;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> implements Filterable {

    private ArrayList<JSONObject> mDataset;
    private ArrayList<JSONObject> mDatasetCopy;
    private LayoutInflater mInflater;

    public FriendsAdapter(Context c, ArrayList<JSONObject> dataset){
        mDataset = dataset;
        mDatasetCopy = new ArrayList<JSONObject>(dataset);
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
            String username;
            String accepted;

            holder.name.setText(fullname);
            holder.id.setText(id);

            if(mDataset.get(position).getString("username") != null){
                username = mDataset.get(position).getString("username");
                holder.username.setText(username);
                holder.username.setVisibility(View.VISIBLE);
            }

            if(mDataset.get(position).getString("accepted") != null){
                accepted = mDataset.get(position).getString("accepted");
                holder.accepted.setText(accepted);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount(){
        return mDataset.size();
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final Context context;
        public TextView name;
        public TextView id;
        public TextView accepted;
        public TextView username;

        public FriendsViewHolder(View v){
            super(v);
            context = v.getContext();
            name = v.findViewById(R.id.friend_name);
            id = v.findViewById(R.id.friend_id);
            accepted = v.findViewById(R.id.friend_accepted);
            username = v.findViewById(R.id.username);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, PersonActivity.class);
            intent.putExtra("id", id.getText().toString());
            intent.putExtra("name", name.getText().toString());
            intent.putExtra("accepted", accepted.getText().toString());
            intent.putExtra("username", username.getText().toString());
            intent.putExtra("adapterPosition", getAdapterPosition());
            ((Activity)context).startActivityForResult(intent, 777);
        }

    }


    @Override
    public Filter getFilter() {
        return filter;
    }

    private Filter filter = new Filter() {
        @Override
        //this is automatically run in background thread
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<JSONObject> filteredList = new ArrayList<JSONObject>();

            if(constraint == null || constraint.length() == 0){
                filteredList.addAll(mDatasetCopy);
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(JSONObject o : mDatasetCopy){
                    try{
                        String fullname = o.getString("firstname").toLowerCase() + " " + o.getString("lastname").toLowerCase();
                        String username = o.getString("username").toLowerCase();

                        if(fullname.contains(filterPattern) || username.contains(filterPattern)){
                            filteredList.add(o);
                        }
                    }catch(JSONException e){
                        e.printStackTrace();
                    }

                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        //this automatically runs on UI thread
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mDataset.clear();
            mDataset.addAll((ArrayList<JSONObject>)results.values);
            notifyDataSetChanged();
        }
    };

}
