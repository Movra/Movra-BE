package com.example.movra.bc.study_room.participant.application.event;

import com.example.movra.bc.study_room.participant.domain.Participant;
import com.example.movra.bc.study_room.participant.domain.repository.ParticipantRepository;
import com.example.movra.bc.study_room.room.domain.event.RoomCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RoomCreatedEventHandler {

    private final ParticipantRepository participantRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(RoomCreatedEvent event) {
        Participant participant = Participant.enter(event.userId(), event.roomId());
        participantRepository.save(participant);
    }
}
