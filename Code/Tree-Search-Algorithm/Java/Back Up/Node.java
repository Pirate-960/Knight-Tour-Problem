/**
 * Represents a node in the Knight's Tour problem.
 */
public class Node {
    int x;          // x-coordinate
    int y;          // y-coordinate
    int depth;      // Depth in search tree
    int heuristicValue;  // Heuristic value for this node
    Node parent;    // Parent node in the search tree

    /**
     * Constructor
     * @param x x-coordinate
     * @param y y-coordinate
     * @param depth Depth in search tree
     * @param parent Parent node
     */
    public Node(int x, int y, int depth, Node parent) {
        this.x = x;
        this.y = y;
        this.depth = depth;
        this.parent = parent;
        this.heuristicValue = Integer.MAX_VALUE;
    }

    /**
     * Check if a square has been visited in the current path
     * @param x x-coordinate to check
     * @param y y-coordinate to check
     * @return true if the square has been visited, false otherwise
     */
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

    /**
     * Calculate heuristic h1b (Warnsdorff's Rule)
     * @param moves Possible knight moves
     * @param problem Problem configuration
     */
    void calculateH1b(int[][] moves, Problem problem) {
        int validMoves = 0;
        for (int[] move : moves) {
            int newX = this.x + move[0];
            int newY = this.y + move[1];
            
            // Count valid onward moves
            if (problem.isValid(this, newX, newY)) {
                validMoves++;
            }
        }
        
        // Fewer valid moves preferred (Warnsdorff's Rule)
        this.heuristicValue = validMoves;
    }

    /**
     * Calculate heuristic h2 (Improved Warnsdorff's Heuristic)
     * @param moves Possible knight moves
     * @param problem Problem configuration
     */
    void calculateH2(int[][] moves, Problem problem) {
        int validMoves = 0;
        int accessibleSquares = 0;
        
        for (int[] move : moves) {
            int newX = this.x + move[0];
            int newY = this.y + move[1];
            
            // Check move validity
            if (problem.isValid(this, newX, newY)) {
                validMoves++;
                
                // Count additional accessible squares from this move
                for (int[] additionalMove : moves) {
                    int nextX = newX + additionalMove[0];
                    int nextY = newY + additionalMove[1];
                    
                    if (problem.isValid(this, nextX, nextY)) {
                        accessibleSquares++;
                    }
                }
            }
        }
        
        // Combine number of valid moves and accessible squares
        this.heuristicValue = validMoves + accessibleSquares;
    }
}