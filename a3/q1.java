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
        
        // create 4 threads to run operations on q1a
        Thread[] threadsA = new Thread[4];
        // milliseconds doesnt show significant difference, use nanoseconds
        long startTimeA = System.currentTimeMillis();
        
        for (int i = 0; i < 4; i++) {
            threadsA[i] = new Thread(new Runnable() {
                @Override 
                public void run() {
                    // m operations
                    for (int i = 0; i < m; i ++) {
                        // 100 - k% chance of reading/writing an existing array element
                        if (random.nextInt(99) + 1 < 100 - k) {
                            if (random.nextInt(2) == 0){
                                // read existing array element
                                a.get(random.nextInt(a.array.length));
                            } else {
                                // write to existing array element
                                a.set(random.nextInt(a.array.length), new Object());
                            }
                        } else {
                            if (random.nextInt(2) == 0){
                                // read one past end of array
                                a.get(a.array.length);
                            } else {
                                // write to one past end of array
                                a.set(a.array.length, new Object());
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
        long endTimeA = System.currentTimeMillis();

        // create 4 threads to run operations on q1b
        long startTimeB = System.currentTimeMillis();
        Thread[] threadsB = new Thread[4];
        for (int i = 0; i < 4; i++) {
            threadsB[i] = new Thread(new Runnable() {
                @Override 
                public void run() {
                    // m operations
                    for (int i = 0; i < m; i ++) {
                        // 100 - k% chance of reading/writing an existing array element
                        if (random.nextInt(100) + 1 < 100 - k) {
                            if (random.nextInt(2) == 0){
                                // read existing array element
                                b.get(random.nextInt(b.array.get().length));
                            } else {
                                // write to existing array element
                                b.set(random.nextInt(b.array.get().length), new Object());
                            }
                        } else {
                            if (random.nextInt(2) == 0){
                                // read one past end of array
                                b.get(b.array.get().length);
                            } else {
                                // write to one past end of array
                                b.set(b.array.get().length, new Object());
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
        long endTimeB = System.currentTimeMillis();

        System.out.println("Time taken for q1a: " + (endTimeA - startTimeA) + " ms");
        System.out.println("Time taken for q1b: " + (endTimeB - startTimeB) + " ms");
        
        
    }
}
