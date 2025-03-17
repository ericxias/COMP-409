package a3;
import java.util.concurrent.locks.ReentrantLock;

public class q1a {
    // resizable array
    public volatile Object[] array;
    private final ReentrantLock resize = new ReentrantLock();
    // array of locks for sqrt(n) indexes
    private volatile ReentrantLock[] locks;

    public q1a() {
        // initialize array of size 20 with sqrt(n) locks
        array = new Object[20];
        int numLocks = (int) Math.sqrt(array.length);
        locks = new ReentrantLock[numLocks];
        for (int i = 0; i < locks.length; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    public Object get(int i) {
        // check if resizing, and if i is within the array
        if (i >= array.length) {
            resize.lock();
            try {
                // check size again in case another thread resized the array
                if (i >= array.length) {
                    resize();
                }
            } finally {
                resize.unlock();
            }
        }

        // lock correlated lock for atomic read
        ReentrantLock lock = locks[i % locks.length];
        lock.lock();
        try {
            while (resize.isLocked()) {
                // spin
            }

            return array[i];
        } finally {
            lock.unlock();
        }
        
    }

    public void set(int i, Object o) {
        // check if resizing, and if i is within the array
        if (i >= array.length) {
            resize.lock();
            try {
                // check size again in case another thread resized while waiting
                if (i >= array.length) {
                    resize();
                }
            } finally {
                resize.unlock();
            }
        }

        // lock correlated lock of array for atomic write
        ReentrantLock lock = locks[i % locks.length];
        lock.lock();
        try {
            // check if resizing is in progress
            while (resize.isLocked()) {
                // spin
            }
            array[i] = o;
            
        } finally {
            lock.unlock();
        }

    }

    public void resize(){    
        // resize array with 10 more elements and copy over old elements
        Object[] tempArray = new Object[array.length + 10];
        for (int i = 0; i < array.length; i++) {
            tempArray[i] = array[i];
        }

        // resize locks array to sqrt(new n) locks and copy over old locks
        int numLocks = (int) Math.sqrt(tempArray.length);
        ReentrantLock[] tempLocks = new ReentrantLock[numLocks];
        for (int i = 0; i < locks.length; i++) {
            tempLocks[i] = locks[i];
        }
        for (int i = locks.length; i < tempLocks.length; i++) {
            tempLocks[i] = new ReentrantLock();
        }

        array = tempArray;
        locks = tempLocks;
        
    }
    
}
