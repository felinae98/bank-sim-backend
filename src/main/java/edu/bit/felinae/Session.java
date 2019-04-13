package edu.bit.felinae;

public class Session {
    private SessionStatus status = SessionStatus.Queueing;
    private Operaion operaion;
    private float balance;
    private float money;
    Session(){
    }

    public SessionStatus getStatus() {
        return status;
    }

    public Operaion getOperaion() {
        return operaion;
    }

    public float getBalance() {
        return balance;
    }

    public float getMoney() {
        return money;
    }

}

enum SessionStatus{
    Queueing,
    NumberCalled,
    inTransaction,
    TransactionDone
}
enum Operaion{
    Deposit, Withdrawal, OpenAccount, BalanceInquery
}