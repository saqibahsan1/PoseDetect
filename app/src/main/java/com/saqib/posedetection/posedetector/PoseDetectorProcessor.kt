package com.saqib.posedetection.posedetector

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.android.odml.image.MlImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.saqib.posedetection.utils.FrameMetadata
import com.saqib.posedetection.utils.GraphicOverlay
import com.saqib.posedetection.utils.VisionProcessorBase
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/** A processor to run pose detector. */
class PoseDetectorProcessor(
  context: Context,
  options: PoseDetectorOptionsBase,
  private val showInFrameLikelihood: Boolean,
  private val visualizeZ: Boolean,
  private val rescaleZForVisualization: Boolean
) :
  VisionProcessorBase<PoseDetectorProcessor.PoseWithClassification>(context) {

  private val detector: PoseDetector = PoseDetection.getClient(options)
  private val classificationExecutor: Executor
  /** Internal class to hold Pose and classification results. */
  class PoseWithClassification(val pose: Pose, val classificationResult: List<String>)

  init {
    classificationExecutor = Executors.newSingleThreadExecutor()
  }

  override fun stop() {
    super.stop()
    detector.close()
  }

  override fun detectInImage(image: InputImage): Task<PoseWithClassification> {
    return detector
      .process(image)
      .continueWith(
        classificationExecutor,
        { task ->
          val pose = task.result
          val classificationResult: List<String> = ArrayList()
          PoseWithClassification(pose, classificationResult)
        }
      )
  }

  override fun detectInImage(image: MlImage): Task<PoseWithClassification> {
    return detector
      .process(image)
      .continueWith(
        classificationExecutor,
        { task ->
          val pose = task.result
          val classificationResult: List<String> = ArrayList()
          PoseWithClassification(pose, classificationResult)
        }
      )
  }

  override fun onSuccess(
    results: PoseWithClassification,
    graphicOverlay: GraphicOverlay
  ) {
    graphicOverlay.add(
      PoseGraphic(
        graphicOverlay,
        results.pose,
        showInFrameLikelihood,
        visualizeZ,
        rescaleZForVisualization,
        results.classificationResult
      )
    )
  }

  override fun onFailure(e: Exception) {
    Log.e(TAG, "Pose detection failed!", e)
  }

  override fun isMlImageEnabled(context: Context?): Boolean {
    // Use MlImage in Pose Detection by default, change it to OFF to switch to InputImage.
    return true
  }


  companion object {
    private const val TAG = "PoseDetectorProcessor"
  }
}
