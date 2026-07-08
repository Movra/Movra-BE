
SET @user_id_str = 'b64367a1-570a-4cdf-ac40-14836f6cf2b3';
SET @uid = UUID_TO_BIN(@user_id_str);

SET SQL_SAFE_UPDATES = 0;

-- ---------------------------------------------------------------
-- 0. 정리 (멱등 — 재실행 안전)
-- ---------------------------------------------------------------
-- analytics_event 는 자식 테이블(property)이 FK로 참조하므로 자식부터 삭제
DELETE p FROM tbl_analytics_event_property p
    JOIN tbl_analytics_event e ON p.analytics_event_id = e.analytics_event_id
WHERE e.user_id = @uid AND e.occurred_at >= (UTC_TIMESTAMP() - INTERVAL 40 DAY);

DELETE FROM tbl_analytics_event
WHERE user_id = @uid AND occurred_at >= (UTC_TIMESTAMP() - INTERVAL 40 DAY);

DELETE FROM tbl_focus_session
WHERE user_id = @uid AND started_at >= (UTC_TIMESTAMP() - INTERVAL 40 DAY);

DELETE FROM tbl_daily_reflection
WHERE user_id = @uid AND reflection_date >= (CURDATE() - INTERVAL 40 DAY);

DELETE FROM tbl_tiny_win
WHERE user_id = @uid AND local_date >= (CURDATE() - INTERVAL 40 DAY);

-- ---------------------------------------------------------------
-- 1. behavior_profile (선언된 선호 — 괴리 유발용)
--    선언: 집중 09-11시 / 난이도 HIGH / 회복 QUICK_RESTART
-- ---------------------------------------------------------------
INSERT INTO tbl_behavior_profile
    (behavior_profile_id, user_id, execution_difficulty, social_preference, recovery_style,
     exam_track, preferred_focus_start_hour, preferred_focus_end_hour, coaching_mode)
VALUES
    (UUID_TO_BIN(UUID()), @uid, 'HIGH', 'MEDIUM', 'QUICK_RESTART',
     'MOPYUNG_SUNUNG', 9, 11, 'GENTLE')
ON DUPLICATE KEY UPDATE
    execution_difficulty = 'HIGH',
    social_preference = 'MEDIUM',
    recovery_style = 'QUICK_RESTART',
    exam_track = 'MOPYUNG_SUNUNG',
    preferred_focus_start_hour = 9,
    preferred_focus_end_hour = 11,
    coaching_mode = 'GENTLE';

-- ---------------------------------------------------------------
-- 2. focus_session (완료 세션 — 야간 20:00-21:30 KST = 11:00-12:30 UTC)
--    과거(18~28일 전)에 집중 → 피크 시간대 20시 / 총 집중시간 산정
-- ---------------------------------------------------------------
INSERT INTO tbl_focus_session (focus_session_id, user_id, started_at, ended_at, duration_seconds, preset_minutes)
VALUES
 (UUID_TO_BIN(UUID()), @uid, (UTC_DATE() - INTERVAL 28 DAY) + INTERVAL 660 MINUTE, (UTC_DATE() - INTERVAL 28 DAY) + INTERVAL 750 MINUTE, 5400, 25),
 (UUID_TO_BIN(UUID()), @uid, (UTC_DATE() - INTERVAL 26 DAY) + INTERVAL 660 MINUTE, (UTC_DATE() - INTERVAL 26 DAY) + INTERVAL 750 MINUTE, 5400, 25),
 (UUID_TO_BIN(UUID()), @uid, (UTC_DATE() - INTERVAL 24 DAY) + INTERVAL 660 MINUTE, (UTC_DATE() - INTERVAL 24 DAY) + INTERVAL 750 MINUTE, 5400, 25),
 (UUID_TO_BIN(UUID()), @uid, (UTC_DATE() - INTERVAL 22 DAY) + INTERVAL 660 MINUTE, (UTC_DATE() - INTERVAL 22 DAY) + INTERVAL 750 MINUTE, 5400, 25),
 (UUID_TO_BIN(UUID()), @uid, (UTC_DATE() - INTERVAL 20 DAY) + INTERVAL 660 MINUTE, (UTC_DATE() - INTERVAL 20 DAY) + INTERVAL 750 MINUTE, 5400, 25),
 (UUID_TO_BIN(UUID()), @uid, (UTC_DATE() - INTERVAL 18 DAY) + INTERVAL 660 MINUTE, (UTC_DATE() - INTERVAL 18 DAY) + INTERVAL 750 MINUTE, 5400, 25);

