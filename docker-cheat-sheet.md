remove all containers -----
docker rm $(docker ps -a -f status=exited -q)

remove all images -----
docker rmi $(docker images -a -q)

docker-compose run tweeter-streams
docker-compose up flink-deployer



create tweeter-streams
docker build --rm=true -t tweeter-streams .
docker-compose -f docker-compose-jobs.yml up

ver contenido
docker run -it tweeter-streams sh

get jar file
docker run --rm=true -it -v /Users/charques/Dev/populous-tech/tmp/tweeter-streams:/app tweeter-streams

copy flink job
docker cp /Users/charques/Dev/populous-tech/tmp/tweeter-streams/application.jar $(docker ps --filter name=flink-jobmanager --format={{.ID}}):/application.jar

run the job
docker exec -it $(docker ps --filter name=flink-jobmanager --format={{.ID}}) flink run -c io.populoustech.TweeterStreamToFileJob /application.jar


https://github.com/big-data-europe/docker-flink/tree/master/template/sbt
https://github.com/melentye/flink-docker

http://localhost:8081/#/overview