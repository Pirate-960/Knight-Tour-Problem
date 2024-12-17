import java.util.*;

public class KnightTour {
    static class Node {
        int x, y, depth;
        Node parent;

        public Node(int x, int y, int depth, Node parent) {
            this.x = x;
            this.y = y;
            this.depth = depth;
            this.parent = parent;
        }

        // heuristic function h1b is the number of valid moves from the current node to get least number of valid moves later
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

        // this function is used to break ties in h2 its function is to return the distance of the current node from the nearest corner
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

    static Node treeSearch(Problem problem, String strategy) {
        // initialize frontier (nodes to be explored)
        Stack<Node> stack = new Stack<>();
        Queue<Node> queue = new LinkedList<>();
        // initial state
        Node startNode = new Node(0, 0, 1, null);
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
                System.out.println("Timeout.");
                return null;
            }

            // choose a leaf node for expansion according to the strategy and remove it from the frontier
            Node node;
            if (strategy.equalsIgnoreCase("bfs")) {
                node = queue.poll();
            } else {
                node = stack.pop();
            }

            System.out.println("Popped: " + node.x + ", " + node.y + " D: " + node.depth);

            // return the node if it is a goal state
            if (problem.isGoal(node)) {
                System.out.println("Nodes Expanded: " + nodesExpanded);
                long endTime = System.currentTimeMillis();
                System.out.println("Time spent: " + (endTime - startTime) / 1000.0 + " seconds");
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
                    System.out.println("Pushed: " + children.get(i).x + ", " + children.get(i).y + " D: "
                            + children.get(i).depth);
                }
            }
            // default dfs
            else {
                for (Node child : children) {
                    stack.push(child);
                    System.out.println("Pushed: " + child.x + ", " + child.y + " D: " + child.depth);
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
    
        // traverse the path from the end to the start and fill the board with the knight's path (depth)
        while (current != null) {
            board[current.x][current.y] = current.depth;
            current = current.parent;
        }
    
        // print the board (starting from the bottom-left corner for an 8x8 board)
        for (int i = n - 1; i >= 0; i--) {  
            for (int j = 0; j < n; j++) {
                if (board[i][j] == 0) {
                    System.out.print("    - ");  
                } else {
                    System.out.printf("%4d ", board[i][j]);  // print depth of each move
                }
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
                default -> throw new IllegalArgumentException("Invalid method. Please choose a valid option.");
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
            System.err
                    .println("Error: Ran out of memory. Please try a smaller board size or a different search method.");
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }
}
