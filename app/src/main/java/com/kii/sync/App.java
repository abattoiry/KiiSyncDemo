package com.kii.sync;

import com.kii.cloud.storage.Kii;

import android.app.Application;

/**
 * Created by yue on 15/7/23.
 */
public class App extends Application{

    @Override
    public void onCreate() {
        Kii.initialize("17100168", "b141ca636d5b6548e356ad6799172beb", Kii.Site.JP);
    }
}
