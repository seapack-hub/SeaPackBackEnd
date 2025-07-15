package org.seapack.service;

import org.seapack.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface UserService {
    List<User> getUserList(String userName, String email, LocalDateTime startTime, LocalDateTime endTime);
}
