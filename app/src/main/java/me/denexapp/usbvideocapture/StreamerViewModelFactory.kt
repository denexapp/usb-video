/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
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
package me.denexapp.usbvideocapture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import me.denexapp.usbvideocapture.permission.CameraPermissionState
import me.denexapp.usbvideocapture.permission.RecordAudioPermissionState

class StreamerViewModelFactory(
    private val cameraPermission: CameraPermissionState,
    private val recordAudioPermission: RecordAudioPermissionState,
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST", "KotlinGenericsCast")
  override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {

    return StreamerViewModel(
        requireNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]),
        cameraPermission,
        recordAudioPermission,
    )
        as T
  }
}
