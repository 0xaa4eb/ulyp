package com.perf.agent.benchmarks.impl.spring;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentJpaRepository extends JpaRepository<Department, Long> {

}
