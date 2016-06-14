package com.monte.bluetoothcar.voiceMagic;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by monte on 23/02/16.
 */
public class VoiceMagic {

    private ArrayList<String> magicList;

    public VoiceMagic() {
        initialiseMagic();
    }

    private void initialiseMagic() {

        magicList = new ArrayList<String>();
        magicList.add("set temperature kitchen");
        magicList.add("get temperature kitchen");
        magicList.add("what temperature kitchen");
        magicList.add("set light kitchen");
        magicList.add("get light kitchen");
        magicList.add("what light kitchen");
        magicList.add("set temperature bathroom");
        magicList.add("get temperature bathroom");
        magicList.add("what temperature bathroom");
        magicList.add("set light bathroom");
        magicList.add("get light bathroom");
        magicList.add("what light bathroom");
        magicList.add("make temperature outside");
        magicList.add("set colour red");
        magicList.add("set colour blue");
        magicList.add("set colour green");
    }

    public String[] translateTask(String spokenText) {
        boolean isFunction = true;

        //check if it's not empty
        if (spokenText == null || spokenText.isEmpty())
            return null;

        String[] magicWords;
        String[] magicResponse = new String[4];

        String[] extractedWords = spokenText.split(" ");
        //Toast.makeText(this, spokenText ,Toast.LENGTH_SHORT).show();
        Log.e("fullText", spokenText);
        Log.e("after spokenText", "" + isFunction);

        //check for magical command
        if (isFunction) {
            for (String singleCommand : magicList) {
                magicWords = singleCommand.split(" ");
                isFunction = true;
                for (int i = 0; i < magicWords.length; i++) {
                    if (spokenText.contains(magicWords[i])) {
                        magicResponse[i] = magicWords[i];
                    } else isFunction = false;

                    //    Log.e("contains", magicWords[i]);
                    //    Log.e("isFunction?", isFunction+"");
                }
                if (isFunction) break;
            }
        }

        Log.e("after magicWord", "" + isFunction);
        if (!isFunction)
            return null;

        //Log.e("magicResponse[0] is", "" + magicResponse[0]);

        //Check for number
//        if (!magicResponse[0].equalsIgnoreCase("get") && !magicResponse[0].equalsIgnoreCase("what")) {
//            for (String str : extractedWords) {
//                if (TextUtils.isDigitsOnly(str)) {
//                    magicResponse[3] = str;
//                    isFunction = true;
//                    Log.e("number is", "" + str);
//                    break;
//                } else isFunction = false;
//            }
//        }

        Log.e("after Number", "" + isFunction);

       /* if (isFunction) {
            for (int i = 0; i < magicResponse.length; i++)
                Log.e("" + i + "", magicResponse[i]);
        }*/

        if (isFunction)
            return magicResponse;
        else
            return null;
    }

    public String getVoiceResponse(String[] magicResponse) {
        String tmp = new String();

        if (magicResponse[0].equalsIgnoreCase("set")) {
            tmp = magicResponse[0] + "ting the " + magicResponse[1] + " in the " + magicResponse[2] + " to be " + magicResponse[3] + " degrees";
        } else if (magicResponse[0].equalsIgnoreCase("get") || magicResponse[0].equalsIgnoreCase("what")) {
            tmp = magicResponse[1] + " in the " + magicResponse[2] + " is " + "20" + " degrees";
        } else if (magicResponse[0].equalsIgnoreCase("make")) {
            tmp = "making the" + magicResponse[1] + " on the " + magicResponse[2] + " to be " + magicResponse[3] + " degrees";
        }

        return tmp;
    }

    public String getMessageResponse(String[] magicResponse) {
        String sendStuff = new String();
        if (magicResponse[0].equalsIgnoreCase("set") || magicResponse[0].equalsIgnoreCase("make")) {
            sendStuff = "s" + magicResponse[3];
        } else if (magicResponse[0].equalsIgnoreCase("get") || magicResponse[0].equalsIgnoreCase("what")) {
            sendStuff = "g";
        }
        return sendStuff;
    }

    public String getColourResponse (String[] magicResponse){
        String tmp = new String();

        if (magicResponse[0].equalsIgnoreCase("set")){
            tmp = magicResponse[0] + "ting the " + magicResponse[1] + " " + magicResponse[2];
        }
        return tmp;
    }
}
