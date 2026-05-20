-- ================================================================
-- Movra 부하테스트 시드 데이터 — 집중 통계 N+1 쿼리 재현용
-- ================================================================
-- 목적:
--   monthly stats(2026년 4월)에서 rawPeriods를 최대한 발생시켜
--   N+1 Before 수치와 범위쿼리 1회 After 수치를 비교 측정한다.
--
-- 데이터 패턴 (Asia/Seoul 기준):
--   짝수일 (2,4,6,8,10,12,14): focus_session + daily_focus_summary
--     → SUMMARY 경로, rawPeriod 없음
--   홀수일 (3,5,7,9,11,13,15): focus_session만 (summary 없음)
--     → RAW 경로, rawPeriod 7개 발생 (짝수 summary가 사이에 껴서 각 1일씩)
--   1일, 16-30일: 데이터 없음
--
-- Before (N+1): rawPeriods 7개 × findSessions 개별호출 + findSummaryRange 1회 = DB 8회
-- After  (개선): findSessionsInRange 1회 + findSummaryRange 1회 = DB 2회
--
-- 실행 전 준비:
--   1. 아래 @user_id_str 을 본인 UUID로 교체
--      SELECT BIN_TO_UUID(id) FROM tbl_users LIMIT 5;
--   2. MySQL 8.0+ 에서 실행 (UUID_TO_BIN 지원)
--
-- 실행:
--   mysql -u {USER} -p {DB_NAME} < scripts/seed-focus-stats.sql
-- ================================================================

-- ① 본인 user UUID 입력 (tbl_users.id 조회 후 교체)
SET @user_id_str = 'xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx';

-- safe update 모드 해제 (JOIN DELETE / PK 없는 DELETE 허용)
SET SQL_SAFE_UPDATES = 0;

-- ================================================================
-- 기존 시드 데이터 정리 (멱등성 — 재실행 안전)
-- ================================================================
DELETE dsi
FROM tbl_daily_focus_summary_item dsi
         JOIN tbl_daily_focus_summary ds
              ON dsi.daily_focus_summary_id = ds.daily_focus_summary_id
WHERE ds.user_id = UUID_TO_BIN(@user_id_str)
  AND ds.summary_date BETWEEN '2026-04-01' AND '2026-04-30';

DELETE
FROM tbl_daily_focus_summary
WHERE user_id = UUID_TO_BIN(@user_id_str)
  AND summary_date BETWEEN '2026-04-01' AND '2026-04-30';

-- UTC 기준: 2026-04-01 00:00 KST = 2026-03-31 15:00 UTC
-- UTC 기준: 2026-04-30 24:00 KST = 2026-04-30 15:00 UTC
DELETE
FROM tbl_focus_session
WHERE user_id = UUID_TO_BIN(@user_id_str)
  AND started_at < '2026-04-30 15:00:00'
  AND (ended_at IS NULL OR ended_at > '2026-03-31 15:00:00');

-- ================================================================
-- focus_session UUID 변수 선언
-- (홀수일 = 세션만, 짝수일 = 세션 + summary)
-- ================================================================
-- 세션 UUID: s_{월일}_{순서}
-- 오전 세션: 10:00-11:30 KST = 01:00-02:30 UTC (90분, 5400초)
-- 오후 세션: 14:00-15:00 KST = 05:00-06:00 UTC (60분, 3600초, 짝수일만)

SET @s_0402_1 = UUID(); SET @s_0402_2 = UUID(); -- Apr 2 (짝수, summary O)
SET @s_0403_1 = UUID();                           -- Apr 3 (홀수, summary X)
SET @s_0404_1 = UUID(); SET @s_0404_2 = UUID(); -- Apr 4 (짝수, summary O)
SET @s_0405_1 = UUID();                           -- Apr 5 (홀수, summary X)
SET @s_0406_1 = UUID(); SET @s_0406_2 = UUID(); -- Apr 6 (짝수, summary O)
SET @s_0407_1 = UUID();                           -- Apr 7 (홀수, summary X)
SET @s_0408_1 = UUID(); SET @s_0408_2 = UUID(); -- Apr 8 (짝수, summary O)
SET @s_0409_1 = UUID();                           -- Apr 9 (홀수, summary X)
SET @s_0410_1 = UUID(); SET @s_0410_2 = UUID(); -- Apr 10 (짝수, summary O)
SET @s_0411_1 = UUID();                           -- Apr 11 (홀수, summary X)
SET @s_0412_1 = UUID(); SET @s_0412_2 = UUID(); -- Apr 12 (짝수, summary O)
SET @s_0413_1 = UUID();                           -- Apr 13 (홀수, summary X)
SET @s_0414_1 = UUID(); SET @s_0414_2 = UUID(); -- Apr 14 (짝수, summary O)
SET @s_0415_1 = UUID();                           -- Apr 15 (홀수, summary X)

