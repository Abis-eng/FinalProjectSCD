package com.elcinic.model;

import java.io.Serializable;

public class PaymentCheckoutState implements Serializable {

    private int invoiceId;
    private String method;
    private String cardHolderName;
    private String cardLast4;
    private String maskedCard;
    private String cardExpiry;
    private String mobileNumber;
    private String bankName;
    private String bankTransactionRef;

    public int getInvoiceId() { return invoiceId; }
    public void setInvoiceId(int invoiceId) { this.invoiceId = invoiceId; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }
    public String getCardLast4() { return cardLast4; }
    public void setCardLast4(String cardLast4) { this.cardLast4 = cardLast4; }
    public String getMaskedCard() { return maskedCard; }
    public void setMaskedCard(String maskedCard) { this.maskedCard = maskedCard; }
    public String getCardExpiry() { return cardExpiry; }
    public void setCardExpiry(String cardExpiry) { this.cardExpiry = cardExpiry; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankTransactionRef() { return bankTransactionRef; }
    public void setBankTransactionRef(String bankTransactionRef) { this.bankTransactionRef = bankTransactionRef; }
}
