package com.perf.agent.benchmarks.showcase;

import com.perf.agent.benchmarks.impl.spring.ApplicationConfiguration;
import com.perf.agent.benchmarks.impl.spring.User;
import com.perf.agent.benchmarks.impl.spring.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class HibernateShowcase {

    private ApplicationContext context;
    private UserService saver;

    public void setUp() throws Exception {
        context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
        saver = context.getBean(UserService.class);
    }

    public void tearDown() throws Exception {
        int count = saver.findAll().size();
        if (count != 1) {
            throw new RuntimeException("Doesn't work, users found: " + count);
        }
    }

    public void save() throws Exception {
        User user = new User("Test", "User");
        saver.save(user);
    }

    public static void main(String[] args) throws Exception {
        HibernateShowcase benchmark = new HibernateShowcase();
        benchmark.setUp();
        benchmark.save();
        benchmark.tearDown();
    }
}
