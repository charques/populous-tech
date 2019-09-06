package io.populoustech.manager.domain

sealed case class CreateJobRequest (
    var tag: Option[String]
  )