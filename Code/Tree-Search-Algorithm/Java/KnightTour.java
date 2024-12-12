import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class KnightTour {

    // Constants for knight's moves
    private static final int[][] MOVES = {
        {-2, 1}, {-1, 2}, {1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}
    };

    // Class to represent a board position and state
    static class Node {
        Position position;
        boolean[][] visited;
        int[][] board;
        int moveNumber;

        Node(Position position, boolean[][] visited, int[][] board, int moveNumber) {
            this.position = position;
            this.visited = deepCopy(visited);
            this.board = deepCopy(board);
            this.moveNumber = moveNumber;
        }

        private boolean[][] deepCopy(boolean[][] original) {
            boolean[][] copy = new boolean[original.length][original[0].length];
            for (int i = 0; i < original.length; i++) {
                System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
            }
            return copy;
        }

        private int[][] deepCopy(int[][] original) {
            int[][] copy = new int[original.length][original[0].length];
            for (int i = 0; i < original.length; i++) {
                System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
            }
            return copy;
        }
    }

    static class Position {
        int x, y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // Main Tree-Search Framework
    public static String treeSearch(int n, char method, long timeLimit) {
        long startTime = System.currentTimeMillis();

        // Frontier: nodes to be visited
        Deque<Node> frontier = new ArrayDeque<>();

        // Initialize starting node
        boolean[][] visited = new boolean[n][n];
        int[][] board = new int[n][n];
        Position start = new Position(0, 0);
        visited[0][0] = true;
        board[0][0] = 1;
        frontier.add(new Node(start, visited, board, 1));

        // List to store transitions
        List<String> transitions = new ArrayList<>();
        transitions.add("Start: (0, 0)");

        // Nodes expanded counter
        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            // Timeout check
            if (System.currentTimeMillis() - startTime > timeLimit * 1000) {
                return "Timeout.";
            }

            // Choose the node to expand based on the search method
            Node current;
            if (method == 'a') { // Breadth-First Search
                current = frontier.pollFirst();
            } else if (method == 'b') { // Depth-First Search
                current = frontier.pollLast();
            } else { // DFS with Heuristics (c: h1b, d: h2)
                current = selectWithHeuristic(frontier, n, method);
                frontier.remove(current);
            }

            nodesExpanded++;

            // Check if all squares have been visited (goal state)
            if (allVisited(current.visited)) {
                printTour(current.board, transitions, n);
                printFinalBoard(current.board, n);
                return "A solution found.\nNodes Expanded: " + nodesExpanded;
            }

            // Expand the current node
            for (int[] move : MOVES) {
                int newX = current.position.x + move[0];
                int newY = current.position.y + move[1];

                if (isValidMove(newX, newY, n, current.visited)) {
                    boolean[][] newVisited = current.visited;
                    int[][] newBoard = current.board;
                    newVisited[newX][newY] = true;
                    newBoard[newX][newY] = current.moveNumber + 1;

                    frontier.add(new Node(new Position(newX, newY), newVisited, newBoard, current.moveNumber + 1));
                    transitions.add("Move " + (current.moveNumber + 1) + ": (" + current.position.x + ", " + current.position.y + ") -> (" + newX + ", " + newY + ")");
                }
            }
        }

        return "No solution exists.";
    }

    // Heuristic-based selection
    private static Node selectWithHeuristic(Deque<Node> frontier, int n, char method) {
        Node best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Node node : frontier) {
            int score = (method == 'c') ? h1b(node.position, node.visited, n) : h2(node.position, node.visited, n);
            if (score < bestScore) {
                bestScore = score;
                best = node;
            }
        }

        return best;
    }

    // h1b: Warnsdorff's rule
    private static int h1b(Position pos, boolean[][] visited, int n) {
        int count = 0;
        for (int[] move : MOVES) {
            int newX = pos.x + move[0];
            int newY = pos.y + move[1];
            if (isValidMove(newX, newY, n, visited)) {
                count++;
            }
        }
        return count; // Fewer onward moves mean higher priority
    }

    // h2: Enhanced heuristic (Warnsdorff + proximity to corners)
    private static int h2(Position pos, boolean[][] visited, int n) {
        int count = h1b(pos, visited, n); // Use h1b as the base score

        // Calculate proximity to the nearest corner
        int[] cornerDistances = {
            pos.x + pos.y,                             // (0,0)
            pos.x + (n - 1 - pos.y),                   // (0,N-1)
            (n - 1 - pos.x) + pos.y,                   // (N-1,0)
            (n - 1 - pos.x) + (n - 1 - pos.y)          // (N-1,N-1)
        };

        int minDistance = Arrays.stream(cornerDistances).min().orElse(Integer.MAX_VALUE);
        return count + minDistance; // Combine Warnsdorff's rule with proximity
    }

    // Helper function to check if all squares are visited
    private static boolean allVisited(boolean[][] visited) {
        for (boolean[] row : visited) {
            for (boolean cell : row) {
                if (!cell) return false;
            }
        }
        return true;
    }

    // Helper function to check if a move is valid
    private static boolean isValidMove(int x, int y, int n, boolean[][] visited) {
        return x >= 0 && y >= 0 && x < n && y < n && !visited[x][y];
    }

    // Print the tour and save to file
    private static void printTour(int[][] tour, List<String> transitions, int n) {
        try (FileWriter writer = new FileWriter("knights_tour_output.txt")) {
            // Print transitions
            System.out.println("Transitions:");
            writer.write("Transitions:\n");
            for (String transition : transitions) {
                System.out.println(transition);
                writer.write(transition + "\n");
            }

            // Print the final board
            System.out.println("\nFinal Board:");
            writer.write("\nFinal Board:\n");
            for (int[] row : tour) {
                for (int cell : row) {
                    System.out.print(String.format("%2d ", cell));
                    writer.write(String.format("%2d ", cell));
                }
                System.out.println();
                writer.write("\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    // Print the final board directly to the console
    private static void printFinalBoard(int[][] board, int n) {
        System.out.println("\nFinal Board State:");
        System.out.println("+" + "-".repeat(n * 4 - 1) + "+");
        for (int[] row : board) {
            System.out.print("|");
            for (int cell : row) {
                System.out.print(String.format(" %2d |", cell));
            }
            System.out.println();
            System.out.println("+" + "-".repeat(n * 4 - 1) + "+");
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter board size (n): ");
        int n = scanner.nextInt();

        System.out.println("Enter search method (a: BFS, b: DFS, c: DFS with h1b, d: DFS with h2): ");
        char method = scanner.next().charAt(0);

        System.out.println("Enter time limit (in seconds): ");
        long timeLimit = scanner.nextLong();

        String result = treeSearch(n, method, timeLimit);
        System.out.println("Result: " + result);

        scanner.close();
    }
}
