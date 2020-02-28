FROM ubuntu:16.04

ARG VERSION=4.0.1
ARG user=jenkins
ARG group=jenkins
ARG uid=1000
ARG gid=1000
ARG AGENT_WORKDIR=/home/${user}/agent
ENV DEBIAN_FRONTEND=noninteractive

RUN groupadd -g ${gid} ${group} && \
    useradd -c "Jenkins user" -d /home/${user} -u ${uid} -g ${gid} -m ${user}

RUN apt-get update > /dev/null && \
    apt-get -qq install apt-utils software-properties-common < /dev/null > /dev/null

# Yocto Build Requirements
# https://www.yoctoproject.org/docs/3.0/ref-manual/ref-manual.html
RUN apt-get -qq install gawk wget git-core diffstat unzip texinfo gcc-multilib locales\
    build-essential chrpath socat cpio python python3 python3-pip python3-pexpect \
    xz-utils debianutils iputils-ping python3-git python3-jinja2 libegl1-mesa libsdl1.2-dev \
    pylint3 xterm < /dev/null > /dev/null

# SCM
RUN apt-get -qq install figlet zip pigz jq curl < /dev/null > /dev/null

# Jenkins Slave Requirements
RUN apt-get -qq install openjdk-9-jdk-headless < /dev/null > /dev/null

RUN apt-get clean && \
    apt-get clean autoclean && \
    apt-get autoremove --yes

RUN curl --create-dirs -fsSLo /usr/share/jenkins/agent.jar https://repo.jenkins-ci.org/public/org/jenkins-ci/main/remoting/${VERSION}/remoting-${VERSION}.jar &&\
    chmod 755 /usr/share/jenkins && \
    chmod 644 /usr/share/jenkins/agent.jar && \
    ln -sf /usr/share/jenkins/agent.jar /usr/share/jenkins/slave.jar
COPY jenkins-agent /usr/local/bin/jenkins-agent
RUN chmod +x /usr/local/bin/jenkins-agent &&\
    ln -s /usr/local/bin/jenkins-agent /usr/local/bin/jenkins-slave

# Fix error "Please use a locale setting which supports utf-8."
# See https://wiki.yoctoproject.org/wiki/TipsAndTricks/ResolvingLocaleIssues
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && \
        echo 'LANG="en_US.UTF-8"'>/etc/default/locale && \
        dpkg-reconfigure --frontend=noninteractive locales && \
        update-locale LANG=en_US.UTF-8

ENV LC_ALL en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.UTF-8

# install repo and git-lfs
RUN curl -o /usr/local/bin/repo https://storage.googleapis.com/git-repo-downloads/repo && chmod a+x /usr/local/bin/repo && \
    curl -s https://packagecloud.io/install/repositories/github/git-lfs/script.deb.sh | bash
RUN curl -fL https://getcli.jfrog.io | sh && mv jfrog /usr/local/bin/jfrog && chown ${user}:${group} /usr/local/bin/jfrog
USER ${user}
ENV AGENT_WORKDIR=${AGENT_WORKDIR}

RUN mkdir -p /home/${user}/agent && mkdir /home/${user}/.jenkins && mkdir /home/${user}/.ssh && \
    echo "Host * \n StrictHostKeyChecking no" > /home/${user}/.ssh/config

VOLUME /home/${user}/.jenkins
VOLUME ${AGENT_WORKDIR}
WORKDIR /home/${user}
ENTRYPOINT ["/bin/sh", "/usr/local/bin/jenkins-agent"]
