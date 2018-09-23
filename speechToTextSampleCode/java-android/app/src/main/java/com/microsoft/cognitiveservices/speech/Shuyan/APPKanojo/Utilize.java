package com.microsoft.cognitiveservices.speech.Shuyan.APPKanojo;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Utilize {


    public String FixRecognizedTextForamt(String mataString) {
        String result = mataString;
        result = result.substring(1);
        result = result.substring(0, result.length() - 3);

        return result;
    }

    //end recoginzed text into list and return
    public List<String> putRecognizedTextIntoList(String recgnizedMessage) {
        recgnizedMessage = recgnizedMessage.substring(0,recgnizedMessage.length()-1);
        String[] recgnizedMessageArray = recgnizedMessage.split(" ");
        List<String> recgnizedMessageList = new ArrayList<String>();

        for (int i = 0; i < recgnizedMessageArray.length; i++) {
            recgnizedMessageList.add(recgnizedMessageArray[i]);
        }
        return recgnizedMessageList;
    }//end putRecognizedTextIntoList

    public String macthApp(List<String> recoginezedTextList, List<String> appList) {
        Log.d("Sven", "Utilize.recoginezedTextList: ");
        for (String text : recoginezedTextList) {
            for (String app : appList) {
                if (app.matches(text)) {
                    return app;
                }
            }
        }

        //-----------------if specific----------------------


        return "";
    }

    public String handleSpecificAppName(List<String> recoginezedTextList) {
        Log.d("Sven", "Utilize.handleSpecificAppName: ");

        for (String text : recoginezedTextList) {
            Log.d("Sven", "Utilize.handleSpecificAppName.recoginezedTextList: " + text);
            if (text.matches("calendar")) return "com.android.calendar";
            if (text.matches("calculator")) return "com.android.calculator2";
        }
        return "";
    }


}
