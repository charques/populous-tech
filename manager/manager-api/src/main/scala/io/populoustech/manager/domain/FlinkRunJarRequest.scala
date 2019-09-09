package io.populoustech.manager.domain

class FlinkRunJarRequest(val entryClass: String, var programArgs: String = "") {

  //var parallelism: Option[Int],
  //var jobId: Option[String],
  //var allowNonRestoredState: Option[Boolean],
  //var savepointPath: Option[String]*/

  def addArg(property: String, value: String): Unit = {
    if (programArgs.length > 0) programArgs += ","
    programArgs += property + "=#" + value
  }
}
