[![Price](https://img.shields.io/badge/price-FREE-0098f7.svg)](https://github.com/0xaa4eb/ulyp/blob/master/LICENSE)
[![Build Status](https://circleci.com/gh/0xaa4eb/ulyp/tree/master.svg?style=svg)](https://circleci.com/gh/0xaa4eb/ulyp/tree/master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=0xaa4eb_ulyp&metric=alert_status)](https://sonarcloud.io/dashboard?id=0xaa4eb_ulyp)
[![Maintainability](https://api.codeclimate.com/v1/badges/e76192efb9583aca1170/maintainability)](https://codeclimate.com/github/0xaa4eb/ulyp/maintainability)
![lines of code](https://raw.githubusercontent.com/0xaa4eb/ulyp/project-badges/loc-badge.svg)

## TL;DR

Ulyp instruments all third-party library classes and record their method calls including return values and 
arguments, so that you can have a better understanding of what your code does. Instrumentation is done by [byte-buddy](https://github.com/raphw/byte-buddy) library. 
UI is written using JavaFX.

## Example of usage

Usage is relatively simple.

* First, download or build the agent
* Second, use VM properties in order to enable the recording. Here is the minimum set of options one will need for recording.
    
    
    ```
    -javaagent:~/Work/ulyp/ulyp-agent/build/libs/ulyp-agent-0.2.1.0.jar
    -Dulyp.methods=**.HibernateShowcase.save
    -Dulyp.file=/tmp/hibernate-recording.dat
    ```
    
    
Whenever methods with name `save` and class name `**.HibernateShowcase` (inheritors including) are called, recording will start. 
The data is dropped to the specified file.

* Run your code with system properties set.
    
    
    ```
    @Transactional
    public void save() throws Exception {
        User user = new User("Test", "User");
        saver.save(user);
    }
    ```

* Run the UI and open the recording file

![Hibernate call recorded](https://github.com/0xaa4eb/ulyp/blob/master/images/hibernate.png)

## Similar projects

Ulyp is POC and unstable all the time since the beginning. There are already similar projects which you might consider to use before Ulyp.
<table border="1">
<tr>
		<th>Project</th>
		<th>Link</th>
		<th>Source</th>
</tr>
<tr><td>Bugjail</td><td>bugjail.com</td><td>Closed source</td></tr>
<tr><td>Findtheflow</td><td>findtheflow.io</td><td>Closed source</td></tr>
</table>

## Build from source

Build with gradle:
    `./gradlew build`

Build UI jar file (Java 11+ is preferable) :
    `./gradlew :ulyp-ui:fatJar`

UI jar file for a particular platform can be built as follows:
`./gradlew :ulyp-ui:fatJar -Pplatform=mac`

Available platforms for the build are: `linux`, `linux-aarch64`, `win`, `mac`, `mac-aarch64`

## Key details

All instrumentation is done using [byte buddy](https://github.com/raphw/byte-buddy) library. 
All Java objects are recorded by the [recorders](https://github.com/0xaa4eb/ulyp/tree/master/ulyp-common/src/main/java/com/ulyp/core/recorders). 
Each recorder supports a particular set of Java types and is responsible for serializing object 
values into bytes.
Note that Ulyp doesn't fully serialize objects. Let's say, for `String` the first couple of hundred symbols are only recorded. 

All data is written to file in a flat format. UI later uses [RocksDB](https://github.com/facebook/rocksdb) in order to build the index

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
<tr><td>Hold X</td><td>Show method call duration (if enabled with -Dulyp.timestamps while recording)</td></tr>
<tr><td>Press =</td><td>Increase font size</td></tr>
<tr><td>Press -</td><td>Decrease font size</td></tr>
</table>
