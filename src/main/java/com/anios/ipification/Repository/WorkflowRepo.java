package com.anios.ipification.Repository;

import com.anios.ipification.Entity.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkflowRepo extends JpaRepository<Workflow,Integer> {

}