-- ---------------------------------------------------------------
-- 3. analytics_event (지표 백본)
--    완료 6건(과거 18~28일) / 중단·자동마감 12건(최근 3~14일) → 완료율 33%, 재개율 0%
--    top pick 8건
-- ---------------------------------------------------------------
INSERT INTO tbl_analytics_event (analytics_event_id, user_id, event_type, occurred_at)
VALUES
 -- 완료 6건 (과거)
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_COMPLETED', (UTC_DATE() - INTERVAL 28 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_COMPLETED', (UTC_DATE() - INTERVAL 26 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_COMPLETED', (UTC_DATE() - INTERVAL 24 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_COMPLETED', (UTC_DATE() - INTERVAL 22 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_COMPLETED', (UTC_DATE() - INTERVAL 20 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_COMPLETED', (UTC_DATE() - INTERVAL 18 DAY) + INTERVAL 720 MINUTE),
 -- 중단 9건 (최근)
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_ABANDONED', (UTC_DATE() - INTERVAL 14 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_ABANDONED', (UTC_DATE() - INTERVAL 13 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_ABANDONED', (UTC_DATE() - INTERVAL 12 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_ABANDONED', (UTC_DATE() - INTERVAL 11 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_ABANDONED', (UTC_DATE() - INTERVAL 10 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_ABANDONED', (UTC_DATE() - INTERVAL  9 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_ABANDONED', (UTC_DATE() - INTERVAL  8 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_ABANDONED', (UTC_DATE() - INTERVAL  7 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_ABANDONED', (UTC_DATE() - INTERVAL  6 DAY) + INTERVAL 720 MINUTE),
 -- 자동마감 3건 (최근)
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_AUTO_CLOSED', (UTC_DATE() - INTERVAL 5 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_AUTO_CLOSED', (UTC_DATE() - INTERVAL 4 DAY) + INTERVAL 720 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'FOCUS_SESSION_AUTO_CLOSED', (UTC_DATE() - INTERVAL 3 DAY) + INTERVAL 720 MINUTE),
 -- top pick 8건
 (UUID_TO_BIN(UUID()), @uid, 'TOP_PICK_SELECTED', (UTC_DATE() - INTERVAL 28 DAY) + INTERVAL 540 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'TOP_PICK_SELECTED', (UTC_DATE() - INTERVAL 24 DAY) + INTERVAL 540 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'TOP_PICK_SELECTED', (UTC_DATE() - INTERVAL 20 DAY) + INTERVAL 540 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'TOP_PICK_SELECTED', (UTC_DATE() - INTERVAL 14 DAY) + INTERVAL 540 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'TOP_PICK_SELECTED', (UTC_DATE() - INTERVAL 10 DAY) + INTERVAL 540 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'TOP_PICK_SELECTED', (UTC_DATE() - INTERVAL  6 DAY) + INTERVAL 540 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'TOP_PICK_SELECTED', (UTC_DATE() - INTERVAL  4 DAY) + INTERVAL 540 MINUTE),
 (UUID_TO_BIN(UUID()), @uid, 'TOP_PICK_SELECTED', (UTC_DATE() - INTERVAL  2 DAY) + INTERVAL 540 MINUTE);

-- ---------------------------------------------------------------
-- 4. daily_reflection (정성 데이터 — LLM 맥락 해석용)
-- ---------------------------------------------------------------
INSERT INTO tbl_daily_reflection
    (daily_reflection_id, user_id, reflection_date, what_went_well, what_broke_down)
VALUES
 (UUID_TO_BIN(UUID()), @uid, (CURDATE() - INTERVAL 14 DAY), '밤에 집중이 잘 됐다. 수학 모의고사 오답 정리를 끝냈다.', '아침에는 계획만 세우고 실제로 앉지를 못했다.'),
 (UUID_TO_BIN(UUID()), @uid, (CURDATE() - INTERVAL 11 DAY), '저녁 9시 이후 두 시간 몰입했다.', '오전 타임은 또 미뤘다. 알림이 와도 무시했다.'),
 (UUID_TO_BIN(UUID()), @uid, (CURDATE() - INTERVAL  8 DAY), '국어 지문 분석 루틴이 자리잡는 느낌.', '한 번 흐름이 끊기니 그날은 다시 시작을 못 했다.'),
 (UUID_TO_BIN(UUID()), @uid, (CURDATE() - INTERVAL  5 DAY), '짧게라도 매일 앉으려 노력했다.', '세션을 중간에 자주 포기했다. 회복이 더디다.'),
 (UUID_TO_BIN(UUID()), @uid, (CURDATE() - INTERVAL  2 DAY), '야간 집중 패턴은 확실히 내게 맞는 듯.', '목표를 너무 높게 잡아 부담이 컸다.');

-- ---------------------------------------------------------------
-- 5. tiny_win (작은 성취)
-- ---------------------------------------------------------------
INSERT INTO tbl_tiny_win (tiny_win_id, user_id, title, content, local_date)
VALUES
 (UUID_TO_BIN(UUID()), @uid, '오답노트 완성', '수학 오답 12문제 정리 완료', (CURDATE() - INTERVAL 12 DAY)),
 (UUID_TO_BIN(UUID()), @uid, '2시간 몰입', '야간에 끊김 없이 집중', (CURDATE() - INTERVAL 9 DAY)),
 (UUID_TO_BIN(UUID()), @uid, '국어 지문 5개', '비문학 지문 5개 분석', (CURDATE() - INTERVAL 6 DAY)),
 (UUID_TO_BIN(UUID()), @uid, '매일 착석', '일주일 연속 책상 앉기 성공', (CURDATE() - INTERVAL 3 DAY));

SET SQL_SAFE_UPDATES = 1;

-- ---------------------------------------------------------------
-- 6. 확인
-- ---------------------------------------------------------------
SELECT '=== analytics_event 분포 ===' AS '';
SELECT event_type, COUNT(*) AS cnt
FROM tbl_analytics_event
WHERE user_id = @uid AND occurred_at >= (UTC_TIMESTAMP() - INTERVAL 30 DAY)
GROUP BY event_type ORDER BY event_type;

SELECT '=== focus_session(완료) ===' AS '';
SELECT COUNT(*) AS sessions, SUM(duration_seconds) AS total_seconds
FROM tbl_focus_session
WHERE user_id = @uid AND started_at >= (UTC_TIMESTAMP() - INTERVAL 30 DAY);

SELECT '=== reflection / tiny_win ===' AS '';
SELECT
  (SELECT COUNT(*) FROM tbl_daily_reflection WHERE user_id = @uid AND reflection_date >= (CURDATE() - INTERVAL 30 DAY)) AS reflections,
  (SELECT COUNT(*) FROM tbl_tiny_win WHERE user_id = @uid AND local_date >= (CURDATE() - INTERVAL 30 DAY)) AS tiny_wins;
