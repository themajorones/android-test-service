FROM fedora:44

# Install requested packages
RUN dnf -y update && \
    dnf -y install \
    jq git gh wget curl fish \
    java-25-openjdk-devel nodejs npm make \
    util-linux-user && \
    dnf clean all

# Set fish as default shell for root
RUN chsh -s /usr/bin/fish root

ENV JAVA_HOME=/usr/lib/jvm/java-25-openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Copy repository into the container
WORKDIR /workspaces/autotest_service
COPY . .