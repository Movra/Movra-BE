package com.example.movra.bc.visioning.future_vision.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.domain.exception.InvalidFutureVisionException;
import com.example.movra.bc.visioning.future_vision.domain.vo.FutureVisionId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_future_vision")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FutureVision extends AbstractAggregateRoot {

    private static final int YEARLY_VISION_DESCRIPTION_MAX_LENGTH = 100;

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "future_vision_id"))
    private FutureVisionId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id", unique = true))
    private UserId userId;

    @Column(nullable = false)
    private String weeklyVisionImageUrl;

    @Column(nullable = false)
    private String yearlyVisionImageUrl;

    @Column(length = YEARLY_VISION_DESCRIPTION_MAX_LENGTH, nullable = false)
    private String yearlyVisionDescription;

    @Column(nullable = false)
    private LocalDate yearlyVisionCreatedAt;

    public static FutureVision create(UserId userId, String weeklyVisionImageUrl, String yearlyVisionImageUrl, String yearlyVisionDescription){
        validate(userId, weeklyVisionImageUrl, yearlyVisionImageUrl, yearlyVisionDescription);

        return FutureVision.builder()
                .id(FutureVisionId.newId())
                .userId(userId)
                .weeklyVisionImageUrl(weeklyVisionImageUrl)
                .yearlyVisionImageUrl(yearlyVisionImageUrl)
                .yearlyVisionDescription(yearlyVisionDescription)
                .yearlyVisionCreatedAt(LocalDate.now())
                .build();
    }

    public void updateWeeklyVision(String weeklyVisionImageUrl){
        validateText(weeklyVisionImageUrl, Integer.MAX_VALUE);
        this.weeklyVisionImageUrl = weeklyVisionImageUrl;
    }

    public void updateYearlyVision(String yearlyVisionImageUrl, String yearlyVisionDescription){
        validateText(yearlyVisionImageUrl, Integer.MAX_VALUE);
        validateText(yearlyVisionDescription, YEARLY_VISION_DESCRIPTION_MAX_LENGTH);
        this.yearlyVisionImageUrl = yearlyVisionImageUrl;
        this.yearlyVisionDescription = yearlyVisionDescription;
        this.yearlyVisionCreatedAt = LocalDate.now();
    }

    private static void validate(
            UserId userId,
            String weeklyVisionImageUrl,
            String yearlyVisionImageUrl,
            String yearlyVisionDescription
    ) {
        if (userId == null) {
            throw new InvalidFutureVisionException();
        }

        validateText(weeklyVisionImageUrl, Integer.MAX_VALUE);
        validateText(yearlyVisionImageUrl, Integer.MAX_VALUE);
        validateText(yearlyVisionDescription, YEARLY_VISION_DESCRIPTION_MAX_LENGTH);
    }

    private static void validateText(String value, int maxLength) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new InvalidFutureVisionException();
        }
    }
}
