package com.example.movra.bc.feedback.tiny_win.domain;

import com.example.movra.bc.account.domain.user.vo.UserId;
import com.example.movra.bc.feedback.tiny_win.domain.vo.TinyWinId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_tiny_win")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TinyWin {

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "tiny_win_id"))
    private TinyWinId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id"))
    private UserId userId;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(length = 3000, nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDate localDate;

    public static TinyWin create(UserId userId, String title, String content){
        return TinyWin.builder()
                .id(TinyWinId.newId())
                .userId(userId)
                .title(title)
                .content(content)
                .localDate(LocalDate.now())
                .build();
    }

    public void updateTitle(String title){
        this.title = title;
    }

    public void updateContent(String content){
        this.content = content;
    }
}