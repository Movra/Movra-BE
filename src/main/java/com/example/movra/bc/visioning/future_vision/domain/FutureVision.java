package com.example.movra.bc.visioning.future_vision.domain;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.visioning.future_vision.domain.vo.FutureVisionId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_future_vision")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FutureVision {

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

    @Column(length = 100, nullable = false)
    private String yearlyVisionDescription;

    @Column(nullable = false)
    private LocalDate yearlyVisionCreatedAt;

    public static FutureVision create(UserId userId, String weeklyVisionImageUrl, String yearlyVisionImageUrl, String yearlyVisionDescription){
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
        this.weeklyVisionImageUrl = weeklyVisionImageUrl;
    }

    public void updateYearlyVision(String yearlyVisionImageUrl, String yearlyVisionDescription){
        this.yearlyVisionImageUrl = yearlyVisionImageUrl;
        this.yearlyVisionDescription = yearlyVisionDescription;
        this.yearlyVisionCreatedAt = LocalDate.now();
    }
}
