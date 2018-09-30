//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//
// <code>
package com.shuyan.appkanojo;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.microsoft.cognitiveservices.speech.RecognitionStatus;
import com.microsoft.cognitiveservices.speech.Shuyan.appkanojo.R;
import com.microsoft.cognitiveservices.speech.SpeechFactory;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Future;

import static android.Manifest.permission.*;

public class MainActivity extends AppCompatActivity {

    // Replace below with your own subscription key
    private static String speechSubscriptionKey = "f1229ac024184f1eb56941a0ea3c7484";//
    // Replace below with your own service region (e.g., "westus").
    private static String serviceRegion = "westus";

    private List<String> installedAppsList;
    private Map<String, String> installedAppsNameMap;//short name, full name
    private String recgnizedMessage;
    private List<String> recgnizedMessageList;

    private Utilize utilize = new Utilize();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Sven", "mainActivity.onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playWelcomeAudio();

        // Note: we need to request the permissions
        int requestCode = 5; // unique code for the permission request
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, INTERNET}, requestCode);

        //list all installed apps
        installedAppsList = getInstalledAppsList();

        //build sppech factory
        try {
            // Note: Configure native platform binding. This currently configures the directory
            //       in which to store certificates required to access the speech service.
            //       It is required to call this once after app start.
            SpeechFactory.configureNativePlatformBindingWithDefaultCertificate();
        } catch (Exception ex) {
            Log.d("Sven", "unexpected " + ex.getMessage());
            assert (false);
        }
    }//end on create


    //when start button has been clicked
    public void onSpeechButtonClicked(View v) throws InterruptedException {
        Log.d("Sven", "mainActivity.onSpeechButtonClicked: ");
        TextView txt = (TextView) this.findViewById(R.id.textView_mainActivity_messageTextView); // mapping message text view to txt

        Handler handler = new Handler();

        txt.setVisibility(View.INVISIBLE);
        displayPopUpWindow();

        handler.postDelayed(new Runnable() {
            public void run() {
                startSpeechRecognition();
            }
        }, 100);


    }// end onSpeechButtonClicked


    //return a list of install apps
    private List<String> getInstalledAppsList() {
        Log.d("Sven", "mainActivity.getInstalledAppsList: ");
        final PackageManager pm = getPackageManager();

        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<String> appList = new ArrayList<String>();
        installedAppsNameMap = new HashMap<String, String>();
        String packageShortName = "";
        String packageFullName = "";

        for (ApplicationInfo packageInfo : packages) {
            packageFullName = packageInfo.packageName;
            String[] packageFullNameArray = packageFullName.split("\\.");
            packageShortName = packageFullNameArray[packageFullNameArray.length - 1];

            appList.add(packageShortName);
            installedAppsNameMap.put(packageShortName, packageFullName);
//            Log.d("Sven", "Source dir : " + packageInfo.sourceDir);
//            Log.d("Sven", "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
            Log.d("Sven", "mainActivity.packageFullName: " + packageFullName);
            Log.d("Sven", "mainActivity.packageShortName: " + packageShortName);
        }

        return appList;
    }//end list installed apps

    private void startSpeechRecognition() {
        Log.d("Sven", "mainActivity.startSpeechRecognition: ");
        //temporary
        TextView txt = (TextView) this.findViewById(R.id.textView_mainActivity_messageTextView); // mapping message text view to txt

        recgnizedMessage = "";
        recgnizedMessageList = new ArrayList<String>();
        try {
            //invoke speech SDK
            SpeechFactory factory = SpeechFactory.fromSubscription(speechSubscriptionKey, serviceRegion);
            assert (factory != null);

            SpeechRecognizer reco = factory.createSpeechRecognizer();
            assert (reco != null);

            Future<SpeechRecognitionResult> task = reco.recognizeAsync();
            assert (task != null);

            // Note: this will block the UI thread, so eventually, you want to
            //        register for the event (see full samples)
            SpeechRecognitionResult result = task.get();
            assert (result != null);

            //if successfully recognized.
            if (result.getReason() == RecognitionStatus.Recognized) {
                recgnizedMessage = result.getText();//format:<word1 word2 word3 ... wordn.>.
                recgnizedMessageList = utilize.putRecognizedTextIntoList(recgnizedMessage);

                Log.d("Sven", "mainActivity.startSpeechRecognitionSTT.recgnizedMessage: " + result.getText());

                //process the result
                startMatchingOpeartion();
            }

            //else if does not recognize anything
            else {
                txt.setText("Error recognizing. Please check microphone" + System.lineSeparator() + "Reason: " + result.getReason()
                        + System.lineSeparator() + "error: " + result.getErrorDetails()
                        + System.lineSeparator() + "result: " + result.toString());
            }

            reco.close();
            factory.close();

        } catch (Exception ex) {
            Log.d("Sven", "unexpected " + ex.getMessage());
            assert (false);
        }
    }


    private void startMatchingOpeartion() {
        Log.d("Sven", "mainActivity.startMatchingOpeartion: ");
        String specificAppName = "";
        String mathcedAppName = "";

        specificAppName = utilize.handleSpecificAppName(recgnizedMessageList);
        Log.d("Sven", "mainActivity.startMatchingOpeartion.recoginze specificAppName: " + specificAppName);
        if (!specificAppName.equals("")) {
            displayPopUpWindowHepler("I am opening!");
            openTheSpecificApp(specificAppName);
            playSuccessfulAudio();
        } else {
            mathcedAppName = utilize.macthApp(recgnizedMessageList, installedAppsList);
            if (mathcedAppName == "" || mathcedAppName == null) {
                Log.d("Sven", "mainActivity.startMatchingOpeartion: 404");
                displayPopUpWindowHepler("I cannot find it QAQ");
                playFaildAudio();
            } else {
                Log.d("Sven", "mainActivity.startMatchingOpeartion: find " + mathcedAppName);
                startHandleResult(mathcedAppName);
            }
        }

    }

    private void startHandleResult(String result) {
        Log.d("Sven", "mainActivity.startHandleResult: " + result);

        String matchedAppFullName = installedAppsNameMap.get(result);
        Log.d("Sven", "mainActivity.startHandleResult: find " + matchedAppFullName);

        displayPopUpWindowHepler("I am opening!");
        openTheSpecificApp(matchedAppFullName);
        playSuccessfulAudio();
    }

    private void openTheSpecificApp(String appFullName) {
        Log.d("Sven", "mainActivity.openTheSpecificApp: " + appFullName);
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(appFullName);
        if (launchIntent != null) {
            try {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);//null pointer check in case package name was not found
                Log.d("Sven", "mainActivity.openTheSpecificApp: open " + launchIntent.toString());
            } catch (Exception e) {
                Log.d("Sven", "mainActivity.openTheSpecificApp: Exception " + e.getMessage());
            }
        } else {
            Log.d("Sven", "mainActivity.openTheSpecificApp: Intent empty! ");
        }
    }

    private void displayPopUpWindow() throws InterruptedException {
        Log.d("Sven", "mainActivity.displayPopUpWindow: ");
        Handler handler = new Handler();
        TextView pupTextView = (TextView) this.findViewById(R.id.textView_mainActivity_popUpTextView);
        pupTextView.setVisibility(View.VISIBLE);

        pupTextView.setText("I am listening! ......");

    }

    //debugging function
    private void displayPopUpWindowHepler(String message) {
        Log.d("Sven", "mainActivity.displayPopUpWindowHepler: " + message);
        TextView pupTextView = (TextView) this.findViewById(R.id.textView_mainActivity_popUpTextView);
        pupTextView.setText(message);
    }

    private void playWelcomeAudio() {
        Log.d("Sven", "mainActivity.playWelcomeAudio: ");
        Random rd = new java.util.Random();
        MediaPlayer mediaPlayer;
        if ((rd.nextInt(2) + 1) == 2) {
            mediaPlayer = MediaPlayer.create(this, R.raw.welcome_2);
        } else {
            mediaPlayer = MediaPlayer.create(this, R.raw.welcome_1);
        }

        if (!mediaPlayer.isPlaying()) mediaPlayer.start();

        else if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.start();
        }
    }//end playWelcomeAudio

    private void playSuccessfulAudio() {
        Log.d("Sven", "mainActivity.playSuccessfulAudio: ");
        Random rd = new java.util.Random();
        MediaPlayer mediaPlayer;
        if ((rd.nextInt(2) + 1) == 2) {
            mediaPlayer = MediaPlayer.create(this, R.raw.successful_1);
        } else {
            mediaPlayer = MediaPlayer.create(this, R.raw.successful_2);
        }

        if (!mediaPlayer.isPlaying()) mediaPlayer.start();

        else if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.start();
        }
    }//end playSuccessfulAudio

    private void playFaildAudio() {
        Log.d("Sven", "mainActivity.playFaildAudio: ");
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(this, R.raw.failed_1);

        if (!mediaPlayer.isPlaying()) mediaPlayer.start();

        else if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.start();
        }
    }//end playSuccessfulAudio


}// end main activity
