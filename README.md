[![Build Status](https://travis-ci.com/0xaa4eb/ulyp.svg?branch=master)](https://travis-ci.com/0xaa4eb/ulyp)
[![Build Status](https://circleci.com/gh/0xaa4eb/ulyp/tree/master.svg?style=svg)](https://circleci.com/gh/0xaa4eb/ulyp/tree/master)
[![](https://tokei.rs/b1/github/0xaa4eb/ulyp)](https://github.com/0xaa4eb/ulyp)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=0xaa4eb_ulyp&metric=alert_status)](https://sonarcloud.io/dashboard?id=0xaa4eb_ulyp)

# TL;DR

### Example of usage

Ulyp is a Java recording debugger. Usage is relatively simple.

* First, download or build the agent
* Second, use system properties in order to enable the recording. Here is the minimum set of options one will need for recording.
    
    
    ```
    -javaagent:~/Work/ulyp/ulyp-agent/build/libs/ulyp-agent-0.2.1.0.jar
    -Dulyp.methods=**.HibernateShowcase.save
    -Dulyp.file=/tmp/hibernate-recording.dat
    ```
    
    
All methods with name `save` and class name `HibernateShowcase` (regardless of the package) will be recorded including all nested method calls.
    

* Run your code with system properties set.
    
    
    ```
    public void save() throws Exception {
        User user = new User("Test", "User");
        saver.save(user);
    }
    ```

* Run the UI and open the recording file. Enjoy the view

![Hibernate call recorded](https://github.com/0xaa4eb/ulyp/blob/master/images/hibernate.png)

### How to build

Build with gradle:
    `./gradlew build`

Build without tests:
`./gradlew build -x test`

Run UI:
    `./gradlew :ulyp-ui:run`

### Key details

All instrumentation is done using [byte buddy](https://github.com/raphw/byte-buddy) library. 
All Java objects are recorded by the [recorders](https://github.com/0xaa4eb/ulyp/tree/master/ulyp-common/src/main/java/com/ulyp/core/recorders). 
Each recorder supports a particular set of Java types and is responsible for serializing object 
values into bytes. [SBE](https://github.com/real-logic/simple-binary-encoding) library is used for serializing objects into bytes.
Note that Ulyp doesn't fully serialize objects. Let's say, for `String` the first couple of hundred symbols are only recorded. 


All data is written to file in a flat format. UI later uses [RocksDB](https://github.com/facebook/rocksdb) in order to build the index


The agent has functional tests. The agent is built and then used to record Java subprocess execution. 
The recording is then later analyzed and verified. [Here](https://github.com/0xaa4eb/ulyp/blob/master/ulyp-agent-tests/src/test/java/com/agent/tests/recorders/CharRecorderTest.java) is the example of test. 

## What's not recorded

Currently, none of java standard library classes are instrumented. This means calls of, let's say, `add` method of java
collections are not recorded. However, Ulyp does record **object values** of java system library classes like strings, numbers,
collections (more on that below) etc.

Type initializers (`static` blocks) are not recorded and there are no plans to support this.

Constructors are not recorded by default (which may distort the view), but may be recorded by specifying system prop `-Dulyp.constructors`. The reason why constructors
are not recorded by default, is simply that it's not possible to instrument constructors in such fashion that any exception thrown inside the constructor
is caught and rethrown. But this is exactly what should be done, but is not possible. Therefore, when such case happens, the corresponding recording file 
may become invalid. In that case UI will show error, and a user should disable the option and run the code again without constructors recorded.

Collections are not recorded by default but can be with system prop `-Dulyp.collections`. The available values are `JAVA` 
and `ALL`. When set to `ALL` all collection values will be recorded (actually only first three items are recorded). This means Ulyp
will iterate any object which implements `java.util.Collection` interface. This may be too much especially for certain programs
where there are a lot of proxy collections (like Hibernate or Hazelcast proxy collections when iteration triggers network calls).
Hence the `JAVA` option value which only records Java collections.

Try running some code with additional properties `-Dulyp.constructors -Dulyp.collections=JAVA` to see the difference:

![Hibernate call recorded with constructors](https://github.com/0xaa4eb/ulyp/blob/master/images/hibernate_constructors.png)

# UI

### UI Controls

<table border="1">
<tr>
		<th>Hotkey</th>
		<th>Action</th>
</tr>
<tr><td>Hold Shift</td><td>Show full type names</td></tr>
<tr><td>Press =</td><td>Increase font size</td></tr>
<tr><td>Press -</td><td>Decrease font size</td></tr>
</table>