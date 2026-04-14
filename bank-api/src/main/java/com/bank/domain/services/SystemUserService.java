package com.bank.domain.services;

import com.bank.domain.entities.UserStatus;
import com.bank.domain.entities.SystemUser;

public class SystemUserService {

    public void validateActiveUser(SystemUser systemUser) {
        if (systemUser == null) {
            throw new IllegalArgumentException("User required");
        }
        if (systemUser.getUserStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("User is not active");
        }
    }
}
