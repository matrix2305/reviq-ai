package com.reviq.shared.exception;

import lombok.Getter;

@Getter
public class AccountLockedException extends BusinessException {

    private final long remainingSeconds;

    public AccountLockedException(long remainingSeconds) {
        super("ACCOUNT_LOCKED", "Account is locked. Try again in " + (remainingSeconds / 60 + 1) + " minutes");
        this.remainingSeconds = remainingSeconds;
    }
}
