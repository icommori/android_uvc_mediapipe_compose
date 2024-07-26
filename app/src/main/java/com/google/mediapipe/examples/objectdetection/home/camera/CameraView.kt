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
package com.google.mediapipe.examples.objectdetection.home.camera

import android.Manifest
import android.content.Context
import android.util.Log
import android.view.SurfaceHolder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mediapipe.examples.objectdetection.composables.ResultsOverlay
import com.google.mediapipe.examples.objectdetection.myApp
import com.google.mediapipe.examples.objectdetection.objectdetector.ObjectDetectorHelper
import com.google.mediapipe.examples.objectdetection.objectdetector.ObjectDetectorListener
import com.google.mediapipe.examples.objectdetection.ui.theme.InnocommColor
import com.google.mediapipe.examples.objectdetection.utils.getFittedBoxSize
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult
import com.jiangdg.ausbc.CameraClient
import com.jiangdg.ausbc.callback.IPreviewDataCallBack
import com.jiangdg.ausbc.camera.CameraUvcStrategy
import com.jiangdg.ausbc.camera.bean.CameraRequest
import com.jiangdg.ausbc.utils.CameraUtils
import com.jiangdg.ausbc.widget.AspectRatioSurfaceView2
import java.util.concurrent.Executors

// Here we have the camera view which is displayed in Home screen

// It's used to run object detection on live camera feed

// It takes as input the object detection options, and a function to update the inference time state

