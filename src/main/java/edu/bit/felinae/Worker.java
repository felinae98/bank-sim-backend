package edu.bit.felinae;


import java.util.Random;

public class Worker extends Thread{
    @Override
    public void run() {
        MainQueue queue = MainQueue.getInstance();
        Database db = Database.getInstance();
        String session_id;
        while (true) {
            do {
                session_id = queue.poll();
            } while (!queue.getShutdown() && session_id == null);
            if (session_id != null && queue.getShutdown()) return;
            Session session = Session.getSession(session_id);
            System.out.println(session);
            session.status = SessionStatus.inTransaction;
            Session.saveSession(session_id, session);
            if (session.transaction_type == TransactionType.account) {
                switch (session.transaction) {
                    case Deposit:
                        break;
                    case Withdrawal:
                        break;
                    case Register:
                        if (db.register(session.username, session.password)) {
                            session.res = "success";
                        } else {
                            session.res = "fail";
                        }
                        break;
                    case Delete:
                        break;
                }
            } else {
                switch (session.transaction) {
                    case BalanceInquery:
                        if (db.checkCreditial(session.username, session.password)) {
                            session.res = "success";
                            session.balance = db.checkBalance(session.username);
                        } else {
                            session.res = "password error";
                        }

                        break;
                    case Deposit:
                        if (db.checkCreditial(session.username, session.password)) {
                            if (db.deposit(session.username, session.amount)) {
                                session.res = "success";
                            } else {
                                session.res = "fail";
                            }
                        } else {
                            session.res = "password error";
                        }

                        break;
                    case Withdrawal:
                        if (db.checkCreditial(session.username, session.password)) {
                            if (db.withdrawal(session.username, session.amount)) {
                                session.res = "success";
                            } else {
                                session.res = "fail";
                            }
                        } else {
                            session.res = "password error";
                        }
                        break;
                }
            }
            Random rand = new Random();
            try {
                Thread.sleep(rand.nextInt(2000) + 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            session.status = SessionStatus.TransactionDone;
            Session.saveSession(session_id, session);
            if (queue.getShutdown()) return;
        }
    }
}
