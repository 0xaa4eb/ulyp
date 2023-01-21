package com.agent.tests.libs.util.hibernate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    @Autowired
    private JpaProxyUserRepository repository;

    @Autowired
    private ApplicationContext applicationContext;

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return repository.all();
    }

    public void save(User user) {
        repository.save(user);
    }
}
