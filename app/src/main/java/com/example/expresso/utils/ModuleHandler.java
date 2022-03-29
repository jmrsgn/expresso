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

public class ModuleHandler {
    // constants
    private static final String TAG = "ModuleHandler";
    private static ModuleHandler instance;

    // variables
    private StringRequest stringRequest;
    private Context mContext;

    public void checkUserModules(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_CHECK_USER_MODULES, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.toString());
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

    public void getUnlockedModuleIDs(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_UNLOCKED_MODULE_IDS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.toString());
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

    public void getMaxUserModuleID(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_MAX_USER_MODULE_ID, new Response.Listener<String>() {
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

    public void getMaxUserDoneModule(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_MAX_USER_DONE_MODULE, new Response.Listener<String>() {
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

    public void getModuleSummativeID(String moduleID, VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_MODULE_SUMMATIVE_ID, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                volleyResponseListener.onResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "onErrorResponse: " + error.toString());
                volleyResponseListener.onError(error.toString());
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("module_id", moduleID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void getDoneModuleIDs(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_DONE_MODULE_IDS, new Response.Listener<String>() {
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

    public void getAllModulesAndUserModulesCount(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_ALL_MODULES_AND_USER_DONE_MODULES_COUNT, new Response.Listener<String>() {
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

    public void getAllModules(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_ALL_MODULES, new Response.Listener<String>() {
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

    public void checkModuleID(String moduleID, VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_CHECK_MODULE_ID, new Response.Listener<String>() {
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
                params.put("module_id", moduleID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void checkDoneModuleID(String moduleID, VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_CHECK_DONE_MODULE_ID, new Response.Listener<String>() {
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
                params.put("module_id", moduleID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void addModule(String moduleID, VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_ADD_MODULE, new Response.Listener<String>() {
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
                params.put("module_id", moduleID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void updateModuleDone(String moduleID, VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_UPDATE_MODULE_DONE, new Response.Listener<String>() {
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
                params.put("module_id", moduleID);

                return params;
            }
        };

        RequestQueueSingleton.getInstance(mContext).addToRequestQueue(stringRequest);
    }

    public void getModulesPathIndexes(VolleyResponseListener volleyResponseListener) {
        stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_MODULES_PATH_INDEXES, new Response.Listener<String>() {
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

    // logs

    public void logVisitModule(String moduleSlug) {
        LogsHandler.getInstance(mContext).addLog("visit module: " + moduleSlug , new VolleyResponseListener() {
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
    private ModuleHandler(Context context) {
        mContext = context;
    }

    public static ModuleHandler getInstance(Context context) {
        if (instance == null) {
            instance = new ModuleHandler(context);
        }

        return instance;
    }
}
