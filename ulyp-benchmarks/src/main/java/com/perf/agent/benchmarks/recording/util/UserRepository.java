package com.perf.agent.benchmarks.recording.util;

import java.util.List;

public interface UserRepository {

    User save(User user);

    List<User> all();
}
