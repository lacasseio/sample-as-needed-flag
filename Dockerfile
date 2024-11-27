# Python required by compiler-explorer/infra build process
FROM --platform=linux/amd64 python:3.12-bookworm

# Install Java for Gradle
RUN apt update && apt install -y openjdk-17-jdk

# Install Compiler Explorer toolchain installer
RUN apt update && apt install -y make git curl xz-utils bash \
    && git clone https://github.com/compiler-explorer/infra.git \
    && cd infra \
    && make ce \
    && ln -s $PWD/bin/ce_install /usr/local/bin/ce_install

# Install minimum package for building native
RUN apt update && apt install -y gcc-multilib binutils

# Install GCC toolchain
RUN ce_install install compilers/c++/x86/gcc 14.2.0
ENV PATH="/opt/compiler-explorer/gcc-14.2.0/bin:$PATH"

ENTRYPOINT ["bash"]