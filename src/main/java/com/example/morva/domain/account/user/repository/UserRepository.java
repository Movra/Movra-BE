package com.example.morva.domain.account.user.repository;

import com.example.morva.domain.account.user.User;
import com.example.morva.domain.account.user.vo.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UserId> {
}
