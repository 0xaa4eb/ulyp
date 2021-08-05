package com.perf.agent.benchmarks.impl.spring;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Department {

    @Id
    @GeneratedValue
    private long id;
    @OneToMany(mappedBy = "department", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<Person> people = new HashSet<>();

    public Set<Person> getPeople() {
        return people;
    }

    public Department setPeople(Set<Person> people) {
        this.people = people;
        return this;
    }

    public long getId() {
        return id;
    }

    public Department setId(long id) {
        this.id = id;
        return this;
    }

    public Person removePerson() {
        Person person = people.iterator().next();
        people.remove(person);
        person.setDepartment(null);
        return person;
    }

    public void addPerson(Person person) {
        people.add(person);
        person.setDepartment(this);
    }
}
