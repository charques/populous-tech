package io.populoustech.manager

object ConfigurationKeys extends Enumeration {
    val MANAGER_API_HOST = "MANAGER_API_HOST"
    val MANAGER_API_PORT = "MANAGER_API_PORT"

    val FLINK_HOST = "FLINK_HOST"
    val FLINK_PORT = "FLINK_PORT"

    val JAR_FILE_NAME = "JAR_FILE_NAME"
    val JAR_PATH = "JAR_PATH"
    val JAR_MEDIA_TYPE = "JAR_MEDIA_TYPE"
    val JOB_ENTRY_CLASS = "JOB_ENTRY_CLASS"
}
