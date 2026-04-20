package com.example.movra.bc.planning.daily_plan.application.service.daily_plan.support.helper;

import com.example.movra.bc.planning.daily_plan.domain.Task;
import com.example.movra.bc.planning.daily_plan.domain.repository.TaskRepository;
import com.example.movra.bc.planning.daily_plan.domain.vo.TaskId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskCompletionReader {

    private final TaskRepository taskRepository;

    public Map<TaskId, Boolean> findCompletionByTaskIds(Collection<TaskId> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Map.of();
        }

        List<Task> tasks = taskRepository.findAllByTaskIdIn(taskIds);

        Map<TaskId, Boolean> result = new HashMap<>();

        for (Task task : tasks) {
            result.put(task.getTaskId(), task.isCompleted());
        }

        return result;
    }
}