-- summary UUID 변수
SET @sum_0402 = UUID();
SET @sum_0404 = UUID();
SET @sum_0406 = UUID();
SET @sum_0408 = UUID();
SET @sum_0410 = UUID();
SET @sum_0412 = UUID();
SET @sum_0414 = UUID();

-- ================================================================
-- focus_session INSERT
-- (모든 날짜: 홀수 + 짝수)
-- ================================================================
INSERT INTO tbl_focus_session (focus_session_id, user_id, started_at, ended_at, duration_seconds, preset_minutes)
VALUES
-- Apr 2 (짝수): 오전 90분 + 오후 60분
(UUID_TO_BIN(@s_0402_1), UUID_TO_BIN(@user_id_str), '2026-04-02 01:00:00', '2026-04-02 02:30:00', 5400, 25),
(UUID_TO_BIN(@s_0402_2), UUID_TO_BIN(@user_id_str), '2026-04-02 05:00:00', '2026-04-02 06:00:00', 3600, 25),
-- Apr 3 (홀수): 오전 90분만
(UUID_TO_BIN(@s_0403_1), UUID_TO_BIN(@user_id_str), '2026-04-03 01:00:00', '2026-04-03 02:30:00', 5400, 25),
-- Apr 4 (짝수): 오전 90분 + 오후 60분
(UUID_TO_BIN(@s_0404_1), UUID_TO_BIN(@user_id_str), '2026-04-04 01:00:00', '2026-04-04 02:30:00', 5400, 25),
(UUID_TO_BIN(@s_0404_2), UUID_TO_BIN(@user_id_str), '2026-04-04 05:00:00', '2026-04-04 06:00:00', 3600, 25),
-- Apr 5 (홀수): 오전 90분만
(UUID_TO_BIN(@s_0405_1), UUID_TO_BIN(@user_id_str), '2026-04-05 01:00:00', '2026-04-05 02:30:00', 5400, 25),
-- Apr 6 (짝수): 오전 90분 + 오후 60분
(UUID_TO_BIN(@s_0406_1), UUID_TO_BIN(@user_id_str), '2026-04-06 01:00:00', '2026-04-06 02:30:00', 5400, 25),
(UUID_TO_BIN(@s_0406_2), UUID_TO_BIN(@user_id_str), '2026-04-06 05:00:00', '2026-04-06 06:00:00', 3600, 25),
-- Apr 7 (홀수): 오전 90분만
(UUID_TO_BIN(@s_0407_1), UUID_TO_BIN(@user_id_str), '2026-04-07 01:00:00', '2026-04-07 02:30:00', 5400, 25),
-- Apr 8 (짝수): 오전 90분 + 오후 60분
(UUID_TO_BIN(@s_0408_1), UUID_TO_BIN(@user_id_str), '2026-04-08 01:00:00', '2026-04-08 02:30:00', 5400, 25),
(UUID_TO_BIN(@s_0408_2), UUID_TO_BIN(@user_id_str), '2026-04-08 05:00:00', '2026-04-08 06:00:00', 3600, 25),
-- Apr 9 (홀수): 오전 90분만
(UUID_TO_BIN(@s_0409_1), UUID_TO_BIN(@user_id_str), '2026-04-09 01:00:00', '2026-04-09 02:30:00', 5400, 25),
-- Apr 10 (짝수): 오전 90분 + 오후 60분
(UUID_TO_BIN(@s_0410_1), UUID_TO_BIN(@user_id_str), '2026-04-10 01:00:00', '2026-04-10 02:30:00', 5400, 25),
(UUID_TO_BIN(@s_0410_2), UUID_TO_BIN(@user_id_str), '2026-04-10 05:00:00', '2026-04-10 06:00:00', 3600, 25),
-- Apr 11 (홀수): 오전 90분만
(UUID_TO_BIN(@s_0411_1), UUID_TO_BIN(@user_id_str), '2026-04-11 01:00:00', '2026-04-11 02:30:00', 5400, 25),
-- Apr 12 (짝수): 오전 90분 + 오후 60분
(UUID_TO_BIN(@s_0412_1), UUID_TO_BIN(@user_id_str), '2026-04-12 01:00:00', '2026-04-12 02:30:00', 5400, 25),
(UUID_TO_BIN(@s_0412_2), UUID_TO_BIN(@user_id_str), '2026-04-12 05:00:00', '2026-04-12 06:00:00', 3600, 25),
-- Apr 13 (홀수): 오전 90분만
(UUID_TO_BIN(@s_0413_1), UUID_TO_BIN(@user_id_str), '2026-04-13 01:00:00', '2026-04-13 02:30:00', 5400, 25),
-- Apr 14 (짝수): 오전 90분 + 오후 60분
(UUID_TO_BIN(@s_0414_1), UUID_TO_BIN(@user_id_str), '2026-04-14 01:00:00', '2026-04-14 02:30:00', 5400, 25),
(UUID_TO_BIN(@s_0414_2), UUID_TO_BIN(@user_id_str), '2026-04-14 05:00:00', '2026-04-14 06:00:00', 3600, 25),
-- Apr 15 (홀수): 오전 90분만
(UUID_TO_BIN(@s_0415_1), UUID_TO_BIN(@user_id_str), '2026-04-15 01:00:00', '2026-04-15 02:30:00', 5400, 25);

