/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.streampark.spark.client.impl

import org.apache.streampark.common.enums.SparkExecutionMode
import org.apache.streampark.common.util.HadoopUtils
import org.apache.streampark.common.util.Implicits._
import org.apache.streampark.spark.client.`trait`.SparkClientTrait
import org.apache.streampark.spark.client.bean._

import org.apache.hadoop.yarn.api.records.ApplicationId
import org.apache.spark.launcher.{SparkAppHandle, SparkLauncher}

import java.util.concurrent.CountDownLatch

import scala.util.{Failure, Success, Try}

/** yarn application mode submit */
object YarnClient extends SparkClientTrait {

  override def doStop(stopRequest: StopRequest): StopResponse = {
    HadoopUtils.yarnClient.killApplication(ApplicationId.fromString(stopRequest.jobId))
    null
  }

  override def setConfig(submitRequest: SubmitRequest): Unit = {}

  override def doSubmit(submitRequest: SubmitRequest): SubmitResponse = {
    // 1) prepare sparkLauncher
    val launcher: SparkLauncher = prepareSparkLauncher(submitRequest)

    // 2) set spark config
    setSparkConfig(submitRequest, launcher)

    // 3) launch
    Try(launch(launcher)) match {
      case Success(handle: SparkAppHandle) =>
        logger.info(s"[StreamPark][Spark][YarnClient] spark job: ${submitRequest.effectiveAppName} is submit successful, " +
          s"appid: ${handle.getAppId}, " +
          s"state: ${handle.getState}")
        SubmitResponse(handle.getAppId, submitRequest.properties)
      case Failure(e) => throw e
    }
  }

  private def launch(sparkLauncher: SparkLauncher): SparkAppHandle = {
    logger.info("[StreamPark][Spark][YarnClient] The spark job starting")
    val submitFinished: CountDownLatch = new CountDownLatch(1)
    val sparkAppHandle = sparkLauncher.startApplication(new SparkAppHandle.Listener() {
      override def infoChanged(sparkAppHandle: SparkAppHandle): Unit = {}
      override def stateChanged(handle: SparkAppHandle): Unit = {
        if (handle.getAppId != null) {
          logger.info("{} stateChanged :{}", Array(handle.getAppId, handle.getState.toString))
        } else {
          logger.info("stateChanged :{}", handle.getState.toString)
        }
        if (SparkAppHandle.State.FAILED == handle.getState) {
          logger.error("Task run failure stateChanged :{}", handle.getState.toString)
        }
        if (handle.getState.isFinal) {
          submitFinished.countDown()
        }
      }
    })
    submitFinished.await()
    sparkAppHandle
  }

  private def prepareSparkLauncher(submitRequest: SubmitRequest) = {
    new SparkLauncher()
      .setSparkHome(submitRequest.sparkVersion.sparkHome)
      .setAppResource(submitRequest.userJarPath)
      .setMainClass(submitRequest.appMain)
      .setAppName(submitRequest.effectiveAppName)
      .setConf(
        "spark.yarn.jars",
        submitRequest.hdfsWorkspace.sparkLib + "/*.jar")
      .setVerbose(true)
      .setMaster("yarn")
      .setDeployMode(submitRequest.executionMode match {
        case SparkExecutionMode.YARN_CLIENT => "client"
        case SparkExecutionMode.YARN_CLUSTER => "cluster"
        case _ => throw new IllegalArgumentException("[StreamPark][Spark] Invalid spark on yarn deployMode, only support \"client\" and \"cluster\".")
      })
  }

  private def setSparkConfig(submitRequest: SubmitRequest, sparkLauncher: SparkLauncher): Unit = {
    logger.info("[StreamPark][Spark][YarnClient] set spark configuration.")
    // 1) set spark conf
    submitRequest.properties.foreach(prop => {
      val k = prop._1
      val v = prop._2
      logInfo(s"| $k  : $v")
      sparkLauncher.setConf(k, v)
    })

    // 2) appArgs...
    if (submitRequest.hasExtra("sql")) {
      sparkLauncher.addAppArgs("--sql", submitRequest.getExtra("sql").toString)
    }
  }

}
