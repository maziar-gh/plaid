/*
 *   Copyright 2018 Google LLC
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package io.plaidapp.dribbble.ui;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.io.File;

import io.plaidapp.core.dribbble.data.api.model.Images;
import io.plaidapp.dribbble.BuildConfig;
import io.plaidapp.core.dribbble.data.api.model.Shot;

/**
 * An AsyncTask which retrieves a File from the Glide cache then shares it.
 */
class ShareDribbbleImageTask extends AsyncTask<Void, Void, File> {

    private final Activity activity;
    private final Shot shot;

    ShareDribbbleImageTask(Activity activity, Shot shot) {
        this.activity = activity;
        this.shot = shot;
    }

    @Override
    protected File doInBackground(Void... params) {
        final String url = shot.getImages().best();
        final Images.ImageSize size = shot.getImages().bestSize();
        try {
            return Glide.with(activity)
                        .asFile()
                        .load(url)
                        .submit(size.getWidth(), size.getHeight())
                        .get();
        } catch (Exception ex) {
            Log.w("SHARE", "Sharing " + url + " failed", ex);
            return null;
        }
    }

    @Override
    protected void onPostExecute(File result) {
        if (result == null) { return; }
        // glide cache uses an unfriendly & extension-less name,
        // massage it based on the original
        String fileName = shot.getImages().best();
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        File renamed = new File(result.getParent(), fileName);
        result.renameTo(renamed);
        Uri uri = FileProvider.getUriForFile(activity, BuildConfig.FILES_AUTHORITY, renamed);
        ShareCompat.IntentBuilder.from(activity)
                .setText(getShareText())
                .setType(getImageMimeType(fileName))
                .setSubject(shot.getTitle())
                .setStream(uri)
                .startChooser();
    }

    private String getShareText() {
        return "“" + shot.getTitle() + "” by " + shot.getUser().getName() + "\n" + shot.getUrl();
    }

    private String getImageMimeType(@NonNull String fileName) {
        if (fileName.endsWith(".png")) {
            return "image/png";
        } else if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/jpeg";
    }
}
