ARG POSTGRES_IMAGE

FROM $POSTGRES_IMAGE

ADD init_setup.sql /docker-entrypoint-initdb.d/
RUN chown -R postgres:postgres /docker-entrypoint-initdb.d/*.sql
