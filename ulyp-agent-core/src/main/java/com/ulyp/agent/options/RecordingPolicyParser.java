package com.ulyp.agent.options;

import com.ulyp.agent.policy.*;

import java.time.Duration;

public class RecordingPolicyParser implements Parser<OverridableRecordingPolicy> {

    @Override
    public OverridableRecordingPolicy parse(String textValue) {
        StartRecordingPolicy policy;
        if (textValue == null || textValue.isEmpty()) {
            policy = new EnabledRecordingPolicy();
        } else if (textValue.startsWith("delay:")) {
            policy = new DelayBasedRecordingPolicy(Duration.ofSeconds(Integer.parseInt(textValue.replace("delay:", ""))));
        } else if (textValue.equals("api")) {
            policy = new DisabledRecordingPolicy();
        } else {
            throw new IllegalArgumentException("Unsupported recording policy: " + textValue);
        }
        return new OverridableRecordingPolicy(policy);
    }
}
