import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class KnightTour {

    // Constants for knight's moves
    private static final int[][] MOVES = {
        {-2, 1}, {-1, 2}, {1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}
    };

    // Class to represent a board position
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
        Deque<Position> frontier = new ArrayDeque<>();

        // Track visited nodes
        boolean[][] visited = new boolean[n][n];
        int[][] tour = new int[n][n]; // Track the move number

        // Start from the bottom-left corner (e.g., a1)
        Position start = new Position(0, 0);
        frontier.add(start);
        visited[0][0] = true;
        tour[0][0] = 1;

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
            Position current;
            if (method == 'a') { // Breadth-First Search
                current = frontier.pollFirst();
            } else if (method == 'b') { // Depth-First Search
                current = frontier.pollLast();
            } else { // DFS with Heuristics (c: h1b, d: h2)
                current = selectWithHeuristic(frontier, visited, n, method);
                frontier.remove(current);
            }

            nodesExpanded++;

            // Check if all squares have been visited (goal state)
            if (allVisited(visited)) {
                printTour(tour, transitions, n);
                return "A solution found.\nNodes Expanded: " + nodesExpanded;
            }

            // Expand the current node
            for (int[] move : MOVES) {
                int newX = current.x + move[0];
                int newY = current.y + move[1];

                if (isValidMove(newX, newY, n, visited)) {
                    frontier.add(new Position(newX, newY));
                    visited[newX][newY] = true;
                    tour[newX][newY] = tour[current.x][current.y] + 1;
                    transitions.add("Move " + tour[newX][newY] + ": (" + current.x + ", " + current.y + ") -> (" + newX + ", " + newY + ")");
                }
            }
        }

        return "No solution exists.";
    }

    // Heuristic-based selection
    private static Position selectWithHeuristic(Deque<Position> frontier, boolean[][] visited, int n, char method) {
        Position best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Position pos : frontier) {
            int score = (method == 'c') ? h1b(pos, visited, n) : h2(pos, visited, n);
            if (score < bestScore) {
                bestScore = score;
                best = pos;
            }
        }

        return best;
    }

    // h1b: Warnsdorff rule
    private static int h1b(Position pos, boolean[][] visited, int n) {
        int count = 0;
        for (int[] move : MOVES) {
            int newX = pos.x + move[0];
            int newY = pos.y + move[1];
            if (isValidMove(newX, newY, n, visited)) {
                count++;
            }
        }
        return count; // Fewer moves mean higher priority
    }

    // h2: Advanced heuristic
    private static int h2(Position pos, boolean[][] visited, int n) {
        int count = h1b(pos, visited, n);

        // Penalize moves closer to the center (encouraging exploration of edges and corners first)
        int centerX = n / 2;
        int centerY = n / 2;
        int distanceToCenter = Math.abs(pos.x - centerX) + Math.abs(pos.y - centerY);

        return count + distanceToCenter; // Combine Warnsdorff with distance to center
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
