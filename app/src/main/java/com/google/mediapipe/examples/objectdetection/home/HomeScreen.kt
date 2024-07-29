/*
 * Copyright 2023 The Innocomm Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.objectdetection.home

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mediapipe.examples.objectdetection.R
import com.google.mediapipe.examples.objectdetection.composables.MediaPipeBanner
import com.google.mediapipe.examples.objectdetection.composables.TabsTopBar
import com.google.mediapipe.examples.objectdetection.home.camera.CameraView
import com.google.mediapipe.examples.objectdetection.home.gallery.GalleryView
import com.google.mediapipe.examples.objectdetection.myApp
import com.google.mediapipe.examples.objectdetection.objectdetector.ObjectDetectorHelper

// The Home screen contains the camera view and the gallery view
@Composable
fun HomeScreen(
    onOptionsButtonClick: () -> Unit,
    threshold: Float,
    maxResults: Int,
    delegate: Int,
    mlModel: Int
) {
    // We declare a state to control which view we're displaying: camera or gallery
    val selectedTabIndex = rememberSaveable {
        mutableStateOf(0)
    }

    // This state stores the inference time of the latest object detection process
    // to be displayed at the bottom of the screen
    val inferenceTime = rememberSaveable {
        mutableStateOf(0)
    }
    val detectFps = rememberSaveable {
        mutableStateOf(0)
    }
    val camState = remember {
        mutableStateOf(ObjectDetectorHelper.CAMSTATE_IDLE)
    }
    val showPopup = remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current
    Column {
        // MediaPipe banner showing an options button to navigate to Options screen with
        MediaPipeBanner(
            onOptionsButtonClick = onOptionsButtonClick,
            onBackButtonClick = { context.findActivity()?.finish() },
            onPreviewListButtonClick = { showPopup.value = true },
            camState = camState.value
        )
        // The tabs at the top to switch between camera and gallery views
        TabsTopBar(
            selectedTabIndex = selectedTabIndex.value,
            setSelectedTabIndex = {
                selectedTabIndex.value = it
                inferenceTime.value = 0
            }
        )
        // Here we display the camera view or the gallery view based on the selected tab, both
        // of which need to be provided with the object detector options as well as a function
        // to update the inference value when running an object detection process
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (showPopup.value) {
                if (camState.value != ObjectDetectorHelper.CAMSTATE_PREVIEWING) {
                    popupMenuPreviewSize {
                        showPopup.value = false
                    }
                }
            } else {
                if (selectedTabIndex.value == 0) {
                    CameraView(
                        threshold = threshold,
                        maxResults = maxResults,
                        delegate = delegate,
                        mlModel = mlModel,
                        setInferenceTime = { inferenceTime.value = it },
                        setFPS = { detectFps.value = it },
                        onCamStateChange = {
                            Log.v("innocomm", "onCamStateChange " + it)
                            camState.value = it
                            if (it != ObjectDetectorHelper.CAMSTATE_PREVIEWING) {
                                inferenceTime.value = 0
                                detectFps.value = 0
                            }
                        }
                    )
                } else {
                    inferenceTime.value = 0
                    detectFps.value = 0
                    GalleryView(
                        threshold = threshold,
                        maxResults = maxResults,
                        delegate = delegate,
                        mlModel = mlModel,
                        setInferenceTime = { inferenceTime.value = it },
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .padding(10.dp),
        ) {
            Row {
                Text(text = "Inference Time: ${inferenceTime.value} ms, ")
                if (detectFps.value > 0) Text(text = "fps ${detectFps.value}, ")
                Text(
                    text = when (delegate) {
                        ObjectDetectorHelper.DELEGATE_CPU -> "CPU"
                        else -> "GPU"
                    }
                )
                Text(text = ", ")
                Text(
                    text = when (mlModel) {
                        ObjectDetectorHelper.MODEL_EFFICIENTDETV0 -> stringResource(id = R.string.model_efficentdetv0)
                        ObjectDetectorHelper.MODEL_EFFICIENTDETV2 -> stringResource(id = R.string.model_efficentdetv2)
                        ObjectDetectorHelper.MODEL_MOBILENETV2 ->  stringResource(id = R.string.model_mobilenetv2)
                        else -> "NONE"
                    }
                )
            }
        }
    }
}


fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun popupMenuPreviewSize(onDone: () -> Unit) {

    AlertDialog(onDismissRequest = { onDone() },
        //modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 16.dp),
        text = {
            Column {
                Row {
                    Text(
                        stringResource(id = R.string.previewlist),
                        fontSize = 20.sp
                    )
                }

                LazyColumn(
                    userScrollEnabled = true,
                    modifier = Modifier.wrapContentSize()
                ) {
                    items(myApp.previewList.size) { idx ->
                        val text =
                            myApp.previewList[idx].width.toString() + "x" + myApp.previewList[idx].height.toString()
                        ListItem(
                            modifier = Modifier.combinedClickable(
                                onClick = {
                                    myApp.updatePreviewSize(myApp.previewList[idx])
                                    onDone()
                                }
                            ),
                            leadingContent = {

                            },
                            headlineText = {
                                Text(text = text)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = { }
    )

}