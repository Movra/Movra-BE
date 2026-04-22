package com.example.movra.bc.feedback.daily_reflection.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.feedback.daily_reflection.domain.exception.InvalidDailyReflectionException;
import com.example.movra.bc.feedback.daily_reflection.domain.vo.DailyReflectionId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_daily_reflection", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "reflection_date"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyReflection {

    private static final int WHAT_WENT_WELL_MAX_LENGTH = 500;
    private static final int WHAT_BROKE_DOWN_MAX_LENGTH = 1000;
    private static final int IF_CONDITION_MAX_LENGTH = 500;
    private static final int THEN_ACTION_MAX_LENGTH = 500;

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "daily_reflection_id"))
    private DailyReflectionId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(name = "reflection_date", nullable = false)
    private LocalDate reflectionDate;

    @Column(name = "what_went_well", nullable = false, length = WHAT_WENT_WELL_MAX_LENGTH)
    private String whatWentWell;

    @Column(name = "what_broke_down", nullable = false, length = WHAT_BROKE_DOWN_MAX_LENGTH)
    private String whatBrokeDown;

    // Keep these nullable during the transition from next_action to if/then fields.
    @Column(name = "if_condition", length = IF_CONDITION_MAX_LENGTH)
    private String ifCondition;

    @Column(name = "then_action", length = THEN_ACTION_MAX_LENGTH)
    private String thenAction;

    @Column(name = "next_action", length = THEN_ACTION_MAX_LENGTH)
    private String legacyNextAction;

    public static DailyReflection create(
            UserId userId,
            LocalDate reflectionDate,
            String whatWentWell,
            String whatBrokeDown,
            String ifCondition,
            String thenAction
    ) {
        validate(userId, reflectionDate, whatWentWell, whatBrokeDown, ifCondition, thenAction);

        return DailyReflection.builder()
                .id(DailyReflectionId.newId())
                .userId(userId)
                .reflectionDate(reflectionDate)
                .whatWentWell(whatWentWell)
                .whatBrokeDown(whatBrokeDown)
                .ifCondition(ifCondition)
                .thenAction(thenAction)
                .legacyNextAction(thenAction)
                .build();
    }

    public void update(String whatWentWell, String whatBrokeDown, String ifCondition, String thenAction) {
        validateText(whatWentWell, WHAT_WENT_WELL_MAX_LENGTH);
        validateText(whatBrokeDown, WHAT_BROKE_DOWN_MAX_LENGTH);
        validateText(ifCondition, IF_CONDITION_MAX_LENGTH);
        validateText(thenAction, THEN_ACTION_MAX_LENGTH);

        this.whatWentWell = whatWentWell;
        this.whatBrokeDown = whatBrokeDown;
        this.ifCondition = ifCondition;
        this.thenAction = thenAction;
        this.legacyNextAction = thenAction;
    }

    public String getIfCondition() {
        return ifCondition != null ? ifCondition : "";
    }

    public String getThenAction() {
        if (thenAction != null) {
            return thenAction;
        }

        return legacyNextAction != null ? legacyNextAction : "";
    }

    private static void validate(
            UserId userId,
            LocalDate reflectionDate,
            String whatWentWell,
            String whatBrokeDown,
            String ifCondition,
            String thenAction
    ) {
        if (userId == null || reflectionDate == null) {
            throw new InvalidDailyReflectionException();
        }

        validateText(whatWentWell, WHAT_WENT_WELL_MAX_LENGTH);
        validateText(whatBrokeDown, WHAT_BROKE_DOWN_MAX_LENGTH);
        validateText(ifCondition, IF_CONDITION_MAX_LENGTH);
        validateText(thenAction, THEN_ACTION_MAX_LENGTH);
    }

    private static void validateText(String value, int maxLength) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new InvalidDailyReflectionException();
        }
    }
}
