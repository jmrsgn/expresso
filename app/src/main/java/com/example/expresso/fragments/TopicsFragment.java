package com.example.expresso.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expresso.R;
import com.example.expresso.activities.Topics;
import com.example.expresso.adapters.TopicsAdapter;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.models.Topic;
import com.example.expresso.utils.Constants;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.TopicHandler;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class TopicsFragment extends Fragment {
    // constants
    private static final String TAG = "TopicsFragment";
    private Context mContext;

    // variables
    private String moduleID;
    private ArrayList<Topic> topics;
    private ArrayList<String> moduleTopics, unlockedTopicIDs, doneTopicIDs, moduleTopicIDs;
    private TopicsAdapter topicsAdapter;
    private SharedPreferences spTopicsInfo, spUserAvailableTopicsID;
    private Gson gson;

    // views
    private RecyclerView rvTopicsFragmentTopics;
    private LinearLayout llTopicsFragmentLoading, llTopicsFragmentNoResultsFound;

    public TopicsFragment(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_topics, container, false);
        initAll(view);

        llTopicsFragmentLoading.setVisibility(View.VISIBLE);

        Topics activity = (Topics) getActivity();
        moduleID = activity.getModuleID();

        if (Generals.checkInternetConnection(mContext)) {
            getUserUnlockedTopics();
        } else {
            getAllModuleTopicsPathIndex();
        }

        return view;
    }


    // -- offline features ========================================
    private void getAllModuleTopicsPathIndex() {
        ArrayList<Integer> moduleTopicsPathIndexes = new ArrayList<>();

        Map<String,?> keys_ = spTopicsInfo.getAll();
        for(Map.Entry<String,?> entry : keys_.entrySet()){
            gson = new Gson();

            String json = spTopicsInfo.getString(entry.getKey(), null);
            String[] topicInfo = gson.fromJson(json, String[].class);

            if (moduleID.equals(topicInfo[2])) {
                moduleTopicsPathIndexes.add(Integer.parseInt(topicInfo[4]));
            }
        }

        Collections.sort(moduleTopicsPathIndexes);
        getOfflineTopicsPathIndex(moduleTopicsPathIndexes);
    }

    private void getOfflineTopicsPathIndex(ArrayList<Integer> moduleTopicsPathIndexes) {
        Map<String,?> keys_ = spTopicsInfo.getAll();

        for (int i = 0; i < moduleTopicsPathIndexes.size(); i ++) {
            for(Map.Entry<String,?> entry : keys_.entrySet()){
                gson = new Gson();

                String json = spTopicsInfo.getString(entry.getKey(), null);
                String[] topicInfo = gson.fromJson(json, String[].class);

                if (moduleID.equals(topicInfo[2]) && topicInfo[4].equals(Integer.toString(moduleTopicsPathIndexes.get(i)))) {
                    moduleTopics.add(topicInfo[0]);
                }
            }
        }

        loadOfflineTopics();
    }

    private void loadOfflineTopics() {
        String json = spUserAvailableTopicsID.getString(Constants.ID, null);

        if (json != null || !json.equals("[]")) {
            String[] availableTopics = gson.fromJson(json, String[].class);

            for (int i = 0; i < moduleTopics.size(); i ++) {
                for (String availableTopic : availableTopics) {
                    if (moduleTopics.get(i).equals(availableTopic)) {
                        gson = new Gson();
                        String json_ = spTopicsInfo.getString(moduleTopics.get(i), null);
                        String[] topicInfo = gson.fromJson(json_, String[].class);

                        topics.add(new Topic(topicInfo[0],
                                             topicInfo[1],
                                             topicInfo[2],
                                             topicInfo[3],
                                             topicInfo[4],
                                             topicInfo[5],
                                             topicInfo[6]));
                    }
                }
            }
        }

        displayTopics(topics);
    }



    // -- online features ========================================
    private void getUserUnlockedTopics() {
        TopicHandler.getInstance(mContext).getUnlockedTopicIDs(new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getUnlockedTopicIDs: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray arrayTopicIDs = new JSONArray(response);

                    for (int i = 0; i < arrayTopicIDs.length(); i ++) {
                        JSONObject objectTopicID = arrayTopicIDs.getJSONObject(i);

                        unlockedTopicIDs.add(objectTopicID.getString("topic_id"));
                    }

                    TopicHandler.getInstance(mContext).getDoneTopicIDs(new VolleyResponseListener() {
                        @Override
                        public void onError(String response) {
                            Log.e(TAG, "onError: getDoneTopicIDs: " + response);
                        }

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray arrayTopicIDs = new JSONArray(response);

                                for (int j = 0; j < arrayTopicIDs.length(); j ++) {
                                    JSONObject objectTopicID = arrayTopicIDs.getJSONObject(j);

                                    doneTopicIDs.add(objectTopicID.getString("topic_id"));
                                }

                                ArrayList<String> topicsPathIndexes = new ArrayList<>();
                                SharedPreferences spTopicsInfo = mContext.getSharedPreferences(Constants.SP_TOPICS_INFO, Context.MODE_PRIVATE);
                                SharedPreferences spTopicsPathIndexes = mContext.getSharedPreferences(Constants.SP_TOPICS_PATH_INDEXES, Context.MODE_PRIVATE);
                                Gson gson = new Gson();

                                String topicsPathIndexesJson = spTopicsPathIndexes.getString(moduleID, null);
                                String[] topicsPathIndexesArray = gson.fromJson(topicsPathIndexesJson, String[].class);

                                Collections.addAll(topicsPathIndexes, topicsPathIndexesArray);

                                for (int i = 0; i < topicsPathIndexes.size(); i ++) {
                                    Map<String,?> keys = spTopicsInfo.getAll();
                                    for(Map.Entry<String,?> entry : keys.entrySet()){
                                        gson = new Gson();

                                        String json_ = spTopicsInfo.getString(entry.getKey(), null);
                                        String[] topicInfo = gson.fromJson(json_, String[].class);

                                        if (topicInfo[4].equals(topicsPathIndexes.get(i)) && topicInfo[2].equals(moduleID)) {
                                            topics.add(new Topic(topicInfo[0],
                                                                 topicInfo[1],
                                                                 topicInfo[2],
                                                                 topicInfo[3],
                                                                 topicInfo[4],
                                                                 topicInfo[5],
                                                                 topicInfo[6]));
                                        }
                                    }
                                }

                                displayTopics(topics);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void displayTopics(ArrayList<Topic> topics) {
        llTopicsFragmentLoading.setVisibility(View.GONE);

        if (topics.size() == 0) {
            llTopicsFragmentNoResultsFound.setVisibility(View.VISIBLE);
        } else {
            topicsAdapter = new TopicsAdapter(mContext);
            rvTopicsFragmentTopics.setAdapter(topicsAdapter);
            rvTopicsFragmentTopics.setLayoutManager(new LinearLayoutManager(mContext));
            topicsAdapter.setTopics(topics);
            topicsAdapter.setModuleID(moduleID);
            topicsAdapter.setUnlockedTopicIDs(unlockedTopicIDs);
            topicsAdapter.setDoneTopicIDs(doneTopicIDs);
        }
    }

    private void initAll(View view) {
        // variables
        topics = new ArrayList<>();
        moduleTopics = new ArrayList<>();
        unlockedTopicIDs = new ArrayList<>();
        doneTopicIDs = new ArrayList<>();
        moduleTopicIDs = new ArrayList<>();
        spTopicsInfo = mContext.getSharedPreferences(Constants.SP_TOPICS_INFO, Context.MODE_PRIVATE);
        spUserAvailableTopicsID = mContext.getSharedPreferences(Constants.SP_USER_AVAILABLE_TOPICS_ID, Context.MODE_PRIVATE);

        // views
        rvTopicsFragmentTopics = view.findViewById(R.id.rv_topics_fragment_topics);
        llTopicsFragmentLoading = view.findViewById(R.id.ll_topics_fragment_loading);
        llTopicsFragmentNoResultsFound = view.findViewById(R.id.ll_topics_fragment_no_results_found);
    }
}