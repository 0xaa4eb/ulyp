package com.perf.agent.benchmarks.showcase;

import com.perf.agent.benchmarks.benchmarks.util.ApplicationConfiguration;
import com.perf.agent.benchmarks.benchmarks.util.User;
import com.perf.agent.benchmarks.benchmarks.util.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class HibernateShowcase {

    private UserService saver;

    public static void main(String[] args) {
        HibernateShowcase benchmark = new HibernateShowcase();
        benchmark.setUp();
        benchmark.save();
        benchmark.tearDown();
    }

    public void setUp() {
        ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
        saver = context.getBean(UserService.class);
    }

    public void tearDown() {
        int count = saver.findAll().size();
        if (count != 1) {
            throw new RuntimeException("Doesn't work, users found: " + count);
        }
    }

    public void save() {
        User user = new User("Test", "User");
        saver.save(user);
    }
}
