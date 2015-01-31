/*
 * Copyright 2013-2015 Michael Krolikowski
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
package com.github.mkroli.webresources

import org.apache.commons.io.FileUtils
import sbt._
import sbt.Keys.target
import sbt.Keys.streams

object WebResources extends Plugin {
  val webResources = settingKey[Map[String, String]]("Mapping from file to url")

  val webResourcesBase = settingKey[File]("Base folder of downloaded files")

  val resolveWebResources = taskKey[Seq[File]]("Download files")

  lazy val webResourceSettings = Seq(
    webResources := Map(),
    webResourcesBase := target.value / "webresources",
    resolveWebResources <<= (streams, webResources, webResourcesBase) map {
      (streams, webResources, webResourcesBase) =>
        def download(file: File, url: URL) = {
          streams.log.info(s"Downloading ${file.getName}")
          FileUtils.copyURLToFile(url, file, 10000, 10000)
          file
        }

        webResources.map {
          case (filename, url) => (webResourcesBase / filename) -> new URL(url)
        }.foldLeft(Map[URL, List[File]]()) {
          case (map, (file, url)) =>
            map + (url -> (file :: map.getOrElse(url, List())))
        }.par.flatMap {
          case (url, filenames) =>
            val downloaded = filenames.find(_.exists).getOrElse(download(filenames.head, url))
            filenames.collect {
              case filename if !filename.exists => FileUtils.copyFile(downloaded, filename)
            }
            filenames
        }.seq.toSeq
    })
}