-- ================================================================
-- daily_focus_summary INSERT (짝수일만 — SUMMARY 경로)
-- KST 자정 이후 집계됨: closed_at = 해당일 자정(UTC) + 5분
-- 예) Apr 2 KST 자정 = Apr 2 15:00 UTC → closed_at = Apr 2 15:05 UTC
-- ================================================================
INSERT INTO tbl_daily_focus_summary
    (daily_focus_summary_id, user_id, summary_date, total_seconds, session_count, closed_at)
VALUES
-- Apr 2: 세션 2개, 오전(5400) + 오후(3600) = 9000초
(UUID_TO_BIN(@sum_0402), UUID_TO_BIN(@user_id_str), '2026-04-02', 9000, 2, '2026-04-02 15:05:00'),
-- Apr 4
(UUID_TO_BIN(@sum_0404), UUID_TO_BIN(@user_id_str), '2026-04-04', 9000, 2, '2026-04-04 15:05:00'),
-- Apr 6
(UUID_TO_BIN(@sum_0406), UUID_TO_BIN(@user_id_str), '2026-04-06', 9000, 2, '2026-04-06 15:05:00'),
-- Apr 8
(UUID_TO_BIN(@sum_0408), UUID_TO_BIN(@user_id_str), '2026-04-08', 9000, 2, '2026-04-08 15:05:00'),
-- Apr 10
(UUID_TO_BIN(@sum_0410), UUID_TO_BIN(@user_id_str), '2026-04-10', 9000, 2, '2026-04-10 15:05:00'),
-- Apr 12
(UUID_TO_BIN(@sum_0412), UUID_TO_BIN(@user_id_str), '2026-04-12', 9000, 2, '2026-04-12 15:05:00'),
-- Apr 14
(UUID_TO_BIN(@sum_0414), UUID_TO_BIN(@user_id_str), '2026-04-14', 9000, 2, '2026-04-14 15:05:00');

-- ================================================================
-- daily_focus_summary_item INSERT
-- overlap_started_at / overlap_ended_at: 세션이 해당일 KST 기간 내에 완전히 포함되므로
-- session의 started_at / ended_at 과 동일
-- ================================================================
INSERT INTO tbl_daily_focus_summary_item
    (daily_focus_summary_item_id, daily_focus_summary_id, original_focus_session_id,
     started_at_snapshot, ended_at_snapshot, recorded_duration_seconds_snapshot,
     overlap_started_at, overlap_ended_at, overlap_seconds, display_order)
VALUES
-- Apr 2 items
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0402), UUID_TO_BIN(@s_0402_1),
 '2026-04-02 01:00:00', '2026-04-02 02:30:00', 5400,
 '2026-04-02 01:00:00', '2026-04-02 02:30:00', 5400, 1),
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0402), UUID_TO_BIN(@s_0402_2),
 '2026-04-02 05:00:00', '2026-04-02 06:00:00', 3600,
 '2026-04-02 05:00:00', '2026-04-02 06:00:00', 3600, 2),

-- Apr 4 items
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0404), UUID_TO_BIN(@s_0404_1),
 '2026-04-04 01:00:00', '2026-04-04 02:30:00', 5400,
 '2026-04-04 01:00:00', '2026-04-04 02:30:00', 5400, 1),
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0404), UUID_TO_BIN(@s_0404_2),
 '2026-04-04 05:00:00', '2026-04-04 06:00:00', 3600,
 '2026-04-04 05:00:00', '2026-04-04 06:00:00', 3600, 2),

