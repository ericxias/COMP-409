package a3;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class q2 {
    private static volatile boolean finalb = true;
    private static volatile int finalf = 0;
    private static volatile int finalm = Integer.MAX_VALUE;
    
    public static void main(String [] args){
        int n = 0;
        int t = 0;
        int s = 0;

        if (args.length < 2){
            System.out.println("Usage: java q2.java <n> <t> <s (optional)> ");
            return;
        }

        if (args.length == 3){
            // n = string length, t = number of threads, s = seed
            n = Integer.parseInt(args[0]);
            t = Integer.parseInt(args[1]);
            s = Integer.parseInt(args[2]);
        } else {
            // seed from current time
            n = Integer.parseInt(args[0]);
            t = Integer.parseInt(args[1]);
            s = (int) System.currentTimeMillis();
        }

        // generate initial array of characters
        char[] array = Bracket.construct(n,s);
        //System.out.println(array);

        // track start time for output
        long startTime = System.currentTimeMillis();
        // create a new thread pool
        ExecutorService executor = Executors.newFixedThreadPool(t);

        for (int i = 0; i < t; i++){
            // starting index of the substring to be parsed through by each thread
            // if last thread that deals with the remainder of the string, endIndex = n, else endIndex = startIndex + n/t
            int startIndex = i * (n/t);
            int endIndex;
            if (i == t - 1){
                endIndex = n;
            } else {
                endIndex = startIndex + (n/t);
            }

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    // b = boolean for properly matched brackets, f = counter result of substring, m = minimum counter value of substring
                    boolean b = true;
                    int f = 0;
                    int m = Integer.MAX_VALUE;

                    // parse through the substring, incrementing f for each opening bracket, decrementing for each closing bracket
                    for (int j = startIndex; j < endIndex; j++){
                        if (array[j] == '['){
                            f++;
                        } else if (array[j] == ']'){
                            f--;
                        }
                        m = Math.min(m, f);
                        // if f is not properly matched or the m is negative, set b to false
                        if (f != 0 || m < 0){
                            b = false;
                        } else {
                            b = true;
                        }
                    }

                    // Compute final b, f, m values with equation from assignment
                    synchronized (q2.class){
                        finalb = (finalb && b) || ((finalf + f == 0) && (finalm >= 0) && (finalf + m >= 0));
                        finalf += f;
                        finalm = Math.min(finalm, finalf + m);
                    }
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            // spin until all threads are done
        }
        long endTime = System.currentTimeMillis();

        System.out.println("Time: " + (endTime - startTime) + "ms");
        System.out.println(finalb + " " + Bracket.verify());

    }
}
