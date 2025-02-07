package a2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

public class q1 {

    public static List<Character> letters = new ArrayList<>();
    public static List<Integer> frequencies = new ArrayList<>();
    public static List<String> dictionary = new ArrayList<>();
    public static Cell[][] grid;
    public static void main(String[] args){
        
        if (args.length != 4){
            System.out.println("Usage: java q1.java <s> <n> <t> <k>");
            System.exit(1);
        }

        int s = Integer.parseInt(args[0]);
        int n = Integer.parseInt(args[1]);
        int t = Integer.parseInt(args[2]);
        int k = Integer.parseInt(args[3]);
        
        // Read and add letters and frequencies to the lists from freq.txt
        File file = new File("freq.txt");
        try (Scanner reader = new Scanner(file)){
            while (reader.hasNextLine()){
                String data = reader.nextLine().trim();
                if (!data.isEmpty()){
                    String[] parts = data.split("\\s+");
                    letters.add(parts[0].charAt(0));
                    frequencies.add(Integer.parseInt(parts[1]));
                };
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Create and draw the grid
        grid = new Cell[n][n];
        Random random = new Random(s);
        for (int i = 0; i < 5; i++){
            for (int j = 0; j < n; j++){
                // calculate total frequency, generate random letter based on frequency, and assign letter to grid
                int totalFrequency = 0;
                for (int frequency: frequencies) {
                    totalFrequency += frequency;
                }
                int randomLetter = random.nextInt(totalFrequency);
                int frequencySum = 0;
                for (int x = 0; x < letters.size(); x++){
                    frequencySum += frequencies.get(x);
                    if (randomLetter < frequencySum){
                        grid[i][j] = new Cell(letters.get(x));
                        break;
                    }
                }
            }
        }

        // Draw grid
        for (int i = 0; i < n; i ++){
            for (int j = 0; j < n; j++){
                System.out.print(grid[i][j]);
            }
            System.out.println();
        }

        // Read and add words to the dictionary from dictionary.txt
        File file2 = new File("dictionary.txt");
        try (Scanner reader = new Scanner(file2)){
            while (reader.hasNextLine()){
                String data = reader.nextLine().trim();
                if (!data.isEmpty()){
                    dictionary.add(data);
                };
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /*
         * launch t threads to conduct a random search for words. Each thread picks a random starting cell,
         * and pre-selects a sequence of up to 7 random moves by incrementally creating a plan of moves to make.
         * Move choices are random, but the sequence must avoid visiting the same cell more than once. This
         * may limit the feasible length of the sequence, although a 2-move (3-letter) sequence is always possible (n > 1).
         */

        // Create t threads
        Thread[] threads = new Thread[t];
        for (int i = 0; i < t; i++){
            threads[i] = new Thread(new Runnable() {
                @Override
                public void run(){
                    Random random = new Random();
                    // each thread tests k starting cells
                    for (int i = 0; i < k; i++) {
                        // random starting cell
                        int x = random.nextInt(n);
                        int y = random.nextInt(n);
                        // list to store moves
                        List<int[]> moves = new ArrayList<>();
                        moves.add(new int[]{x, y});

                        // pre-select a sequence of up to 7 random moves
                        for (int j = 0; j < 6; j++){
                            int[] current = moves.get(moves.size() - 1);
                            int currx = current[0];
                            int curry = current[1];
                            int[] nextMove = getNextMove(currx, curry, n, moves);
                            moves.add(nextMove);
                        }
                        
                    }
                }
            });
            threads[i].start();
        }

        // join to wait for all threads to finish
        for (int i = 0; i < t; i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        
    }

    public static int[] getNextMove(int x, int y, int n, List<int[]> moves){
        // get all possible next moves, check if they are valid, and return a random next move
        Random random = new Random();
        List<int[]> possibleMoves = new ArrayList<>();
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {-1, -1}, {-1, 1}, {1, 1}, {1, -1}};
        for (int[] direction: directions){
            int nextx = x + direction[0];
            int nexty = y + direction[1];
            if (nextx >= 0 && nextx < n && nexty >= 0 && nexty < n && !moves.contains(new int[]{nextx, nexty})){
                possibleMoves.add(new int[]{nextx, nexty});
            }
        }
        return possibleMoves.get(random.nextInt(possibleMoves.size()));
    }

    // Cell class with locks
    public static class Cell {
        private final char letter;
        // words the cell contributed to
        private final List<String> words;
        private final ReentrantLock lock;

        public Cell(char letter){
            this.letter = letter;
            this.words = new ArrayList<>();
            this.lock = new ReentrantLock();
        }

        public char getLetter(){
            return this.letter;
        }

        public List<String> getWords(){
            return this.words;
        }

        public ReentrantLock getLock(){
            return this.lock;
        }

        public void addWord(String word){
            this.words.add(word);
        }
    }
}