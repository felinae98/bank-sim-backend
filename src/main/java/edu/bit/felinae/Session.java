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

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public void setOperaion(Operaion operaion) {
        this.operaion = operaion;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public void setMoney(float money) {
        this.money = money;
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