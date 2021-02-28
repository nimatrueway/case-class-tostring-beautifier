#!/usr/bin/env bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

docker build \
  $DIR/../../ \
  -f build/linux/Dockerfile \
  -t case-class-string-beautifier

docker run \
  --rm \
  -v $DIR/output:/home/output \
  case-class-string-beautifier:latest \
  /bin/bash -c \
    "sbt nativeImage && \
      find /home/target/native-image -mindepth 1 -not -name '.*' -not -name '*.o' -exec cp {} /home/output/ \;"