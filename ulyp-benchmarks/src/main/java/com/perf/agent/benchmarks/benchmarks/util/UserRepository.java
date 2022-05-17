package com.perf.agent.benchmarks.benchmarks.util;

import java.util.List;

public interface UserRepository {

	public User save(User user);

	public List<User> all();
}
