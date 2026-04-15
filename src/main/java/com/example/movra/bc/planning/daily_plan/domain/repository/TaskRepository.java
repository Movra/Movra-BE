package com.example.movra.bc.planning.daily_plan.domain.repository;

import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, TaskId> {

    List<Task> findAllByTaskIdIn(Collection<TaskId> taskIds);
}
