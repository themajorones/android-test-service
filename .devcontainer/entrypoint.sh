#!/bin/bash

# Run dnf installs on first container run
if [ ! -f /var/lib/autotest_service_initialized ]; then
    dnf -y update && \
    dnf -y install \
    jq git gh wget curl fish \
    java-25-openjdk-devel nodejs npm make \
    util-linux-user && \
    dnf clean all
    touch /var/lib/autotest_service_initialized
    chsh -s `which fish`
fi

# Execute the original command or keep container running
exec "$@"
