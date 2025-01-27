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


    public static void main(String[] args) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[i] = new Cell(i, Cell.CellType.REGULAR, -1);
        }

        // create snakes
        for (int i = 0; i < 10; i++){
            int head, tail;
            do {
                // random locations
                head = random.nextInt(BOARD_SIZE - 1) + 1;
                tail = random.nextInt(BOARD_SIZE - 1) + 1;
            // ensuring the tail is in a row higher than the head, both are regular cells, and not in the same row
            } while (head <= tail || board[head].getType() != Cell.CellType.REGULAR || 
            board[tail].getType() != Cell.CellType.REGULAR || (head/10) == (tail/10));

            board[head].setType(Cell.CellType.SNAKE);
            board[head].setDestination(tail);
            snakes.add(new int[]{head, tail});
        }

        // create ladders
        for (int i = 0; i < 10; i++){
            int top, bottom;
            do {
                top = random.nextInt(BOARD_SIZE - 1) + 1;
                bottom = random.nextInt(BOARD_SIZE - 1) + 1;
            // ensuring the top is in a row higher than the bottom, both are regular cells, and not in the same row
            } while (top <= bottom || board[top].getType() != Cell.CellType.REGULAR ||
            board[bottom].getType() != Cell.CellType.REGULAR || (top/10) == (bottom/10));

            board[top].setType(Cell.CellType.LADDER);
            board[top].setDestination(bottom);
            ladders.add(new int[]{top, bottom});
        }
    }




    public static class Cell {
        private int position;
        private CellType type;
        private int destination;

        public Cell(int position, CellType type, int destimation) {
            this.position = position;
            this.type = type;
            this.destination = destimation;
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
