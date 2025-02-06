package a2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class q1 {

    public static List<Character> letters = new ArrayList<>();
    public static List<Integer> frequencies = new ArrayList<>();
    public static Random random = new Random();
    public static void main(String[] args){
        
        if (args.length != 3){
            System.out.println("Usage: java q1.java <n> <t> <k>");
            System.exit(1);
        }

        int n = Integer.parseInt(args[0]);
        int t = Integer.parseInt(args[1]);
        int k = Integer.parseInt(args[2]);
        
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
        char[][] grid = new char[n][n];
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
                        grid[i][j] = letters.get(x);
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

    }
}