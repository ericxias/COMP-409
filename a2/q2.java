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
            System.out.println("Usage: java q2.java <k > 3> <0 < q < 100");
            System.exit(1);
        }

        int k = Integer.parseInt(args[0]);
        int q = Integer.parseInt(args[1]);

        try {
            University university = new University(k, q);
            Thread professorThread = new Thread(new Professor(university));
            professorThread.start();

            for (int i = 0; i < k; i++) {
                new Thread(new TA(i, university)).start();
            }

            for (int i = 0; i < 5; i++) {
                new Thread(new Student(i, university, professorThread)).start();
            }

            professorThread.join();
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class University {
        private final Lock lock = new ReentrantLock();
        private final Condition taCondition = lock.newCondition();
        private final Condition studentCondition = lock.newCondition();
        private int k;
        private int q;
        private int taCount = 0;
        private int studentCount = 0;
        private boolean allStudentsArrived = false;
        private final List<Integer> currentTAs = new ArrayList<>();

        public University(int k, int q){
            this.k = k;
            this.q = q;
        }

        public void taQuestion(int id, Thread taThread) throws InterruptedException{
            lock.lock();
            try {
                taCount++;
                currentTAs.add(id);
                if (allStudentsArrived){
                    taThread.interrupt();
                }
                if (taCount < 3) {
                    taCondition.await();
                } else {
                    taCondition.signal();
                    taCondition.signal();
                }
            } finally {
                lock.unlock();
            }
            
        }

        public void answerQuestions() throws InterruptedException{
            lock.lock();
            try{
                while (taCount < 3 && !allStudentsArrived){
                    taCondition.await();
                }
                System.out.println("a group of TAs starts to be seen by P" + currentTAs.get(0) + " " + currentTAs.get(1) + " " + currentTAs.get(2));

                Thread.sleep(500);
                System.out.println("a group of TAs finishes to be seen by P");
                for (int i = 0; i < 3; i++){
                    if (currentTAs.size() == 0){
                        break;
                    }
                    currentTAs.remove(0);
                }
                taCount -= 3;
            } finally {
                lock.unlock();
            }
        }

        public void studentArrives(Thread professorThread, Thread studentThread) throws InterruptedException{
            lock.lock();
            try {
                studentCount++;
                if (studentCount < 5){
                    studentCondition.await();
                }
                if (studentCount == 5){
                    allStudentsArrived = true;
                    System.out.println("a grad student interrupts a TA session");
                    professorThread.interrupt();
                    studentThread.interrupt();
                }
                studentCondition.await();
            } finally {
                lock.unlock();
            }
        }
    }

    // Professor Thread
    public static class Professor implements Runnable {
        private final University university;

        public Professor(University university){
            this.university = university;
            
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                try {
                    university.answerQuestions();
                    if (university.allStudentsArrived){
                        break;
                    }
                } catch (InterruptedException e) {
                    System.out.println("P wakes their grad students");
                    university.lock.lock();
                    try {
                        university.studentCondition.signalAll();
                        System.out.println("all grad students have been woken");
                    } finally {
                        university.lock.unlock();   
                    }
                }
            }
        }
    }

    // TA Thread
    public static class TA implements Runnable {
        private final int id;
        private final University university;
        private final Random random = new Random();

        public TA(int id, University university){
            this.id = id;
            this.university = university;
        }

        @Override
        public void run() {
            while(!Thread.currentThread().isInterrupted()){
                try {
                if (university.allStudentsArrived){
                    Thread.currentThread().interrupt();
                }
                Thread.sleep(1000);
                if (random.nextInt(100) < university.q){
                    System.out.println("a TA " + id + " comes up with a question");
                    university.taQuestion(id, Thread.currentThread());
                }
                
                } catch (InterruptedException e) {
                    System.out.println("a TA " + id + " is interrupted");
                    break;
                }
            }
            
        
        }
    

    }

    // Student Thread
    public static class Student implements Runnable {
        private final int id;
        private final University university;
        private final Thread professorThread;
        private final Random random = new Random();

        public Student(int id, University university, Thread professorThread){
            this.id = id;
            this.university = university;
            this.professorThread = professorThread;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                try {
                    Thread.sleep(random.nextInt(50000) + 10000);
                    System.out.println("a grad student " + id + " arrives");
                    university.studentArrives(professorThread, Thread.currentThread());
                } catch (InterruptedException e) {
                    System.out.println("a grad student " + id + " is interrupted");
                    break;
                }
            }
        }
    }
        
}
