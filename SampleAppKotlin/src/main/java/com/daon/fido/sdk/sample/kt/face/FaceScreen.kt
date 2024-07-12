package com.daon.fido.sdk.sample.kt.face

import android.Manifest
import android.content.pm.ActivityInfo
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.daon.fido.sdk.sample.kt.R
import com.daon.fido.sdk.sample.kt.ui.theme.ButtonColor
import com.daon.fido.sdk.sample.kt.util.CircularIndeterminateProgressBar
import com.daon.fido.sdk.sample.kt.util.LockScreenOrientation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@Composable
fun FaceScreen(onNavigateUp: () -> Unit) {
  val faceViewModel = hiltViewModel<FaceViewModel>()
  var authStateProcessed = faceViewModel.authStateProcessed.collectAsState()
  val isEnrol = faceViewModel.isEnrol.collectAsState()
  val previewSize = faceViewModel.previewSize.collectAsState()

  DisposableEffect(key1 = faceViewModel) {
      faceViewModel.getAuthenticationMode()

    onDispose { }
  }
    val context = LocalContext.current
    LaunchedEffect(key1 = faceViewModel.faceCaptureState) {
        faceViewModel.faceCaptureState.collect { faceCaptureState ->
            if (faceCaptureState.message != null) {
                Toast.makeText(context, faceCaptureState.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(key1 = faceViewModel.captureComplete) {
        faceViewModel.captureComplete.collect { captureComplete ->
            if (captureComplete) {
                faceViewModel.resetCaptureComplete()
                onNavigateUp()
            }
        }
    }

  if (authStateProcessed.value) {
    if (isEnrol.value) {
      RegisterFaceScreen(previewSize.value) {
        onNavigateUp()
      }
    } else {
      AuthenticateFaceScreen(previewSize.value) {
        onNavigateUp()
      }
    }
  } else {
    Text("Waiting for the controller to initialize ...")
  }
}

enum class PhotoMode {
  DETECT,
  TAKE,
  RETAKE
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RegisterFaceScreen(previewSize: Size, onNavigateUp: () -> Unit) {
  val registerFaceViewModel = hiltViewModel<FaceViewModel>()

  var instructions = registerFaceViewModel.instructions.collectAsState()
  var status = registerFaceViewModel.status.collectAsState()
  var warning = registerFaceViewModel.warnings.collectAsState()
  var maskWarning = registerFaceViewModel.maskWarnings.collectAsState()
  var photoMode = registerFaceViewModel.photoMode.collectAsState()
  var enablePreview = registerFaceViewModel.enablePreview.collectAsState()
  var isQuality = registerFaceViewModel.isQuality.collectAsState()
  var inProgress = registerFaceViewModel.inProgress.collectAsState()
  var takePhotoEnabled = registerFaceViewModel.takePhotoEnabled.collectAsState()
  var processing = registerFaceViewModel.processing.collectAsState()

  var color by remember { mutableStateOf(Color.Red) }
  color =
      if (processing.value) {
          Color.White }
      else if (isQuality.value) {
        Color.Green
      } else {
        Color.Red
      }

  val context = LocalContext.current

  val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(key1 = lifecycleOwner, effect = {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_START) {
        if (permissionState.hasPermission) {
          Log.d("DAON", "CAMERA permission granted, do nothing")
        } else {
          permissionState.launchPermissionRequest()
        }
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  })

  LaunchedEffect(key1 = registerFaceViewModel.captureComplete) {
    registerFaceViewModel.captureComplete.collect { captureComplete ->
      if (captureComplete) {
        registerFaceViewModel.resetCaptureComplete()
        onNavigateUp()
      }
    }
  }

  LaunchedEffect(key1 = registerFaceViewModel.faceCaptureState) {
    registerFaceViewModel.faceCaptureState.collect { faceCaptureState ->
      if (faceCaptureState.message != null) {
        Toast.makeText(context, faceCaptureState.message, Toast.LENGTH_SHORT).show()
      }
    }
  }

  DisposableEffect(key1 = registerFaceViewModel) {
    registerFaceViewModel.onStart()

    onDispose { registerFaceViewModel.onStop()
    }
  }

  BackHandler {
    //Cancel the ongoing fido operation and capture controller
    registerFaceViewModel.cancelCurrentOperation()
    onNavigateUp()
  }


  LockScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

  Column(modifier = Modifier.fillMaxHeight()) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .weight(1f)) {
      Text(
          text = instructions.value,
          color = ButtonColor,
          style = MaterialTheme.typography.body1,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(10.dp))
      Text(
          text = status.value,
          color = ButtonColor,
          style = MaterialTheme.typography.body1,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(10.dp))
      Box(
          modifier =
          Modifier
              .fillMaxWidth()
              .border(BorderStroke(2.dp, color))
              .aspectRatio(3f / 4f)) {
            CameraPreview(
                processing.value,
                photoMode = photoMode.value, previewSize, imageAnalyzer = ImageAnalyzer(registerFaceViewModel))
            if (!processing.value) {
                Image(
                    painterResource(id = R.drawable.preview_overlay),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            PreviewImage(enablePreview = enablePreview.value, registerFaceViewModel)
            CircularIndeterminateProgressBar(
                isDisplayed = inProgress.value, modifier = Modifier.align(Alignment.Center))
            Text(
                text = warning.value,
                color = ButtonColor,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.Center))
          }
      Text(
          text = maskWarning.value,
          color = ButtonColor,
          style = MaterialTheme.typography.body1,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(10.dp))
    }
    // Keeping this outside column with weight , since this will be drawn first
      TakeRetakeAndEnrollButtonGroup(
        photoMode = photoMode.value,
        takePhotoEnabled = takePhotoEnabled.value,
        registerFaceViewModel)
  }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AuthenticateFaceScreen(previewSize: Size, onNavigateUp: () -> Unit) {
  val authenticateFaceViewModel = hiltViewModel<AuthenticateFaceViewModel>()

  var instructions = authenticateFaceViewModel.instructions.collectAsState()
  var status = authenticateFaceViewModel.status.collectAsState()
  var warning = authenticateFaceViewModel.warnings.collectAsState()
  var maskWarning = authenticateFaceViewModel.maskWarnings.collectAsState()
  var photoMode = authenticateFaceViewModel.photoMode.collectAsState()
  var enablePreview = authenticateFaceViewModel.enablePreview.collectAsState()
  var isQuality = authenticateFaceViewModel.isQuality.collectAsState()
  var inProgress = authenticateFaceViewModel.inProgress.collectAsState()
  var takePhotoEnabled = authenticateFaceViewModel.takePhotoEnabled.collectAsState()
  var analyzing = authenticateFaceViewModel.processing.collectAsState()

    var color by remember { mutableStateOf(Color.Red) }
    color =
        if (analyzing.value) {
            Color.White
        }else if (isQuality.value) {
            Color.Green
        } else {
            Color.Red
        }

    val context = LocalContext.current


    val permissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (permissionState.hasPermission) {
                    Log.d("DAON", "CAMERA permission granted, do nothing")
                } else {
                    permissionState.launchPermissionRequest()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })

    LaunchedEffect(key1 = authenticateFaceViewModel.captureComplete) {
    authenticateFaceViewModel.captureComplete.collect { captureComplete ->
      if (captureComplete) {
        authenticateFaceViewModel.resetCaptureComplete()
        onNavigateUp()
      }
    }
  }

  LaunchedEffect(key1 = authenticateFaceViewModel.faceCaptureState) {
    authenticateFaceViewModel.faceCaptureState.collect { faceCaptureState ->
      if (faceCaptureState.message != null) {
        Toast.makeText(context, faceCaptureState.message, Toast.LENGTH_SHORT).show()
      }
      authenticateFaceViewModel.resetFaceCaptureState()
    }
  }

  DisposableEffect(key1 = authenticateFaceViewModel) {
    authenticateFaceViewModel.onStart()

    onDispose { authenticateFaceViewModel.onStop() }
  }

  BackHandler {
      //Cancel the ongoing fido operation and capture controller
      authenticateFaceViewModel.cancelCurrentOperation()
      onNavigateUp()
  }

    LockScreenOrientation(orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

  Column(modifier = Modifier.fillMaxHeight()) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .weight(1f)) {
      Text(
          text = instructions.value,
          color = ButtonColor,
          style = MaterialTheme.typography.body1,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(10.dp))
      Text(
          text = status.value,
          color = ButtonColor,
          style = MaterialTheme.typography.body1,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(10.dp))
      Box(
          modifier =
          Modifier
              .fillMaxWidth()
              .border(BorderStroke(2.dp, color))
              .aspectRatio(3f / 4f)) {
            // ToDo - CameraPreview is getting resized when button row is becoming visible - need to
            // fix that
            CameraPreview(
                analyzing.value,
                photoMode = photoMode.value,
                previewSize,
                imageAnalyzer = ImageAnalyzer(authenticateFaceViewModel))
            if (!analyzing.value) {
                Image(
                    painterResource(id = com.daon.fido.sdk.sample.kt.R.drawable.preview_overlay),
                    contentDescription = "",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // ToDo - PreviewImage size is less than the cameraPreview
            PreviewImage(enablePreview = enablePreview.value, authenticateFaceViewModel)
            CircularIndeterminateProgressBar(
                isDisplayed = inProgress.value, modifier = Modifier
                    .align(Alignment.Center)
                    .padding(10.dp))
            Text(
                text = warning.value,
                color = ButtonColor,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.Center))
          }
      Text(
          text = maskWarning.value,
          color = ButtonColor,
          style = MaterialTheme.typography.body1,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(10.dp))
    }
    // Keeping this outside column with weight , since this will be drawn first
      TakeRetakeAndEnrollButtonGroup(
        photoMode = photoMode.value,
        takePhotoEnabled = takePhotoEnabled.value,
        authenticateFaceViewModel)
  }
}

@Composable
fun PreviewImage(enablePreview: Boolean, viewModel: FaceViewModel) {
  if (enablePreview) {
    getPreviewImage(viewModel)?.let {
      Image(
          bitmap = it,
          contentDescription = "",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize())
    }
  }
}

@Composable
fun QualityIndicator(isQuality: Boolean, modifier: Modifier) {
  Box(
      modifier =
      Modifier
          .padding(35.dp)
          .size(20.dp)
          .clip(shape = CircleShape)
          .background(Color(0xff2abc2a)))
}

@Composable
fun TakeRetakeAndEnrollButtonGroup(photoMode: PhotoMode, takePhotoEnabled: Boolean, viewModel: FaceViewModel) {
    var takePhotoText: String by remember { mutableStateOf("Take Photo") }
    var enrollVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (takePhotoEnabled) {
            Button(
                onClick = {
                    if (photoMode == PhotoMode.TAKE) {
                        viewModel.takePhoto()
                        enrollVisible = true
                        takePhotoText = "Retake"
                    } else {
                        viewModel.retakePhoto()
                        enrollVisible = false
                        takePhotoText = "Take Photo"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                shape = CutCornerShape(10),
                colors = ButtonDefaults.buttonColors(backgroundColor = ButtonColor)
            ) {
                Text(
                    text = takePhotoText,
                    style = TextStyle(fontSize = 15.sp, color = Color.White)
                )
            }
        }

        if (enrollVisible) {
            Button(
                onClick = {
                    enrollVisible = false
                    viewModel.enroll()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                shape = CutCornerShape(10),
                colors = ButtonDefaults.buttonColors(backgroundColor = ButtonColor)
            ) {
                Text(
                    text = "Enroll",
                    style = TextStyle(fontSize = 15.sp, color = Color.White)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

fun getPreviewImage(viewModel: FaceViewModel): ImageBitmap? {
  return viewModel.getCapturedImage()
}

@Composable
fun CameraPreview(analyzing: Boolean, photoMode: PhotoMode, previewSize: Size, imageAnalyzer: ImageAnalyzer) {
    if (!analyzing) {
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
        var imageAnalysis: ImageAnalysis? by remember { mutableStateOf(null) }
        val imageAnalyzerExecutor = remember { Executors.newSingleThreadExecutor() }

        val currentPhotoMode = remember { mutableStateOf(PhotoMode.DETECT) }
        val previousPhotoMode = remember { mutableStateOf(PhotoMode.DETECT) }
        // var preview by remember { mutableStateOf<Preview?>(null) }

        // AndroidView is a composable that can be used to add Android Views inside of a @Composable
        // function
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener(
                    {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview =
                            Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                        val cameraSelector =
                            CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                                .build()

                        val screenSize = Size(previewSize.width, previewSize.height)
                        // ToDo - size to be dynamic depending on the liveness type
                        imageAnalysis =
                            ImageAnalysis.Builder()
                                .setResolutionSelector(
                                    ResolutionSelector.Builder()
                                        .setResolutionStrategy(ResolutionStrategy(screenSize, ResolutionStrategy.FALLBACK_RULE_NONE))
                                        .build())
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                                .build()
                                .apply { setAnalyzer(imageAnalyzerExecutor, imageAnalyzer) }

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            imageAnalysis,
                            preview
                        )
                    },
                    executor
                )
                /*preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }*/
                previewView
            },
            update = {
                previousPhotoMode.value = currentPhotoMode.value
                currentPhotoMode.value = photoMode
                if (photoMode == PhotoMode.RETAKE && previousPhotoMode.value == PhotoMode.TAKE) {
                    imageAnalysis?.apply { clearAnalyzer() }
                } else if (photoMode == PhotoMode.DETECT && previousPhotoMode.value == PhotoMode.RETAKE) {
                    imageAnalysis?.apply { setAnalyzer(imageAnalyzerExecutor, imageAnalyzer) }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

class ImageAnalyzer(private val viewModel: FaceViewModel) : ImageAnalysis.Analyzer {
    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
    viewModel.analyzeImage(image)
  }
}