-- Apr 6 items
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0406), UUID_TO_BIN(@s_0406_1),
 '2026-04-06 01:00:00', '2026-04-06 02:30:00', 5400,
 '2026-04-06 01:00:00', '2026-04-06 02:30:00', 5400, 1),
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0406), UUID_TO_BIN(@s_0406_2),
 '2026-04-06 05:00:00', '2026-04-06 06:00:00', 3600,
 '2026-04-06 05:00:00', '2026-04-06 06:00:00', 3600, 2),

-- Apr 8 items
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0408), UUID_TO_BIN(@s_0408_1),
 '2026-04-08 01:00:00', '2026-04-08 02:30:00', 5400,
 '2026-04-08 01:00:00', '2026-04-08 02:30:00', 5400, 1),
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0408), UUID_TO_BIN(@s_0408_2),
 '2026-04-08 05:00:00', '2026-04-08 06:00:00', 3600,
 '2026-04-08 05:00:00', '2026-04-08 06:00:00', 3600, 2),

-- Apr 10 items
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0410), UUID_TO_BIN(@s_0410_1),
 '2026-04-10 01:00:00', '2026-04-10 02:30:00', 5400,
 '2026-04-10 01:00:00', '2026-04-10 02:30:00', 5400, 1),
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0410), UUID_TO_BIN(@s_0410_2),
 '2026-04-10 05:00:00', '2026-04-10 06:00:00', 3600,
 '2026-04-10 05:00:00', '2026-04-10 06:00:00', 3600, 2),

-- Apr 12 items
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0412), UUID_TO_BIN(@s_0412_1),
 '2026-04-12 01:00:00', '2026-04-12 02:30:00', 5400,
 '2026-04-12 01:00:00', '2026-04-12 02:30:00', 5400, 1),
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0412), UUID_TO_BIN(@s_0412_2),
 '2026-04-12 05:00:00', '2026-04-12 06:00:00', 3600,
 '2026-04-12 05:00:00', '2026-04-12 06:00:00', 3600, 2),

-- Apr 14 items
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0414), UUID_TO_BIN(@s_0414_1),
 '2026-04-14 01:00:00', '2026-04-14 02:30:00', 5400,
 '2026-04-14 01:00:00', '2026-04-14 02:30:00', 5400, 1),
(UUID_TO_BIN(UUID()), UUID_TO_BIN(@sum_0414), UUID_TO_BIN(@s_0414_2),
 '2026-04-14 05:00:00', '2026-04-14 06:00:00', 3600,
 '2026-04-14 05:00:00', '2026-04-14 06:00:00', 3600, 2);

-- ================================================================
-- 삽입 결과 확인
-- ================================================================
SELECT '=== focus_session 삽입 결과 ===' AS '';
SELECT
    DATE(CONVERT_TZ(started_at, '+00:00', '+09:00')) AS kst_date,
    COUNT(*) AS session_count,
    SUM(duration_seconds) AS total_seconds
FROM tbl_focus_session
WHERE user_id = UUID_TO_BIN(@user_id_str)
  AND started_at >= '2026-03-31 15:00:00'
  AND started_at < '2026-04-30 15:00:00'
GROUP BY kst_date
ORDER BY kst_date;

SELECT '=== daily_focus_summary 삽입 결과 ===' AS '';
SELECT summary_date, total_seconds, session_count
FROM tbl_daily_focus_summary
WHERE user_id = UUID_TO_BIN(@user_id_str)
  AND summary_date BETWEEN '2026-04-01' AND '2026-04-30'
ORDER BY summary_date;

-- safe update 모드 복원
SET SQL_SAFE_UPDATES = 1;

SELECT '=== 데이터 패턴 검증 ===' AS '';
SELECT
    fs_days.kst_date,
    fs_days.session_count AS sessions,
    IF(dfs.summary_date IS NOT NULL, 'O (SUMMARY)', 'X (RAW → rawPeriod)') AS has_summary
FROM (
    SELECT
        DATE(CONVERT_TZ(started_at, '+00:00', '+09:00')) AS kst_date,
        COUNT(*) AS session_count
    FROM tbl_focus_session
    WHERE user_id = UUID_TO_BIN(@user_id_str)
      AND started_at >= '2026-03-31 15:00:00'
      AND started_at < '2026-04-30 15:00:00'
    GROUP BY kst_date
) fs_days
LEFT JOIN tbl_daily_focus_summary dfs
    ON dfs.user_id = UUID_TO_BIN(@user_id_str)
   AND dfs.summary_date = fs_days.kst_date
ORDER BY fs_days.kst_date;
