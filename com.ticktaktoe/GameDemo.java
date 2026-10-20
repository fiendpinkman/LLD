
import java.util.*;

public class GameDemo {

    // 1. Domain types
    enum Symbol { X, O, A, B }

    enum Status { IN_PROGRESS, WON, DRAW}

    static final class Player {
        final String name;
        final Symbol symbol;

        Player(String name, Symbol symbol) {
            if (name == null || name.trim().isEmpty()
                    || symbol == null) {
                throw new IllegalArgumentException("Invalid player");
            }
            this.name = name;
            this.symbol = symbol;
        }
    }

    // 2. Board: owns cells, not turns
    static final class Board {
        private final Symbol[][] cells;
        private int occupied;

        Board(int size) {
            if (size < 3) { throw new IllegalArgumentException("Board must be at least 3x3"); }
            cells = new Symbol[size][size];
        }

        int size() { return cells.length; }

        boolean inside(int row, int col) { return row >= 0 && row < size() && col >= 0 && col < size(); }

        Symbol at(int row, int col) {
            if (!inside(row, col)) {
                throw new IllegalArgumentException("Out of bounds");
            }
            return cells[row][col];
        }

        boolean canPlace(int row, int col) {
            return inside(row, col) && cells[row][col] == null;
        }

        void place(int row, int col, Symbol symbol) {
            if (symbol == null || !canPlace(row, col)) {
                throw new IllegalArgumentException("Invalid move");
            }
            cells[row][col] = symbol;
            occupied++;
        }

        boolean isFull() { return occupied == size() * size(); }
    }

    // 3. Strategy: knows how to detect a winner
    interface WinStrategy {
        boolean hasWon(Board board, int row, int col, Symbol symbol, int winningLength);
    }

    static final class ConsecutiveWinStrategy implements WinStrategy {

        private static final int[][] DIRECTIONS = { {0, 1}, {1, 0}, {1, 1}, {1, -1} };

        @Override
        public boolean hasWon(Board board, int row, int col, Symbol symbol, int k) {

            for (int[] direction : DIRECTIONS) {
                int dr = direction[0];
                int dc = direction[1];

                int total = 1 + count(board, row, col, dr, dc, symbol, k) + count(board, row, col, -dr, -dc, symbol, k);

                if (total >= k) {
                    return true;
                }
            }
            return false;
        }

        private int count(Board board, int row, int col, int dr, int dc, Symbol symbol, int k) {

            int matches = 0;

            for (int step = 1; step < k; step++) {
                int r = row + step * dr;
                int c = col + step * dc;

                if (!board.inside(r, c) || board.at(r, c) != symbol) {
                    break;
                }
                matches++;
            }

            return matches;
        }
    }

    // 4. Immutable result
    static final class MoveResult {
        final boolean accepted;
        final Status status;
        final String message;

        MoveResult(boolean accepted, Status status, String message) {
            this.accepted = accepted;
            this.status = status;
            this.message = message;
        }
    }

    // 5. Service interface
    interface GameActions {
        MoveResult play(int row, int col);
    }

    // 6. Game: the referee
    static final class Game implements GameActions {

        private final Board board;
        private final List<Player> players;
        private final WinStrategy winStrategy;
        private final int winningLength;

        private int currentPlayerIndex;
        private Status status = Status.IN_PROGRESS;

        Game(int size, int winningLength, List<Player> players, WinStrategy winStrategy) {

            if (winningLength < 3 || winningLength > size) { throw new IllegalArgumentException("Invalid winning length");}
            
            if (players == null || players.size() < 2 || players.size() > Symbol.values().length) {
                throw new IllegalArgumentException("Need 2 to 4 players");
            }

            if (winStrategy == null) { throw new IllegalArgumentException("Missing win strategy"); }

            Set<Symbol> usedSymbols = EnumSet.noneOf(Symbol.class);

            for (Player player : players) {
                if (player == null || !usedSymbols.add(player.symbol)) { 
                    throw new IllegalArgumentException("Players need distinct symbols");
                }
            }

            this.board = new Board(size);
            this.winningLength = winningLength;
            this.players = Collections.unmodifiableList(new ArrayList<>(players));
            this.winStrategy = winStrategy;
        }

        @Override
        public MoveResult play(int row, int col) {

            if (status != Status.IN_PROGRESS) {
                return new MoveResult(false, status, "Game already finished");
            }

            if (!board.canPlace(row, col)) {
                return new MoveResult(false, status, "Cell unavailable or out of bounds");
            }

            Player active = players.get(currentPlayerIndex);

            board.place(row, col, active.symbol);

            if (winStrategy.hasWon(board, row, col, active.symbol, winningLength)) {
                status = Status.WON;
                return new MoveResult(true, status, active.name + " wins!");
            }

            if (board.isFull()) {
                status = Status.DRAW;
                return new MoveResult(true, status, "Draw!");
            }

            // Only valid, non-terminal moves advance the turn.
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

            return new MoveResult(true, status, "Next: " + players.get(currentPlayerIndex).name);
        }

        int boardSize() { return board.size(); }

        Symbol symbolAt(int row, int col) { return board.at(row, col); }

        Player currentPlayer() { return players.get(currentPlayerIndex); }
    }

    // 7. Presentation stays outside Board
    static void printBoard(Game game) {
        for (int row = 0; row < game.boardSize(); row++) {
            for (int col = 0; col < game.boardSize(); col++) {
                Symbol symbol = game.symbolAt(row, col);
                System.out.print((symbol == null ? "." : symbol.name()) + " ");
            }
            System.out.println();
        }
    }

    // 8. Execute a sample game
    public static void main(String[] args) {

        Game game = new Game(
            3,
            3,
            Arrays.asList(
                new Player("Alice", Symbol.X),
                new Player("Bob", Symbol.O)
            ),
            new ConsecutiveWinStrategy()
        );

        int[][] moves = {
            {0, 0},
            {1, 0},
            {0, 1},
            {1, 1},
            {0, 2}
        };

        for (int[] move : moves) {
            MoveResult result =
                game.play(move[0], move[1]);

            System.out.println(result.message);
        }

        printBoard(game);
    }
}
