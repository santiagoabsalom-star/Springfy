package com.surrogate.springfy.models.bussines.streaming;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Control{
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    public volatile boolean pausado = false;

    public void stop() {

        synchronized (lock) {
            pausado= true;
        }

    }

    public void reanudar() {
        lock.lock();
        try {
            pausado = false;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
    public void esperarSiEstaPausado(){
        lock.lock();
        try {
            while (pausado) {
                condition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }



//clase auxiliar para parar y reanudar la musica


}