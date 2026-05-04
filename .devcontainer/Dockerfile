FROM fedora:44

# Install OpenJDK 25, Node.js 22, and build essentials
RUN dnf -y update && \
    dnf -y install \
    java-25-openjdk-devel \
    nodejs \
    npm \
    git \
    make \
    && dnf clean all

ENV JAVA_HOME=/usr/lib/jvm/java-25-openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"