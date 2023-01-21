package com.agent.tests.libs.util.hibernate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
