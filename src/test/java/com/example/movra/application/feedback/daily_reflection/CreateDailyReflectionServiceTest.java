package com.example.movra.application.feedback.daily_reflection;

import com.example.movra.bc.account.user.domain.user.vo.UserId;
import com.example.movra.bc.feedback.daily_reflection.application.exception.DailyReflectionAlreadyExistsException;
import com.example.movra.bc.feedback.daily_reflection.application.service.CreateDailyReflectionService;
import com.example.movra.bc.feedback.daily_reflection.application.service.dto.request.CreateDailyReflectionRequest;
import com.example.movra.bc.feedback.daily_reflection.domain.exception.InvalidDailyReflectionException;
import com.example.movra.bc.feedback.daily_reflection.domain.repository.DailyReflectionRepository;
import com.example.movra.sharedkernel.user.AuthenticatedUser;
import com.example.movra.sharedkernel.user.CurrentUserQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CreateDailyReflectionServiceTest {

    @InjectMocks
    private CreateDailyReflectionService createDailyReflectionService;

    @Mock
    private DailyReflectionRepository dailyReflectionRepository;

    @Mock
    private CurrentUserQuery currentUserQuery;

    private final UserId userId = UserId.newId();
    private final LocalDate reflectionDate = LocalDate.of(2026, 4, 10);

    private void givenCurrentUser() {
        lenient().when(currentUserQuery.currentUser()).thenReturn(
                AuthenticatedUser.builder().userId(userId).build()
        );
    }

    private DataIntegrityViolationException duplicateKeyViolation() {
        return new DataIntegrityViolationException(
                "duplicate",
                new SQLIntegrityConstraintViolationException("duplicate", "23000", 1062)
        );
    }

    private DataIntegrityViolationException otherIntegrityViolation() {
        return new DataIntegrityViolationException(
                "integrity",
                new SQLException("integrity", "23514", 23514)
        );
    }

    @Test
    @DisplayName("create succeeds")
    void create_success() {
        givenCurrentUser();
        CreateDailyReflectionRequest request = new CreateDailyReflectionRequest(
                reflectionDate,
                "Started one important task",
                "Lost focus in the afternoon",
                "If I lose focus in the afternoon",
                "Start the afternoon task in smaller chunks"
        );
        given(dailyReflectionRepository.existsByUserIdAndReflectionDate(userId, reflectionDate)).willReturn(false);

        createDailyReflectionService.create(request);

        then(dailyReflectionRepository).should().saveAndFlush(any());
    }

    @Test
    @DisplayName("create throws when reflection already exists")
    void create_alreadyExists_throwsException() {
        givenCurrentUser();
        CreateDailyReflectionRequest request = new CreateDailyReflectionRequest(
                reflectionDate,
                "One win",
                "One breakdown",
                "If condition",
                "Then action"
        );
        given(dailyReflectionRepository.existsByUserIdAndReflectionDate(userId, reflectionDate)).willReturn(true);

        assertThatThrownBy(() -> createDailyReflectionService.create(request))
                .isInstanceOf(DailyReflectionAlreadyExistsException.class);
    }

    @Test
    @DisplayName("create converts unique constraint violations to DailyReflectionAlreadyExistsException")
    void create_duplicateAtWrite_throwsException() {
        givenCurrentUser();
        CreateDailyReflectionRequest request = new CreateDailyReflectionRequest(
                reflectionDate,
                "One win",
                "One breakdown",
                "If condition",
                "Then action"
        );
        given(dailyReflectionRepository.existsByUserIdAndReflectionDate(userId, reflectionDate)).willReturn(false);
        given(dailyReflectionRepository.saveAndFlush(any()))
                .willThrow(duplicateKeyViolation());

        assertThatThrownBy(() -> createDailyReflectionService.create(request))
                .isInstanceOf(DailyReflectionAlreadyExistsException.class);
    }

    @Test
    @DisplayName("create rethrows non-duplicate integrity violations")
    void create_otherIntegrityViolation_rethrowsException() {
        givenCurrentUser();
        CreateDailyReflectionRequest request = new CreateDailyReflectionRequest(
                reflectionDate,
                "One win",
                "One breakdown",
                "If condition",
                "Then action"
        );
        given(dailyReflectionRepository.existsByUserIdAndReflectionDate(userId, reflectionDate)).willReturn(false);
        given(dailyReflectionRepository.saveAndFlush(any()))
                .willThrow(otherIntegrityViolation());

        assertThatThrownBy(() -> createDailyReflectionService.create(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("create throws when reflection content is invalid")
    void create_invalidContent_throwsException() {
        givenCurrentUser();
        CreateDailyReflectionRequest request = new CreateDailyReflectionRequest(
                reflectionDate,
                " ",
                "One breakdown",
                "If condition",
                "Then action"
        );
        given(dailyReflectionRepository.existsByUserIdAndReflectionDate(userId, reflectionDate)).willReturn(false);

        assertThatThrownBy(() -> createDailyReflectionService.create(request))
                .isInstanceOf(InvalidDailyReflectionException.class);
    }
}
