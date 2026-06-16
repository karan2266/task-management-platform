package com.example.task.enums;

import java.util.Set;

public enum TaskStatus {

    TODO,
    IN_PROGRESS,
    DONE;

    /**
     * Returns the set of states this status is allowed to transition into.
     * Used by TaskService to enforce the state machine.
     */
    public Set<TaskStatus> getAllowedTransitions() {
        return switch (this) {
            case TODO        -> Set.of(IN_PROGRESS);
            case IN_PROGRESS -> Set.of(DONE);
            case DONE        -> Set.of(IN_PROGRESS);   // re-open
        };
    }

    public boolean canTransitionTo(TaskStatus next) {
        return getAllowedTransitions().contains(next);
    }
}

