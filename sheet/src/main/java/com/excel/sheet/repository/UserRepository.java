package com.excel.sheet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.excel.sheet.model.UserData;

@Repository
public interface UserRepository extends JpaRepository<UserData, Integer> {

}
