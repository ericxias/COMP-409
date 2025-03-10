package a3;
import java.util.Random;

public class q1 {

    public static void main(String [] args){
        Random random = new Random();

        if (args.length != 2 || Integer.parseInt(args[0]) >= 100) {
            System.out.println("Usage: java q1.java <k> <m>");
            return;
        }

        int k = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);

        q1a a = new q1a();
        q1b b = new q1b();
        
        // m operations for each thread
        Thread[] threadsA = new Thread[4];
        long startTimeA = System.nanoTime();
        for (int i = 0; i < 4; i++) {
            final int threadId = i;
            threadsA[i] = new Thread(new Runnable() {
                @Override 
                public void run() {
                    for (int i = 0; i < m; i ++) {
                        if (random.nextInt(99) + 1 < 100 - k) {
                            // read/write an existing array element
                            if (random.nextInt(1) == 0){
                                // read
                                a.get(random.nextInt(a.array.length));
                            } else {
                                // write
                                a.set(random.nextInt(a.array.length), new Object());
                            }
                        } else {
                            // read/write one past end of array
                            if (random.nextInt(1) == 0){
                                // read
                                a.get(a.array.length + 1);
                            } else {
                                // write
                                a.set(a.array.length + 1, new Object());
                            }
                        }
                    }
                }
            });
            threadsA[i].start();
        }

        for (Thread thread : threadsA) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long endTimeA = System.nanoTime();

        long startTimeB = System.nanoTime();
        Thread[] threadsB = new Thread[4];
        for (int i = 0; i < 4; i++) {
            final int threadId = i;
            threadsB[i] = new Thread(new Runnable() {
                @Override 
                public void run() {
                    for (int i = 0; i < m; i ++) {
                        if (random.nextInt(100) + 1 < 100 - k) {
                            // read/write an existing array element
                            if (random.nextInt(1) == 0){
                                // read
                                b.get(random.nextInt(b.array.length));
                            } else {
                                // write
                                b.set(random.nextInt(b.array.length), new Object());
                            }
                        } else {
                            // read/write one past end of array
                            if (random.nextInt(1) == 0){
                                // read
                                b.get(b.array.length);
                                //System.out.println(b.array.length + " from Thread " + threadId);
                            } else {
                                // write
                                b.set(b.array.length + 1, new Object());
                            }
                        }
                    }
                }
            });
            threadsB[i].start();
        }

        for (Thread thread : threadsB) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long endTimeB = System.nanoTime();

        System.out.println("Time taken for q1a: " + (endTimeA - startTimeA) + " ns");
        System.out.println("Time taken for q1b: " + (endTimeB - startTimeB) + " ns");
        
        
    }
}
