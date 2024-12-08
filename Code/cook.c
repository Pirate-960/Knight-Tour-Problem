#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <time.h>

// Knight's moves
const int dx[8] = {2, 2, -2, -2, 1, -1, 1, -1};
const int dy[8] = {1, -1, 1, -1, 2, 2, -2, -2};

// Structure to store the knight's position
typedef struct {
    int x, y;
} Position;

// Structure for BFS nodes
typedef struct Node {
    Position pos;
    struct Node* parent;
} Node;

// Function prototypes
bool isValidMove(int x, int y, int n, bool visited[n][n]);
void printBoard(int n, Position path[], int steps);
void printPath(Position path[], int steps);
char* formatTransition(const char* from, const char* to);
bool dfs(int x, int y, int n, bool visited[n][n], Position path[], int depth, int* nodesExpanded, time_t startTime, int timeLimit);
int heuristicH1b(int x, int y, int n, bool visited[n][n]);
int heuristicH2(int x, int y, int n, bool visited[n][n]);
bool dfsHeuristic(int x, int y, int n, bool visited[n][n], Position path[], int depth, int (*heuristic)(int, int, int, bool[n][n]), int* nodesExpanded, time_t startTime, int timeLimit);
bool bfs(int n, bool visited[n][n], Position path[], int* nodesExpanded, time_t startTime, int timeLimit);
void printExpansionTree(Position path[], int steps, int n);
void indexToChessNotation(Position pos, char* notation);

// Main function
int main() {
    int n, method, timeLimit;
    printf("Enter the board size (n): ");
    scanf("%d", &n);

    printf("Choose method:\n");
    printf("1: Breadth-First Search (BFS)\n");
    printf("2: Depth-First Search (DFS)\n");
    printf("3: DFS with h1b (Warnsdorff's Rule)\n");
    printf("4: DFS with h2 (Enhanced Heuristic)\n");
    printf("Enter your choice: ");
    scanf("%d", &method);

    printf("Enter the time limit in seconds: ");
    scanf("%d", &timeLimit);

    bool visited[n][n];
    Position path[n * n];
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            visited[i][j] = false;
        }
    }

    Position start = {0, 0}; // Start position
    visited[start.x][start.y] = true;
    path[0] = start;

    time_t startTime = time(NULL);
    int nodesExpanded = 0;
    bool solved = false;

    printf("Search Method: %d, Time Limit: %d seconds\n", method, timeLimit);

    switch (method) {
        case 1:
            solved = bfs(n, visited, path, &nodesExpanded, startTime, timeLimit);
            break;
        case 2:
            solved = dfs(start.x, start.y, n, visited, path, 1, &nodesExpanded, startTime, timeLimit);
            break;
        case 3:
            solved = dfsHeuristic(start.x, start.y, n, visited, path, 1, heuristicH1b, &nodesExpanded, startTime, timeLimit);
            break;
        case 4:
            solved = dfsHeuristic(start.x, start.y, n, visited, path, 1, heuristicH2, &nodesExpanded, startTime, timeLimit);
            break;
        default:
            printf("Invalid method.\n");
            return 1;
    }

    if (solved) {
        printf("A solution was found!\n");
        printf("Nodes Expanded: %d\n", nodesExpanded);
        printf("Coordinates of the tour:\n");
        printPath(path, n * n);
        printf("Expansion Tree:\n");
        printExpansionTree(path, n * n, n);
        printf("Board representation:\n");
        printBoard(n, path, n * n);
    } else {
        if (difftime(time(NULL), startTime) >= timeLimit) {
            printf("Timeout.\n");
        } else {
            printf("No solution exists.\n");
        }
    }

    return 0;
}

// Check if a move is valid
bool isValidMove(int x, int y, int n, bool visited[n][n]) {
    return x >= 0 && x < n && y >= 0 && y < n && !visited[x][y];
}

// Print the board with the knight's path
void printBoard(int n, Position path[], int steps) {
    int board[n][n];
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            board[i][j] = -1;
        }
    }
    for (int i = 0; i < steps; i++) {
        board[path[i].x][path[i].y] = i + 1;
    }

    printf("   ");
    for (int j = 0; j < n; j++) {
        printf("  %c  ", 'a' + j); // Column labels
    }
    printf("\n");

    for (int i = 0; i < n; i++) {
        printf("   ");
        for (int j = 0; j < n; j++) {
            printf("-----");
        }
        printf("-\n");

        printf(" %2d ", n - i); // Row labels
        for (int j = 0; j < n; j++) {
            if (board[i][j] == -1) {
                printf("|  X "); // Empty square
            } else {
                printf("|%3d ", board[i][j]); // Move number
            }
        }
        printf("|\n");
    }

    printf("   ");
    for (int j = 0; j < n; j++) {
        printf("-----");
    }
    printf("-\n");

    printf("   ");
    for (int j = 0; j < n; j++) {
        printf("  %c  ", 'a' + j); // Column labels
    }
    printf("\n");
}

