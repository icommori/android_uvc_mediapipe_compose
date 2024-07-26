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
package com.google.mediapipe.examples.objectdetection

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.mediapipe.examples.objectdetection.home.HomeScreen
import com.google.mediapipe.examples.objectdetection.objectdetector.ObjectDetectorHelper
import com.google.mediapipe.examples.objectdetection.options.OptionsScreen
import com.google.mediapipe.examples.objectdetection.ui.theme.ObjectDetectionTheme
import com.jiangdg.ausbc.utils.CameraUtils

//Entry point of our example app
class MainActivity : ComponentActivity() {
    var _hasUSBPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        _hasUSBPermission = hasUSBPermission()
        setContent {
            ObjectDetectionExampleApp()
        }
    }

    override fun onPause() {
        super.onPause()
        _hasUSBPermission = hasUSBPermission()
        Log.v("MainActivity", "onPause ")
    }

    override fun onResume() {
        super.onResume()
        Log.v("MainActivity", "onResume")
        if (_hasUSBPermission != hasUSBPermission()) {
            Log.v("MainActivity", "restartMyApp")
            restartMyApp(this)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v("MainActivity", "onDestroy ")
    }

    fun hasUSBPermission(): Boolean {
        //获取service
        val manager = getSystemService(USB_SERVICE) as UsbManager
        //获取设备列表(一般只有一个,usb 口只有一个)
        val result = manager.deviceList.entries.toList()
        val deviceList =
            result.filter { CameraUtils.isUsbCamera(it.value) && manager.hasPermission(it.value) }
                .toList()

        deviceList.forEach {
            Log.v("MainActivity", "USBCam ${it.key} ${it.value.deviceName}: has USB permission")
        }
        val r = deviceList.size > 0
        Log.v("MainActivity", "hasUSBPermission: ${r}")
        return r
    }

    fun restartMyApp(context: Activity) {
        val packageManager: PackageManager = context.packageManager
        val intent: Intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
        val componentName: ComponentName = intent.component!!
        val restartIntent: Intent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(restartIntent)
        Runtime.getRuntime().exit(0)
    }
}

// Root component of our app components tree
@Composable
fun ObjectDetectionExampleApp() {
    // Here we're first defining the object detector parameters states

    // We're defining them at the top of the components tree so that they
    // are accessible to all the app components, and any change of these
    // states will be reflected across the entire app, ensuring consistency

    // We're using "rememberSaveable" rather than "remember" so that the state
    // is preserved when the app change its orientation.

    // Since using a data class with "rememberSaveable" requires additional
    // configuration, we'll just define each option state individually as
    // "rememberSaveable" works with primitive values out of the box

    val threshold = rememberSaveable {
        mutableStateOf(0.4f)
    }
    val maxResults = rememberSaveable {
        mutableStateOf(5)
    }
    val delegate = rememberSaveable {
        mutableStateOf(ObjectDetectorHelper.DELEGATE_CPU)
    }
    val mlModel = rememberSaveable {
        mutableStateOf(ObjectDetectorHelper.MODEL_EFFICIENTDETV0)
    }

    ObjectDetectionTheme(darkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize()) {
            // Here we handle navigation between Home screen and Options screen
            // Nothing too fancy, we only have two screens.

            // We define a controller first and provide it to NavHost
            // We will later use it to navigate between screens
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = "Home",
            ) {
                // Here we associate a route name with each screen
                // We also provide a callback function to each screen
                // to navigate to the other one
                composable(route = "Home") {
                    HomeScreen(
                        onOptionsButtonClick = {
                            navController.navigate("Options")
                        },
                        threshold = threshold.value,
                        maxResults = maxResults.value,
                        delegate = delegate.value,
                        mlModel = mlModel.value
                    )
                }
                composable(route = "Options") {
                    OptionsScreen(
                        onBackButtonClick = {
                            navController.popBackStack()
                        },
                        threshold = threshold.value,
                        setThreshold = { threshold.value = it },
                        maxResults = maxResults.value,
                        setMaxResults = { maxResults.value = it },
                        delegate = delegate.value,
                        setDelegate = { delegate.value = it },
                        mlModel = mlModel.value,
                        setMlModel = { mlModel.value = it },
                    )
                }
            }
        }
    }
}


