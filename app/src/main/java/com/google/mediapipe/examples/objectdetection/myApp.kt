/*
 * Copyright 2024 InnoComm Mobile Technology Corporation , All Rights Reserved
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
package com.google.mediapipe.examples.objectdetection

import android.app.Application
import android.content.Context
import com.jiangdg.ausbc.camera.bean.PreviewSize

class myApp: Application() {

    companion object {
        private const val TAG = "InnoApplication"
        var previewSize = PreviewSize(640,480)
        val previewList = ArrayList<PreviewSize>()
        private lateinit var instance: Application

        fun getInstance(): Application {
            return instance
        }
        fun updatePreviewSize(size: PreviewSize){
            previewSize = size
        }

    }
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }


}