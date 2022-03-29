package com.example.expresso.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.expresso.R;
import com.example.expresso.glideSVG.GlideApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TopicPlaygroundHandler {
    // constants
    private static final String TAG = "Generals";

    // general
    private static void showOutputLoading(Context mContext) {
        LinearLayout llTopicPlaygroundFragmentOutputLoading = ((Activity) mContext).findViewById(R.id.ll_topic_playground_fragment_output_loading);
        llTopicPlaygroundFragmentOutputLoading.setVisibility(View.VISIBLE);
    }

    private static void hideOutputLoading(Context mContext) {
        LinearLayout llTopicPlaygroundFragmentOutputLoading = ((Activity) mContext).findViewById(R.id.ll_topic_playground_fragment_output_loading);
        llTopicPlaygroundFragmentOutputLoading.setVisibility(View.GONE);
    }

    // compilation
    public static void compileScript(Context mContext, String script) {
        Generals.saveFile(mContext, script);
        String modifiedScript = Generals.loadFile(mContext, Constants.MAIN_SCRIPT_FILE_NAME + Constants.TXT_EXTENSION);
        showOutputLoading(mContext);
        new Compile(mContext, modifiedScript).execute();
    }

    private static class Compile extends AsyncTask<Void, Void, Void> {
        protected String script;
        protected final Handler handler;
        protected Context mContext;

        public Compile(Context mContext, String script) {
            this.script = script;
            this.mContext = mContext;
            handler = new Handler(mContext.getMainLooper());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(Routes.URL_COMPILE);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");

                // views
                EditText etTopicPlaygroundFragmentInputs = (EditText) ((Activity) mContext).findViewById(R.id.et_topic_playground_fragment_inputs);

                String input = "";

                if (!etTopicPlaygroundFragmentInputs.getText().equals("")) {
                    input = "{\"script\":\"" + script + "\",\"stdin\":\"" + etTopicPlaygroundFragmentInputs.getText().toString().replaceAll("\n", "\\\\n") + "\"} ";
                } else {
                    input = "{\"script\":\"" + script + "\"} ";
                }

                System.out.println(input);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(input.getBytes());
                outputStream.flush();

                BufferedReader bufferedReader;
                bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));

                String output = "";

                while ((output = bufferedReader.readLine()) != null) {
                    System.out.println(output);
                    JSONObject object = new JSONObject(output);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView tvTopicPlaygroundFragmentOutput = (TextView) ((Activity) mContext).findViewById(R.id.tv_topic_playground_fragment_output);
                            TextView tvTopicPlaygroundFragmentMemoryAllocated = (TextView) ((Activity) mContext).findViewById(R.id.tv_topic_playground_fragment_memory_allocated);
                            TextView tvTopicPlaygroundFragmentCPUTime = (TextView) ((Activity) mContext).findViewById(R.id.tv_topic_playground_fragment_cpu_time);

                            try {
                                tvTopicPlaygroundFragmentOutput.setVisibility(View.VISIBLE);
                                tvTopicPlaygroundFragmentMemoryAllocated.setVisibility(View.VISIBLE);
                                tvTopicPlaygroundFragmentCPUTime.setVisibility(View.VISIBLE);

                                tvTopicPlaygroundFragmentOutput.setText(object.getString("output"));
                                tvTopicPlaygroundFragmentMemoryAllocated.setText("\nMemory allocated: " + object.getString("memory"));
                                tvTopicPlaygroundFragmentCPUTime.setText("CPU time: " + object.getString("cpuTime") + "ms");

                                hideOutputLoading(mContext);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                connection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private void runOnUiThread(Runnable r) {
            handler.post(r);
        }
    }


    // visualization
    public static void visualizeScript(Context mContext, String script) {
        Generals.saveFile(mContext, script);
        String modifiedScript = Generals.loadFile(mContext, Constants.MAIN_SCRIPT_FILE_NAME + Constants.TXT_EXTENSION);
        new Visualize(mContext, modifiedScript).execute();
    }

    private static class Visualize extends AsyncTask<Void, Void, Void> {
        protected String script;
        protected final Handler handler;
        protected Context mContext;

        public Visualize(Context mContext, String script) {
            this.script = script;
            this.mContext = mContext;
            handler = new Handler(mContext.getMainLooper());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(Routes.URL_VISUALIZE);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");

                String input = "{\"script\":\"" + script + "\"} ";

                System.out.println(input);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(input.getBytes());
                outputStream.flush();

                BufferedReader bufferedReader;
                bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));

                String output = "";

                while ((output = bufferedReader.readLine()) != null) {
                    System.out.println(output);
                    JSONObject object = new JSONObject(output);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                showVisualizedCodeDialog(mContext, object.getString("url"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                connection.disconnect();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        private void runOnUiThread(Runnable r) {
            handler.post(r);
        }
    }

    private static void showVisualizedCodeDialog(Context mContext, String url) {
        Dialog visualizeCodeDialog = new Dialog(mContext);
        visualizeCodeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        visualizeCodeDialog.getWindow().setBackgroundDrawableResource(R.drawable.visualized_code_background);
        visualizeCodeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView ivVisualizedCode = new ImageView(mContext);

        String modifiedURL = url.replaceAll("https", "https").replaceAll("http", "https");

        GlideApp.with(mContext)
                .load(modifiedURL)
                .centerCrop()
                .into(ivVisualizedCode);

        visualizeCodeDialog.addContentView(ivVisualizedCode, new RelativeLayout.LayoutParams(1000, 300));
        visualizeCodeDialog.show();
    }
}
