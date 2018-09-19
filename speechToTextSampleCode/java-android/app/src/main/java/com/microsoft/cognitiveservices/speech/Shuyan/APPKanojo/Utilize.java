package com.microsoft.cognitiveservices.speech.Shuyan.APPKanojo;

public class Utilize {


    public String FixRecognizedTextForamt(String mataString)
    {
        String result = mataString;
        result = result.substring(1);
        result = result.substring(0, result.length()-3);

        return result;
    }
}
