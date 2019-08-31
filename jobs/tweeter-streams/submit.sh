#/wait-for-step.sh

cd /usr/src/app
cp ${FLINK_APPLICATION_JAR_ORIGIN} ${FLINK_APPLICATION_JAR_DESTINATION}

echo "Submit application ${FLINK_APPLICATION_JAR_DESTINATION} with main class ${FLINK_APPLICATION_MAIN_CLASS} to Flink master"
echo "Passing arguments ${FLINK_APPLICATION_ARGS}"
FLINK_MASTER_PORT_8081_TCP_ADDR=`host ${FLINK_MASTER_PORT_8081_TCP_ADDR} | grep "has address" | awk '{print $4}'`

#/execute-step.sh
/opt/flink/bin/flink run -c ${FLINK_APPLICATION_MAIN_CLASS} -m $FLINK_MASTER_PORT_8081_TCP_ADDR:$FLINK_MASTER_PORT_8081_TCP_PORT \
    ${FLINK_APPLICATION_JAR_DESTINATION} ${FLINK_APPLICATION_ARGS}
#/finish-step.sh