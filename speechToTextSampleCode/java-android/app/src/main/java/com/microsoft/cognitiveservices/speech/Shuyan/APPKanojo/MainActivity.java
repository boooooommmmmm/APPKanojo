//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE.md file in the project root for full license information.
//
// <code>
package com.microsoft.cognitiveservices.speech.Shuyan.APPKanojo;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.microsoft.cognitiveservices.speech.RecognitionStatus;
import com.microsoft.cognitiveservices.speech.SpeechFactory;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            Log.e("Sven", "unexpected " + ex.getMessage());
            assert (false);
        }
    }//end on create


    //when start button has been clicked
    public void onSpeechButtonClicked(View v) throws InterruptedException {
        TextView txt = (TextView) this.findViewById(R.id.textView_mainActivity_messageTextView); // mapping message text view to txt
        Handler handler = new Handler();

        txt.setVisibility(View.INVISIBLE);
        displayPopUpWindow();

        handler.postDelayed(new Runnable() {
            public void run() {
                startSpeechRecognition();
            }
        }, 50);


    }// end onSpeechButtonClicked


    //return a list of install apps
    private List<String> getInstalledAppsList() {
        final PackageManager pm = getPackageManager();

        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<String> appList = new ArrayList<String>();
        installedAppsNameMap = new HashMap<String, String>();
        String packageShortName = "";
        String packageFullName = "";

        for (ApplicationInfo packageInfo : packages) {
            String[] packageFullNameArray = packageInfo.packageName.split("\\.");
            packageShortName = packageFullNameArray[packageFullNameArray.length-1];

            appList.add(packageShortName);
            installedAppsNameMap.put(packageShortName, packageFullName);
//            Log.e("Sven", "Source dir : " + packageInfo.sourceDir);
//            Log.e("Sven", "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
        }
        return  appList;
    }//end list installed apps

    private void startSpeechRecognition(){
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
                recgnizedMessage = result.getText();
                recgnizedMessageList = utilize.putRecognizedTextIntoList(recgnizedMessage);

                txt.setText(recgnizedMessage);//format:<word1 word2 word3 ... wordn.>.
                Log.d("Sven","STT: " + result.getText());

                //process the result
                String opeartionResult = startMatchingOpeartion();

            }

            //else if does not recognize anything
            else{
                txt.setText("Error recognizing. Please check microphone" + System.lineSeparator() + "Reason: " + result.getReason()
                        + System.lineSeparator() + "error: " + result.getErrorDetails()
                        + System.lineSeparator() + "result: " + result.toString());
            }

            reco.close();
            factory.close();


            //----------------------for test only--------------
            recgnizedMessage = "calendar";
            txt.setText("");
            txt.setText(startMatchingOpeartion());
            //-------------------------------------------------
        } catch (Exception ex) {
            Log.e("Sven", "unexpected " + ex.getMessage());
            assert (false);
        }
    }


    private String startMatchingOpeartion (){
        String returnResult = "";
        String mathcedAppName = utilize.macthApp(recgnizedMessageList, installedAppsList);
        if(mathcedAppName == "" || mathcedAppName == null){
            returnResult = "app not find";
        }else{
            returnResult = "app find!: " + mathcedAppName;
        }


        String matchedAppFullName = installedAppsNameMap.get(mathcedAppName);
        openTheSpecificApp(matchedAppFullName);

        return returnResult;
    }

    private void openTheSpecificApp (String appFullName){
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(appFullName);
        if (launchIntent != null) {
            startActivity(launchIntent);//null pointer check in case package name was not found
        }
    }

    private void displayPopUpWindow() throws InterruptedException {
        Handler handler = new Handler();
        TextView pupTextView = (TextView) this.findViewById(R.id.textView_mainActivity_popUpTextView);
        pupTextView.setVisibility(View.VISIBLE);

        pupTextView.setText("I am listening! ......");

//        handler.postDelayed(new Runnable() {
//            public void run() {
//                displayPopUpWindowHepler("I am listening!.");
//            }
//        }, 1000);
//
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                displayPopUpWindowHepler("I am listening!..");
//            }
//        }, 1000);


    }

    private void displayPopUpWindowHepler(String message){
        TextView pupTextView = (TextView) this.findViewById(R.id.textView_mainActivity_popUpTextView);
        pupTextView.setText(message);
    }




}// end main activity
