package com.example.expresso.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresso.R;
import com.example.expresso.activities.Topics;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.models.Module;
import com.example.expresso.utils.Constants;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.TopicHandler;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class ModulesAdapter extends RecyclerView.Adapter<ModulesAdapter.ViewHolder> {
    // constants
    private static final String TAG = "ModulesAdapter";

    // context
    private Context mContext;

    // variables
    private ArrayList<Module> modules;
    private ArrayList<String> moduleIDs, unlockedModuleIDs, doneModuleIDs;
    private SharedPreferences spUserSavedModulesID, spUserAvailableTopicsID;
    private SharedPreferences.Editor editor;
    private Gson gson;

    public ModulesAdapter(Context mContext) {
        this.mContext = mContext;
        initAll();
    }

    private void initAll() {
        // variables
        modules = new ArrayList<>();
        moduleIDs = new ArrayList<>();
        unlockedModuleIDs = new ArrayList<>();
        doneModuleIDs = new ArrayList<>();
        spUserSavedModulesID = mContext.getSharedPreferences(Constants.SP_USER_SAVED_MODULES_ID, Context.MODE_PRIVATE);
        spUserAvailableTopicsID = mContext.getSharedPreferences(Constants.SP_USER_AVAILABLE_TOPICS_ID, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_module, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModulesAdapter.ViewHolder holder, int position) {
        if (!Generals.checkInternetConnection(mContext)) {
            holder.smItemModuleAvailableOffline.setVisibility(View.GONE);
            holder.ivItemModuleDone.setVisibility(View.GONE);
        } else {
            if (!unlockedModuleIDs.contains(modules.get(holder.getAdapterPosition()).getId())) {
                holder.smItemModuleAvailableOffline.setEnabled(false);
                holder.mcvItemModuleContainer.setEnabled(false);
                holder.tvItemModuleTitle.setAlpha(0.30f);
                holder.tvItemModuleDescription.setAlpha(0.30f);
            }

            if (doneModuleIDs.contains(modules.get(holder.getAdapterPosition()).getId())) {
                holder.ivItemModuleDone.setVisibility(View.VISIBLE);
            }
        }

        holder.tvItemModuleTitle.setText(modules.get(position).getTitle());
        holder.tvItemModuleDescription.setText(modules.get(position).getDescription());

        checkIfSwitchIsEnabled(holder);

        // listeners
        holder.mcvItemModuleContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Generals.checkInternetConnection(mContext)) {
                    ArrayList<String> topicsPathIndexes = new ArrayList<>();
                    SharedPreferences spTopicsInfo = mContext.getSharedPreferences(Constants.SP_TOPICS_INFO, Context.MODE_PRIVATE);
                    SharedPreferences spTopicsPathIndexes = mContext.getSharedPreferences(Constants.SP_TOPICS_PATH_INDEXES, Context.MODE_PRIVATE);
                    Gson gson = new Gson();

                    String topicsPathIndexesJson = spTopicsPathIndexes.getString(modules.get(holder.getAdapterPosition()).getId(), null);
                    String[] topicsPathIndexesArray = gson.fromJson(topicsPathIndexesJson, String[].class);

                    Collections.addAll(topicsPathIndexes, topicsPathIndexesArray);

                    Map<String,?> keys = spTopicsInfo.getAll();
                    for(Map.Entry<String,?> entry : keys.entrySet()){
                        String json_ = spTopicsInfo.getString(entry.getKey(), null);
                        String[] topicInfo = gson.fromJson(json_, String[].class);

                        if (topicInfo[4].equals(topicsPathIndexes.get(0)) && topicInfo[2].equals(modules.get(holder.getAdapterPosition()).getId())) {
                            TopicHandler.getInstance(mContext).checkTopicID(topicInfo[0], new VolleyResponseListener() {
                                @Override
                                public void onError(String response) {
                                    Log.e(TAG, "onError: checkTopicID: " + response);
                                }

                                @Override
                                public void onResponse(String response) {
                                    if (response.equals("Success")) {
                                        goToTopics(modules.get(holder.getAdapterPosition()).getId());
                                    } else {
                                        TopicHandler.getInstance(mContext).addTopic(topicInfo[0], new VolleyResponseListener() {
                                            @Override
                                            public void onError(String response) {
                                                Log.e(TAG, "onError: addTopic: " + response);
                                            }

                                            @Override
                                            public void onResponse(String response) {
                                                goToTopics(modules.get(holder.getAdapterPosition()).getId());
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                } else {
                    goToTopics(modules.get(holder.getAdapterPosition()).getId());
                }
            }
        });

        holder.smItemModuleAvailableOffline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (holder.smItemModuleAvailableOffline.isChecked()) {
                    moduleIDs.add(modules.get(holder.getAdapterPosition()).getId());
                    showSnackbarConfirmDialog(compoundButton, holder);
                } else {
                    moduleIDs.remove(modules.get(holder.getAdapterPosition()).getId());
                    showSnackbarConfirmDialog(compoundButton, holder);
                }
            }
        });
    }

    private void goToTopics(String moduleID) {
        Intent intent = new Intent(mContext, Topics.class);
        intent.putExtra("FROM_ACTIVITY", "Modules");
        intent.putExtra("module_id", moduleID);
        mContext.startActivity(intent);
    }

    private void checkIfSwitchIsEnabled(ViewHolder holder) {
        String modulesIDsJson = spUserSavedModulesID.getString(Constants.ID, null);
        if (modulesIDsJson != null) {
            String[] modulesID = gson.fromJson(modulesIDsJson, String[].class);

            for (String id : modulesID) {
                if (id.equals(modules.get(holder.getAdapterPosition()).getId())) {
                    holder.smItemModuleAvailableOffline.setChecked(true);
                    break;
                }
            }
        } else {
            holder.smItemModuleAvailableOffline.setChecked(false);
        }

        if (holder.smItemModuleAvailableOffline.isChecked()) {
            moduleIDs.add(modules.get(holder.getAdapterPosition()).getId());
        }
    }

    private void showSnackbarConfirmDialog(View v, ViewHolder holder) {
        String moduleMessage = "";

        if (moduleIDs.size() == 1) {
            moduleMessage = moduleIDs.size() + " module";
        } else if (moduleIDs.size() == 0) {
            moduleMessage = "No modules ";
        } else {
            moduleMessage = moduleIDs.size() + " modules";
        }

        Snackbar snackbar = Snackbar.make(v, moduleMessage + " will be available offline", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Confirm", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (moduleIDs.size() != 0) {
                    saveModuleToLocal(moduleIDs);
                }
            }
        });

        if (moduleIDs.size() != 0) {
            snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);
            snackbar.show();
        } else {
            snackbar.setDuration(BaseTransientBottomBar.LENGTH_SHORT);
            snackbar.show();

            mContext.getSharedPreferences(Constants.SP_USER_SAVED_MODULES_ID, 0).edit().clear().apply();
            mContext.getSharedPreferences(Constants.SP_USER_AVAILABLE_TOPICS_ID, 0).edit().clear().apply();
        }
    }

    private void saveModuleToLocal(ArrayList<String> moduleIDs) {
        // save module ids
        editor = spUserSavedModulesID.edit();
        gson = new Gson();
        String userModulesJson = gson.toJson(moduleIDs);
        editor.putString(Constants.ID, userModulesJson);
        editor.apply();

        saveDoneTopicsToLocal();
    }

    private void saveDoneTopicsToLocal() {
        ArrayList<String> doneTopicIDs = new ArrayList<>();

        TopicHandler.getInstance(mContext).getDoneTopicIDs(new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getDoneTopicIDs: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arrayTopicIDs = new JSONArray(response);

                    for (int i = 0; i < arrayTopicIDs.length(); i ++) {
                        JSONObject objectTopicID = arrayTopicIDs.getJSONObject(i);

                        doneTopicIDs.add(objectTopicID.getString("topic_id"));
                    }

                    editor = spUserAvailableTopicsID.edit();
                    gson = new Gson();
                    String userDoneTopicsJson = gson.toJson(doneTopicIDs);
                    editor.putString(Constants.ID, userDoneTopicsJson);
                    editor.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setModules(ArrayList<Module> modules) {
        this.modules = modules;
    }

    public void setUnlockedModuleIDs(ArrayList<String> unlockedModuleIDs) {
        this.unlockedModuleIDs = unlockedModuleIDs;
    }

    public void setDoneModuleIDs(ArrayList<String> doneModuleIDs) {
        this.doneModuleIDs = doneModuleIDs;
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // views
        private MaterialCardView mcvItemModuleContainer;
        private TextView tvItemModuleTitle, tvItemModuleDescription;
        private ImageView ivItemModuleDone;
        private SwitchMaterial smItemModuleAvailableOffline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // views
            ivItemModuleDone = itemView.findViewById(R.id.iv_item_module_done);
            mcvItemModuleContainer = itemView.findViewById(R.id.mcv_item_module_container);
            tvItemModuleTitle = itemView.findViewById(R.id.tv_item_module_title);
            tvItemModuleDescription = itemView.findViewById(R.id.tv_item_module_description);
            smItemModuleAvailableOffline = itemView.findViewById(R.id.sm_item_module_available_offline);
        }
    }
}