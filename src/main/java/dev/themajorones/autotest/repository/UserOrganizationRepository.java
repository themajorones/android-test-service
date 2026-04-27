package dev.themajorones.autotest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.themajorones.autotest.entity.UserOrganization;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganization, Integer> {
}
