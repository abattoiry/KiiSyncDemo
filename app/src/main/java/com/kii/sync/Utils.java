package com.kii.sync;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yue on 14/12/24.
 */
public class Utils {

    public static final String PREFS_NAME = "prefs";
    public static final String KEY_TOEKN = "token";

    public static void saveToken(Context context, String token) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME,
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor e = prefs.edit();
        e.putString(KEY_TOEKN, token);
        e.commit();
    }

}
