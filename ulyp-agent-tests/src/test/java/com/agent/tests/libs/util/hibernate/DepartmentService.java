package com.agent.tests.libs.util.hibernate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DepartmentService {

    @Autowired
    public DepartmentJpaRepository repository;

    public void save(Department department) {
        repository.save(department);
    }

    public List<Department> findAll() {
        return repository.findAll();
    }
}
