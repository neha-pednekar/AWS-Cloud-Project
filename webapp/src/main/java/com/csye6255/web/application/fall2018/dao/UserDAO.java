package com.csye6255.web.application.fall2018.dao;
import com.csye6255.web.application.fall2018.pojo.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserDAO extends CrudRepository<User, Long> {

    List<User> findByUserName(String userName);
}