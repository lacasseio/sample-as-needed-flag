FROM --platform=linux/amd64 eclipse-temurin:11-jdk-jammy

# Install Compiler Explorer toolchain installer
RUN <<EOF
apt update && apt install -y make git curl xz-utils bash python3
git clone https://github.com/compiler-explorer/infra.git && cd infra
# As of 2024-11-30, the latest main branch was buggy
git reset --hard ca3e34a621b38533283e033b4fdefcf1ecc1b6bb
make ce
ln -s $PWD/bin/ce_install /usr/local/bin/ce_install
EOF

# Install minimum package for building native
RUN apt update && apt install -y gcc-multilib binutils
#RUN apt install -y wget

# Install GCC toolchain
ENV GCC_VERSION=11.4.0
RUN ce_install install compilers/c++/x86/gcc $GCC_VERSION
ENV PATH="/opt/compiler-explorer/gcc-$GCC_VERSION/bin:$PATH"

ENTRYPOINT ["bash"]
