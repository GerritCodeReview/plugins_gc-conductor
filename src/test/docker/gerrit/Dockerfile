ARG GERRIT_IMAGE

FROM $GERRIT_IMAGE/gerrit:3.5.0-rc2

ENV GERRIT_BRANCH=stable-3.5

ENV GERRIT_CI_URL=https://gerrit-ci.gerritforge.com/job
ARG POSTGRES_DRIVER

ADD --chown=gerrit:gerrit $GERRIT_CI_URL/plugin-javamelody-bazel-master-$GERRIT_BRANCH/lastSuccessfulBuild/artifact/bazel-bin/plugins/javamelody/javamelody.jar /var/gerrit/plugins/javamelody.jar
ADD --chown=gerrit:gerrit $POSTGRES_DRIVER /var/gerrit/lib/postgresql-42.2.5.jar

USER root

ADD start_gerrit.sh /bin/

RUN rm -Rf /var/gerrit/{git,index,cache}/*

ARG GERRIT_UID=1000
RUN usermod -u ${GERRIT_UID} gerrit &> /dev/null

ENTRYPOINT ["/usr/bin/env"]
CMD /bin/start_gerrit.sh
