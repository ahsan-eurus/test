FROM ubuntu:latest
RUN apt-get update -y
RUN apt-get -y install openssh-client

ADD keygen.sh keygen.sh
RUN chmod 755 keygen.sh

CMD ["./keygen.sh"]
