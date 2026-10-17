package com.diligent.expense.repository;

import com.diligent.expense.model.GroupMembership;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    List<GroupMembership> findByGroupIdOrderByUserUsernameAsc(Long groupId);

    Optional<GroupMembership> findByGroupIdAndUserUsername(Long groupId, String username);

    boolean existsByGroupIdAndUserUsername(Long groupId, String username);

    long countByGroupId(Long groupId);
}
