/**
 * The KnightTour class implements a solution to the Knight's Tour problem using various search methods.
 * It allows the user to choose between BFS, DFS, DFS with heuristic h1b, and DFS with heuristic h2.
 * The program reads the board size, search method, and time constraint from the user, and attempts to find a solution.
 * If a solution is found, it prints the path and the board configuration.
 * 
 * Methods:
 * - main(String[] args): The entry point of the program. Reads input from the user and initiates the search.
 * - treeSearch(Problem problem, String strategy): Performs the search based on the chosen strategy (BFS, DFS, DFS-H1B, DFS-H2).
 * - printBoard(Node node, int n): Prints the board configuration with the knight's path.
 * - printPath(Node node, int n): Prints the path of the knight in both coordinate and chess notation.
 * 
 * Inner Classes:
 * - Node: Represents a node in the search tree, containing the knight's position, depth, and parent node.
 *   - calculateH1b(int[][] moves, Problem problem): Calculates the heuristic h1b value for the node.
 *   - nearestCornerDistance(int[][] moves, Problem problem): Calculates the distance to the nearest corner for tie-breaking in heuristic h2.
 * - Problem: Represents the Knight's Tour problem, containing the board size and possible moves.
 *   - isGoal(Node node): Checks if the node represents a goal state (complete tour).
 *   - expand(Node node, String heuristicType): Expands the node to generate its children based on the chosen heuristic.
 *   - isValid(Node node, int x, int y): Checks if a move to the given coordinates is valid (within bounds and not visited).
 * 
 * Fields:
 * - timeConstraint: The time constraint for the search in milliseconds.
 * 
 * Usage:
 * 1. Run the program.
 * 2. Enter the board size (n).
 * 3. Choose the search method:
 *    - a: BFS (Breadth-First Search)
 *    - b: DFS (Depth-First Search)
 *    - c: DFS-H1B (Depth-First Search with heuristic h1b)
 *    - d: DFS-H2 (Depth-First Search with heuristic h2)
 * 4. Enter the time constraint in minutes.
 * 5. The program will attempt to find a solution within the given time constraint.
 * 6. If a solution is found, the path and board configuration will be printed.
 * 7. If no solution is found within the time constraint, a timeout message will be displayed.
 * 
 * Example:
 * Enter board size (n): 8
 * Enter search method (a: BFS, b: DFS, c: DFS-H1B, d: DFS-H2): b
 * Enter time constraint in minutes: 1
 * 
 * Output:
 * A solution found.
 * Knight Tour Path:
 * Step 1: (0, 0) -> a1
 * Step 2: (2, 1) -> b3
 * ...
 * 
 * Board Configuration:
 *    1    -    -    -    -    -    -    -
 *    -    -    -    -    -    -    -    -
 *    -    2    -    -    -    -    -    -
 *    -    -    -    -    -    -    -    -
 *    -    -    -    -    -    -    -    -
 *    -    -    -    -    -    -    -    -
 *    -    -    -    -    -    -    -    -
 *    -    -    -    -    -    -    -    -
 */
import java.io.*;
import java.util.*;

public class KnightTour {
    
