package com.creditville.notifications.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String credit;
    private String debit;
    private String narration;
    private String valueDate;
    private String balance;

    protected Transactions() {
    }

    public Transactions(String credit, String debit, String narration, String valueDate, String balance) {
        this.credit = credit;
        this.debit = debit;
        this.narration = narration;
        this.valueDate = valueDate;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getDebit() {
        return debit;
    }

    public void setDebit(String debit) {
        this.debit = debit;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getValueDate() {
        return valueDate;
    }

    public void setValueDate(String valueDate) {
        this.valueDate = valueDate;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return String.format("Transactions[id=%d, credit='%s', debit='%s', narration='%s', valueDate='%s']",id,credit,debit,narration,valueDate);
    }
}
