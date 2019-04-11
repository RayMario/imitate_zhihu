package com.imitatezhihu.zhihu_imitate;

import java.util.concurrent.ArrayBlockingQueue;

public class Test {
    static ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(10);

    public static void main(String[] args) {
        int capacity = 10;
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    queue.put("23");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(queue);
            }
            // create object of ArrayBlockingQueue
            // ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(capacity);

            // Add element to ArrayBlockingQueue
        });
        thread1.start();
    }
}