// You will notice we have a decorator that indicated we're using an experimental API for
// permissions, we're using it cause it's easy to check for permissions with it, and we need camera
// permission in this composable.
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraView(
    threshold: Float,
    maxResults: Int,
    delegate: Int,
    mlModel: Int,
    setInferenceTime: (newInferenceTime: Int) -> Unit,
    setFPS: (fps: Int) -> Unit,
    onCamStateChange:(state:Int) -> Unit
) {
    // We first have to deal with the camera permission, so we declare a state for it
    val storagePermissionState: PermissionState =
        rememberPermissionState(Manifest.permission.CAMERA)

    // When using this composable, we wanna check the camera permission state, and ask for the
    // permission to use the phone camera in case we don't already have it
    LaunchedEffect(key1 = Unit) {
        if (!storagePermissionState.hasPermission) {
            storagePermissionState.launchPermissionRequest()
        }
    }


    // In case we don't have the permission to use a camera, we'll just display a text to let the
    // user know that that's the case, and we won't show anything else
    if (!storagePermissionState.hasPermission) {
        Text(text = "No Storage Permission!")
        return
    }

    // At this point we have our permission to use the camera. Now we define some states

    // This state holds the object detection results
    val results = remember {
        mutableStateOf< ObjectDetectorResult?>(null)
    }

    // These states hold the dimensions of the camera frames. We don't know their values yet so
    // we just set them initially to 1x1
    val frameHeight = remember {
        mutableStateOf(myApp.previewSize.height)
    }

    val frameWidth = remember {
        mutableStateOf(myApp.previewSize.width)
    }

    // This state is used to prevent further state updates when this camera view is being disposed
    // We check for it before updating states, and we set it to false when we dispose of the view
    val active = remember {
        mutableStateOf(true)
    }

    val previewing = remember {
        mutableStateOf(false)
    }

    // We need the following objects setup the camera preview later
    val context = LocalContext.current

    val previewFps = remember { mutableStateOf(0) }
    //init USBCam
    val objectDetectorHelper =
        ObjectDetectorHelper(
            context = context,
            threshold = threshold,
            currentDelegate = delegate,
            currentModel = mlModel,
            maxResults = maxResults,
            // Since we're detecting objects in a live camera feed, we need
            // to have a way to listen for the results
            objectDetectorListener = ObjectDetectorListener(
                onErrorCallback = { _, _ -> },
                onResultsCallback = {
                    // On receiving results, we now have the exact camera
                    // frame dimensions, so we set them here
                    frameHeight.value = it.inputImageHeight
                    frameWidth.value = it.inputImageWidth

                    // Then we check if the camera view is still active,
                    // if so, we set the state of the results and
                    // inference time.
                    if (active.value) {
                        results.value = it.results.first()
                        setInferenceTime(it.inferenceTime.toInt())
                        setFPS(it.fps)
                    }
                },
                onFps = { previewFps.value = it}
            ),
            runningMode = RunningMode.LIVE_STREAM
        )
    val cameraClient = initUSBCamClient(context,objectDetectorHelper){
        onCamStateChange(ObjectDetectorHelper.CAMSTATE_PREVIEWING)
        previewing.value = true
    }
    if(previewing.value){
        cameraClient.getAllPreviewSizes()?.let {plist->
            if (plist.size > 0) {
                with(myApp.previewList){
                    clear()
                    addAll(plist)
                    forEach {
                        Log.v("innocomm", "Preview " + it.width + "x" + it.height)
                    }
                }
            }
        }
    }

    // Here we setup what will happen when the camera view is being disposed. We just need to set
    // "active" to false to stop any further state updates, and to close any currently open cameras
    DisposableEffect(Unit) {
        onDispose {
            active.value = false
            //cameraProviderFuture.get().unbindAll()
        }
    }

    // Next we describe the UI of this camera view.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {

        val cameraPreviewSize = getFittedBoxSize(
            containerSize = Size(
                width = this.maxWidth.value,
                height = this.maxHeight.value,
            ),
            boxSize = Size(
                width = frameWidth.value.toFloat(),
                height = frameHeight.value.toFloat()
            )
        )

        Box(
            Modifier//.fillMaxWidth()
                .width(cameraPreviewSize.width.dp)
                .height(cameraPreviewSize.height.dp),
        ) {

            AndroidView(
                modifier = Modifier.onGloballyPositioned {
                    //Log.i("innocomm", "onGloballyPositioned "+it.size.toString())
                },
                factory = { ctx ->
                    onCamStateChange(ObjectDetectorHelper.CAMSTATE_INITIALIZING)
                    AspectRatioSurfaceView2(ctx).apply {
                        setAspectRatio(
                            frameWidth.value,
                            frameHeight.value
                        )
                        this.holder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceCreated(holder: SurfaceHolder) {
                                Log.i("innocomm", "surfaceCreated ")
                                cameraClient.openCamera(this@apply)
                            }

                            override fun surfaceChanged(
                                holder: SurfaceHolder,
                                format: Int,
                                width: Int,
                                height: Int
                            ) {
                                Log.i("innocomm","surfaceChanged " + width + "x" + height)
                                frameWidth.value = width
                                frameHeight.value= height
                                cameraClient.setRenderSize(width, height)
                            }

                            override fun surfaceDestroyed(holder: SurfaceHolder) {
                                Log.i("innocomm", "surfaceDestroyed ")
                                cameraClient.closeCamera()
                                objectDetectorHelper.clearObjectDetector()
                                onCamStateChange(ObjectDetectorHelper.CAMSTATE_IDLE)
                            }
                        })
                    }
                }
            )
            // Finally, we check for current results, if there's any, we display the results overlay
            results.value?.let {
                ResultsOverlay(
                    results = it,
                    frameWidth = frameWidth.value,
                    frameHeight = frameHeight.value
                )
            }
        }

        Text(
            text =  "${previewFps.value} fps",
            color = InnocommColor,
            modifier = Modifier.wrapContentSize().align(Alignment.TopEnd).padding(5.dp),
            fontSize = 20.sp
        )
    }
}
@Composable
fun initUSBCamClient(context: Context,objectDetectorHelper: ObjectDetectorHelper,onPreviewing:() -> Unit):CameraClient = remember{
    val cameraUvcStrategy = CameraUvcStrategy(context)
    val list = cameraUvcStrategy.getUsbDeviceList()
    val backgroundExecutor = Executors.newSingleThreadExecutor()
    var notifyPreview = true
    list?.let { item->
        item.filter { CameraUtils.isUsbCamera(it) }.toSet().forEach{
            Log.v("innocomm", "Available UVCCam: " + it.deviceName)
        }
    }

    cameraUvcStrategy.addPreviewDataCallBack(object : IPreviewDataCallBack {
        override fun onPreviewData(data: ByteArray?, format: IPreviewDataCallBack.DataFormat) {
            data?.let {
                if(notifyPreview){
                    notifyPreview = false
                    onPreviewing()
                }
                backgroundExecutor.execute {
                    objectDetectorHelper.detectLivestreamFrame2(it)
                }
            }
        }
    })

    CameraClient.newBuilder(context).apply {
        setEnableGLES(true)
        setRawImage(false)
        setCameraStrategy(cameraUvcStrategy)
        setCameraRequest(
            CameraRequest.Builder()
                .setFrontCamera(false)
                .setPreviewWidth(myApp.previewSize.width)
                .setPreviewHeight(myApp.previewSize.height)
                .create()
        )
        openDebug(true)
    }.build()
}