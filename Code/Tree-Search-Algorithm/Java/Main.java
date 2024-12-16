import java.util.*;

/**
 * Main class for Knight's Tour Problem implementation.
 * Supports different search strategies and board configurations.
 */
public class Main {
    public static void main(String[] args) {
        // Default parameters
        int boardSize = 8;
        String searchMethod = "DFS_H2";
        long timeLimit = 15 * 60 * 1000; // 15 minutes in milliseconds

        // Parse command-line arguments if provided
        if (args.length >= 3) {
            boardSize = Integer.parseInt(args[0]);
            searchMethod = args[1];
            timeLimit = Long.parseLong(args[2]) * 60 * 1000; // Convert minutes to milliseconds
        }

        // Create problem and knight tour solver
        System.out.println("Knight's Tour Problem");
        Problem problem = new Problem(boardSize);
        KnightTour knightTour = new KnightTour();

        // Solve the Knight's Tour
        long startTime = System.currentTimeMillis();
        SearchResult result = knightTour.solve(searchMethod, problem, timeLimit);
        long endTime = System.currentTimeMillis();

        // Print results
        printSearchResults(result, searchMethod, timeLimit, startTime, endTime);
    }

    /**
     * Print detailed search results
     * @param result Search result
     * @param searchMethod Search method used
     * @param timeLimit Time limit
     * @param startTime Search start time
     * @param endTime Search end time
     */
    private static void printSearchResults(SearchResult result, 
                                           String searchMethod, 
                                           long timeLimit, 
                                           long startTime, 
                                           long endTime) {
        System.out.println("Knight's Tour Problem Results");
        System.out.println("-----------------------------");
        System.out.println("Search Method: " + searchMethod);
        System.out.println("Time Limit: " + (timeLimit / 60000) + " minutes");

        // Print return status
        if (result.solution == null) {
            if (result.nodesExpanded > 0) {
                System.out.println("Return Status: " + 
                    (System.currentTimeMillis() - startTime >= timeLimit ? "Timeout" : "No solution exists"));
            } else {
                System.out.println("Return Status: Out of Memory");
            }
        } else {
            System.out.println("Return Status: A solution found.");
            System.out.println("Solution Time: " + (endTime - startTime) + " ms");
            
            // Reconstruct and print solution path
            List<Node> path = reconstructPath(result.solution);
            System.out.println("Solution Path:");
            for (Node node : path) {
                System.out.println(convertToChessNotation(node.x, node.y));
            }
        }

        System.out.println("Nodes Expanded: " + result.nodesExpanded);
    }

    /**
     * Reconstruct solution path
     * @param solution Final solution node
     * @return List of nodes in the solution path
     */
    private static List<Node> reconstructPath(Node solution) {
        List<Node> path = new ArrayList<>();
        Node current = solution;
        while (current != null) {
            path.add(0, current);
            current = current.parent;
        }
        return path;
    }

    /**
     * Convert board coordinates to chess notation
     * @param x x-coordinate
     * @param y y-coordinate
     * @return Chess notation string
     */
    private static String convertToChessNotation(int x, int y) {
        char column = (char) ('a' + x);
        int row = y + 1;
        return column + String.valueOf(row);
    }
}