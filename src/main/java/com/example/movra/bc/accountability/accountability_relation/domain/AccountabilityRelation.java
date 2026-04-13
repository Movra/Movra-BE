package com.example.movra.bc.accountability.accountability_relation.domain;

import com.example.movra.bc.accountability.accountability_relation.domain.vo.AccountabilityRelationId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_accountability_relation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountabilityRelation {

    @EmbeddedId
    private AccountabilityRelationId id;


}


/*

필드 구조
    - AccountabilityRelationId
    - 감시자의 UserId
    - 감시 당하는 사람의 UserId
    - 감시 허용 목록
        - FucusSession
        - TopPicks
        - Timetable & Task
        - ALL
    - 감시자 초대 코드


감시자의 역할
	- 사용자는 자신이 원하는 목표를 올바르게 이룰 수 있도록 감시하는 역할

감시자가 감시하는 것
	- 선택하여 원하는 것만 공개 가능

	- 오늘 몇 분 이상 집중을 하였는가?
	- 설정한 Topicks를 다 완료 하였는가?
	- Timetable & Task : Timeable에 설정한 Task를 다 완료 하였는가?


감시자를 정하는 기준
	- 감시를 원하는 사용자는 감시자 코드를 생성하여 상대방을 초대 가능
	- 감시자 코드로 참여하면, 그 사람은 감시자 역할이 부여됨
	- 감시자는 한 명이 최대


감시자 알림
	- **시간 간격동안 진행 사항이 없다면, 감시자에게 알림이 감
	- 24시간이 될때까지 목표를 해결하지 못했다면, 감시자에게 알림이 감
	- 감시자는 피드뱍이나 응원 메시지를 알림 형태로 보낼 수 있음
	- 감시자는 현재 상황을 직접 볼 수 있음
	- 감시자는 원하는 상황에 직접 알림을 작성해서 보낼수도 있음

 */