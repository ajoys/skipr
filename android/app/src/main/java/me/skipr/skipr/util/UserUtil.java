package me.skipr.skipr.util;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

/**
 * Created by Navjot on 6/13/2015.
 */

//TODO: this is weird. make it not weird.
public class UserUtil {
    private static String sUserID = "";

    public static void generateUserId(Context context){
        sUserID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String getUserId(){
        return sUserID;
    }
}
