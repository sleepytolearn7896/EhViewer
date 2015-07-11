/*
 * Copyright 2015 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer;

import android.app.ActivityManager;
import android.content.Context;

import com.hippo.beerbelly.SimpleDiskCache;
import com.hippo.conaco.Conaco;
import com.hippo.ehviewer.client.EhClient;
import com.hippo.ehviewer.network.EhHttpClient;
import com.hippo.ehviewer.util.Settings;
import com.hippo.httpclient.HttpClient;
import com.hippo.scene.SceneApplication;
import com.hippo.util.Log;
import com.hippo.vectorold.content.VectorContext;

import java.io.File;
import java.io.IOException;

public class EhApplication extends SceneApplication {

    private HttpClient mHttpClient;
    private EhClient mEhClient;
    private Conaco mConaco;
    private SimpleDiskCache mReadcache;

    @Override
    public void onCreate() {
        super.onCreate();

        Settings.initialize(this);

        mHttpClient = new EhHttpClient(this);
        mEhClient = new EhClient(mHttpClient);

        Conaco.Builder conacoBuilder = new Conaco.Builder();
        conacoBuilder.hasMemoryCache = true;
        conacoBuilder.memoryCacheMaxSize = getMemoryCacheMaxSize();
        conacoBuilder.hasDiskCache = true;
        conacoBuilder.diskCacheDir = new File(getCacheDir(), "conaco");
        conacoBuilder.diskCacheMaxSize = 50 * 1024 * 1024;
        conacoBuilder.httpClient = mHttpClient;
        mConaco = conacoBuilder.build();

        try {
            mReadcache = new SimpleDiskCache(new File(getCacheDir(), "readCache"), 200 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getMemoryCacheMaxSize() {
        final ActivityManager activityManager = (ActivityManager)
                getSystemService(Context.ACTIVITY_SERVICE);
        return Math.min(20 * 1024 * 1024,
                Math.round(0.2f * activityManager.getMemoryClass() * 1024 * 1024));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(VectorContext.wrapContext(newBase));
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Log.d("Application hiden");
        }
    }

    public static HttpClient getHttpClient(Context context) {
        EhApplication application = (EhApplication) context.getApplicationContext();
        return application.mHttpClient;
    }

    public static EhClient getEhClient(Context context) {
        EhApplication application = (EhApplication) context.getApplicationContext();
        return application.mEhClient;
    }

    public static Conaco getConaco(Context context) {
        EhApplication application = (EhApplication) context.getApplicationContext();
        return application.mConaco;
    }

    public static SimpleDiskCache getReadCache(Context context) {
        EhApplication application = (EhApplication) context.getApplicationContext();
        return application.mReadcache;
    }
}
