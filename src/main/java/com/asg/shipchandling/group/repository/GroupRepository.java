package com.asg.shipchandling.group.repository;

import com.asg.shipchandling.group.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, Long> {
    boolean existsByGroupPoid(Long groupPoid);
}

