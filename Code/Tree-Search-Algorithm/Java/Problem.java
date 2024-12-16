/**
 * Represents the problem configuration for the Knight's Tour.
 */
public class Problem {
    // Board size
    int n;
    
    // Possible knight moves
    int[][] moves = {{2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}, {2, -1}};

    /**
     * Constructor
     * @param n Board size (n x n)
     */
    public Problem(int n) {
        this.n = n;
    }

    /**
     * Check if a move is valid
     * @param node Current node
     * @param x New x coordinate
     * @param y New y coordinate
     * @return True if move is valid, false otherwise
     */
    public boolean isValid(Node node, int x, int y) {
        // Check board boundaries
        if (x < 0 || x >= n || y < 0 || y >= n) {
            return false;
        }
        
        // Check if square has been visited
        return !node.isVisited(x, y);
    }

    /**
     * Check if goal state is reached (visited all squares)
     * @param node Current node
     * @return True if goal is reached, false otherwise
     */
    public boolean isGoal(Node node) {
        return node.depth == n * n;
    }

    /**
     * Expand children nodes and apply heuristics
     * @param node Current node
     * @param strategy Search strategy
     * @return Array of child nodes
     */
    public Node[] expand(Node node, String strategy) {
        Node[] children = new Node[8];
        int count = 0;
        
        // Try all possible knight moves
        for (int[] move : moves) {
            int newX = node.x + move[0];
            int newY = node.y + move[1];
            
            // Check if move is valid
            if (isValid(node, newX, newY)) {
                Node child = new Node(newX, newY, node.depth + 1, node);
                
                // Calculate heuristics for DFS strategies
                if (strategy.equals("DFS_H1B")) {
                    // Warnsdorff's Rule: Minimize onward moves
                    child.calculateH1b(moves, this);
                } else if (strategy.equals("DFS_H2")) {
                    // Improved Warnsdorff's Heuristic
                    child.calculateH2(moves, this);
                }
                
                children[count++] = child;
            }
        }
        
        // Trim to actual number of children
        Node[] result = new Node[count];
        System.arraycopy(children, 0, result, 0, count);
        return result;
    }
}