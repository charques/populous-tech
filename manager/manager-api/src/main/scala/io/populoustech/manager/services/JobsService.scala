package io.populoustech.manager.services

import io.populoustech.manager.domain.Job

import scala.concurrent.Future

trait JobsService {

  def list(): Future[Iterable[Job]]
  //def create(job: Job): Future[Job]
  //def update(id: String, job: Job): Future[Option[Job]]
  //def retrieve(id: String): Future[Option[Job]]
}
