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

public class CertificateHandler {
    // constants
    private static final String TAG = "TopicHandler";
    private static CertificateHandler instance;

    // variables
    private StringRequest stringRequest;
    private Context mContext;

    public void getCertificateName(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_CERTIFICATE_NAME, new Response.Listener<String>() {
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

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void addCertificate(String name, VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_ADD_CERTIFICATE, new Response.Listener<String>() {
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
                params.put("name", name);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }





    // public
    private CertificateHandler(Context context) {
        mContext = context;
    }

    public static CertificateHandler getInstance(Context context) {
        if (instance == null) {
            instance = new CertificateHandler(context);
        }

        return instance;
    }
}
