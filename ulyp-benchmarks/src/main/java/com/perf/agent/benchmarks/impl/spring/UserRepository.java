package com.perf.agent.benchmarks.impl.spring;

import java.util.List;

public interface UserRepository {

	public User save(User user);

	public List<User> all();
}
