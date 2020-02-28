FROM jenkinsci/jnlp-slave

ENV DOCKER_BUCKET get.docker.com
ENV DOCKER_VERSION 1.11.0.tgz
ENV DOCKER_SHA256 87331b3b75d32d3de5d507db9a19a24dd30ff9b2eb6a5a9bdfaba954da15e16b
ENV DOCKER_HOME /tmp/docker_tar
ENV DOCKER_HOST unix:///var/run/docker.sock
ENV DOCKER_COMPOSE_VERSION 1.9.0
# GID currently in use by AWS EC2 Container Service
ENV DOCKER_GID 497

USER root

RUN curl -fSL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-$DOCKER_VERSION" -o ${DOCKER_HOME} && \
    echo "${DOCKER_SHA256} ${DOCKER_HOME}" | sha256sum -c - && \
    chmod +x ${DOCKER_HOME} && \
    tar -xvf /tmp/docker_tar -C /tmp && \
    cp /tmp/docker/docker /usr/bin/docker && \
    rm -rf /tmp/docker_tar && \
    rm -rf /tmp/docker && \
    groupadd -g ${DOCKER_GID} docker && \
    usermod -G docker jenkins

RUN curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/bin/docker-compose \
    && chmod +x /usr/bin/docker-compose

RUN \
  # install utilities
  apt-get update && apt-get install -y \
     wget \
     curl \
     git \
     zip \
     bzip2 \
     jq \
     python \
     python-pip && \
     pip install awscli && \
     apt-get clean all
USER jenkins
