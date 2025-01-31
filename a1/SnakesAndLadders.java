package a1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakesAndLadders {

    // Parameters
    public static final int BOARD_SIZE = 100;
    private static Cell[] board = new Cell[BOARD_SIZE];
    // shared list of snakes and ladders between adder/remover threads
    private static List<int[]> snakes = new ArrayList<>();
    private static List<int[]> ladders = new ArrayList<>();
    private static Random random = new Random();
    // shared log between threads, contains 2 strings, timestamp and the operation conducted, volatile to prevent data races
    private volatile static List<String[]> log = new ArrayList<>();


    public static void main(String[] args) {
        // Check and parse input
        if (args.length != 3) {
            System.out.println("Usage: java SnakesAndLadders <k> <j> <s>");
            System.exit(1);
        }

        int k = Integer.parseInt(args[0]);
        int j = Integer.parseInt(args[1]);
        int s = Integer.parseInt(args[2]);
        long startTime = System.currentTimeMillis();

        // create board with 100 cells
        for (int i = 0; i < 100; i++) {
            board[i] = new Cell(i, Cell.CellType.REGULAR, -1);
            board[i].setDestination(i);
        }

        // create snakes
        for (int i = 0; i < 10; i++){
            int head, tail;
            do {
                // random locations
                head = random.nextInt(BOARD_SIZE - 1) + 1;
                tail = random.nextInt(BOARD_SIZE - 1) + 1;
            // ensuring the tail is in a row higher than the head, both are regular cells, not start/end cells, and not in the same row
            } while (head >= tail || board[head].getType() != Cell.CellType.REGULAR || 
            board[tail].getType() != Cell.CellType.REGULAR || (head/10) == (tail/10) || head == 0 || tail == 99);

            // valid snake, set the head and tail cells, destination of the tail cell, and add to snakes list and adder log
            board[head].setType(Cell.CellType.SNAKE);
            board[tail].setType(Cell.CellType.SNAKE);
            board[tail].setDestination(head);
            snakes.add(new int[]{head, tail});
            log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Adder snake " + head + " " + tail});
        }

        // create ladders
        for (int i = 0; i < 9; i++){
            int top, bottom;
            do {
                top = random.nextInt(BOARD_SIZE - 1) + 1;
                bottom = random.nextInt(BOARD_SIZE - 1) + 1;
            // ensuring the top is in a row higher than the bottom, both are regular cells not start/end cells, and not in the same row
            } while (top <= bottom || board[top].getType() != Cell.CellType.REGULAR ||
            board[bottom].getType() != Cell.CellType.REGULAR || (top/10) == (bottom/10) || top == 99 || bottom == 0);

            // valid ladder, set the top and bottom cells, destination of the bottom cell, and add to ladders list and adder log
            board[top].setType(Cell.CellType.LADDER);
            board[bottom].setType(Cell.CellType.LADDER);
            board[bottom].setDestination(top);
            ladders.add(new int[]{top, bottom});
            log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Adder ladder " + top + " " + bottom});
        }

        try {
            /* 
            // debugging print the board: 0 = bottom left cell, 99 = top right cell
            for (int i = 9; i >= 0; i--) {
                for (int x = 0; x < 10; x++ ){
                    System.out.print(i * 10 + x + " ");
                    if (board[i * 10 + x].getType() == Cell.CellType.SNAKE) {
                        System.out.print("S" + board[i * 10 + x].getDestination() + " ");
                    } else if (board[i * 10 + x].getType() == Cell.CellType.LADDER) {
                        System.out.print("L" + board[i * 10 + x].getDestination() + " ");
                    } else {
                        System.out.print("R ");
                    }
                    if ((i * 10 + x) % 10 == 9) {
                        System.out.println();
                    }
                }
            }

            // print the snakes
            System.out.println("Snakes:");
            for (int[] snake : snakes) {
                System.out.println(snake[0] + " " + snake[1]);
            }

            // print the ladders
            System.out.println("Ladders:");
            for (int[] ladder : ladders) {
                System.out.println(ladder[0] + " " + ladder[1]);
            } */

            // Player thread
            Thread player = new Thread(new Runnable() {
                @Override
                public void run() {
                    // position = bottom left cell = 0
                    int position = 0;

                    while (!Thread.currentThread().isInterrupted()) {
                        // roll dice and move player
                        int diceRoll = random.nextInt(6) + 1;
                        position += diceRoll;

                        // if player reaches or passes top right cell, player wins
                        if (position >= 99) {
                            //System.out.println("Player wins");
                            log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Player wins"});
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                break;
                            }
                            position = 0;
                        } else {
                            log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Player " + position});
                        }

                        // if player arrives at snake tail or ladder bottom, move to snake head or ladder top
                        // only cells with destination != position are snake tails and ladder bottoms
                        if ((board[position].getType() == Cell.CellType.SNAKE || 
                        board[position].getType() == Cell.CellType.LADDER) && board[position].getDestination() != position) {
                            log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Player " + position + " " + board[position].getDestination()});
                            position = board[position].getDestination();
                        }
                        
                        try {
                            Thread.sleep(random.nextInt(31) + 20);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            });

            // Adder thread
            Thread adder = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        // randomly add a snake or ladder
                        if (random.nextInt(2) == 0){
                            int head, tail;

                            // ensuring the tail is in a row higher than the head, both are regular cells, not start/end cells, and not in the same row
                            do {
                                head = random.nextInt(BOARD_SIZE - 1) + 1;
                                tail = random.nextInt(BOARD_SIZE - 1) + 1;
                            } while (head >= tail || board[head].getType() != Cell.CellType.REGULAR || 
                            board[tail].getType() != Cell.CellType.REGULAR || (head/10) == (tail/10) || head == 99 || tail == 0);

                            // valid snake, set the head and tail cells, destination of the tail cell, and add to snakes list and adder log, synchronized to prevent data races
                            synchronized (board) {
                                board[head].setType(Cell.CellType.SNAKE);
                                board[tail].setType(Cell.CellType.SNAKE);
                                board[tail].setDestination(head);
                                snakes.add(new int[]{head, tail});
                            }
                            log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Adder snake " + head + " " + tail});
                            
                        } else {
                            int top, bottom;

                            // ensuring the top is in a row higher than the bottom, both are regular cells not start/end cells, and not in the same row
                            do {
                                top = random.nextInt(BOARD_SIZE - 1) + 1;
                                bottom = random.nextInt(BOARD_SIZE - 1) + 1;
                            } while (top <= bottom || board[top].getType() != Cell.CellType.REGULAR ||
                            board[bottom].getType() != Cell.CellType.REGULAR || (top/10) == (bottom/10) || top == 0 || bottom == 99);

                            // valid ladder, set the top and bottom cells, destination of the bottom cell, and add to ladders list and adder log, synchronized to prevent data races
                            synchronized (board) {
                                board[top].setType(Cell.CellType.LADDER);
                                board[bottom].setType(Cell.CellType.LADDER);
                                board[bottom].setDestination(top);
                                ladders.add(new int[]{top, bottom});
                            }
                            log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Adder ladder " + top + " " + bottom});

                            // debugging: print the snakes and ladders
                            /*System.out.println("Snakes:");
                            for (int[] snake : snakes) {
                                System.out.println(snake[0] + " " + snake[1]);
                            }

                            System.out.println("Ladders:");
                            for (int[] ladder : ladders) {
                                System.out.println(ladder[0] + " " + ladder[1]);
                            } */
                        }
                        try {
                            Thread.sleep(k);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            });

            // Remover Thread
            Thread remover = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {

                        // randomly remove a snake or ladder, synchronized to prevent data races
                        synchronized (board) {
                            if (random.nextInt(2) == 0 && !snakes.isEmpty()){
                                // remove a random snake and set to regular cells
                                int[] snake = snakes.get(random.nextInt(snakes.size()));
                                board[snake[0]].setType(Cell.CellType.REGULAR);
                                board[snake[1]].setType(Cell.CellType.REGULAR);
                                board[snake[1]].setDestination(snake[1]);
                                snakes.remove(snake);
                                log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Remover snake " + snake[0] + " " + snake[1]});

                            } else if (!ladders.isEmpty()){
                                // remove a random ladder and set to regular cells
                                int[] ladder = ladders.get(random.nextInt(ladders.size()));
                                board[ladder[0]].setType(Cell.CellType.REGULAR);
                                board[ladder[1]].setType(Cell.CellType.REGULAR);
                                board[ladder[1]].setDestination(ladder[1]);
                                ladders.remove(ladder);
                                log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Remover ladder " + ladder[0] + " " + ladder[1]});
                            }
                            
                            // debugging: print the snakes and ladders
                            /*System.out.println("Snakes:");
                            for (int[] snake : snakes) {
                                System.out.println(snake[0] + " " + snake[1]);
                            }

                            System.out.println("Ladders:");
                            for (int[] ladder : ladders) {
                                System.out.println(ladder[0] + " " + ladder[1]);
                            }*/
                        }


                        try {
                            Thread.sleep(j);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            });

            player.start();
            adder.start();
            remover.start();

            // main thread sleeps for s seconds, then stops the simulation
            try {
                // convert ms to s
                Thread.sleep(s * 1000);
                player.interrupt();
                adder.interrupt();
                remover.interrupt();
                player.join();
                adder.join();
                remover.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            

            // debugging: print final state of the board
            // print the board
            for (int i = 9; i >= 0; i--) {
                for (int x = 0; x < 10; x++ ){
                    System.out.print(i * 10 + x + " ");
                    if (board[i * 10 + x].getType() == Cell.CellType.SNAKE) {
                        System.out.print("S" + board[i * 10 + x].getDestination() + " ");
                    } else if (board[i * 10 + x].getType() == Cell.CellType.LADDER) {
                        System.out.print("L" + board[i * 10 + x].getDestination() + " ");
                    } else {
                        System.out.print("R ");
                    }
                    if ((i * 10 + x) % 10 == 9) {
                        System.out.println();
                    }
                }
            }

            // print the snakes
            System.out.println("Snakes:");
            for (int[] snake : snakes) {
                System.out.println(snake[0] + " " + snake[1]);
            }

            // print the ladders
            System.out.println("Ladders:");
            for (int[] ladder : ladders) {
                System.out.println(ladder[0] + " " + ladder[1]);
            }

            // sort log by timestamp and print log
            log.sort((a, b) -> Long.compare(Long.parseLong(a[0]), Long.parseLong(b[0])));
            for (String[] entry: log) {
                System.out.println(entry[0] + " " + entry[1]);
            }

             

        } catch (Exception e) {
            System.out.println("ERROR " + e);
            e.printStackTrace();
        }
    }



    // Cell class
    public static class Cell {
        private int position;
        private CellType type;
        private int destination;

        public Cell(int position, CellType type, int destination) {
            this.position = position;
            this.type = type;
            this.destination = destination;
        }

        public int getPosition() {
            return position;
        }

        public CellType getType() {
            return type;
        }

        public int getDestination() {
            return destination;
        }

        public void setDestination(int destination) {
            this.destination = destination;
        }

        public void setType(CellType type) {
            this.type = type;
        }

        enum CellType {
            REGULAR,
            SNAKE,
            LADDER
        }
    }
}
 