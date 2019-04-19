package edu.bit.felinae;

import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.Before;
import org.junit.runners.MethodSorters;
import static org.junit.Assert.*;
import redis.clients.jedis.Jedis;


@FixMethodOrder(value= MethodSorters.NAME_ASCENDING)
public class WokerTest {
    private Database db;
    private MainQueue queue;
    @Before public void testCreateUser() {
        db = Database.getInstance();
        queue = MainQueue.getInstance();
        db.register("spencer", "123");
    }
    @Test public void test00CreateAccount() {
        Session session = new Session();
        session.transaction_type = TransactionType.account;
        session.transaction = Transaction.Register;
        session.username = "spencerr";
        session.password = "123";
        Session.saveSession("000", session);
        queue.enqueue("000");
        Thread test = new Worker();
        test.run();
        try {
            test.join();
        }catch (Exception e){
            System.err.println(e.getMessage());
        }finally {
            session = Session.getSession("000");
            assertNotNull(session);
            assertEquals("success",session.res);
        }

    }
    @Test public void test01CheckBalance() {
        db.deposit("spencer", 10);
        Session session = new Session();
        session.transaction_type = TransactionType.general;
        session.transaction = Transaction.BalanceInquery;
        session.username = "spencer";
        session.password = "123";
        Session.saveSession("000", session);
        queue.enqueue("000");
        Thread test = new Worker();
        test.run();
        try {
            test.join();
        }catch (Exception e){
            System.err.println(e.getMessage());
        }finally {
            session = Session.getSession("000");
            assertNotNull(session);
            assertEquals(10,session.balance, 0.001);
            assertEquals("success",session.res);
        }
    }
    @Test public void test02Deposit() {
        Session session = new Session();
        session.transaction_type = TransactionType.general;
        session.transaction = Transaction.Deposit;
        session.amount = 10.0;
        session.username = "spencer";
        session.password = "123";
        Session.saveSession("000", session);
        queue.enqueue("000");
        Thread test = new Worker();
        test.run();
        try {
            test.join();
        }catch (Exception e){
            System.err.println(e.getMessage());
        }finally {
            session = Session.getSession("000");
            assertNotNull(session);
            assertEquals("success",session.res);
            assertEquals(10, db.checkBalance("spencer"), 0.001);
        }
    }
    @Test public void test03WithdrawalOK() {
        db.deposit("spencer", 10);
        Session session = new Session();
        session.transaction_type = TransactionType.general;
        session.transaction = Transaction.Withdrawal;
        session.amount = 5.0;
        session.username = "spencer";
        session.password = "123";
        Session.saveSession("000", session);
        queue.enqueue("000");
        Thread test = new Worker();
        test.run();
        try {
            test.join();
        }catch (Exception e){
            System.err.println(e.getMessage());
        }finally {
            session = Session.getSession("000");
            assertNotNull(session);
            assertEquals("success",session.res);
            assertEquals(5, db.checkBalance("spencer"), 0.001);
        }
    }
    @Test public void test04WithdrawalGG() {
        db.deposit("spencer", 5);
        Session session = new Session();
        session.transaction_type = TransactionType.general;
        session.transaction = Transaction.Withdrawal;
        session.amount = 10.0;
        session.username = "spencer";
        session.password = "123";
        Session.saveSession("000", session);
        queue.enqueue("000");
        Thread test = new Worker();
        test.run();
        try {
            test.join();
        }catch (Exception e){
            System.err.println(e.getMessage());
        }finally {
            session = Session.getSession("000");
            assertNotNull(session);
            assertEquals("fail",session.res);
            assertEquals(5, db.checkBalance("spencer"), 0.001);
        }
    }
    @After public void test999Close() {
        Jedis jedis = new Jedis("localhost");
        jedis.flushAll();
        db.cleanDB();
    }
}
