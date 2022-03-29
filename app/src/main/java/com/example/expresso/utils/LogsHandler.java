package com.example.expresso.utils;

import android.content.Context;

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

public class LogsHandler {
    private static final String TAG = "LogsHandler";
    private static LogsHandler instance;

    //context
    private Context mContext;

    //variables
    private StringRequest stringRequest;

    // methods
    public void addLog(String event, VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_ADD_LOG, new Response.Listener<String>() {
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
                params.put("event", event);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }










    // public

    private LogsHandler(Context context) {
        mContext = context;
    }

    // public
    public static LogsHandler getInstance(Context context) {
        if (instance == null) {
            instance = new LogsHandler(context);
        }

        return instance;
    }
}
