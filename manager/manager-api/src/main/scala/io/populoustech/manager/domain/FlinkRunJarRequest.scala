package io.populoustech.manager.domain

sealed case class FlinkRunJarRequest (
    var entryClass: String/*,
    var programArgs: Option[String],
    var parallelism: Option[Int],
    var jobId: Option[String],
    var allowNonRestoredState: Option[Boolean],
    var savepointPath: Option[String]*/
)
