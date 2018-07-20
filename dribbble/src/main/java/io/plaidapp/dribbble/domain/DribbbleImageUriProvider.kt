/*
 * Copyright 2018 Google, Inc.
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

package io.plaidapp.dribbble.domain

import android.content.Context
import android.net.Uri
import android.support.annotation.WorkerThread
import android.support.v4.content.FileProvider
import com.bumptech.glide.Glide
import io.plaidapp.core.dribbble.data.api.model.Images
import io.plaidapp.dribbble.BuildConfig
import java.io.File

/**
 * A class responsible for resolving an image as identified by Url into a sharable [Uri].
 */
class DribbbleImageUriProvider(private val context: Context) {

    init {
        // Only accept application context to avoid leaks
        if (context.applicationContext != context) throw IllegalArgumentException()
    }

    @WorkerThread
    operator fun invoke(url: String, size: Images.ImageSize): Uri {
        // Retrieve the image from Glide (hopefully cached) as a File
        val file = Glide.with(context)
            .asFile()
            .load(url)
            .submit(size.width, size.height)
            .get()
        // Glide cache uses an unfriendly & extension-less name, massage it based on the original
        val fileName = url.substring(url.lastIndexOf('/') + 1)
        val renamed = File(file.parent, fileName)
        file.renameTo(renamed)
        return FileProvider.getUriForFile(context, BuildConfig.FILES_AUTHORITY, renamed)
    }
}