    public static void main(String[] args) throws IOException {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("Enter board size (n): ");
            int n = sc.nextInt();

            System.out.println("Enter search method (a: BFS, b: DFS, c: DFS-H1B, d: DFS-H2): ");
            String method = sc.next();
            switch (method.toLowerCase()) {
                case "a" -> method = "bfs";
                case "b" -> method = "dfs";
                case "c" -> method = "dfs-h1b";
                case "d" -> method = "dfs-h2";
                default -> throw new IllegalArgumentException("Invalid method. Please choose a valid option.");
            }

            System.out.println("Enter time constraint in minutes: ");
            int timeLimitInMinutes = sc.nextInt();
            timeConstraint = timeLimitInMinutes * 60L * 1000L;

            Problem problem = new Problem(n);
            Node result = treeSearch(problem, method);

            if (result != null) {
                System.out.println("A solution found.");
                printPath(result, n);
                printBoard(result, n);
            }
        } catch (OutOfMemoryError e) {
            System.err
                    .println("Error: Ran out of memory. Please try a smaller board size or a different search method.");
        } catch (IllegalArgumentException e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    static class Node {
        int x, y, depth;
        Node parent;

        public Node(int x, int y, int depth, Node parent) {
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.parent = parent;
        }

        // heuristic function h1b is the number of valid moves from the current node to
        // get least number of valid moves later
        int calculateH1b(int[][] moves, Problem problem) {
            int count = 0;
            for (int[] move : moves) {
                int newX = x + move[0];
                int newY = y + move[1];
                if (problem.isValid(this, newX, newY)) {
                    // option to move to a valid cell from the child node increases
                    count++;
                }
            }
            return count;
        }

        // this function is used to break ties in h2 its function is to return the
        // distance of the current node from the nearest corner
        int nearestCornerDistance(int[][] moves, Problem problem) {
            int n = problem.n;
            int topLeft = x + y; // (0, 0)
            int topRight = x + (n - 1 - y); // (0, n-1)
            int bottomLeft = (n - 1 - x) + y; // (n-1, 0)
            int bottomRight = (n - 1 - x) + (n - 1 - y); // (n-1, n-1)
            return Math.min(Math.min(topLeft, topRight), Math.min(bottomLeft, bottomRight));
        }

    }

    static class Problem {
        int n;
        // possible moves for the knight
        int[][] moves = { { -2, -1 }, { -1, -2 }, { 1, -2 }, { 2, -1 }, { 2, 1 }, { 1, 2 }, { -1, 2 }, { -2, 1 } };

        public Problem(int n) {
            this.n = n;
        }

        boolean isGoal(Node node) {
            return node.depth == n * n;
        }

        List<Node> expand(Node node, String heuristicType) {
            List<Node> children = new ArrayList<>();
            for (int[] move : moves) {
                int newX = node.x + move[0];
                int newY = node.y + move[1];
                if (isValid(node, newX, newY)) {
                    Node child = new Node(newX, newY, node.depth + 1, node);
                    children.add(child);
                }
            }
            // sort the children based on the heuristic type to push them into the stack
            if (heuristicType.equals("h1b")) {
                // children are sorted by h1b from smallest to largest
                children.sort(Comparator.comparingInt(childNode -> childNode.calculateH1b(moves, this)));
            } else if (heuristicType.equals("h2")) {
                // sorting by h1b with tie-breaking for h2
                children.sort((node1, node2) -> {
                    // If diff is negative, node1 comes before node2. If diff is positive, node2
                    // comes before node1. sorting by h1b is also achieved.
                    int diff = node1.calculateH1b(moves, this) - node2.calculateH1b(moves, this);
                    if (diff == 0) {
                        // if h1b is the same, sort by h2 which is the distance from the nearest corner
                        return node1.nearestCornerDistance(moves, this)
                                - node2.nearestCornerDistance(moves, this);
                    }
                    return diff;
                });
            }

            return children;
        }

        boolean isValid(Node node, int x, int y) {
            // check if the coordinates are within bounds
            if (x < 0 || y < 0 || x >= n || y >= n) {
                return false;
            }

            // check if the node has been visited
            Node current = node;
            while (current != null) {
                if (current.x == x && current.y == y) {
                    // has been visited
                    return false;
                }
                current = current.parent;
            }

            return true;
        }
    }

    static long timeConstraint;

    static Node treeSearch(Problem problem, String strategy) throws IOException {
        // initialize frontier (nodes to be explored)
        Stack<Node> stack = new Stack<>();
        Queue<Node> queue = new LinkedList<>();
        // initial state
        Node startNode = new Node(0, 0, 1, null);

        // initialize frontier based on the strategy
        try (BufferedWriter logWriter = new BufferedWriter(new FileWriter("moves.log"))) {
            // initialize frontier based on the strategy
            if (strategy.equalsIgnoreCase("bfs")) {
                queue.add(startNode);
            } else {
                stack.push(startNode);
            }
            
            long startTime = System.currentTimeMillis();
            int nodesExpanded = 0;
            
            while (!(queue.isEmpty() && stack.isEmpty())) {
                long currentTime = System.currentTimeMillis();
                // check if the time constraint has been reached
                if (currentTime - startTime > timeConstraint) {
                    System.out.println("Nodes Expanded: " + nodesExpanded);
                    logWriter.write("Nodes Expanded : " + nodesExpanded);
                    System.out.println("Timeout.");
                    logWriter.write("Timeout.");
                    logWriter.close();
                    return null;
                }
                
                // choose a leaf node for expansion according to the strategy and remove it from
                // the frontier
                Node node;
                if (strategy.equalsIgnoreCase("bfs")) {
                    node = queue.poll();
                } else {
                    node = stack.pop();
                }
                
                System.out.println("<-|| Popped: (" + node.x + ", " + node.y + ") --- Depth: " + node.depth);
                logWriter.write("Popped: (" + node.x + ", " + node.y + ") --- Depth: " + node.depth + "\n");
                
                // return the node if it is a goal state
                if (problem.isGoal(node)) {
                    System.out.println("Nodes Expanded: " + nodesExpanded);
                    logWriter.write("Nodes Expanded: " + nodesExpanded + "\n");
                    long endTime = System.currentTimeMillis();
                    System.out.println("Time spent: " + (endTime - startTime) / 1000.0 + " seconds");
                    logWriter.write("Time spent: " + (endTime - startTime) / 1000.0 + " seconds\n");
                    logWriter.close();
                    return node;
                }
                
                String heuristicType = strategy.contains("h") ? strategy.split("-")[1] : "";
                // expand the node and add the resulting nodes to the frontier
                List<Node> children = problem.expand(node, heuristicType);
                nodesExpanded++;
                
                // add children to the stack or queue based on the strategy
                if (strategy.equalsIgnoreCase("bfs")) {
                    queue.addAll(children);
                } else if (strategy.equalsIgnoreCase("dfs-h1b") || strategy.equalsIgnoreCase("dfs-h2")) {
                    for (int i = children.size() - 1; i >= 0; i--) {
                        stack.push(children.get(i));
                        System.out.println("||-> Pushed: (" + children.get(i).x + ", " + children.get(i).y + ") --- Depth: "
                                + children.get(i).depth);
                        logWriter.write("Pushed: (" + children.get(i).x + ", " + children.get(i).y + ") --- Depth: "
                                + children.get(i).depth + "\n");
                    }
                }
                // default dfs
                else {
                    for (Node child : children) {
                        stack.push(child);
                        System.out.println("||-> Pushed: (" + child.x + ", " + child.y + ") --- Depth: " + child.depth);
                        logWriter.write("Pushed: (" + child.x + ", " + child.y + ") --- Depth: " + child.depth + "\n");
                    }
                }
            }
            
            System.out.println("No solution exists.");
            logWriter.write("No solution exists.\n");
            System.out.println("Nodes Expanded: " + nodesExpanded);
            logWriter.write("Nodes Expanded: " + nodesExpanded + "\n");
        }
        return null;
    }

    static void printBoard(Node node, int n) throws IOException {
        int[][] board = new int[n][n];
        Node current = node;

        // traverse the path from the end to the start and fill the board with the
        // knight's path (depth)
        while (current != null) {
            board[current.x][current.y] = current.depth;
            current = current.parent;
        }

        // print the board (starting from the bottom-left corner for an nxn board)
        try (BufferedWriter boardWriter = new BufferedWriter(new FileWriter("board.txt"))) {
            // print the board (starting from the bottom-left corner for an nxn board)
            for (int i = n - 1; i >= 0; i--) {
                for (int j = 0; j < n; j++) {
                    if (board[i][j] == 0) {
                        System.out.print("    - ");
                        boardWriter.write("    - ");
                    } else {
                        System.out.printf("%4d ", board[i][j]); // print depth of each move
                        boardWriter.write(String.format("%4d ", board[i][j]));
                    }
                }
                System.out.println();
                boardWriter.write("\n");
            }
        }
    }

    // Method to print the path in x,y coordinates and chess notation
    static void printPath(Node node, int n) throws IOException {
        List<Node> path = new ArrayList<>();
        Node current = node;

        // Backtrack to collect the full path
        while (current != null) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path); // Reverse to start from the initial move

        try (BufferedWriter pathWriter = new BufferedWriter(new FileWriter("path.txt"))) {
            System.out.println("\nKnight Tour Path:");
            pathWriter.write("Knight Tour Path:\n");
            
            for (Node step : path) {
                char col = (char) ('a' + step.y); // Convert column index to letter
                int row = step.x + 1; // Convert row index to 1-based
                System.out.printf("Step %d: (%d, %d) -> %c%d%n", step.depth, step.x, step.y, col, row);
                pathWriter.write(String.format("Step %d: (%d, %d) -> %c%d%n", step.depth, step.x, step.y, col, row));
            }
        }
    }
}
