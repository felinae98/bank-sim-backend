package edu.bit.felinae;


import java.util.Random;

public class Worker extends Thread{
    @Override
    public void run() {
        MainQueue queue = MainQueue.getInstance();
        Database db = Database.getInstance();
        String session_id;
        do{
            session_id = queue.poll();
        }while(!queue.getShutdown() && session_id == null);
        if(queue.getShutdown()) return;
        Session session = Session.getSession(session_id);
        if(session.transaction_type==TransactionType.account){
            switch (session.transaction){
                case Deposit:
                    break;
                case Withdrawal:
                    break;
                case Register:
                    if(db.register(session.username, session.password)) {
                        session.res = "success";
                    }else{
                        session.res = "fail";
                    }
                    Session.saveSession(session_id, session);
                    break;
                case BalanceInquery:
                    if(db.checkCreditial(session.username, session.password)) {
                        session.res = "success";
                        session.balance = db.checkBalance(session.username);
                    }else{
                        session.res = "password error";
                    }

                    Session.saveSession(session_id, session);
                    break;
                case Delete:
                    break;
            }
        }
        Random rand = new Random();
        try{
            Thread.sleep(rand.nextInt()%2000+1000);
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
}
