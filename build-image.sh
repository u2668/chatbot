#!/bin/sh

# build app
./mvnw package

# build image
docker build -t u2668/chat-bot .

# tag image
docker tag u2668/chat-bot u2668/chat-bot:with-gears

# publish image
docker push u2668/chat-bot:with-gears