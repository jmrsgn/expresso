package com.example.expresso.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.expresso.activities.Home;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SessionHandler {
    // constants
    private static SessionHandler instance;
    private final Context mContext;
    private static final String TAG = "SessionHandler";

    // google oauth
    private GoogleSignInOptions options;
    private GoogleSignInClient client;
    private GoogleSignInAccount account;
    public static Intent intent;

    private void initAll() {
        options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Constants.CLIENT_OAUTH_KEY)
                .requestId()
                .requestEmail()
                .requestProfile()
                .build();

        client = GoogleSignIn.getClient(mContext, options);
    }

    public void signIn() {
        intent = client.getSignInIntent();
    }

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);
            updateUI(account);
        } catch (ApiException e) {
            Generals.makeToast(mContext, e.toString());
            updateUI(null);
        }
    }

    public void checkUserIfLoggedIn() {
        account = GoogleSignIn.getLastSignedInAccount(mContext);

        if (account != null) {
            updateUI(account);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            if (checkIfEmailInDomain(account.getEmail())) {
                Constants.IS_EMAIL_IN_DOMAIN = true;

                UserHandler.getInstance(mContext).setUserInfo(account);

                if (!Generals.checkInternetConnection(mContext)) {
                    mContext.startActivity(new Intent(mContext, Home.class));

                    UserHandler.getInstance(mContext).logLoggedInToTheSystem();
                } else {
                    UserHandler.getInstance(mContext).checkExistingUserInDB(new VolleyResponseListener() {
                        @Override
                        public void onError(String response) {
                            Log.e(TAG, "onError: checkExistingUserInDB: " + response);
                        }

                        @Override
                        public void onResponse(String response) {
                            if (response.equals("Failed")) {
                                UserHandler.getInstance(mContext).registerUser(new VolleyResponseListener() {
                                    @Override
                                    public void onError(String response) {
                                        Log.e(TAG, "onError: registerUser: " + response);
                                    }

                                    @Override
                                    public void onResponse(String response) {
                                        // only works at new users

                                        QuizHandler.getInstance(mContext).addQuiz("1", new VolleyResponseListener() {
                                            @Override
                                            public void onError(String response) {
                                                Log.e(TAG, "onError: addQuiz: " + response);
                                            }

                                            @Override
                                            public void onResponse(String response) {
                                                // do nothing
                                            }
                                        });
                                    }
                                });
                            }

                            UserHandler.getInstance(mContext).getRoleID(new VolleyResponseListener() {
                                @Override
                                public void onError(String response) {
                                    Log.e(TAG, "onError: getRoleID: " + response);
                                }

                                @Override
                                public void onResponse(String response) {
                                    if (response.equals("2")) {
                                        mContext.startActivity(new Intent(mContext, Home.class));

                                        UserHandler.getInstance(mContext).logLoggedInToTheSystem();
                                    } else {
                                        Generals.makeToast(mContext, "Admin not allowed");
                                        signOut();
                                    }
                                }
                            });
                        }
                    });
                }
            } else {
                signOut();
                Constants.IS_EMAIL_IN_DOMAIN = false;
            }
        }
    }

    private Boolean checkIfEmailInDomain(String email) {
        String[] splitEmail = email.split("@");
        String domain = splitEmail[1];

        return domain.equals("my.jru.edu");
    }

    public void signOut() {
        client.signOut()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // do nothing
                    }
                });
    }

    private SessionHandler(Context context) {
        mContext = context;
        initAll();
    }

    // public
    public static SessionHandler getInstance(Context context) {
        if (instance == null) {
            instance = new SessionHandler(context);
        }

        return instance;
    }
}
