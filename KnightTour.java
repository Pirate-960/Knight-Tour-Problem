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
                this.board[i] = board[i].clone();
            }
            this.visited = new HashSet<>(visited);
        }

        // h1b: Number of valid moves from the current position
        int calculateH1b(int[][] moves, Problem problem) {
            int validMoves = 0;
            for (int[] move : moves) {
                int newX = x + move[0];
                int newY = y + move[1];
                if (problem.isValid(this, newX, newY)) {
                    validMoves++;
                }
            }
            return validMoves;
        }

        // h2: h1b with proximity to the corners as a tiebreaker
        int calculateH2(int[][] moves, Problem problem) {
            int h1b = calculateH1b(moves, problem);
            int proximityToCorner = Math.min(
                Math.min(x, problem.n - 1 - x),
                Math.min(y, problem.n - 1 - y)
            );
            return h1b * 100 + proximityToCorner; // Multiplying to prioritize h1b first
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
        Deque<Node> frontier = new ArrayDeque<>();
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

            Node node = switch (strategy.toLowerCase()) {
                case "dfs" -> frontier.pollLast();
                case "dfs-h1b" -> pollWithHeuristic(frontier, problem, "h1b");
                case "dfs-h2" -> pollWithHeuristic(frontier, problem, "h2");
                default -> frontier.poll(); // BFS
            };

            if (node == null) continue;

            if (problem.isGoal(node)) {
                System.out.println("Nodes Expanded: " + nodesExpanded);
                long endTime = System.currentTimeMillis();
                System.out.println("Time spent: " + (endTime - startTime) / 1000.0 + " seconds");
                return node;
            }

            List<Node> children = problem.expand(node);
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

        if ("h1b".equals(heuristic)) {
            // comparator is used to sort the nodes in descending order of h1b meaning the node with the highest h1b will be at the end
            // small values of h1b are better
            nodes.sort(Comparator.comparingInt(n -> n.calculateH1b(problem.moves, problem)));
        } else if ("h2".equals(heuristic)) {
            nodes.sort(Comparator.comparingInt(n -> n.calculateH2(problem.moves, problem)));
        }

        // Add the nodes back to the frontier in the order of the sorted list
        for (int i = nodes.size() - 1; i > 0; i--) {
            frontier.addLast(nodes.get(i));
        }

        return nodes.isEmpty() ? null : nodes.get(0);
    }

    static String toChessNotation(int x, int y, int n) {
        char column = (char) ('a' + y);
        int row = n - x;
        return "" + column + row;
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
                int[][] finalBoard = result.board;
                while (result != null) {
                    path.add(toChessNotation(result.x, result.y, n));
                    result = result.parent;
                }
                Collections.reverse(path);
                System.out.println("Path: " + String.join(" -> ", path));
                problem.printBoard(finalBoard);
            }
        } catch (OutOfMemoryError e) {
            System.out.println("Error: The program ran out of memory. Try reducing the board size.");
            System.gc(); // Attempt to clear memory
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
}
