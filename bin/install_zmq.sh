#!/bin/bash
export JAVA_HOME=${JAVA_HOME:/usr/libexec/java_home}

if [ ! -d "$JAVA_HOME/include" ]; then
    echo "
    Looks like you're missing your 'include' directory. If you're using Mac OS X, You'll need to install the Java dev package (JDK).

- Navigate to http://goo.gl/UEOm8b (JDK 7u25 download link, use a more recent one if applicable)
- Install the appropriate version
- Make sure the JDK is on your path
- Try again

"
    exit -1;
fi

#install zeromq
wget http://download.zeromq.org/zeromq-2.1.7.tar.gz
tar -xzf zeromq-2.1.7.tar.gz
cd zeromq-2.1.7
./configure
make
sudo make install

cd ../

#install jzmq (both native and into local maven cache)
git clone https://github.com/nathanmarz/jzmq.git
cd jzmq
./autogen.sh
./configure
make
sudo make install
