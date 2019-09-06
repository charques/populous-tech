package io.populoustech.manager.domain

sealed case class FlinkJarsUploadResponse(
    var filename: Option[String],
    var status: Option[String]
  )
