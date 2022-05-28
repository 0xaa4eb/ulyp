[![Build Status](https://travis-ci.com/0xaa4eb/ulyp.svg?branch=master)](https://travis-ci.com/0xaa4eb/ulyp)
[![Build Status](https://circleci.com/gh/0xaa4eb/ulyp/tree/master.svg?style=svg)](https://circleci.com/gh/0xaa4eb/ulyp/tree/master)
[![](https://tokei.rs/b1/github/0xaa4eb/ulyp)](https://github.com/0xaa4eb/ulyp)

## TL;DR

Ulyp is a Java recording debugger. Usage is relatively simple.

* First, download or build the agent
* Second, use system properties in order to enable the recording. The properties should tell what method should be recorded and where recording result should be saved.
    

    -javaagent:~/Work/ulyp/ulyp-agent/build/libs/ulyp-agent-0.2.1.0.jar
    -Dulyp.file=/tmp/hibernate-recording.dat
    -Dulyp.methods=**.HibernateShowcase.save

* Run your code with system properties set.


    public class HibernateShowcase {
        ...

        public void save() throws Exception {
            User user = new User("Test", "User");
            saver.save(user);
        }
    }

* Run the UI and open the recording file. Enjoy the view

![Ulyp UI](https://github.com/0xaa4eb/ulyp/blob/master/images/hibernate.png)

## How to build

Build with gradle:
    `./gradlew build`

Currently, all JDKs are supported since java 8. However, there is one caveat. JavaFX was removed since Java 9, so in order to run the UI one will need a command `./gradlew :ulyp-ui:run`

## Key details

All instrumentation is done using [byte buddy](https://github.com/raphw/byte-buddy) library. 
All Java objects are recorded by the [recorders](https://github.com/0xaa4eb/ulyp/tree/master/ulyp-common/src/main/java/com/ulyp/core/recorders). 
Each recorder supports a particular set of Java types and is responsible for serializing object 
values into bytes. [SBE](https://github.com/real-logic/simple-binary-encoding) library is used for serializing objects into bytes.
Note that Ulyp doesn't fully serialize objects. Let's say, for `String` the first couple of hundred symbols are only recorded. 


All data is written to file in a flat format. UI later uses [RocksDB](https://github.com/facebook/rocksdb) in order to build the index


The agent has functional tests. The agent is built and then used to record Java subprocess execution. 
The recording is then later analyzed and verified. [Here](https://github.com/0xaa4eb/ulyp/blob/master/ulyp-agent-tests/src/test/java/com/agent/tests/recorders/CharRecorderTest.java) is the example of test. 