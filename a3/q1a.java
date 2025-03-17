package a3;
import java.util.concurrent.locks.ReentrantLock;

public class q1a {
    // resizable array
    public volatile Object[] array;
    private final ReentrantLock resize = new ReentrantLock();
    // array of locks for each index of the array
    private volatile ReentrantLock[] locks;

    public q1a() {
        // initialize array of size 20 with locks
        array = new Object[20];
        locks = new ReentrantLock[20];
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

        // lock index i of array for atomicity
        locks[i].lock();
        try {
            while (resize.isLocked()) {
                // spin
            }

            return array[i];
        } finally {
            locks[i].unlock();
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

        // lock index i of array for atomicity
        locks[i].lock();
        try {
            // check if resizing is in progress
            while (resize.isLocked()) {
                // spin
            }
            array[i] = o;
            
        } finally {
            locks[i].unlock();
        }

    }

    public void resize(){    
        // create new array and locks with 10 more elements and copy over old elements
        Object[] newArray = new Object[array.length + 10];
        ReentrantLock[] newLocks = new ReentrantLock[array.length + 10];
        for (int i = 0; i < array.length; i++) {
            newArray[i] = array[i];
            newLocks[i] = locks[i];
        }
        for (int i = array.length; i < newLocks.length; i++) {
            newLocks[i] = new ReentrantLock();
        }
        array = newArray;
        locks = newLocks;
        
    }
    
}
