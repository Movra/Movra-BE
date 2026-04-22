package com.example.movra.bc.study_room.chat.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class InvalidChatMessageException extends CustomException {
    public InvalidChatMessageException() {
        super(ErrorCode.INVALID_CHAT_MESSAGE);
    }
}
