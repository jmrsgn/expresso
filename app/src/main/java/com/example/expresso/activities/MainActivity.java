package com.example.expresso.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expresso.R;
import com.example.expresso.utils.Constants;
import com.example.expresso.utils.SessionHandler;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    // constants
    private static final String TAG = "MainActivity";
    private final Context thisContext = MainActivity.this;
    private final int RC_SIGN_IN = 0;

    // views
    private Button btnMainGoogleLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: figure out why 2-factor auth not working in mobile
        // update: 12-03-21

        initAll();

        btnMainGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btn_main_continue_with_google) {
                    SessionHandler.getInstance(thisContext).signIn();
                    startActivityForResult(SessionHandler.intent, RC_SIGN_IN);
                } else {
                    throw new IllegalStateException("Unexpected value: " + v.getId());
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            SessionHandler.getInstance(thisContext).handleSignInResult(task);

            if (!Constants.IS_EMAIL_IN_DOMAIN) {
                showEmailDeclinedDialog();
            }
        }
    }

    private void initAll() {
        // views
        btnMainGoogleLogin = findViewById(R.id.btn_main_continue_with_google);

        SessionHandler.getInstance(thisContext).checkUserIfLoggedIn();
    }

    private void showEmailDeclinedDialog() {
        Dialog emailDeclinedDialog = new Dialog(thisContext);
        emailDeclinedDialog.setContentView(R.layout.alert_box_email_declined);

        emailDeclinedDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        emailDeclinedDialog.show();
    }
}