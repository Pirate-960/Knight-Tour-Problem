#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <time.h>
#include <string.h>

// Knight's moves
const int dx[8] = {2, 2, -2, -2, 1, -1, 1, -1};
const int dy[8] = {1, -1, 1, -1, 2, 2, -2, -2};

// Structure to store the knight's position
typedef struct {
    int x, y;
} Position;

// Structure for a node in the search queue or other data structures
typedef struct Node {
    Position pos;
    struct Node* parent;
    int depth;
} Node;

// Function prototypes
bool isValidMove(int x, int y, int n, bool visited[n][n]);
void printBoard(int n, Position path[], int steps);
void printPath(Position path[], int steps);
void freeQueue(Node** queue, int* front, int* rear);
void indexToChessNotation(Position pos, char* notation);

// BFS implementation
bool bfs(int n, bool visited[n][n], Position path[], int* nodesExpanded, time_t startTime, int timeLimit);

// DFS implementation
bool dfs(int x, int y, int n, bool visited[n][n], Position path[], int depth, int* nodesExpanded, time_t startTime, int timeLimit);

// Heuristic implementations
int heuristicH1b(int x, int y, int n, bool visited[n][n]);
int heuristicH2(int x, int y, int n, bool visited[n][n]);
bool dfsHeuristic(int x, int y, int n, bool visited[n][n], Position path[], int depth, int (*heuristic)(int, int, int, bool[n][n]), int* nodesExpanded, time_t startTime, int timeLimit);

// Main Function
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

    // Allocate visited and path dynamically
    bool* visitedMem = malloc(n * n * sizeof(bool));
    bool (*visited)[n] = (bool (*)[n])visitedMem;
    Position* path = malloc(n * n * sizeof(Position));

    // Initialize visited
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
            free(visitedMem);
            free(path);
            return 1;
    }

    if (solved) {
        printf("A solution was found!\n");
        printf("Nodes Expanded: %d\n", nodesExpanded);
        printf("Time spent: %.2f seconds\n", difftime(time(NULL), startTime));
        printf("Coordinates of the tour:\n");
        printPath(path, n * n);
        printf("Board representation:\n");
        printBoard(n, path, n * n);
    } else {
        if (difftime(time(NULL), startTime) >= timeLimit) {
            printf("Timeout.\n");
        } else {
            printf("No solution exists.\n");
        }
        printf("Nodes Expanded: %d\n", nodesExpanded);
    }

    // Free dynamically allocated memory
    free(visitedMem);
    free(path);

    return 0;
}

// BFS implementation
bool bfs(int n, bool visited[n][n], Position path[], int* nodesExpanded, time_t startTime, int timeLimit) {
    Node** queue = malloc(n * n * n * sizeof(Node*));
    int front = 0, rear = 0;
    *nodesExpanded = 0;

    // Initialize first node
    Node* startNode = malloc(sizeof(Node));
    startNode->pos.x = 0;
    startNode->pos.y = 0;
    startNode->parent = NULL;
    startNode->depth = 1;

    queue[rear++] = startNode;
    visited[0][0] = true;

    while (front < rear) {
        // Check for timeout
        if (difftime(time(NULL), startTime) >= timeLimit) {
            freeQueue(queue, &front, &rear);
            return false;
        }

        Node* current = queue[front++];
        (*nodesExpanded)++;

        // If we've reached a full tour
        if (current->depth == n * n) {
            // Reconstruct path
            Node* temp = current;
            int pathLength = 0;
            while (temp != NULL) {
                path[pathLength++] = temp->pos;
                temp = temp->parent;
            }

            // Reverse path to get correct order
            for (int i = 0; i < pathLength / 2; i++) {
                Position tempPos = path[i];
                path[i] = path[pathLength - 1 - i];
                path[pathLength - 1 - i] = tempPos;
            }

            // Free queue and nodes
            freeQueue(queue, &front, &rear);
            return true;
        }

        // Try all possible knight moves
        for (int i = 0; i < 8; i++) {
            int nx = current->pos.x + dx[i];
            int ny = current->pos.y + dy[i];

            // Check if move is valid
            if (isValidMove(nx, ny, n, visited)) {
                Node* newNode = malloc(sizeof(Node));
                newNode->pos.x = nx;
                newNode->pos.y = ny;
                newNode->parent = current;
                newNode->depth = current->depth + 1;

                queue[rear++] = newNode;
                visited[nx][ny] = true;
            }
        }
    }

    // No solution found
    freeQueue(queue, &front, &rear);
    return false;
}

// Utility function to free queue and prevent memory leaks
void freeQueue(Node** queue, int* front, int* rear) {
    while (*front < *rear) {
        Node* node = queue[*front];
        free(node);
        (*front)++;
    }
    free(queue);
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

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            if (board[i][j] == -1) {
                printf("X\t");
            } else {
                printf("%d\t", board[i][j]);
            }
        }
        printf("\n");
    }
}

// Print the path as coordinates
void printPath(Position path[], int steps) {
    char notation[4];
    for (int i = 0; i < steps; i++) {
        indexToChessNotation(path[i], notation);
        printf("%s", notation);
        if (i < steps - 1) {
            printf(" -> ");
        }
    }
    printf("\n");
}

// DFS implementation (unchanged from previous code)
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

// DFS with heuristic (from previous code)
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

// Convert index to chess notation
void indexToChessNotation(Position pos, char* notation) {
    notation[0] = 'a' + pos.y;
    notation[1] = '1' + pos.x;
    notation[2] = '\0';
}