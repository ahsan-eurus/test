FROM 270996056496.dkr.ecr.us-east-1.amazonaws.com/jenkins-slave:base
ENV TERRAFORM_VERSION 0.12.18

ENV TF_PLUGIN_CACHE_DIR=/home/jenkins/.terraform.d/plugin-cache

USER root
COPY provider.tf /tmp
RUN mkdir -p ${TF_PLUGIN_CACHE_DIR}/linux_amd64 && \
    mkdir -p /home/jenkins/.terraform.d/plugins/linux_amd64 && \
    wget https://releases.hashicorp.com/terraform/${TERRAFORM_VERSION}/terraform_${TERRAFORM_VERSION}_linux_amd64.zip && \
    wget https://github.com/SumoLogic/sumologic-terraform-provider/releases/download/v1.0.0/sumologic-terraform-provider_1.0.0_Linux_64-bit.zip && \
    unzip sumologic-terraform-provider_1.0.0_Linux_64-bit.zip && \
    mv terraform-provider-sumologic /home/jenkins/.terraform.d/plugins/linux_amd64/terraform-provider-sumologic_v1.0.0 && \
    unzip terraform_${TERRAFORM_VERSION}_linux_amd64.zip -d /usr/local/bin/ && \
    rm *.zip && \
    mkdir /opt/terraform && \
    ln -s /usr/local/bin/terraform /opt/terraform/terraform && \
    cd /tmp && terraform init && rm -rf /tmp/*
USER jenkins
