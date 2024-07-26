# MediaPipe Tasks Object Detection Android Demo From Innocomm

https://www.innocomm.com

### Overview

This is an USB camera app that continuously detects the objects (bounding boxes, classes, and confidence) in the frames seen by your device's USB camera, in an image imported from the device gallery, or in a video imported by the device gallery, with the option to use a quantized [EfficientDet Lite 0](https://storage.googleapis.com/mediapipe-tasks/object_detector/efficientdet_lite0_uint8.tflite), or [EfficientDet Lite2](https://storage.googleapis.com/mediapipe-tasks/object_detector/efficientdet_lite2_uint8.tflite) model.

The model files are downloaded by a Gradle script when you build and run the app. You don't need to do any steps to download TFLite models into the project explicitly unless you wish to use your own models. If you do use your own models, place them into the app's _assets_ directory.

This application should be run on a physical Android device to take advantage of the physical camera, though the gallery tab will enable you to use an emulator for opening locally stored files.

### Demo on Innocomm SC51
![Object Detection Demo1](mediapipe001.png?raw=true "Object Detection Demo1") 
![Object Detection Demo2](mediapipe002.gif?raw=true "Object Detection Demo2") 
![Object Detection Demo3](mediapipe003.png?raw=true "Object Detection Demo3")
![Object Detection Demo4](mediapipe004.png?raw=true "Object Detection Demo4")

## Build the demo using Android Studio

### Prerequisites

- The **[Android Studio](https://developer.android.com/studio/index.html)**
  IDE. This sample has been tested on Android Studio Flamingo.

- A physical Android device with a minimum OS version of SDK 24 (Android 7.0 -
  Nougat) with developer mode enabled. The process of enabling developer mode
  may vary by device. You may also use an Android emulator with more limited
  functionality.

### Building

- Open Android Studio. From the Welcome screen, select Open an existing
  Android Studio project.

- From the Open File or Project window that appears, navigate to and select
  the mediapipe/examples/object_detection/android-jetpack-compose directory.
  Click OK. You may be asked if you trust the project. Select Trust.

- If it asks you to do a Gradle Sync, click OK.

- With your Android device connected to your computer and developer mode
  enabled, click on the green Run arrow in Android Studio.

### Models used

Downloading, extraction, and placing the models into the _assets_ folder is
managed automatically by the **download.gradle** file.
