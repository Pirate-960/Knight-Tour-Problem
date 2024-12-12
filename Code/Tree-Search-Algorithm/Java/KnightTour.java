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

            // Deep copy of board state
            this.board = new int[board.length][board[0].length];
            for (int i = 0; i < board.length; i++) {
                this.board[i] = board[i].clone();
            }

            // Deep copy of visited set
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
                if (isValid(newX, newY, node)) {
                    Node child = new Node(newX, newY, node.depth + 1, node, node.board, node.visited);
                    child.board[newX][newY] = child.depth; // Mark as visited
                    child.visited.add(newX + "," + newY);
                    children.add(child);
                }
            }
            return children;
        }

        boolean isValid(int x, int y, Node node) {
            return x >= 0 && y >= 0 && x < n && y < n && node.board[x][y] == 0 && !node.visited.contains(x + "," + y);
        }

        void printBoard(int[][] board) {
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[i].length; j++) {
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

        int[][] initialBoard = new int[problem.n][problem.n];
        Set<String> initialVisited = new HashSet<>();
        Node startNode = new Node(problem.n - 1, 0, 1, null, initialBoard, initialVisited);
        startNode.board[startNode.x][startNode.y] = 1;
        startNode.visited.add(startNode.x + "," + startNode.y);
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

            // Poll the next node
            Node node = "DFS".equalsIgnoreCase(strategy)
                    ? ((ArrayDeque<Node>) frontier).pollLast() // DFS (LIFO)
                    : frontier.poll(); // BFS (FIFO)

            // Debugging: Print the node being processed
            System.out.println("Processing Node: (" + node.x + ", " + node.y + "), Depth: " + node.depth);

            if (problem.isGoal(node)) {
                System.out.println("Nodes Expanded: " + nodesExpanded);
                long endTime = System.currentTimeMillis();
                System.out.println("Time spent: " + (endTime - startTime) / 1000.0 + " seconds");
                return node;
            }

            List<Node> children = problem.expand(node);
            nodesExpanded++;

            // Debugging: Print the children being expanded
            System.out.print("Expanding Children: ");
            for (Node child : children) {
                System.out.print("(" + child.x + ", " + child.y + ") ");
            }
            System.out.println();

            frontier.addAll(children);
        }

        System.out.println("No solution exists.");
        problem.printBoard(frontier.peek().board);
        System.out.println("Nodes Expanded: " + nodesExpanded);
        return null;
    }

    static String toChessNotation(int x, int y, int n) {
        char column = (char) ('a' + y);
        int row = n - x;
        return "" + column + row;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter board size (n): ");
        int n = sc.nextInt();

        System.out.println("Enter search method (a: BFS, b: DFS): ");
        String method = sc.next();
        switch (method.toLowerCase()) {
            case "a" -> method = "BFS";
            case "b" -> method = "DFS";
            default -> {
                System.out.println("Invalid method. Defaulting to BFS.");
                method = "BFS";
            }
        }

        System.out.println("Enter time limit (minutes): ");
        int timeLimitMinutes = sc.nextInt();
        timeLimitMillis = timeLimitMinutes * 60 * 1000L;

        System.out.println("Search Method: " + method);
        System.out.println("Time Limit: " + timeLimitMinutes + " minutes");

        Problem problem = new Problem(n);
        Node result = treeSearch(problem, method);

        if (result != null) {
            System.out.println("A solution found.");
            List<String> path = new ArrayList<>();
            while (result != null) {
                path.add(toChessNotation(result.x, result.y, n));
                result = result.parent;
            }
            Collections.reverse(path);
            System.out.println("Path: " + String.join(" -> ", path));
            problem.printBoard(result.board);
        } else {
            System.out.println("No solution or timeout.");
        }

        sc.close();
    }
}
