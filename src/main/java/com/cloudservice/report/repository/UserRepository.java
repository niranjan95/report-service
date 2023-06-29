package com.cloudservice.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cloudservice.report.model.UserData;

@Repository
public interface UserRepository extends JpaRepository<UserData, Long> {
	UserData findBySecretkey(String secretkey);
}