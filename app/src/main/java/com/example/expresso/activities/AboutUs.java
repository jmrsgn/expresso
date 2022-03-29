package com.example.expresso.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expresso.R;

public class AboutUs extends AppCompatActivity {
    // constants
    private static final String TAG = "AboutUs";
    private final Context thisContext = AboutUs.this;

    // views
    private ImageView ivAboutUsBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        initAll();

        ivAboutUsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(thisContext, Home.class));
            }
        });
    }

    private void initAll() {
        ivAboutUsBack = findViewById(R.id.iv_about_us_back);
    }
}