package com.agent.tests.libs.util.hibernate;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Person {

    @Id
    @GeneratedValue
    private Long id;
    @Basic
    private String firstName;
    @Basic
    private String lastName;
    @Basic
    private String phoneNumber;
    @Basic
    private int age;
    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;

    public Person() {
    }

    public Long getId() {
        return id;
    }

    public Person setId(Long id) {
        this.id = id;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public Person setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public Person setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Person setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public int getAge() {
        return age;
    }

    public Person setAge(int age) {
        this.age = age;
        return this;
    }

    public Department getDepartment() {
        return department;
    }

    public Person setDepartment(Department department) {
        this.department = department;
        return this;
    }
}
