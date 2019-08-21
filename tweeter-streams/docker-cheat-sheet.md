docker-machine ls
docker-machine start default
eval $(docker-machine env default)
docker ps
docker ps -a

docker exec -it populous-tech_tweeter-streams_1 /bin/bash

create image ---
docker build -t populous:v3 .

docker run --network docker-elk_elk --name populous_v3 populous:v3 

docker exec -it populous_v4 /bin/bash

remove all containers -----
docker rm $(docker ps -a -f status=exited -q)

remove all images -----
docker rmi $(docker images -a -q)