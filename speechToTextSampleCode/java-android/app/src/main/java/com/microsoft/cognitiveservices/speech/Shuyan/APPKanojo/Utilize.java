package com.microsoft.cognitiveservices.speech.Shuyan.APPKanojo;

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
        String[] recgnizedMessageArray = recgnizedMessage.split(" ");
        List<String> recgnizedMessageList = new ArrayList<String>();

        for (int i = 0; i < recgnizedMessageArray.length - 1; i++) {
            recgnizedMessageList.add(recgnizedMessageArray[i]);
        }
        return recgnizedMessageList;
    }//end putRecognizedTextIntoList

    public String macthApp(List<String> recoginezedTextList, List<String> appList) {



        for (String text : recoginezedTextList) {
            //calendar
            if(text.equals("calendar")) return "com.android.calendar";

            for (String app : appList) {
                if (app.matches(text)) {
                    return app;
                }
            }
            if(text.equals("calculator")) return "com.android.calculator2";
        }

        return "";
    }


}
