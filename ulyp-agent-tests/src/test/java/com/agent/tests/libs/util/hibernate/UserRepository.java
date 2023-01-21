package com.agent.tests.libs.util.hibernate;

import java.util.List;

public interface UserRepository {

    User save(User user);

    List<User> all();
}
