[![Price](https://img.shields.io/badge/price-FREE-0098f7.svg)](https://github.com/0xaa4eb/ulyp/blob/master/LICENSE)
[![Build Status](https://circleci.com/gh/0xaa4eb/ulyp/tree/master.svg?style=svg)](https://circleci.com/gh/0xaa4eb/ulyp/tree/master)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=0xaa4eb_ulyp&metric=alert_status)](https://sonarcloud.io/dashboard?id=0xaa4eb_ulyp)
[![Maintainability](https://api.codeclimate.com/v1/badges/e76192efb9583aca1170/maintainability)](https://codeclimate.com/github/0xaa4eb/ulyp/maintainability)
![lines of code](https://raw.githubusercontent.com/0xaa4eb/ulyp/project-badges/loc-badge.svg)

## TL;DR

Ulyp instruments all third-party library classes and record their method calls including return values and 
arguments, so that you can have a better understanding of what your code and frameworks do, find inefficiencies, debug faster. Instrumentation is done by [byte-buddy](https://github.com/raphw/byte-buddy) library. 
All data is saved to a file during recording. No clouds uploads, server, etc. UI is a desktop app written using JavaFX.

Here is the basic example of recorded execution of Hibernate framework

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
    -javaagent:~/ulyp-agent-1.0.0.jar
    -Dulyp.methods=**.HibernateShowcase.save
    -Dulyp.file=/tmp/hibernate-recording.dat
    ```
    
    
Whenever methods with name `save` and class name `**.HibernateShowcase` (inheritors including) are called, recording will start. 
The data is written to the specified file which can later be opened in the UI.

## Key details

All instrumentation is done using [byte buddy](https://github.com/raphw/byte-buddy) library. All Java objects are recorded by the specialized [recorders](https://github.com/0xaa4eb/ulyp/tree/master/ulyp-common/src/main/java/com/ulyp/core/recorders). 
Each recorder supports a particular set of Java types and is responsible for serializing object values into bytes. 
Note that Ulyp doesn't fully serialize objects. Let's say, for `String` the first couple of hundred symbols are only recorded. 
All data is written to file in a flat format. 
UI later uses [RocksDB](https://github.com/facebook/rocksdb) in order to build the index

## Options

The agent is controlled via JVM properties.

| Property                          | Description                                                                                                                                                                                                                                       | Example                                                   | Default      |
|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|--------------|
| ulyp.file                         | A path to a file where all recording data should be written                                                                                                                                                                                       | `-Dulyp.file=/tmp/test.dat`                               | -            |
| ulyp.methods                      | A list of method matchers where recording should start                                                                                                                                                                                            | `-Dulyp.methods=**.Runnable.run`                          | Main method  |
| ulyp.start                        | Controls when recording can start. `default` is start recording any time when some of `ulyp.method` is called.<br/> Another option is `delay:X` where X is the number of seconds to wait from the app launch, `api` - remote enable/disable (WIP) | `-Dulyp.start=delay:120`                                  | `default`    |
| ulyp.packages                     | A list of packages to instrument                                                                                                                                                                                                                  | `-Dulyp.packages=org.springframework,io.grpc`             | All packages |
| ulyp.record-timestamps            | Record duration of calls                                                                                                                                                                                                                          | `-Dulyp.record-timestamps`                                | Disabled     |
| ulyp.exclude-packages             | A list of packages to exclude from instrumentation                                                                                                                                                                                                | `-Dulyp.exclude-packages=org.springframework`             | -            |
| ulyp.exclude-types                | A list of type matchers to exclude from instrumentation                                                                                                                                                                                           | `-Dulyp.exclude-types=org.springframework.**.Interceptor` | -            |
| ulyp.record-constructors          | Enables instrumentation (and possibly recording) of constructors                                                                                                                                                                                  | `-Dulyp.record-constructors`                              | Disabled     |
| ulyp.record-collections           | Records values (some number of items) of collections. Possible values are: `NONE`, `JDK` (only JDK collections), `ALL` (dangerous)                                                                                                                | `-Dulyp.record-collections=JDK`                           | `NONE`       |
| ulyp.record-collections.max-items | Max items of collections to record                                                                                                                                                                                                                | `-Dulyp.record-collections.max-items=5`                   | 3            |
| ulyp.record-arrays                | Records values (some number of items) of arrays                                                                                                                                                                                                   | `-Dulyp.record-arrays`                                    | Disabled     |
| ulyp.record-arrays.max-items      | Max items of arrays to record                                                                                                                                                                                                                     | `-Dulyp.record-arrays.max-items=10`                       | 3            |
| ulyp.record-lambdas               | Enabled instrumentation (and possibly recording) of lambdas (experimental)                                                                                                                                                                        | `-Dulyp.record-lambdas`                                   | Disabled     |
| ulyp.record-static-blocks         | Enabled instrumentation (and possibly recording) of static blocks (experimental)                                                                                                                                                                  | `-Dulyp.record-static-blocks`                             | Disabled     |
| ulyp.print-types                  | A list of type matchers to print with toString() while recording their values                                                                                                                                                                     | `-Dulyp.print-types=com.enterprise.**.SomeEntity`         | -            |

## UI

### Controls

<table border="1">
<tr>
		<th>Hotkey</th>
		<th>Action</th>
</tr>
<tr><td>Hold Shift</td><td>Show full type names</td></tr>
<tr><td>Hold X</td><td>Show method call duration (if enabled with -Dulyp.record-timestamps while recording)</td></tr>
<tr><td>Press =</td><td>Increase font size</td></tr>
<tr><td>Press -</td><td>Decrease font size</td></tr>
</table>

## Build from source

Build agent (no tests):
`./gradlew :ulyp-agent:shadowJar`

Build UI jar file (Java 11+ (preferred) or Java 8 Oracle with Java FX bundled) :
`./gradlew :ulyp-ui:fatJar`

UI jar file for a particular platform can be built as follows:
`./gradlew :ulyp-ui:fatJar -Pplatform=mac`

Available platforms for the build are: `linux`, `linux-aarch64`, `win`, `mac`, `mac-aarch64`