package com.example.expresso.interfaces;

public interface VolleyResponseListener {
    void onError(String response);
    void onResponse(String response);
}
