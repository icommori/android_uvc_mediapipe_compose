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
package com.google.mediapipe.examples.objectdetection.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.mediapipe.examples.objectdetection.R
import com.google.mediapipe.examples.objectdetection.objectdetector.ObjectDetectorHelper
import com.google.mediapipe.examples.objectdetection.ui.theme.InnocommColor
import com.google.mediapipe.examples.objectdetection.ui.theme.ObjectDetectionTheme
import com.google.mediapipe.examples.objectdetection.ui.theme.Turquoise

// The MediaPipe banner displayed at the top of the app screens.

// For our purposes, it can show a back button on the left side
// and an options button on the right side when the corresponding
// callback functions are provided to it.

// It's intended to provide buttons to navigate between Home and Options screens

@Composable
fun MediaPipeBanner(
    onOptionsButtonClick: (() -> Unit)? = null,
    onBackButtonClick: (() -> Unit)? = null,
    onPreviewListButtonClick: (() -> Unit)? = null,
    camState: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xEEEEEEEE)),
    ) {
        if (onBackButtonClick != null) {
            IconButton(
                onClick = onBackButtonClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Backward arrow icon",
                    tint = Turquoise
                )
            }
        }
        Row(modifier = Modifier.align(Alignment.Center)) {
            Image(
                painter = painterResource(id = R.drawable.media_pipe_banner),
                contentDescription = "MediaPipe logo",
                contentScale = ContentScale.Fit,

            )
            Text(
                text = "(Innocomm)",
                color = InnocommColor,
                modifier = Modifier.wrapContentSize().align(Alignment.CenterVertically),
                fontSize = 20.sp
            )
        }

        if (onOptionsButtonClick != null) {
            Row(modifier = Modifier.align(Alignment.CenterEnd)){
                if (onPreviewListButtonClick != null) {
                    IconButton(
                        onClick = onPreviewListButtonClick,
                        enabled = camState != ObjectDetectorHelper.CAMSTATE_INITIALIZING,
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "PreviewList icon",
                            tint = Turquoise.copy(alpha = LocalContentColor.current.alpha),
                        )
                    }
                }
                IconButton(
                    onClick = onOptionsButtonClick,
                    enabled = camState!=ObjectDetectorHelper.CAMSTATE_INITIALIZING,
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = "Settings icon",
                        tint = Turquoise.copy(alpha =LocalContentColor.current.alpha),
                    )
                }
            }

        }
    }
}

@Preview(showBackground = true, device = "spec:width=600dp,height=1024dp,dpi=160")
@Composable
fun DefaultPreviewMediaPipeBanner() {
    ObjectDetectionTheme {
        Box(
            modifier = Modifier.wrapContentSize()
        ) {
            MediaPipeBanner(
                onOptionsButtonClick = {},
                onBackButtonClick = {},
                onPreviewListButtonClick = {},
                camState = 0
            )
        }
    }
}