package io.github.toniidev.fantasiamoney.classes;

import io.github.toniidev.fantasiamoney.enums.TransactionType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class BankTransaction {
    private double amount;
    private TransactionType type;
    private String date;

    public BankTransaction(double amount, TransactionType type) {
        this.amount = amount;
        this.type = type;

        LocalDateTime time = LocalDateTime.now();
        int rawDay = time.getDayOfMonth();
        int rawMonth = time.getMonthValue();

        String day = rawDay < 10 ?
                "0" + rawDay : String.valueOf(rawDay);
        String month = rawMonth < 10 ?
                "0" + rawMonth : String.valueOf(rawMonth);
        String year = String.valueOf(time.getYear()).substring(2);

        date = day + "/" + month + "/" + year;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getDate(){
        return date;
    }
}
