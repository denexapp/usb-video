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

import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import me.denexapp.usbvideocapture.animation.ZoomOutPageTransformer
import me.denexapp.usbvideocapture.permission.getCameraPermissionState
import me.denexapp.usbvideocapture.permission.getRecordAudioPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val TAG = "StreamerActivity"

private const val NON_IMMERSIVE_SCREEN = 0

enum class StreamerScreen {
  Status,
  Streaming,
}

class StreamerActivity : ComponentActivity() {

  private lateinit var viewPager: ViewPager2

  private val streamerViewModel: StreamerViewModel by viewModels {
    StreamerViewModelFactory(getCameraPermissionState(), getRecordAudioPermissionState())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    doOnCreate()
  }

  private fun doOnCreate() {
    enableEdgeToEdge()
    window.insetsController?.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    streamerViewModel.prepareCameraPermissionLaunchers(this)
    streamerViewModel.prepareUsbBroadcastReceivers(this)
    setContentView(R.layout.activity_streamer)
    viewPager = findViewById(R.id.view_pager)
    viewPager.offscreenPageLimit = 1
    viewPager.setPageTransformer(ZoomOutPageTransformer())
    val screensAdapter = StreamerScreensAdapter(this, streamerViewModel)
    viewPager.adapter = screensAdapter
    viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
      override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        updateImmersiveMode()
      }
    })
    lifecycleScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        streamerViewModel.restartStreaming()
        streamerViewModel.startStopSignal.collect()
      }
    }
    lifecycleScope.launch {
      streamerViewModel.uiActionFlow().collect {
        when (it) {
          Initialize -> {
            viewPager.setCurrentItem(0, true)
          }
          RequestCameraPermission -> {
            streamerViewModel.requestCameraPermission()
          }
          RequestRecordAudioPermission -> {
            streamerViewModel.requestRecordAudioPermission()
          }
          RequestUsbPermission -> {
            // no-op here but handle separately below by checking for status of USB permission in
            // onResume because we yield to OS to avoid double permission dialog.
            delay(2000)
            if (lifecycle.currentState == Lifecycle.State.RESUMED) {
              streamerViewModel.requestUsbPermission(this@StreamerActivity.lifecycle)
            }
          }
          PresentStreamingScreen -> {
            if (!screensAdapter.screens.contains(StreamerScreen.Streaming)) {
              screensAdapter.screens = listOf(StreamerScreen.Status, StreamerScreen.Streaming,)
              screensAdapter.notifyItemInserted(1)
              viewPager.setCurrentItem(1, true)
            }
          }
          DismissStreamingScreen -> {
            stopStreaming(screensAdapter)
          }
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    updateImmersiveMode()
  }

  private fun stopStreaming(screensAdapter: StreamerScreensAdapter) {
    val screensCount = screensAdapter.screens.size
    if (screensCount > 1) {
      screensAdapter.screens = listOf(StreamerScreen.Status)
      screensAdapter.notifyItemRangeRemoved(1, screensCount - 1)
      viewPager.setCurrentItem(0, true)
    }
  }

  private fun updateImmersiveMode() {
    if (viewPager.currentItem != NON_IMMERSIVE_SCREEN) {
      enableImmersiveMode()
    } else {
      disableImmersiveMode()
    }
  }

  private fun enableImmersiveMode() {
    window.insetsController?.hide(WindowInsetsCompat.Type.systemBars())
  }

  private fun disableImmersiveMode() {
    window.insetsController?.show(WindowInsetsCompat.Type.systemBars())
  }
}
