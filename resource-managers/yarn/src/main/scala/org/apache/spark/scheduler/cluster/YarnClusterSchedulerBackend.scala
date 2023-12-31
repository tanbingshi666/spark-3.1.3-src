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

package org.apache.spark.scheduler.cluster

import org.apache.spark.SparkContext
import org.apache.spark.deploy.yarn.ApplicationMaster
import org.apache.spark.scheduler.TaskSchedulerImpl
import org.apache.spark.util.YarnContainerInfoHelper

private[spark] class YarnClusterSchedulerBackend(
    scheduler: TaskSchedulerImpl,
    sc: SparkContext)
  extends YarnSchedulerBackend(scheduler, sc) {

  override def start(): Unit = {
    // 1 获取 ApplicationMaster ApplicationAttemptId
    val attemptId = ApplicationMaster.getAttemptId
    // 2 赋值 ApplicationId、ApplicationAttemptId 信息
    bindToYarn(attemptId.getApplicationId(), Some(attemptId))
    // 3 启动 YarnClusterSchedulerBackend 也即调用其父类 CoarseGrainedSchedulerBackend.start()
    // 里面关于 Security 信息
    super.start()
    // 4 获取希望 executor 个数 默认 2
    totalExpectedExecutors = SchedulerBackendUtils.getInitialTargetExecutorNumber(sc.conf)
    startBindings()
  }

  override def getDriverLogUrls: Option[Map[String, String]] = {
    YarnContainerInfoHelper.getLogUrls(sc.hadoopConfiguration, container = None)
  }

  override def getDriverAttributes: Option[Map[String, String]] = {
    YarnContainerInfoHelper.getAttributes(sc.hadoopConfiguration, container = None)
  }
}
