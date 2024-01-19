package com.perf.agent.benchmarks.util;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.Map;

public class HazelcastSample {

    public static void main(String[] args) {
        Config helloWorldConfig = new Config();
        helloWorldConfig.setClusterName("bench-cluster");

        HazelcastInstance instance1 = Hazelcast.newHazelcastInstance(helloWorldConfig);
        HazelcastInstance instance2 = Hazelcast.newHazelcastInstance(helloWorldConfig);

        Map<String, String> map1 = instance1.getMap("my-distributed-map");
        Map<String, String> map2 = instance2.getMap("my-distributed-map");

        for (int i = 0; i < 1000; i++) {
            map1.put(String.valueOf(i), "Value" + i);
        }

        instance1.shutdown();
        instance2.shutdown();
    }
}
