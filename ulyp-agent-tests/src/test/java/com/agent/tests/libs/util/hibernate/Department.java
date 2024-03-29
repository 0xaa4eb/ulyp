package com.agent.tests.libs.util.hibernate;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Department {

    @Id
    @GeneratedValue
    private long id;
    @OneToMany(mappedBy = "department", fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
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
}
