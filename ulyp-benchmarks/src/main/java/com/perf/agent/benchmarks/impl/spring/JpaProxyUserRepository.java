package com.perf.agent.benchmarks.impl.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class JpaProxyUserRepository implements UserRepository {

	@Autowired
	private UserJpaRepository jpaRepository;

	public User save(User newUser) {
		UserJpaEntity newEntity = new UserJpaEntity(newUser);
		UserJpaEntity savedEntity = jpaRepository.save(newEntity);
		User savedUser = savedEntity.toUser();
		return savedUser;
	}

	public List<User> all() {
		List<UserJpaEntity> entities = jpaRepository.findAll();
		List<User> users = new ArrayList<User>(entities.size());
		for (UserJpaEntity entity : entities) {
			User user = entity.toUser();
			users.add(user);
		}
		return users;
	}


}
