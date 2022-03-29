package com.example.expresso.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.example.expresso.R;
import com.example.expresso.interfaces.VolleyResponseListener;
import com.example.expresso.utils.CertificateHandler;
import com.example.expresso.utils.Constants;
import com.example.expresso.utils.Generals;
import com.example.expresso.utils.ModuleHandler;
import com.google.android.youtube.player.YouTubeBaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Certificate extends YouTubeBaseActivity {
    // constants
    private static final String TAG = "Certificate";
    private final Context thisContext = Certificate.this;

    // views
    private Toolbar tbCertificates;
    private TextView tvCertificatesMessage, tvCertificatesVerificationMessage;
    private EditText etCertificatesUserName;
    private Button btnCertificatesConfirmUserName;
    private LinearLayout llCertificatesLoading, llCertificatesVerificationContainer, llCertificatesNoInternetConnection;

    // variables
    private String allModulesCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate);

        initAll();

        btnCertificatesConfirmUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etCertificatesUserName.getText().toString().equals("")) {
                    Generals.makeToast(thisContext, "Name cannot be blank");
                } else {
                    tvCertificatesMessage.setVisibility(View.VISIBLE);
                    tvCertificatesMessage.setText("Congratulations, your certificate has been sent to your email! Thank you for choosing Expresso as your guide in your learning journey with Java OOP concepts. Expresso would like you to know that they’re glad you took the time to learn more and enhance your skills!");
                    etCertificatesUserName.setEnabled(false);
                    etCertificatesUserName.setAlpha(0.3f);
                    btnCertificatesConfirmUserName.setVisibility(View.GONE);
                    createCertificate(etCertificatesUserName.getText().toString());
                }
            }
        });
    }

    private void createCertificate(String name) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint namePaint = new Paint();

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1651, 1201, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.certificate_template);
        Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, 1651, 1201, false);
        canvas.drawBitmap(scaleBitmap, 0, 0, paint);

        int textSize = 84;

        for (int i = 0; i < name.length(); i ++) {
            if (i > 20) {
                textSize -= 2;
            }
        }

        namePaint.setTextAlign(Paint.Align.CENTER);
        namePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        namePaint.setTextSize(textSize);

        canvas.drawText(name, (1651/2) + ((1651/2)/2) - (((1651/2)/2)/2) , (1201/2) - (1201/24), namePaint);
        pdfDocument.finishPage(page);

        FileOutputStream fos = null;

        try {
            fos = thisContext.openFileOutput(Constants.MAIN_CERTIFICATE_FILE_NAME + Constants.PDF_EXTENSION, thisContext.MODE_PRIVATE);
            pdfDocument.writeTo(fos);

            sendCertificate(Constants.MAIN_CERTIFICATE_FILE_NAME + Constants.PDF_EXTENSION);
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

        pdfDocument.close();
    }

    private void sendCertificate(String fileName) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(Constants.SYSTEM_EMAIL, Constants.SYSTEM_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(Constants.SYSTEM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(Constants.EMAIL));

            // subject
            message.setSubject("Expresso Certificate of Completion");

            MimeMultipart multipart = new MimeMultipart();

            MimeBodyPart attachment = new MimeBodyPart();
            attachment.attachFile(new File(getFilesDir() + "/" + fileName));

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent("<p>Congratulations, here’s your certificate! \uD83E\uDD73\n" + "Thank you for choosing Expresso as your guide in your learning journey with Java OOP concepts. Expresso would like you to know that they’re glad you took the time to learn more and enhance your skills!</p>", "text/html");
            multipart.addBodyPart(messageBodyPart);
            multipart.addBodyPart(attachment);

            message.setContent(multipart);

            // async task
            new SendMail().execute(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SendMail extends AsyncTask<Message, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Message... messages) {
            try {
                Transport.send(messages[0]);

                CertificateHandler.getInstance(thisContext).addCertificate(etCertificatesUserName.getText().toString(), new VolleyResponseListener() {
                    @Override
                    public void onError(String response) {
                        Log.e(TAG, "onError: addCertificate: " + response);
                    }

                    @Override
                    public void onResponse(String response) {
                        // do nothing
                    }
                });

                return "Success";
            } catch (MessagingException e) {
                e.printStackTrace();
                return "Error";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    private void initAll() {
        // views
        tbCertificates = findViewById(R.id.mtb_certificates);
        etCertificatesUserName = findViewById(R.id.et_certificates_user_name);
        btnCertificatesConfirmUserName = findViewById(R.id.btn_certificates_confirm_user_name);
        llCertificatesLoading = findViewById(R.id.ll_certificates_loading);
        llCertificatesVerificationContainer = findViewById(R.id.ll_certificates_verification_container);
        llCertificatesNoInternetConnection = findViewById(R.id.ll_certificates_no_internet_connection);
        tvCertificatesMessage = findViewById(R.id.tv_certificates_message);
        tvCertificatesVerificationMessage = findViewById(R.id.tv_certificates_verification_message);

        // listeners
        tbCertificates.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (llCertificatesLoading.getVisibility() == View.GONE) {
                    startActivity(new Intent(thisContext, Home.class));
                }
            }
        });

        hideViews();
        llCertificatesLoading.setVisibility(View.VISIBLE);

        if (!Generals.checkInternetConnection(thisContext)) {
            llCertificatesLoading.setVisibility(View.GONE);
            llCertificatesNoInternetConnection.setVisibility(View.VISIBLE);
        } else {
            ModuleHandler.getInstance(thisContext).getAllModulesAndUserModulesCount(new VolleyResponseListener() {
                @Override
                public void onError(String response) {
                    Log.e(TAG, "onError: getAllModulesAndUserModulesCount: " + response);
                }

                @Override
                public void onResponse(String response) {
                    try {
                        JSONArray arrayModuleCounts = new JSONArray(response);
                        JSONObject objectModuleCount = arrayModuleCounts.getJSONObject(0);

                        String allModulesCountResponse = objectModuleCount.getString("allModulesCount");
                        String userDoneModulesCountResponse = objectModuleCount.getString("userDoneModulesCount");

                        allModulesCount = String.valueOf(Integer.parseInt(allModulesCountResponse) - 1);

                        if (!userDoneModulesCountResponse.equals(allModulesCount)) {
                            tvCertificatesVerificationMessage.setText("Oops! your certificate is not yet ready.\nFinish all the modules first to get your very own Expresso certificate.");
                            llCertificatesVerificationContainer.setVisibility(View.VISIBLE);
                            llCertificatesLoading.setVisibility(View.GONE);
                        } else {
                            CertificateHandler.getInstance(thisContext).getCertificateName(new VolleyResponseListener() {
                                @Override
                                public void onError(String response) {
                                    Log.e(TAG, "onError: checkCertificate: " + response);
                                }

                                @Override
                                public void onResponse(String response) {
                                    String name = response;

                                    if (response.equals("")) {
                                        // do nothing
                                    } else {
                                        etCertificatesUserName.setText(name);
                                        etCertificatesUserName.setEnabled(false);
                                        etCertificatesUserName.setAlpha(0.3f);
                                    }

                                    llCertificatesVerificationContainer.setVisibility(View.GONE);
                                    llCertificatesLoading.setVisibility(View.GONE);
                                    showViews();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void hideViews() {
        etCertificatesUserName.setVisibility(View.GONE);
        btnCertificatesConfirmUserName.setVisibility(View.GONE);
    }

    private void showViews() {
        etCertificatesUserName.setVisibility(View.VISIBLE);
        btnCertificatesConfirmUserName.setVisibility(View.VISIBLE);
    }
}