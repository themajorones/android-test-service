package dev.themajorones.autotest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.themajorones.autotest.entity.Authentication;

@Repository
public interface AuthenticationRepository extends JpaRepository<Authentication, Integer> {
}
