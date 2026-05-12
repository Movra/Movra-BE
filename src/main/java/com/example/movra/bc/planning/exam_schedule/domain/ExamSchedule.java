package com.example.movra.bc.planning.exam_schedule.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.planning.exam_schedule.domain.exception.InvalidExamScheduleException;
import com.example.movra.bc.planning.exam_schedule.domain.type.ExamType;
import com.example.movra.bc.planning.exam_schedule.domain.vo.ExamScheduleId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(
        name = "tbl_exam_schedule",
        indexes = @Index(name = "idx_exam_schedule_user_date", columnList = "user_id, exam_date")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExamSchedule extends AbstractAggregateRoot {

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int SUBJECT_MAX_LENGTH = 50;

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "exam_schedule_id"))
    private ExamScheduleId examScheduleId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 32)
    private ExamType examType;

    @Column(name = "title", nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "subject", length = SUBJECT_MAX_LENGTH)
    private String subject;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static ExamSchedule create(
            UserId userId,
            ExamType examType,
            String title,
            LocalDate examDate,
            String subject,
            Clock clock
    ) {
        validate(userId, examType, title, examDate, subject);
        if (clock == null) {
            throw new InvalidExamScheduleException();
        }

        return ExamSchedule.builder()
                .examScheduleId(ExamScheduleId.newId())
                .userId(userId)
                .examType(examType)
                .title(title.trim())
                .examDate(examDate)
                .subject(normalizeSubject(subject))
                .createdAt(clock.instant())
                .build();
    }

    public void update(ExamType examType, String title, LocalDate examDate, String subject) {
        validateFields(examType, title, examDate, subject);

        this.examType = examType;
        this.title = title.trim();
        this.examDate = examDate;
        this.subject = normalizeSubject(subject);
    }

    private static void validate(
            UserId userId,
            ExamType examType,
            String title,
            LocalDate examDate,
            String subject
    ) {
        if (userId == null) {
            throw new InvalidExamScheduleException();
        }

        validateFields(examType, title, examDate, subject);
    }

    private static void validateFields(ExamType examType, String title, LocalDate examDate, String subject) {
        if (examType == null || title == null || title.isBlank() || examDate == null) {
            throw new InvalidExamScheduleException();
        }

        if (title.trim().length() > TITLE_MAX_LENGTH) {
            throw new InvalidExamScheduleException();
        }

        if (subject != null && subject.trim().length() > SUBJECT_MAX_LENGTH) {
            throw new InvalidExamScheduleException();
        }
    }

    private static String normalizeSubject(String subject) {
        if (subject == null || subject.isBlank()) {
            return null;
        }

        return subject.trim();
    }
}
