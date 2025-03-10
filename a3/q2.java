package a3;

import java.util.concurrent.Executors;

public class q2 {
    
    public static void main(String [] args){
        if (args.length < 2){
            System.out.println("Usage: java q2.java <n> <t> <s (optional)> ");
            return;
        }

        if (args.length == 3){
            // n = string length, t = number of threads, s = seed
            int n = Integer.parseInt(args[0]);
            int t = Integer.parseInt(args[1]);
            int s = Integer.parseInt(args[2]);
        } else {
            int n = Integer.parseInt(args[0]);
            int t = Integer.parseInt(args[1]);
            int s = (int) System.currentTimeMillis();
        }


    }
}
