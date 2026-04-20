package com.example.movra.sharedkernel.exception;

import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.util.Locale;

public final class DataIntegrityViolationUtils {

    private static final int MYSQL_DUPLICATE_ENTRY_ERROR_CODE = 1062;
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    private DataIntegrityViolationUtils() {
    }

    public static boolean isDuplicateKeyViolation(DataIntegrityViolationException exception) {
        SQLException sqlException = findSQLException(exception);
        if (sqlException == null) {
            return false;
        }

        SQLException current = sqlException;
        while (current != null) {
            if (current.getErrorCode() == MYSQL_DUPLICATE_ENTRY_ERROR_CODE
                    || UNIQUE_VIOLATION_SQL_STATE.equals(current.getSQLState())) {
                return true;
            }
            current = current.getNextException();
        }

        return false;
    }

    public static boolean isDuplicateKeyViolation(
            DataIntegrityViolationException exception,
            String constraintName
    ) {
        if (!isDuplicateKeyViolation(exception)) {
            return false;
        }

        String normalizedConstraintName = constraintName.toLowerCase(Locale.ROOT);
        return containsConstraintName(exception, normalizedConstraintName);
    }

    private static SQLException findSQLException(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlException) {
                return sqlException;
            }
            current = current.getCause();
        }
        return null;
    }

    private static boolean containsConstraintName(Throwable throwable, String normalizedConstraintName) {
        Throwable current = throwable;
        while (current != null) {
            if (messageContains(current.getMessage(), normalizedConstraintName)) {
                return true;
            }

            if (current instanceof SQLException sqlException && containsConstraintName(sqlException, normalizedConstraintName)) {
                return true;
            }

            current = current.getCause();
        }
        return false;
    }

    private static boolean containsConstraintName(SQLException sqlException, String normalizedConstraintName) {
        SQLException current = sqlException;
        while (current != null) {
            if (messageContains(current.getMessage(), normalizedConstraintName)) {
                return true;
            }
            current = current.getNextException();
        }
        return false;
    }

    private static boolean messageContains(String message, String normalizedConstraintName) {
        return message != null && message.toLowerCase(Locale.ROOT).contains(normalizedConstraintName);
    }
}
