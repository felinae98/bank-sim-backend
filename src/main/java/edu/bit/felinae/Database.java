package edu.bit.felinae;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Database {
    private Lock lock;
    private static Database instance = new Database();
    private Connection conn;
    public static Database getInstance() {
        return instance;
    }
    private void initDatabase() {
        try {
            DatabaseMetaData md = conn.getMetaData();
            ArrayList<String> table_name = new ArrayList<>();
            ResultSet set = md.getTables(null, null, "%", null);
            while(set.next()){
                table_name.add(set.getString(3));
                System.out.println(set.getString(3));
            }
            if(!table_name.contains("user")){
                lock.lock();
                Statement stmt = conn.createStatement();
                String sql = "CREATE TABLE user (" +
                        "id integer PRIMARY KEY," +
                        "username varchar(225) NOT NULL," +
                        "password varchar(225) NOT NULL," +
                        "balance float DEFAULT 0" +
                        ");";
                lock.lock();
                stmt.execute(sql);
                lock.unlock();
                System.out.println("insert ok");
            }

        }catch (SQLException e){
            System.err.println(e.getMessage());
        }
    }
    private Database() {
        lock = new ReentrantLock();
        String url = "jdbc:sqlite:db.sqlite";
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(url);
            System.out.println("success");
            initDatabase();
        }catch (SQLException s) {
            System.err.println(s.toString());
        }catch (ClassNotFoundException e){
            System.err.println(e.getMessage());
        }
    }

    public boolean checkCreditial(String username, String password) {
        String sql = "SELECT COUNT(*) FROM user WHERE username=? AND password=?";
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) == 1;

        }catch (SQLException e){
            System.err.println(e.getMessage());
            return false;
        }
    }
}
