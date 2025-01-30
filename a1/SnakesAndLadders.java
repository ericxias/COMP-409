package a1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakesAndLadders {
    public static final int BOARD_SIZE = 100;
    private static Cell[] board = new Cell[BOARD_SIZE];
    private static List<int[]> snakes = new ArrayList<>();
    private static List<int[]> ladders = new ArrayList<>();
    private static Random random = new Random();
    private volatile static List<String[]> log = new ArrayList<>();


    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java SnakesAndLadders <k> <j> <s>");
            System.exit(1);
        }

        int k = Integer.parseInt(args[0]);
        int j = Integer.parseInt(args[1]);
        int s = Integer.parseInt(args[2]);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < BOARD_SIZE; i++) {
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
            // ensuring the tail is in a row higher than the head, both are regular cells, not start/end cells, 
            // and not in the same row
            } while (head <= tail || board[head].getType() != Cell.CellType.REGULAR || 
            board[tail].getType() != Cell.CellType.REGULAR || (head/10) == (tail/10) || head == 0 || tail == 99);

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
            // ensuring the top is in a row higher than the bottom, both are regular cells not start/end cells,
            // and not in the same row
            } while (top >= bottom || board[top].getType() != Cell.CellType.REGULAR ||
            board[bottom].getType() != Cell.CellType.REGULAR || (top/10) == (bottom/10) || top == 99 || bottom == 0);

            board[top].setType(Cell.CellType.LADDER);
            board[bottom].setType(Cell.CellType.LADDER);
            board[bottom].setDestination(top);
            ladders.add(new int[]{top, bottom});
            log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Adder ladder " + top + " " + bottom});
        }

        try {
            // print the board: 0 = top left cell, 99 = bottom right cell
            for (int i = 0; i < BOARD_SIZE; i++) {
                System.out.print(i + " ");
                if (board[i].getType() == Cell.CellType.SNAKE) {
                    System.out.print("S" + board[i].getDestination() + " ");
                } else if (board[i].getType() == Cell.CellType.LADDER) {
                    System.out.print("L" + board[i].getDestination() + " ");
                } else {
                    System.out.print("R ");
                }
                if (i % 10 == 9) {
                    System.out.println();
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


            /*
             * Define and start 3 threads such that each thread keeps a log of the operations they perform, timestamps (ms)
             * associated with them. Pre-populate adder log with initial snake/ladder additions.
             * One player thread plays the game, starting at the beginning of the board, and moves according to the dice roll.
             * If arriving at a cell containing a snake tail, moves to the snake head. If arriving at a cell containing a ladder bottom,
             * moves to the ladder top. After each move, it sleeps for 20-50 ms at random. Once it reaches/goes past top left cell, 
             * sleeps for 100 ms and starts again
             */

            Thread player = new Thread(new Runnable() {
                @Override
                public void run() {
                    int position = 99;
                    while (!Thread.currentThread().isInterrupted()) {
                        int diceRoll = random.nextInt(6) + 1;
                        position -= diceRoll;

                        if (position <= 0) {
                            //System.out.println("Player wins");
                            log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Player wins"});
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                break;
                            }
                            position = 99;
                        } else {
                            //System.out.println("Player " + position);
                            log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Player " + position});
                        }

                        if ((board[position].getType() == Cell.CellType.SNAKE || 
                        board[position].getType() == Cell.CellType.LADDER) && board[position].getDestination() != position) {
                            log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Player " + position + " " + board[position].getDestination()});
                            //System.out.println("Player " + position + " " + board[position].getDestination());
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

            /*
             * This Adder thread is in an infinite loop adding snakes or ladders. This thread adds a snake or ladder with 
             * equal probability between 2 random cells on different rows, verifying they are valid end-points
             * (not used by other snakes or ladders and not the starting or ending cell). Once it succeeds, it sleeps for k ms
             */
            Thread adder = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        if (random.nextInt(2) == 0){
                            int head, tail;
                            do {
                                head = random.nextInt(BOARD_SIZE - 1) + 1;
                                tail = random.nextInt(BOARD_SIZE - 1) + 1;
                            } while (head <= tail || board[head].getType() != Cell.CellType.REGULAR || 
                            board[tail].getType() != Cell.CellType.REGULAR || (head/10) == (tail/10) || head == 99 || tail == 0);

                            synchronized (board) {
                                board[head].setType(Cell.CellType.SNAKE);
                                board[tail].setType(Cell.CellType.SNAKE);
                                board[tail].setDestination(head);
                                snakes.add(new int[]{head, tail});
                            }
                            log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Adder snake " + head + " " + tail});
                            //System.out.println("Adder snake " + head + " " + tail);
                        } else {
                            int top, bottom;
                            do {
                                top = random.nextInt(BOARD_SIZE - 1) + 1;
                                bottom = random.nextInt(BOARD_SIZE - 1) + 1;
                            } while (top >= bottom || board[top].getType() != Cell.CellType.REGULAR ||
                            board[bottom].getType() != Cell.CellType.REGULAR || (top/10) == (bottom/10) || top == 0 || bottom == 99);

                            synchronized (board) {
                                board[top].setType(Cell.CellType.LADDER);
                                board[bottom].setType(Cell.CellType.LADDER);
                                board[bottom].setDestination(top);
                                ladders.add(new int[]{top, bottom});
                            }
                            log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Adder ladder " + top + " " + bottom});
                            //System.out.println("Adder ladder " + top + " " + bottom);

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

            /*
             * This Remover thread is an infinite loop removing snakes or ladders. The remover looks for either a snake 
             * or ladder and removes it from the board. Once it succeeds it sleeps for j ms
             */
            Thread remover = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        synchronized (board) {
                            if (random.nextInt(2) == 0 && !snakes.isEmpty()){
                                int[] snake = snakes.get(random.nextInt(snakes.size()));
                                board[snake[0]].setType(Cell.CellType.REGULAR);
                                board[snake[1]].setType(Cell.CellType.REGULAR);
                                board[snake[1]].setDestination(snake[1]);
                                snakes.remove(snake);
                                log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Remover snake " + snake[0] + " " + snake[1]});
                                //System.out.println("Remover snake " + snake[0] + " " + snake[1]);
                            } else if (!ladders.isEmpty()){
                                int[] ladder = ladders.get(random.nextInt(ladders.size()));
                                board[ladder[0]].setType(Cell.CellType.REGULAR);
                                board[ladder[1]].setType(Cell.CellType.REGULAR);
                                board[ladder[1]].setDestination(ladder[1]);
                                ladders.remove(ladder);
                                log.add(new String[]{String.format("%08d", System.currentTimeMillis() - startTime), "Remover ladder " + ladder[0] + " " + ladder[1]});
                                //System.out.println("Remover ladder " + ladder[0] + " " + ladder[1]);
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
            for (int i = 0; i < BOARD_SIZE; i++) {
                System.out.print(i + " ");
                if (board[i].getType() == Cell.CellType.SNAKE) {
                    System.out.print("S" + board[i].getDestination() + " ");
                } else if (board[i].getType() == Cell.CellType.LADDER) {
                    System.out.print("L" + board[i].getDestination() + " ");
                } else {
                    System.out.print("R ");
                }
                if (i % 10 == 9) {
                    System.out.println();
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
