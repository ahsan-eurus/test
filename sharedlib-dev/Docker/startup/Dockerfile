FROM alpine:3.1
USER root
RUN apk add --update bash git 
RUN mkdir -p /jenkins
WORKDIR /jenkins
COPY startup.sh startup.sh

RUN chmod +x startup.sh
CMD ["sh", "startup.sh"]