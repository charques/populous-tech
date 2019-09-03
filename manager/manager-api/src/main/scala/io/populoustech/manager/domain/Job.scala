package io.populoustech.manager.domain

import io.populoustech.manager.PopulousManagerServer._

sealed case class Job(
    var id: Option[String], // newly created todos won't have an id
    var tag: Option[String]
  ) {

  lazy val url = s"http://$HOST:$PORT/jobs/${id.getOrElse("")}"

}
