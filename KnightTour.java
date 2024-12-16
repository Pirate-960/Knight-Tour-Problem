import java.util.*;

public class KnightTour {
    static class Node {
        int x, y, depth, heuristic;
        Node parent;

        public Node(int x, int y, int depth, Node parent) {
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.parent = parent;
        }

        void calculateH1b(int[][] moves, Problem problem) {
            int minOptions = Integer.MAX_VALUE;
            for (int[] move : moves) {
                int newX = x + move[0];
                int newY = y + move[1];
                if (problem.isValid(this, newX, newY)) {
                    int options = 0;
                    for (int[] nextMove : moves) {
                        int nextX = newX + nextMove[0];
                        int nextY = newY + nextMove[1];
                        if (problem.isValid(this, nextX, nextY)) {
                            options++;
                        }
                    }
                    minOptions = Math.min(minOptions, options);
                }
            }
            this.heuristic = minOptions == Integer.MAX_VALUE ? 0 : minOptions;
        }

        void calculateH2(int[][] moves, Problem problem) {
            calculateH1b(moves, problem);
            int distanceToCorner = distanceToNearestCorner(problem.n);
            this.heuristic = this.heuristic * 1000 + distanceToCorner;
        }

        int distanceToNearestCorner(int n) {
            int topLeft = x + y;
            int topRight = x + (n - 1 - y);
            int bottomLeft = (n - 1 - x) + y;
            int bottomRight = (n - 1 - x) + (n - 1 - y);
            return Math.min(Math.min(topLeft, topRight), Math.min(bottomLeft, bottomRight));
        }

        boolean isVisited(int x, int y) {
            Node current = this;
            while (current != null) {
                if (current.x == x && current.y == y) {
                    return true;
                }
                current = current.parent;
            }
            return false;
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
                    Node child = new Node(newX, newY, node.depth + 1, node);
                    if ("h1b".equals(heuristicType)) {
                        child.calculateH1b(moves, this);
                    } else if ("h2".equals(heuristicType)) {
                        child.calculateH2(moves, this);
                    }
                    children.add(child);
                }
            }
            children.sort(Comparator.comparingInt(n -> n.heuristic)); // Sort children by heuristic
            return children;
        }

        boolean isValid(Node node, int x, int y) {
            return x >= 0 && y >= 0 && x < n && y < n && !node.isVisited(x, y);
        }
    }

    static long timeConstraint;

    static Node treeSearch(Problem problem, String strategy) {
        Stack<Node> stack = new Stack<>();
        Queue<Node> queue = new LinkedList<>();

        Node startNode = new Node(problem.n - 1, 0, 1, null);

        if (strategy.equalsIgnoreCase("bfs")) {
            queue.add(startNode);
        } else {
            stack.push(startNode);
        }

        long startTime = System.currentTimeMillis();
        int nodesExpanded = 0;

        while (!(queue.isEmpty() && stack.isEmpty())) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > timeConstraint) {
                System.out.println("Timeout.");
                return null;
            }

            Node node;
            if (strategy.equalsIgnoreCase("bfs")) {
                node = queue.poll();
            } else if (strategy.toLowerCase().contains("dfs")) {
                node = stack.pop();
            } else {
                continue;
            }

            if (problem.isGoal(node)) {
                System.out.println("Nodes Expanded: " + nodesExpanded);
                long endTime = System.currentTimeMillis();
                System.out.println("Time spent: " + (endTime - startTime) / 1000.0 + " seconds");
                return node;
            }

            List<Node> children = problem.expand(node, strategy.toLowerCase().contains("h") ? strategy.split("-")[1] : "");
            nodesExpanded++;
            if (strategy.equalsIgnoreCase("bfs")) {
                queue.addAll(children);
            } else {
                for (Node child : children) {
                    stack.push(child);
                }
            }
        }

        System.out.println("No solution exists.");
        System.out.println("Nodes Expanded: " + nodesExpanded);
        return null;
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
            timeConstraint = timeLimitInMinutes * 60L * 1000L;

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
