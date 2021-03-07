[![Build Status](https://travis-ci.org/0xaa4eb/ulyp.svg?branch=master)](https://travis-ci.org/0xaa4eb/ulyp)
[![](https://tokei.rs/b1/github/0xaa4eb/ulyp)](https://github.com/0xaa4eb/ulyp)

# Preface
Ulyp is a proof-of-concept recording instrumentation agent. It records all method calls in the desired
methods (excluding java system classes). The agent should be provided with only two mandatory properties: 
methods to record in the following format (simple class name).(method name) and output file path where all
recording will be saved. 

The main purpose is mostly research of frameworks and  troubleshooting of heavy enterprise applications where 
frameworks like Hibernate or Spring may call thousands of methods. 
Please note, the agent has very high performance impact and it's very intrusive.

# Usage
Ulyp is bundled with very simple desktop UI written on JavaFX and agent itself.

Agent has the following props which are set as java system properties (via `-Dkey=value`)

<table border="1">
<tr>
		<th>Property</th>
		<th>Mandatory</th>
		<th>Default value</th>
		<th>Example</th>
		<th>Description</th>
</tr>
<tr><td>ulyp.file</td><td>Yes</td><td></td><td>/tmp/test.dat</td><td>Output file path</td></tr>
<tr><td>ulyp.methods</td><td>Yes</td><td>main() method of the program</td><td>ClientDao.save,OrderDao.find</td><td>Methods to record</td></tr>
<tr><td>ulyp.packages</td><td>No</td><td>Empty (records all third-part method calls)</td><td>com.my.company,org.hibernate</td><td>Packges to be instrumented. Reducing scope of instrumented class will increase performance</td></tr>
<tr><td>ulyp.exclude-packages</td><td>No</td><td>Empty (doesn't exclude any packages)</td><td>org.apache.log4j</td><td>Packages to be excluded from instrumentation</td></tr>
</table>

Example of running java app with the agent:

	-javaagent:/home/tools/ulyp-agent-0.2.jar -Dulyp.methods=SomeClass.doJob

# Build

	./gradlew clean build test

# Simplest example: Fibbonaci numbers

	package com.perf.agent.benchmarks.showcase;

    public class ComputeFibonacci {
    
        public static int compute(int n) {
            if (n <= 1)
                return n;
            return compute(n - 1) + compute(n - 2);
        }
        
        public static void main(String[] args) {
            System.out.println(compute(7));
        }
    }

In order to activate ulyp the test should be executed with the following additional VM key: 

	-javaagent:C:\Work\ulyp\ulyp-agent\build\libs\ulyp-agent-0.2.jar -Dulyp.file=C:/Temp/test.dat

After the program exits, open the file `/tmp/fibonacci.dat` in the UI

![Ulyp UI](https://github.com/0xaa4eb/ulyp/blob/master/images/fibbonaci.png)

# Example (hibernate + h2 database)
Simple hibernate test recording:
 
    public class HibernateShowcase {
        ...
    
        public void save() throws Exception {
            User user = new User("Test", "User");
            saver.save(user);
        }
    }
  
The whole method traces tree may be investigated in the UI:

![Ulyp UI](https://github.com/0xaa4eb/ulyp/blob/master/images/hibernate.png)

# UI Controls

<table border="1">
<tr>
		<th>Hotkey</th>
		<th>Action</th>
</tr>
<tr><td>Hold Shift</td><td>Show full type names</td></tr>
<tr><td>Press =</td><td>Increase font size</td></tr>
<tr><td>Press -</td><td>Decrease font size</td></tr>
</table>