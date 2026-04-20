package com.example.movra.sharedkernel.persistence;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RequiresNewInsertExecutor {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(Runnable action) {
        action.run();
    }
}
