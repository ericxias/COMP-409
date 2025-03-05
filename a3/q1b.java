package a3;
import java.util.concurrent.atomic.AtomicBoolean;

public class q1b {
    public volatile Object[] array;
    private final AtomicBoolean resizing = new AtomicBoolean(false);

    public q1b() {
        array = new Object[20];
    }
    
    public Object get(int i) {
        while (resizing.get()) {
            // spin
        }
        if (i > array.length) {
            resize();
        }
        return array[i];
    }

    public void set(int i, Object o){
        while (resizing.get()) {
            // spin
        }

        if (i > array.length) {
            resize();
        }
        array[i] = o;
    }

    public void resize() {
        if (resizing.compareAndSet(false, true)){
            try {
                Object[] newArray = new Object[array.length + 10];
                for (int i = 0; i < array.length; i++) {
                    newArray[i] = array[i];
                }
                array = newArray;
            } finally {
                resizing.set(false);
            }
        }       
    }
}
