package com.example.morva.domain.calendar.calendar.repository;

import com.example.morva.domain.calendar.calendar.TodoCalendar;
import com.example.morva.domain.calendar.calendar.vo.TodoCalendarId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoCalendarRepository extends JpaRepository<TodoCalendar, TodoCalendarId> {
}
