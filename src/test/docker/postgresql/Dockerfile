ARG POSTGRES_IMAGE

FROM $POSTGRES_IMAGE/postgres:latest

ADD init_setup.sql /docker-entrypoint-initdb.d/
