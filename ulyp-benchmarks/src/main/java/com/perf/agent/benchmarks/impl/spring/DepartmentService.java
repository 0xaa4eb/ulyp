package com.perf.agent.benchmarks.impl.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Service
@Transactional
public class DepartmentService {

    @Autowired
    public DepartmentJpaRepository repository;

    public void save(Department department) {
        repository.save(department);
    }

    public void shufflePeople() {
        List<Department> departments = repository.findAll();
        LinkedList<Person> people = new LinkedList<>();

        for (Department department : departments) {
            Person person = department.removePerson();
            people.add(person);
        }

        Collections.shuffle(people);

        int peoplePerDep = people.size() / departments.size();

        for (Department department : departments) {
            for (int i = 0; i < peoplePerDep; i++) {
                department.addPerson(people.removeLast());
            }
        }
    }

    public int countPeople() {
        int count = 0;
        for (Department department : repository.findAll()) {
            count += department.getPeople().size();
        }
        return count;
    }

    public void removeAll() {
        repository.deleteAll();
    }
}
