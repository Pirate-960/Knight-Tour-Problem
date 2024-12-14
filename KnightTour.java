import java.util.*;

public class KnightTour {
    static class Node {
        int x, y, depth;
        Node parent;
        int[][] board;
        Set<String> visited;

        public Node(int x, int y, int depth, Node parent, int[][] board, Set<String> visited) {
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.parent = parent;
            this.board = new int[board.length][board.length];
            for (int i = 0; i < board.length; i++) {
                // clone() method creates a new array with the same elements as the original array
                this.board[i] = board[i].clone();
            }
            this.visited = new HashSet<>(visited);
        }
    }

    static class Problem {
        int n; // Board size
        int[][] moves = {{-2, -1}, {-1, -2}, {1, -2}, {2, -1}, {2, 1}, {1, 2}, {-1, 2}, {-2, 1}};

        public Problem(int n) {
            this.n = n;
        }

        boolean isGoal(Node node) {
            return node.depth == n * n;
        }

        List<Node> expand(Node node) {
            List<Node> children = new ArrayList<>();
            for (int[] move : moves) {
                int newX = node.x + move[0];
                int newY = node.y + move[1];
                if (isValid(node, newX, newY)) {
                    int[][] newBoard = new int[n][n];
                    for (int i = 0; i < n; i++) {
                        newBoard[i] = node.board[i].clone();
                    }
                    Set<String> newVisited = new HashSet<>(node.visited);
                    newBoard[newX][newY] = node.depth + 1;
                    newVisited.add(newX + "," + newY);
                    children.add(new Node(newX, newY, node.depth + 1, node, newBoard, newVisited));
                }
            }
            return children;
        }

        boolean isValid(Node node, int x, int y) {
            return x >= 0 && y >= 0 && x < n && y < n && !node.visited.contains(x + "," + y);
        }

        void printBoard(int[][] board) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    System.out.print(board[i][j] + "\t");
                }
                System.out.println();
            }
        }
    }

    static long timeLimitMillis;

    static Node treeSearch(Problem problem, String strategy) {
        Deque<Node> frontier;
        if ("BFS".equalsIgnoreCase(strategy)) {
            frontier = new LinkedList<>(); // Queue for BFS
        } else {
            frontier = new ArrayDeque<>(); // Stack for DFS
        }

        Node startNode = new Node(problem.n - 1, 0, 1, null, new int[problem.n][problem.n], new HashSet<>());
        startNode.board[startNode.x][startNode.y] = 1;
        startNode.visited.add((problem.n - 1) + "," + 0);
        frontier.add(startNode);

        long startTime = System.currentTimeMillis();
        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            if ((System.currentTimeMillis() - startTime) > timeLimitMillis) {
                System.out.println("Timeout.");
                problem.printBoard(frontier.peek().board);
                System.out.println("Nodes Expanded: " + nodesExpanded);
                return null;
            }

            Node node = "DFS".equalsIgnoreCase(strategy)
                    ? ((ArrayDeque<Node>) frontier).pollLast() // DFS (LIFO)
                    : frontier.poll(); // BFS (FIFO)


            // debug with printing node coordinate and depth level
            System.out.println("Processing Node: (" + node.x + ", " + node.y + "), Depth: " + node.depth);

            if (problem.isGoal(node)) {
                System.out.println("Nodes Expanded: " + nodesExpanded);
                long endTime = System.currentTimeMillis();
                System.out.println("Time spent: " + (endTime - startTime) / 1000.0 + " seconds");
                return node;
            }

            List<Node> children = problem.expand(node);
            nodesExpanded++;

            for (Node child : children) {
                frontier.add(child);
            }
        }

        System.out.println("No solution exists.");
        System.out.println("Nodes Expanded: " + nodesExpanded);
        return null;
    }

    static String toChessNotation(int x, int y, int n) {
        char column = (char) ('a' + y);
        int row = n - x;
        return "" + column + row;
    }

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("===================================");
            System.out.print("Enter board size (n): ");
            int n = sc.nextInt();
            System.out.println("===================================");
            
            System.out.println("===================================");
            System.out.println("Enter Search Algorithm: ");
            System.out.println("===================================");
            System.out.println("a: BFS");
            System.out.println("b: DFS");
            System.out.println("c: DFS With Heuristics (h1b)");
            System.out.println("d: DFS With Heuristics (h2)");
            System.out.println("===================================");
            System.out.print("Search Strategy -> ");
            String method = sc.next();
            method = switch (method.toLowerCase()) {
                case "a" -> "BFS";
                case "b" -> "DFS";
                case "c" -> "DFS With Heuristics (h1b)";
                case "d" -> "DFS With Heuristics (h2)";
                default -> {
                    System.out.println("Invalid method. Defaulting to BFS.");
                    yield "BFS";
                }
            };
            System.out.println("===================================");
            
            System.out.println("===================================");
            System.out.print("Enter time limit (minutes): ");
            int timeLimitMinutes = sc.nextInt();
            System.out.println("===================================");

            timeLimitMillis = timeLimitMinutes * 60 * 1000L;
            
            System.out.println("Search Method: " + method);
            System.out.println("Time Limit: " + timeLimitMinutes + " minutes");
            
            Problem problem = new Problem(n);
            Node result = treeSearch(problem, method);
            
            if (result != null) {
                System.out.println("A solution found.");
                List<String> path = new ArrayList<>();
                int[][] finalBoard = result.board; // Store the final board state
                while (result != null) {
                    path.add(toChessNotation(result.x, result.y, n));
                    result = result.parent;
                }
                Collections.reverse(path);
                System.out.println("Path: " + String.join(" -> ", path));
                problem.printBoard(finalBoard); // Print the stored final board state
            } else {
                System.out.println("No solution or timeout.");
            }
        }
    }
}
