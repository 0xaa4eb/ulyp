[![Price](https://img.shields.io/badge/price-FREE-0098f7.svg)](https://github.com/0xaa4eb/ulyp/blob/master/LICENSE)
[![Build Status](https://circleci.com/gh/0xaa4eb/ulyp/tree/master.svg?style=svg)](https://circleci.com/gh/0xaa4eb/ulyp/tree/master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=0xaa4eb_ulyp&metric=alert_status)](https://sonarcloud.io/dashboard?id=0xaa4eb_ulyp)
[![Maintainability](https://api.codeclimate.com/v1/badges/e76192efb9583aca1170/maintainability)](https://codeclimate.com/github/0xaa4eb/ulyp/maintainability)
![lines of code](https://raw.githubusercontent.com/0xaa4eb/ulyp/project-badges/loc-badge.svg)

## TL;DR

Ulyp instruments all third-party library classes and record their method calls including return values and 
arguments, so that you can have a better understanding of what your code does. Instrumentation is done by [byte-buddy](https://github.com/raphw/byte-buddy) library. 
UI is written using JavaFX.

Here is the example of recorded execution of Hibernate framework

    ```
    @Transactional
    public void save(Person person) {
        personRepository.save(person);
    }
    ```

![Hibernate call recorded](https://github.com/0xaa4eb/ulyp/blob/master/images/hibernate.png)

## Example of usage

Usage is relatively simple.

* First, download or build the agent
* Second, use VM properties in order to enable the recording. Here is the minimum set of options one will need for recording.
    
    
    ```
    -javaagent:~/Work/ulyp/ulyp-agent/build/libs/ulyp-agent-0.2.1.0.jar
    -Dulyp.methods=**.HibernateShowcase.save
    -Dulyp.collections=JAVA
    -Dulyp.constructors
    -Dulyp.file=/tmp/hibernate-recording.dat
    ```
    
    
Whenever methods with name `save` and class name `**.HibernateShowcase` (inheritors including) are called, recording will start. 
The data is dropped to the specified file.

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

Build agent (no tests):
    `./gradlew :ulyp-agent:shadowJar`

Build UI jar file (Java 11+ (preferred) or Java 8 Oracle with Java FX bundled) :
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
