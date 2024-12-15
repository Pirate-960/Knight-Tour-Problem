import java.util.*;

public class KnightTour {
    static class Node {
        int x, y, depth, heuristic;
        Node parent;
        Set<String> visited;

        public Node(int x, int y, int depth, Node parent, Set<String> visited) {
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.parent = parent;
            this.visited = new HashSet<>(visited); // Copy visited set for child node
        }

        void calculateH1b(int[][] moves, Problem problem) {
            int validMoves = 0;
            for (int[] move : moves) {
                int newX = x + move[0];
                int newY = y + move[1];
                if (problem.isValid(this, newX, newY)) {
                    validMoves++;
                }
            }
            this.heuristic = validMoves;
        }

        void calculateH2(int[][] moves, Problem problem) {
            int h1b = 0;
            for (int[] move : moves) {
                int newX = x + move[0];
                int newY = y + move[1];
                if (problem.isValid(this, newX, newY)) {
                    h1b++;
                }
            }
            int distanceToCorner = distanceToNearestCorner(problem.n);
            this.heuristic = h1b * 1000 + distanceToCorner;
        }

        int distanceToNearestCorner(int n) {
            int topLeft = x + y;
            int topRight = x + (n - 1 - y);
            int bottomLeft = (n - 1 - x) + y;
            int bottomRight = (n - 1 - x) + (n - 1 - y);
            return Math.min(Math.min(topLeft, topRight), Math.min(bottomLeft, bottomRight));
        }
    }

    static class Problem {
        int n;
        int[][] moves = {{-2, -1}, {-1, -2}, {1, -2}, {2, -1}, {2, 1}, {1, 2}, {-1, 2}, {-2, 1}};

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
                    Set<String> newVisited = new HashSet<>(node.visited);
                    newVisited.add(newX + "," + newY);
                    Node child = new Node(newX, newY, node.depth + 1, node, newVisited);
                    if ("h1b".equals(heuristicType)) {
                        child.calculateH1b(moves, this);
                    } else if ("h2".equals(heuristicType)) {
                        child.calculateH2(moves, this);
                    }
                    children.add(child);
                }
            }
            return children;
        }

        boolean isValid(Node node, int x, int y) {
            return x >= 0 && y >= 0 && x < n && y < n && !node.visited.contains(x + "," + y);
        }
    }

    // Global variable for time constraint (in milliseconds)
    static long timeConstraint;

    static Node treeSearch(Problem problem, String strategy) {
        Deque<Node> frontier = new ArrayDeque<>();
        Node startNode = new Node(problem.n - 1, 0, 1, null, new HashSet<>());
        startNode.visited.add((problem.n - 1) + "," + 0);
        frontier.add(startNode);

        long startTime = System.currentTimeMillis();
        int nodesExpanded = 0;

        while (!frontier.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > timeConstraint) {
                System.out.println("Timeout.");
                return null;
            }

            Node node = switch (strategy.toLowerCase()) {
                case "dfs" -> frontier.pollLast(); // DFS uses LIFO queue polling from the back
                case "dfs-h1b" -> pollWithHeuristic(frontier, problem, "h1b");
                case "dfs-h2" -> pollWithHeuristic(frontier, problem, "h2");
                default -> frontier.poll(); // BFS uses FIFO queue polling from the front
            };

            if (node == null) continue;

            if (problem.isGoal(node)) {
                System.out.println("Nodes Expanded: " + nodesExpanded);
                long endTime = System.currentTimeMillis();
                System.out.println("Time spent: " + (endTime - startTime) / 1000.0 + " seconds");
                return node;
            }

            List<Node> children = problem.expand(node, strategy.toLowerCase().contains("h") ? strategy.split("-")[1] : "");
            nodesExpanded++;
            frontier.addAll(children);
        }

        System.out.println("No solution exists.");
        System.out.println("Nodes Expanded: " + nodesExpanded);
        return null;
    }

    static Node pollWithHeuristic(Deque<Node> frontier, Problem problem, String heuristic) {
        List<Node> nodes = new ArrayList<>(frontier);
        frontier.clear();

        nodes.sort(Comparator.comparingInt(n -> n.heuristic));
        frontier.addAll(nodes);

        return nodes.isEmpty() ? null : frontier.pollFirst();
    }

    static void printBoard(Node node, int n) {
        int[][] board = new int[n][n];
        Node current = node;

        while (current != null) {
            board[current.x][current.y] = current.depth;
            current = current.parent;
        }

        for (int[] row : board) {
            for (int cell : row) {
                System.out.printf("%3d ", cell);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter board size (n): ");
            int n = sc.nextInt();

            System.out.println("Enter search method (a: BFS, b: DFS, c: DFS-H1B, d: DFS-H2): ");
            String method = sc.next();
            switch (method.toLowerCase()) {
                case "a" -> method = "bfs";
                case "b" -> method = "dfs";
                case "c" -> method = "dfs-h1b";
                case "d" -> method = "dfs-h2";
                default -> {
                    System.out.println("Invalid method. Defaulting to BFS.");
                    method = "bfs";
                }
            }

            System.out.println("Enter time constraint in minutes: ");
            int timeLimitInMinutes = sc.nextInt();
            timeConstraint = timeLimitInMinutes * 60L * 1000L; // Convert to milliseconds

            Problem problem = new Problem(n);
            Node result = treeSearch(problem, method);

            if (result != null) {
                System.out.println("A solution found.");
                printBoard(result, n);
            }
        } catch (OutOfMemoryError e) {
            System.err.println("Error: Ran out of memory. Please try a smaller board size or a different search method.");
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }
}
