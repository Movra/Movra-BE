package com.example.movra.bc.feedback.daily_reflection.domain;

import com.example.movra.bc.account.domain.user.vo.UserId;
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

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "daily_reflection_id"))
    private DailyReflectionId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Column(name = "reflection_date", nullable = false)
    private LocalDate reflectionDate;

    @Column(name = "what_went_well", nullable = false, length = 500)
    private String whatWentWell;

    @Column(name = "what_broke_down", nullable = false, length = 1000)
    private String whatBrokeDown;

    @Column(name = "next_action", nullable = false, length = 500)
    private String nextAction;

    public static DailyReflection create(
            UserId userId,
            LocalDate reflectionDate,
            String whatWentWell,
            String whatBrokeDown,
            String nextAction
    ) {
        return DailyReflection.builder()
                .id(DailyReflectionId.newId())
                .userId(userId)
                .reflectionDate(reflectionDate)
                .whatWentWell(whatWentWell)
                .whatBrokeDown(whatBrokeDown)
                .nextAction(nextAction)
                .build();
    }

    public void update(String whatWentWell, String whatBrokeDown, String nextAction) {
        this.whatWentWell = whatWentWell;
        this.whatBrokeDown = whatBrokeDown;
        this.nextAction = nextAction;
    }
}
