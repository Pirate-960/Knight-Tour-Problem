import java.util.*;

/**
 * Implements Knight's Tour search strategies using Tree Search algorithm.
 */
public class KnightTour {
    /**
     * Solve Knight's Tour using specified search strategy
     * @param strategy Search strategy
     * @param problem Problem configuration
     * @param timeConstraint Time limit for search
     * @return Search result containing solution and expanded nodes
     */
    public SearchResult solve(String strategy, Problem problem, long timeConstraint) {
        // Initialize search parameters
        List<Node> frontier = new LinkedList<>();
        Node startNode = new Node(0, 0, 1, null);
        int nodesExpanded = 0;

        // Determine initial frontier strategy
        if (strategy.contains("BFS")) {
            frontier.add(startNode);
        } else { // DFS variants
            frontier.add(0, startNode);
        }

        long startTime = System.currentTimeMillis();

        // Tree Search Algorithm
        while (!frontier.isEmpty()) {
            // Check time constraint
            if (System.currentTimeMillis() - startTime > timeConstraint) {
                return new SearchResult(null, nodesExpanded);
            }

            // Select node based on strategy
            Node node;
            if (strategy.contains("BFS")) {
                node = frontier.remove(0);
            } else if (strategy.contains("DFS_H1B") || strategy.contains("DFS_H2")) {
                // For heuristic-based DFS, select node with lowest heuristic value
                int bestIndex = findBestNode(frontier, strategy);
                node = frontier.remove(bestIndex);
            } else { // Standard DFS
                node = frontier.remove(frontier.size() - 1);
            }

            // Check if goal is reached
            if (problem.isGoal(node)) {
                return new SearchResult(node, nodesExpanded);
            }

            // Expand nodes
            Node[] children = problem.expand(node, strategy);
            nodesExpanded++;

            // Add children to frontier based on strategy
            for (Node child : children) {
                if (strategy.contains("BFS")) {
                    frontier.add(child);
                } else { // DFS variants
                    frontier.add(0, child);
                }
            }
        }
        return new SearchResult(null, nodesExpanded);
    }

    /**
     * Find the best node based on heuristic for node selection
     * @param frontier List of nodes
     * @param strategy Search strategy
     * @return Index of the best node
     */
    private int findBestNode(List<Node> frontier, String strategy) {
        if (frontier.isEmpty()) return 0;

        int bestIndex = 0;
        for (int i = 1; i < frontier.size(); i++) {
            // Apply heuristic comparison based on strategy
            if (strategy.equals("DFS_H1B")) {
                // Warnsdorff's Rule: Prefer nodes with fewer onward moves
                if (frontier.get(i).heuristicValue < frontier.get(bestIndex).heuristicValue) {
                    bestIndex = i;
                }
            } else if (strategy.equals("DFS_H2")) {
                // Improved Warnsdorff's Heuristic
                // You can implement a more sophisticated comparison here
                if (frontier.get(i).heuristicValue < frontier.get(bestIndex).heuristicValue) {
                    bestIndex = i;
                }
            }
        }
        return bestIndex;
    }
}

/**
 * Encapsulates search result with solution node and number of nodes expanded
 */
class SearchResult {
    Node solution;
    int nodesExpanded;

    SearchResult(Node solution, int nodesExpanded) {
        this.solution = solution;
        this.nodesExpanded = nodesExpanded;
    }
}