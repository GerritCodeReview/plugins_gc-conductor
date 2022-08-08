ARG GERRIT_IMAGE

FROM $GERRIT_IMAGE

ENV GERRIT_BRANCH=stable-3.5

ARG POSTGRES_DRIVER

ADD --chown=gerrit:gerrit $POSTGRES_DRIVER /var/gerrit/lib/postgresql-42.2.5.jar

USER root

ADD start_gerrit.sh /bin/

RUN rm -Rf /var/gerrit/{git,index,cache}/*

ARG GERRIT_UID=1000
RUN usermod -u ${GERRIT_UID} gerrit &> /dev/null

ENTRYPOINT ["/usr/bin/env"]
CMD /bin/start_gerrit.sh
