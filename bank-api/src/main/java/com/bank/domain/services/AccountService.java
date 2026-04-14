package com.bank.domain.services;

import com.bank.domain.entities.Account;
import com.bank.domain.valueobjects.Money;

public class AccountService {

    public void deposit(Account account, Money amount) {
        account.deposit(amount);
    }

    public void withdraw(Account account, Money amount) {
        account.withdraw(amount);
    }

    public Money getBalance(Account account) {
        account.validateOperationalAccount();
        return account.getBalance();
    }
}
