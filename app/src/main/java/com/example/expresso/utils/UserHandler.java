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
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.HashMap;
import java.util.Map;

public class UserHandler {
    private static final String TAG = "UserHandler";
    private static UserHandler instance;

    //context
    private Context mContext;

    //variables
    private StringRequest stringRequest;

    public void setUserInfo(GoogleSignInAccount account) {
        if (account != null) {
            Constants.ID = account.getId();
            Constants.EMAIL = account.getEmail();
            Constants.GIVEN_NAME = account.getGivenName();
            Constants.FAMILY_NAME = account.getFamilyName();
            Constants.IMAGE_URL = account.getPhotoUrl() != null? account.getPhotoUrl().toString() : "";
        }
    }

    public void checkExistingUserInDB(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_CHECK_USER_ID, new Response.Listener<String>() {
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
                params.put("id", Constants.ID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void registerUser(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_ADD_USER, new Response.Listener<String>() {
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
                params.put("id", Constants.ID);
                params.put("email", Constants.EMAIL);
                params.put("given_name", Constants.GIVEN_NAME);
                params.put("family_name", Constants.FAMILY_NAME);
                params.put("photo", Constants.IMAGE_URL);
                params.put("role_id", "2");

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void getJoinedDate(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_JOINED_DATE, new Response.Listener<String>() {
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
                params.put("id", Constants.ID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void getRoleID(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_ROLE_ID, new Response.Listener<String>() {
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
                params.put("id", Constants.ID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    // logs

    public void logLoggedInToTheSystem() {
        LogsHandler.getInstance(mContext).addLog("logged in to the system", new VolleyResponseListener() {
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

    public void logLoggedOutFromTheSystem() {
        LogsHandler.getInstance(mContext).addLog("logged out from the system", new VolleyResponseListener() {
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

    private UserHandler(Context context) {
        mContext = context;
    }

    // public
    public static UserHandler getInstance(Context context) {
        if (instance == null) {
            instance = new UserHandler(context);
        }

        return instance;
    }
}
