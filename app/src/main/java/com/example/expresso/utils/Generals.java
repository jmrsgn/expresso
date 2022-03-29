package com.example.expresso.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.models.Module;
import com.example.expresso.models.Topic;
import com.example.expresso.network.RequestQueueSingleton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Generals {
    // constants
    private static final String TAG = "Generals";

    public static boolean checkInternetConnection(Context mContext) {
        boolean connected = false;

        try {
            ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            connected = ni != null && ni.isAvailable() && ni.isConnected();

            return connected;
        } catch (Exception e) {
            Log.e("Connectivity Exception", e.getMessage());
        };

        return connected;
    }

    public static void makeToast(Context mContext, String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public static void fadeInView(View view) {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f );
        fadeIn.setDuration(1200);
        fadeIn.setFillAfter(true);

        view.startAnimation(fadeIn);
    }

    public static void fadeOutView(View view) {
        AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f );
        fadeOut.setDuration(1200);
        fadeOut.setFillAfter(true);

        view.startAnimation(fadeOut);
    }

    public static void getUserDeliverablesCount(Context mContext, VolleyResponseListener volleyResponseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_USER_DELIVERABLES_COUNT, new Response.Listener<String>() {
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

    public static void getUserPassedModuleQuizAndGeneratedExercisesCount(Context mContext, String moduleID, VolleyResponseListener volleyResponseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Routes.URL_GET_USER_PASSED_MODULE_QUIZ_AND_GENERATED_EXERCISES_COUNT, new Response.Listener<String>() {
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

    public static void setUpOfflineModules(Context mContext) {
        deletePreviousModulesFromSF(mContext);

        ArrayList<Module> allModules = new ArrayList<>();
        ArrayList<String> modulesPathIndexes = new ArrayList<>();

        ModuleHandler.getInstance(mContext).getAllModules(new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getAllModules: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i ++) {
                        JSONObject object = array.getJSONObject(i);

                        allModules.add(new Module(object.getString("id"),
                                                  object.getString("title"),
                                                  object.getString("slug"),
                                                  object.getString("photo"),
                                                  object.getString("path_index"),
                                                  object.getString("description")));
                    }

                    saveModulesToSP(allModules, mContext);

                    // ===

                    ModuleHandler.getInstance(mContext).getModulesPathIndexes(new VolleyResponseListener() {
                        @Override
                        public void onError(String response) {
                            Log.e(TAG, "onError: getModulesPathIndexes: " + response);
                        }

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONArray arrayPathIndexes = new JSONArray(response);

                                for (int i = 0; i < arrayPathIndexes.length(); i ++) {
                                    JSONObject objectPathIndex = arrayPathIndexes.getJSONObject(i);
                                    String modulePathIndex = objectPathIndex.getString("path_index");

                                    if (!modulesPathIndexes.contains(modulePathIndex)) {
                                        modulesPathIndexes.add(modulePathIndex);
                                    }
                                }

                                Collections.sort(modulesPathIndexes);

                                SharedPreferences spModulesPathIndexes = mContext.getSharedPreferences(Constants.SP_MODULES_PATH_INDEXES, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor;
                                Gson gson = new Gson();

                                editor = spModulesPathIndexes.edit();
                                String modulesPathIndexesJson = gson.toJson(modulesPathIndexes);
                                editor.putString(Constants.ID, modulesPathIndexesJson);
                                editor.apply();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            // =====

                            setUpOfflineTopics(mContext);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void setUpOfflineTopicsPathIndexes(Context mContext) {
        ArrayList<Integer> moduleTopicsIndexes = new ArrayList<>();
        ArrayList<String> moduleTopicsIndexesString = new ArrayList<>();

        SharedPreferences spModulesInfo = mContext.getSharedPreferences(Constants.SP_MODULES_INFO, Context.MODE_PRIVATE);
        SharedPreferences spTopicsInfo = mContext.getSharedPreferences(Constants.SP_TOPICS_INFO, Context.MODE_PRIVATE);
        SharedPreferences spTopicsPathIndexes = mContext.getSharedPreferences(Constants.SP_TOPICS_PATH_INDEXES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        Gson gson = new Gson();

        Map<String,?> keys = spModulesInfo.getAll();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            gson = new Gson();

            String json = spModulesInfo.getString(entry.getKey(), null);
            String[] moduleInfo = gson.fromJson(json, String[].class);

            // === inner loop

            Map<String,?> keys__ = spTopicsInfo.getAll();
            for(Map.Entry<String,?> entry_ : keys__.entrySet()){
                gson = new Gson();

                String json_ = spTopicsInfo.getString(entry_.getKey(), null);
                String[] topicInfo = gson.fromJson(json_, String[].class);

                if (moduleInfo[0].equals(topicInfo[2])) {
                    moduleTopicsIndexes.add(Integer.parseInt(topicInfo[4]));
                }
            }

            Collections.sort(moduleTopicsIndexes);

            for (int i = 0; i < moduleTopicsIndexes.size(); i ++) {
                moduleTopicsIndexesString.add(String.valueOf(moduleTopicsIndexes.get(i)));
            }

            editor = spTopicsPathIndexes.edit();
            gson = new Gson();
            String moduleTopicsIndexJson = gson.toJson(moduleTopicsIndexesString);
            editor.putString(moduleInfo[0], moduleTopicsIndexJson);
            editor.apply();

            moduleTopicsIndexes.clear();
            moduleTopicsIndexesString.clear();
        }
    }

    private static void deletePreviousModulesFromSF(Context mContext) {
        mContext.getSharedPreferences(Constants.SP_MODULES_INFO, 0).edit().clear().apply();
        mContext.getSharedPreferences(Constants.SP_TOPICS_INFO, 0).edit().clear().apply();
    }

    private static void saveModulesToSP(ArrayList<Module> allModules, Context mContext) {
        ArrayList<String> moduleInfo = new ArrayList<>();

        SharedPreferences spModulesInfo = mContext.getSharedPreferences(Constants.SP_MODULES_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        Gson gson = new Gson();

        for (int i = 0; i < allModules.size(); i ++) {
            moduleInfo.add(allModules.get(i).getId());
            moduleInfo.add(allModules.get(i).getTitle());
            moduleInfo.add(allModules.get(i).getSlug());
            moduleInfo.add(allModules.get(i).getPhoto());
            moduleInfo.add(allModules.get(i).getPathIndex());
            moduleInfo.add(allModules.get(i).getDescription());

            editor = spModulesInfo.edit();
            String moduleInfoJson = gson.toJson(moduleInfo);
            editor.putString(allModules.get(i).getId(), moduleInfoJson);
            editor.apply();

            moduleInfo.clear();
        }
    }

    public static void setUpOfflineTopics(Context mContext) {
        ArrayList<Topic> allTopics = new ArrayList<>();

        TopicHandler.getInstance(mContext).getAllTopics(new VolleyResponseListener() {
            @Override
            public void onError(String response) {
                Log.e(TAG, "onError: getAllTopics: " + response);
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i ++) {
                        JSONObject object = array.getJSONObject(i);

                        allTopics.add(new Topic(object.getString("id"),
                                                object.getString("title"),
                                                object.getString("module_id"),
                                                object.getString("slug"),
                                                object.getString("path_index"),
                                                object.getString("mobile_content"),
                                                object.getString("description")));

                    }

                    saveTopicsToSP(allTopics, mContext);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static void saveTopicsToSP(ArrayList<Topic> allTopics, Context mContext) {
        ArrayList<String> moduleTopicInfo = new ArrayList<>();

        SharedPreferences spTopicsInfo = mContext.getSharedPreferences(Constants.SP_TOPICS_INFO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        Gson gson = new Gson();

        for (int i = 0; i < allTopics.size(); i ++) {
            moduleTopicInfo.add(allTopics.get(i).getId());
            moduleTopicInfo.add(allTopics.get(i).getTitle());
            moduleTopicInfo.add(allTopics.get(i).getModuleID());
            moduleTopicInfo.add(allTopics.get(i).getSlug());
            moduleTopicInfo.add(allTopics.get(i).getPathIndex());
            moduleTopicInfo.add(allTopics.get(i).getContent());
            moduleTopicInfo.add(allTopics.get(i).getDescription());

            editor = spTopicsInfo.edit();
            String moduleTopicInfoJson = gson.toJson(moduleTopicInfo);
            editor.putString(allTopics.get(i).getId(), moduleTopicInfoJson);
            editor.apply();

            moduleTopicInfo.clear();
        }

        setUpOfflineTopicsPathIndexes(mContext);
    }

    public static void saveFile(Context mContext, String script) {
        FileOutputStream fos = null;

        try {
            script = script.replaceAll("", "");

            fos = mContext.openFileOutput(Constants.MAIN_SCRIPT_FILE_NAME + Constants.TXT_EXTENSION, mContext.MODE_PRIVATE);
            fos.write(script.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String loadFile(Context mContext, String fileName) {
        FileInputStream fis = null;
        String modifiedScript = "";

        try {
            fis = mContext.openFileInput(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String strLine;
            String textFile = "";

            while ((strLine = br.readLine()) != null) {
                textFile = textFile + replaceComments(strLine) + "\n";
            }

            modifiedScript = textFile
                    .replaceAll("\n", "")
                    .replaceAll("  ", "")
                    .replaceAll("\t", "")
                    .replaceAll("\"", "\\\\" + "\"")
                    .replaceAll("\\\\\\\\", "\\\\\\\\\\\\\\\\")
                    .replaceAll("\\\\n", "\\\\\\\\n")
                    .replaceAll("\\)", ") ")
                    .replaceAll("else", "else ")
                    .replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)", "");

            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return modifiedScript;
    }

    private static String replaceComments(String strLine) {
        if (strLine.startsWith("//")) {
            return "";
        } else if (strLine.contains("//")) {
            if (strLine.contains("\"")) {
                int lastIndex = strLine.lastIndexOf("\"");
                int lastIndexComment = strLine.lastIndexOf("//");
                if (lastIndexComment > lastIndex) { // ( "" // )
                    strLine = strLine.substring(0, lastIndexComment);
                }
            } else {
                int index = strLine.lastIndexOf("//");
                strLine = strLine.substring(0, index);
            }
        }

        return strLine;
    }

    public static String getUserCode(Context mContext, String fileName) {
        FileInputStream fis = null;
        String userCode = "";

        try {
            fis = mContext.openFileInput(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String strLine;

            while ((strLine = br.readLine()) != null) {
                userCode = userCode + strLine + "\n";
            }

            br.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return userCode;
    }
}
