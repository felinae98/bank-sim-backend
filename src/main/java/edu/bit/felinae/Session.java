package edu.bit.felinae;

public class Session {
    public SessionStatus status = SessionStatus.Queueing;
    public TransactionType transaction_type;
    public String username;
    public String password;
    public Transaction transaction;
    public double amount;
}

enum SessionStatus{
    Queueing,
    NumberCalled,
    inTransaction,
    TransactionDone
}
enum Transaction{
    Deposit, Withdrawal, OpenAccount, BalanceInquery
}
enum TransactionType{
    account,
    general
}