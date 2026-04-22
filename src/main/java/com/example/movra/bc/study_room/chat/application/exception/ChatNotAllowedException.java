package com.example.movra.bc.study_room.chat.application.exception;

import com.example.movra.sharedkernel.exception.CustomException;
import com.example.movra.sharedkernel.exception.ErrorCode;

public class ChatNotAllowedException extends CustomException {
    public ChatNotAllowedException() {
        super(ErrorCode.CHAT_NOT_ALLOWED);
    }
}
