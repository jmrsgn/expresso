package com.example.expresso.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresso.R;
import com.example.expresso.activities.Topic;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.LogsHandler;
import com.example.expresso.utils.TopicHandler;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class TopicsAdapter extends RecyclerView.Adapter<TopicsAdapter.ViewHolder> {
    // constants
    private static final String TAG = "TopicsAdapter";

    // variables
    private String moduleID;
    private ArrayList<com.example.expresso.models.Topic> topics;
    private ArrayList<String> unlockedTopicIDs, doneTopicIDs;
    private Context mContext;

    public TopicsAdapter(Context mContext) {
        this.mContext = mContext;
        initAll();
    }

    private void initAll() {
        // variables
        topics = new ArrayList<>();
        unlockedTopicIDs = new ArrayList<>();
        doneTopicIDs = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_topic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicsAdapter.ViewHolder holder, int position) {
        if (!Generals.checkInternetConnection(mContext)) {
            holder.ivItemTopicDone.setVisibility(View.GONE);
        } else {
            if (!unlockedTopicIDs.contains(topics.get(holder.getAdapterPosition()).getId())) {
                holder.mcvItemTopicContainer.setEnabled(false);
                holder.tvItemTopicTitle.setAlpha(0.30f);
                holder.tvItemTopicDescription.setAlpha(0.30f);
            }

            if (doneTopicIDs.contains(topics.get(holder.getAdapterPosition()).getId())) {
                holder.ivItemTopicDone.setVisibility(View.VISIBLE);
            }
        }
        holder.tvItemTopicTitle.setText(topics.get(position).getTitle());
        holder.tvItemTopicDescription.setText(topics.get(position).getDescription());

        // listeners
        holder.mcvItemTopicContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, Topic.class);
                intent.putExtra("module_id", topics.get(holder.getAdapterPosition()).getModuleID());
                intent.putExtra("topic_id", topics.get(holder.getAdapterPosition()).getId());
                intent.putExtra("module_topic_title", topics.get(holder.getAdapterPosition()).getTitle());
                intent.putExtra("module_content", topics.get(holder.getAdapterPosition()).getContent());
                intent.putExtra("path_index", topics.get(holder.getAdapterPosition()).getPathIndex());
                mContext.startActivity(intent);
            }
        });
    }

    public void setTopics(ArrayList<com.example.expresso.models.Topic> topics) {
        this.topics = topics;
    }

    public void setModuleID(String moduleID) {
        this.moduleID = moduleID;
    }

    public void setUnlockedTopicIDs(ArrayList<String> unlockedTopicIDs) {
        this.unlockedTopicIDs = unlockedTopicIDs;
    }

    public void setDoneTopicIDs(ArrayList<String> doneTopicIDs) {
        this.doneTopicIDs = doneTopicIDs;
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //views
        private MaterialCardView mcvItemTopicContainer;
        private TextView tvItemTopicTitle, tvItemTopicDescription;
        private ImageView ivItemTopicDone;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mcvItemTopicContainer = itemView.findViewById(R.id.mcv_item_topic_container);
            tvItemTopicTitle = itemView.findViewById(R.id.tv_item_topic_title);
            tvItemTopicDescription = itemView.findViewById(R.id.tv_item_topic_description);
            ivItemTopicDone = itemView.findViewById(R.id.iv_item_topic_done);
        }
    }
}
