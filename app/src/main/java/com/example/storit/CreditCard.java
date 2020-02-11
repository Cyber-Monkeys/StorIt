package com.example.storit;

public class CreditCard {

    //Variables
    private String cardName;
    private int cardNumber;
    private int cvv;
    private int month;
    private int year;

    public CreditCard(String cardName, int cardNumber, int cvv, int month, int year) {
        this.cardName = cardName;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.month = month;
        this.year = year;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public int getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(int cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getCvv() {
        return cvv;
    }

    public void setCvv(int cvv) {
        this.cvv = cvv;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
