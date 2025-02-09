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
                System.out.print(grid[i][j].getLetter());
            }
            System.out.println();
        }

        // Read and add words to the dictionary from dictionary.txt
        File file2 = new File("dict.txt");
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
                        System.out.println(x + " " + y + " " + grid[x][y].getLetter());

                        // pre-select a sequence of up to 7 random moves
                        for (int j = 0; j < 7; j++){
                            int[] current = moves.get(moves.size() - 1);
                            int currx = current[0];
                            int curry = current[1];
                            int[] nextMove = getNextMove(currx, curry, n, moves);
                            moves.add(nextMove);
                            System.out.println(nextMove[0] + " " + nextMove[1] + " " + grid[nextMove[0]][nextMove[1]].getLetter());
                        }
                        
                        // check if sequence forms words (3 letters or more)
                        for (int j = 3; j <= moves.size(); j++){
                            String word = "";
                            synchronized (grid){
                                try {
                                    for (int k = 0; k < j; k++){
                                
                                        int[] cell = moves.get(k);
                                        word += grid[cell[0]][cell[1]].getLetter();

                                        System.out.println(word);
                                        grid[cell[0]][cell[1]].getLock().lock();
                                        if (dictionary.contains(word.toLowerCase())){
                                            System.out.println("Found word: " + word + grid[x][y].getWords());
                                            if (!grid[x][y].getWords().contains(word)){
                                                grid[x][y].addWord(word);
                                            }
                                            for (int z = 0; z < k + 1; z++  ){
                                                int[] wordCell = moves.get(z);
                                                if (!grid[wordCell[0]][wordCell[1]].getWords().contains(word)){
                                                    grid[wordCell[0]][wordCell[1]].addWord(word);
                                                }
                                            }
                                        }
                                    }
                           
                                    } finally {
                                        for (int z = 0; z < j; z ++){
                                            int[] wordCell = moves.get(z);
                                            grid[wordCell[0]][wordCell[1]].getLock().unlock();
                                        }
                                    }
                                }
                            }
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
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

        // iterate through all cells and print coordinates, and a space sperated list of words the cell contributed to
        for (int i = 0; i < n; i++){
            for (int j = 0; j < n; j++){
                System.out.print(i + " " + j + " ");
                List<String> words = grid[i][j].getWords();
                for (int x = 0; x < words.size(); x++){
                    System.out.print(words.get(x));
                    if (x != words.size() - 1){
                        System.out.print(" ");
                    }
                }
                System.out.println();
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