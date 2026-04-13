package com.example.movra.bc.accountability.accountability_relation.domain.vo;


import java.io.Serializable;
import java.util.UUID;

public record AccountabilityRelationId(
        UUID id
) implements Serializable {

    public static AccountabilityRelationId newId(){
        return new AccountabilityRelationId(UUID.randomUUID());
    }

    public static AccountabilityRelationId of(UUID accountabilityRelationId){
        return new AccountabilityRelationId(accountabilityRelationId);
    }
}