// Print the path as coordinates
void printPath(Position path[], int steps) {
    printf("\nKnight's Tour Path:\n");
    printf("+-------+----------------+-------------------------+-------------------------+-----------------+\n");
    printf("| Step  | Current Square |       Next Square       | Transition (Chess Not.) | Location (x, y) |\n");
    printf("+-------+----------------+-------------------------+-------------------------+-----------------+\n");

    for (int i = 0; i < steps; i++) {
        char currentNotation[4], nextNotation[4];
        indexToChessNotation(path[i], currentNotation);

        if (i < steps - 1) {
            // For intermediate moves, calculate transition
            indexToChessNotation(path[i + 1], nextNotation);
            printf("| %5d | %14s | %23s | %19s     | %13d   |\n",
                   i + 1, 
                   currentNotation, 
                   nextNotation, 
                   formatTransition(currentNotation, nextNotation),
                   (path[i].x + 1) * 10 + (path[i].y + 1));
        } else {
            // Last move, no next location or transition
            printf("| %5d | %14s | %23s | %19s     | %13d   |\n",
                   i + 1, 
                   currentNotation, 
                   "None", 
                   "None", 
                   (path[i].x + 1) * 10 + (path[i].y + 1));
        }
    }

    printf("+-------+----------------+-------------------------+-------------------------+-----------------+\n");
}

// Helper function to center the transition
char* formatTransition(const char* from, const char* to) {
    static char transition[20];
    snprintf(transition, sizeof(transition), "%s -> %s", from, to);
    return transition;
}

// Print the expansion tree
void printExpansionTree(Position path[], int steps, int n) {
    printf("Expansion Tree:\n");
    for (int i = 0; i < steps; i++) {
        for (int j = 0; j < i; j++) {
            printf("   "); // Indentation
        }
        char notation[4];
        indexToChessNotation(path[i], notation);
        if (i == steps - 1) {
            printf("`-- %s *\n", notation); // Highlight the final move
        } else {
            printf("|-- %s\n", notation);
        }
    }
}

// DFS implementation
bool dfs(int x, int y, int n, bool visited[n][n], Position path[], int depth, int* nodesExpanded, time_t startTime, int timeLimit) {
    if (difftime(time(NULL), startTime) >= timeLimit) return false; // Timeout
    (*nodesExpanded)++;
    if (depth == n * n) return true;

    for (int i = 0; i < 8; i++) {
        int nx = x + dx[i], ny = y + dy[i];
        if (isValidMove(nx, ny, n, visited)) {
            visited[nx][ny] = true;
            path[depth] = (Position){nx, ny};
            if (dfs(nx, ny, n, visited, path, depth + 1, nodesExpanded, startTime, timeLimit)) {
                return true;
            }
            visited[nx][ny] = false;
        }
    }
    return false;
}

// Warnsdorff's rule heuristic (h1b)
int heuristicH1b(int x, int y, int n, bool visited[n][n]) {
    int count = 0;
    for (int i = 0; i < 8; i++) {
        int nx = x + dx[i], ny = y + dy[i];
        if (isValidMove(nx, ny, n, visited)) count++;
    }
    return count;
}

// Enhanced heuristic (h2)
int heuristicH2(int x, int y, int n, bool visited[n][n]) {
    int h1bValue = heuristicH1b(x, y, n, visited);
    int cornerDist = abs(x - n / 2) + abs(y - n / 2); // Approximation for corner preference
    return h1bValue * 10 + cornerDist;
}

// DFS with heuristic
bool dfsHeuristic(int x, int y, int n, bool visited[n][n], Position path[], int depth, int (*heuristic)(int, int, int, bool[n][n]), int* nodesExpanded, time_t startTime, int timeLimit) {
    if (difftime(time(NULL), startTime) >= timeLimit) return false; // Timeout
    (*nodesExpanded)++;
    if (depth == n * n) return true;

    Position moves[8];
    int scores[8], moveCount = 0;

    for (int i = 0; i < 8; i++) {
        int nx = x + dx[i], ny = y + dy[i];
        if (isValidMove(nx, ny, n, visited)) {
            moves[moveCount] = (Position){nx, ny};
            scores[moveCount] = heuristic(nx, ny, n, visited);
            moveCount++;
        }
    }

    // Sort moves based on heuristic scores
    for (int i = 0; i < moveCount - 1; i++) {
        for (int j = i + 1; j < moveCount; j++) {
            if (scores[i] > scores[j]) {
                int tempScore = scores[i];
                scores[i] = scores[j];
                scores[j] = tempScore;

                Position tempPos = moves[i];
                moves[i] = moves[j];
                moves[j] = tempPos;
            }
        }
    }

    for (int i = 0; i < moveCount; i++) {
        int nx = moves[i].x, ny = moves[i].y;
        visited[nx][ny] = true;
        path[depth] = (Position){nx, ny};
        if (dfsHeuristic(nx, ny, n, visited, path, depth + 1, heuristic, nodesExpanded, startTime, timeLimit)) {
            return true;
        }
        visited[nx][ny] = false;
    }
    return false;
}

// BFS implementation
bool bfs(int n, bool visited[n][n], Position path[], int* nodesExpanded, time_t startTime, int timeLimit) {
    Node* queue[n * n * n * n];
    int front = 0, rear = 0;

    Node* root = (Node*)malloc(sizeof(Node));
    root->pos = (Position){0, 0};
    root->parent = NULL;

    queue[rear++] = root;

    while (front < rear) {
        if (difftime(time(NULL), startTime) >= timeLimit) {
            free(root);
            return false; // Timeout
        }

        Node* current = queue[front++];
        (*nodesExpanded)++;

        int x = current->pos.x;
        int y = current->pos.y;

        if (rear == n * n) {
            Node* solution = current;
            int index = 0;

            while (solution != NULL) {
                path[index] = solution->pos;
                solution = solution->parent;
                index++;
            }

            free(root);
            return true;
        }
    }

    return false;
}

// Chess notation
void indexToChessNotation(Position pos, char* notation) {
    notation[0] = 'a' + pos.y;
    notation[1] = '1' + pos.x;
    notation[2] = '\0';
}
