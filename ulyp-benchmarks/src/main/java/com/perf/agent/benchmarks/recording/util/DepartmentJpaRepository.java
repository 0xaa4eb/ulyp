package com.perf.agent.benchmarks.recording.util;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentJpaRepository extends JpaRepository<Department, Long> {

}