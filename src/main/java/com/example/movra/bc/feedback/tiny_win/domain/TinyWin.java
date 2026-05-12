package com.example.movra.bc.feedback.tiny_win.domain;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.feedback.tiny_win.domain.exception.InvalidTinyWinException;
import com.example.movra.bc.feedback.tiny_win.domain.vo.TinyWinId;
import com.example.movra.sharedkernel.domain.AbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_tiny_win")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TinyWin extends AbstractAggregateRoot {

    private static final int TITLE_MAX_LENGTH = 30;
    private static final int CONTENT_MAX_LENGTH = 3000;

    @EmbeddedId
    @AttributeOverride(name = "id", column = @Column(name = "tiny_win_id"))
    private TinyWinId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "user_id"))
    private UserId userId;

    @Column(length = TITLE_MAX_LENGTH, nullable = false)
    private String title;

    @Column(length = CONTENT_MAX_LENGTH, nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDate localDate;

    public static TinyWin create(UserId userId, String title, String content){
        validate(userId, title, content);

        return TinyWin.builder()
                .id(TinyWinId.newId())
                .userId(userId)
                .title(title)
                .content(content)
                .localDate(LocalDate.now())
                .build();
    }

    public void updateTitle(String title){
        validateText(title, TITLE_MAX_LENGTH);
        this.title = title;
    }

    public void updateContent(String content){
        validateText(content, CONTENT_MAX_LENGTH);
        this.content = content;
    }

    private static void validate(UserId userId, String title, String content) {
        if (userId == null) {
            throw new InvalidTinyWinException();
        }

        validateText(title, TITLE_MAX_LENGTH);
        validateText(content, CONTENT_MAX_LENGTH);
    }

    private static void validateText(String value, int maxLength) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new InvalidTinyWinException();
        }
    }
}
