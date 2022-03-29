package com.example.expresso.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.network.RequestQueueSingleton;

import java.util.HashMap;
import java.util.Map;

public class TopicHandler {
    // constants
    private static final String TAG = "TopicHandler";
    private static TopicHandler instance;

    // variables
    private StringRequest stringRequest;
    private Context mContext;


    public void getUnlockedTopicIDs(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_UNLOCKED_TOPIC_IDS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", Constants.ID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void getDoneTopicIDs(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_DONE_TOPIC_IDS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", Constants.ID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void getTopicVideoURLs(String topicID, VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_TOPIC_VIDEO_URLS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("topic_id", topicID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void getAllTopics(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_ALL_TOPICS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        });

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void addTopic(String topicID, VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_ADD_TOPIC, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", Constants.ID);
                params.put("topic_id", topicID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void checkTopicID(String topicID, VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_CHECK_TOPIC_ID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }}, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", Constants.ID);
                params.put("topic_id", topicID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    // logs

    public void logVisitTopic(String topicSlug) {
        LogsHandler.getInstance(mContext).addLog("visit topic: " + topicSlug, new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: addLog: " + response);
            }

            @Override
            public void onResponse(String response) {
                // do nothing
            }
        });
    }

    // public
    private TopicHandler(Context context) {
        mContext = context;
    }

    public static TopicHandler getInstance(Context context) {
        if (instance == null) {
            instance = new TopicHandler(context);
        }

        return instance;
    }
}
