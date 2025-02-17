package a2;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.List;
import java.util.ArrayList;

public class q2 {
    public static void main(String[] args){
        
        if (args.length != 2 || Integer.parseInt(args[0]) <= 3 || Integer.parseInt(args[1]) < 0 || Integer.parseInt(args[1]) > 100){
            System.out.println("Usage: java q2.java <k > 3> <0 < q < 100>");
            System.exit(1);
        }

        int k = Integer.parseInt(args[0]);
        int q = Integer.parseInt(args[1]);

        try {
            University university = new University(k, q);
            
            // k TAs, 1 professor, 5 students
            Thread[] threads = new Thread[k+6];

            // Professor Thread
            Thread professorThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    // thread keeps acting until it gets interrupted when all students arrive
                    while (!Thread.currentThread().isInterrupted()){
                        try {
                            // continuously checks if 3 TA's are available to answer questions
                            university.answerQuestions();
                            if (university.allStudentsArrived){
                                break;
                            }
                        } catch (InterruptedException e) {
                            // when student interrupts the professor thread, the professor thread will wake up all students (signalling all) then thread stops
                            university.lock.lock();
                            try {
                                System.out.println("a grad student interrupts a TA session");
                                System.out.println("P wakes their grad students");
                                university.studentCondition.signalAll();
                                System.out.println("all grad students have been woken");
                            } finally {
                                university.lock.unlock();
                            }
                            break;
                        }
                    }
                }
            });
            threads[0] = professorThread;
            threads[0].start();
            
            // k TA Threads
            for (int i = 0; i < k; i++) {
                final int taId = i;
                threads[i+1] = new Thread(new Runnable(){
                    Random random = new Random();
                    @Override
                    public void run() {
                        // thread keeps acting until it gets interrupted when all students arrive
                        while(!Thread.currentThread().isInterrupted()){
                            try {
                                // TA sleeps for 1 second then checks if it has a question with q probability
                                Thread.sleep(1000);
                                if (random.nextInt(100) < university.q && !university.allStudentsArrived){
                                    System.out.println("a TA " + taId + " comes up with a question");
                                    university.taQuestion(taId, Thread.currentThread());
                                
                                // if all students have arrived, the TA thread will be interrupted    
                                } else if (university.allStudentsArrived){
                                    Thread.currentThread().interrupt();
                                    Thread.sleep(0);
                                }
            
                            
                            } catch (InterruptedException e) {
                                // when interrupted, TA thread stops
                                break;
                            }
                        }
                    }
                });
                threads[i+1].start();
            }

            // 5 Student Threads
            for (int i = 0; i < 5; i++) {
                final int studentId = i;
                threads[i+k+1] = new Thread(new Runnable() {
                    Random random = new Random();
                    @Override
                    public void run() {
                        // thread keeps acting until it gets interrupted when all students arrive
                        while (!Thread.currentThread().isInterrupted() && !university.allStudentsArrived){
                            try {
                                // sleeps for random time between 10 and 60 seconds then arrives
                                Thread.sleep(random.nextInt(50000) + 10000);
                                System.out.println("a grad student " + studentId + " arrives");
                                university.studentArrives(professorThread, Thread.currentThread());
                            } catch (InterruptedException e) {
                                // when interrupted, student thread stops
                                break;
                            }
                        }
                    }
                });
                threads[i+k+1].start();
            }

            // join all threads
            for (int i = 0; i < k + 6; i++) {
                threads[i].join();
            }
            
            System.out.println("All threads have finished");
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Base of the monitor, contains the lock and conditions and shared variables
    public static class University {
        private final Lock lock = new ReentrantLock();
        // TA condition: wait for 3 TAs to be available
        private final Condition taCondition = lock.newCondition();
        // Student condition: wait for all students to arrive
        private final Condition studentCondition = lock.newCondition();
        private int k;
        private int q;
        // counter for TAs with questions
        private int taCount = 0;
        // counter for students arrived
        private int studentCount = 0;
        private boolean allStudentsArrived = false;
        // list of current TAs with questions
        private final List<Integer> currentTAs = new ArrayList<>();

        public University(int k, int q){
            this.k = k;
            this.q = q;
        }

        public void taQuestion(int id, Thread taThread) throws InterruptedException{
            lock.lock();
            try {
                // add TA to the list of TAs with questions, increment counter
                currentTAs.add(id);
                taCount++;
                while (taCount < 3) {
                    // check if all students have arrived, if so, interrupt TA thread, otherwise wait
                    if (allStudentsArrived){
                        taThread.interrupt();
                    }
                    taCondition.await();
                } 

                // if 3 TAs are available, signal 2 waiting TAs for questions to be answered
                taCondition.signal();
                taCondition.signal();
                
            } finally {
                lock.unlock();
            }
            
        }

        public void answerQuestions() throws InterruptedException{
            lock.lock();
            try {
                // wait for 3 TAs to be available
                while (taCount < 3 && !allStudentsArrived){
                    taCondition.await();
                }

                // if all students have arrived, TA session ends, check interrupt flag with sleep(0) for thread to be interrupted
                if (allStudentsArrived) {
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e){
                        throw e;
                    }
                } else {
                    // answer questions of 3 TAs, first 3 TAs in the list
                    System.out.println("a group of TAs starts to be seen by P " + currentTAs.get(0) + " " + currentTAs.get(1) + " " + currentTAs.get(2));
                    try {
                        // takes 0.5s to answer questions
                        Thread.sleep(500);
                        System.out.println("a group of TAs finishes to be seen by P");
                    } catch (InterruptedException e){
                        throw e;
                    }
                    
                    // remove answered TAs from the list, decrement counter
                    for (int i = 0; i < 3; i++){
                        if (currentTAs.size() == 0){
                            break;
                        }
                        currentTAs.remove(0);
                    }
                    taCount -= 3;
                }
                
            } finally {
                lock.unlock();
            }
        }

        public void studentArrives(Thread professorThread, Thread studentThread) throws InterruptedException{
            lock.lock();
            try {
                // increment student counter, if less than 5, wait for other students
                studentCount++;
                if (studentCount < 5){
                    studentCondition.await();
                }
                // if all students have arrived, interrupt professor thread, student thread, wake up all TAs, will interupt themselves
                if (studentCount == 5){
                    allStudentsArrived = true;
                    professorThread.interrupt();
                    studentThread.interrupt();
                    taCondition.signalAll();
                    studentCount++;

                } else if (studentCount > 5){
                    studentThread.interrupt();
                }
            } finally {
                lock.unlock();
            }
        }
    }
}

    