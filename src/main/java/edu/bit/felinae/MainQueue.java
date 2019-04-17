package edu.bit.felinae;

import java.util.concurrent.LinkedBlockingQueue;

public class MainQueue {
    private static MainQueue instance = new MainQueue();
    private LinkedBlockingQueue<String> queue;
    private MainQueue(){
        queue = new LinkedBlockingQueue<>();
    }
    public static MainQueue getInstance(){
        return instance;
    }
    public void enqueue(String sess_id){
        queue.offer(sess_id);
    }
    public void dequeue(){
        try {
            queue.take();
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }
}
