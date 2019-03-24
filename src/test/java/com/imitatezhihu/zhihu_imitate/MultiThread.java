package com.imitatezhihu.zhihu_imitate;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class MyThread extends Thread {
    private int tid;
    public MyThread(int tid){
        this.tid = tid;
    }
    @Override
    public void run() {
        try{
            for(int i = 0;i<10;++i){
                Thread.sleep(1000);
                System.out.println(String.format("%d:%d",tid,i));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

//消费者&生产者模型
class Consumer implements Runnable{
    private BlockingQueue<String> q;
    public Consumer(BlockingQueue<String> q){
        this.q = q;
    }

    @Override
    public void run() {
        try{
            while(true){
                System.out.println(Thread.currentThread().getName()+":"+q.take());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Producer implements Runnable{
    private BlockingQueue<String> q;
    public Producer(BlockingQueue<String> q){
        this.q = q;
    }
    @Override
    public void run() {
        try{
            for(int i = 0;i<100;++i){
                Thread.sleep(10);
                q.put(String.valueOf(i));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


public class MultiThread {
    public static void testThread() {
        for(int i = 0;i<10;++i){
            //起了10个线程每个线程从0-10打印
            //new MyThread(i).start();
        }
        for(int i = 0;i<10;++i){
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        for(int j = 0;j<10;++j){
                            Thread.sleep(1000);
                            System.out.println(String.format("T2 %d:%d",finalI,j));
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static Object obj = new Object();

    public static void testSynchronized1() {
        synchronized (obj){
            try{
                for(int j = 0;j<10;++j){
                    Thread.sleep(1000);
                    System.out.println(String.format("T3 %d",j));
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void testSynchronized2() {
        synchronized (obj){
            try{
                for(int j = 0;j<10;++j){
                    Thread.sleep(1000);
                    System.out.println(String.format("T4 %d",j));
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static void testSynchronized() {
        for(int i = 0;i<10;++i){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    testSynchronized1();
                    testSynchronized2();
                }
            }).start();
        }
    }

    public static void testBlockingQueue(){
        BlockingQueue<String> q = new ArrayBlockingQueue<String>(10);
        new Thread(new Producer(q)).start();
        //这两个Consumer是共享这个名为q的同步队列的。就像两个用户在抢购同一个仓库当中的商品
        new Thread(new Consumer(q),"Consumer1").start();
        new Thread(new Consumer(q),"Consumer2").start();
    }

    //比较普通静态变量与ThreadLocal线程局部静态变量对象的区别：
    private static ThreadLocal<Integer> threadLocalUserIds = new ThreadLocal<>();
    private static int userId;
    /*启动10个线程，对上边的线程局部静态变量进行赋值与打印。
    应当观察到10个线程上面赋值各不相同且恰好平分这0-9十个数字。
    而UserId应当全是9——0-9其实都赋值过去了，只是最后一次（最后一条线程）赋值的是9
    结论：不同线程之间共享普通静态变量，但是线程局部变量是线程独占的。
    */
    private static void testThreadLocal(){
        for(int i = 0;i<10;++i){
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        userId = finalI;
                        threadLocalUserIds.set(finalI);
                        Thread.sleep(1000);
                        System.out.println("ThreadLocal:" +threadLocalUserIds.get());
                        System.out.println("UserId:"+ userId);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
    public static void testExecutor(){
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        //第一个任务
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                for(int i = 0;i<10;++i){
                    try{
                        Thread.sleep(1000);
                        System.out.println("Executor1:"+ i);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        //第二个任务
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                for(int i = 0;i<10;++i){
                    try{
                        Thread.sleep(1000);
                        System.out.println("Executor2:"+ i);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        //testThread();
        //testSynchronized();
        //testBlockingQueue();
        //testThreadLocal();
        testExecutor();
    }
}
