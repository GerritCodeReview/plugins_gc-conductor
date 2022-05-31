#!/bin/bash -e

if [ $# -eq 0 ]; then
  echo "please add arguments --gc-executor-path and --postgres-driver-path"
else
  while [ $# -ne 0 ]; do
    case "$1" in
    "--help")
      echo "Usage: sh $0 [--option $value]"
      echo
      echo "[--gc-conductor-path]            Path to gc-conductor.jar"
      echo
      echo "[--gc-executor-path]            Path to gc-executor.jar"
      echo
      echo "[--postgres-driver-path]        Path to postgresql-42.2.5.jar"
      echo
      echo "[--postgres-image-path]         PostgreSQL image path without image name (optional)"
      echo
      echo "[--gerrit-image-path]           Gerrit image path without image name (optional)"
      echo
      echo "[--detached-mode]               Argument to enable detached mode, [-d]"
      exit 0
      ;;
    "--gc-conductor-path")
          export GC_CONDUCTOR_PATH=$2
          shift
          shift
          ;;
    "--gc-executor-path")
      export GC_EXECUTOR_PATH=$2
      shift
      shift
      ;;
    "--postgres-driver-path")
      export POSTGRES_DRIVER_PATH=$2
      shift
      shift
      ;;
    "--postgres-image-path")
      export POSTGRES_IMAGE_PATH=$2
      shift
      shift
      ;;
    "--gerrit-image-path")
      export GERRIT_IMAGE_PATH=$2
      shift
      shift
      ;;
    "--detached-mode")
      export DETACHED_MODE=$2
      shift
      shift
      ;;
    *)
      echo "Unknown option argument: $1"
      shift
      shift
      ;;
    esac
  done

  if [ -z "$POSTGRES_IMAGE_PATH" ]; then
    export POSTGRES_IMAGE_PATH=postgres
  fi
  if [ -z "$GERRIT_IMAGE_PATH" ]; then
    export GERRIT_IMAGE_PATH=gerritcodereview/gerrit
  fi

  docker-compose build --build-arg POSTGRES_DRIVER=$POSTGRES_DRIVER_PATH \
    --build-arg POSTGRES_IMAGE=$POSTGRES_IMAGE_PATH \
    --build-arg GERRIT_IMAGE=$GERRIT_IMAGE_PATH
  docker-compose up $DETACHED_MODE
fi
