#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#define MAX_SIZE 52
#define INF 1e9
#define TIMEOUT 900 // 15 minutes in seconds

typedef struct {
    int x, y;
} Position;

typedef struct {
    Position pos;
    int moveCount;
} Node;

// Knight's possible moves
int knightMoves[8][2] = {
    {2, 1}, {1, 2}, {-1, 2}, {-2, 1},
    {-2, -1}, {-1, -2}, {1, -2}, {2, -1}
};

// Global variables
int board[MAX_SIZE][MAX_SIZE];
int nodesExpanded = 0;
clock_t startTime;
int timeLimit;

// Function to check if a move is valid
int isValidMove(int n, int x, int y) {
    return (x >= 0 && x < n && y >= 0 && y < n && board[x][y] == -1);
}

// Function to print the board
void printBoard(int n) {
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            printf("%2d ", board[i][j]);
        }
        printf("\n");
    }
}

// Timeout check
int hasTimedOut() {
    return ((clock() - startTime) / CLOCKS_PER_SEC >= timeLimit);
}

// BFS implementation
int bfs(int n) {
    Node queue[n * n * 8]; // Large enough queue
    int front = 0, rear = 0;

    queue[rear++] = (Node){{0, 0}, 1};
    board[0][0] = 0;

    while (front < rear) {
        if (hasTimedOut()) {
            printf("Timeout! Search took too long.\n");
            return 0;
        }

        Node current = queue[front++];
        nodesExpanded++;

        if (current.moveCount == n * n) {
            return 1; // Solution found
        }

        for (int i = 0; i < 8; i++) {
            int nextX = current.pos.x + knightMoves[i][0];
            int nextY = current.pos.y + knightMoves[i][1];
            if (isValidMove(n, nextX, nextY)) {
                board[nextX][nextY] = current.moveCount;
                queue[rear++] = (Node){{nextX, nextY}, current.moveCount + 1};
            }
        }
    }
    return 0;
}


// DFS implementation
int dfs(int n, int moveCount, Position pos) {
    if (hasTimedOut()) return 0;

    if (moveCount == n * n) return 1;

    nodesExpanded++;
    for (int i = 0; i < 8; i++) {
        int nextX = pos.x + knightMoves[i][0];
        int nextY = pos.y + knightMoves[i][1];

        if (isValidMove(n, nextX, nextY)) {
            board[nextX][nextY] = moveCount;
            if (dfs(n, moveCount + 1, (Position){nextX, nextY})) return 1;
            board[nextX][nextY] = -1; // Backtrack
        }
    }
    return 0;
}

// TODO: Implement DFS with h1b
// DFS with heuristic h1b
int heuristicH1b(int n, Position pos) {
    int count = 0;
    for (int i = 0; i < 8; i++) {
        int nextX = pos.x + knightMoves[i][0];
        int nextY = pos.y + knightMoves[i][1];
        if (isValidMove(n, nextX, nextY)) count++;
    }
    return count;
}

int dfsWithH1b(int n, int moveCount, Position pos) {
    if (hasTimedOut()) return 0;

    if (moveCount == n * n) return 1;

    nodesExpanded++;
    Position moves[8];
    int priorities[8];
    int count = 0;

    for (int i = 0; i < 8; i++) {
        int nextX = pos.x + knightMoves[i][0];
        int nextY = pos.y + knightMoves[i][1];
        if (isValidMove(n, nextX, nextY)) {
            moves[count] = (Position){nextX, nextY};
            priorities[count] = heuristicH1b(n, (Position){nextX, nextY});
            count++;
        }
    }

    // Sort moves based on heuristic (ascending order)
    for (int i = 0; i < count - 1; i++) {
        for (int j = 0; j < count - i - 1; j++) {
            if (priorities[j] > priorities[j + 1]) {
                int temp = priorities[j];
                priorities[j] = priorities[j + 1];
                priorities[j + 1] = temp;

                Position tempPos = moves[j];
                moves[j] = moves[j + 1];
                moves[j + 1] = tempPos;
            }
        }
    }

    for (int i = 0; i < count; i++) {
        Position nextPos = moves[i];
        board[nextPos.x][nextPos.y] = moveCount;
        if (dfsWithH1b(n, moveCount + 1, nextPos)) return 1;
        board[nextPos.x][nextPos.y] = -1; // Backtrack
    }
    return 0;
}

// TODO: Implement DFS with h2b
// Distance to the nearest corner
double distanceToCorner(int n, Position pos) {
    Position corners[4] = {
        {0, 0}, {0, n - 1}, {n - 1, 0}, {n - 1, n - 1}
    };
    double minDistance = INF;

    for (int i = 0; i < 4; i++) {
        double dist = (pos.x - corners[i].x) * (pos.x - corners[i].x) +
                      (pos.y - corners[i].y) * (pos.y - corners[i].y);
        if (dist < minDistance) {
            minDistance = dist;
        }
    }
    return minDistance;
}

// DFS with heuristic h2
int dfsWithH2(int n, int moveCount, Position pos) {
    if (hasTimedOut()) return 0;

    if (moveCount == n * n) return 1;

    nodesExpanded++;
    Position moves[8];
    int priorities[8];
    double distances[8];
    int count = 0;

    for (int i = 0; i < 8; i++) {
        int nextX = pos.x + knightMoves[i][0];
        int nextY = pos.y + knightMoves[i][1];
        if (isValidMove(n, nextX, nextY)) {
            moves[count] = (Position){nextX, nextY};
            priorities[count] = heuristicH1b(n, (Position){nextX, nextY});
            distances[count] = distanceToCorner(n, (Position){nextX, nextY});
            count++;
        }
    }

    // Sort moves by heuristic h1b, then by distance to nearest corner
    for (int i = 0; i < count - 1; i++) {
        for (int j = 0; j < count - i - 1; j++) {
            if (priorities[j] > priorities[j + 1] ||
                (priorities[j] == priorities[j + 1] && distances[j] > distances[j + 1])) {
                int tempPriority = priorities[j];
                priorities[j] = priorities[j + 1];
                priorities[j + 1] = tempPriority;

                double tempDistance = distances[j];
                distances[j] = distances[j + 1];
                distances[j + 1] = tempDistance;

                Position tempPos = moves[j];
                moves[j] = moves[j + 1];
                moves[j + 1] = tempPos;
            }
        }
    }

    for (int i = 0; i < count; i++) {
        Position nextPos = moves[i];
        board[nextPos.x][nextPos.y] = moveCount;
        if (dfsWithH2(n, moveCount + 1, nextPos)) return 1;
        board[nextPos.x][nextPos.y] = -1; // Backtrack
    }
    return 0;
}

int main() {
    int n, method;
    printf("Enter board size (n): ");
    scanf("%d", &n);

    if (n <= 0 || n > MAX_SIZE) {
        printf("Invalid board size. Please use 1 <= n <= %d.\n", MAX_SIZE);
        return 1;
    }

    printf("Select method:\n");
    printf("1: BFS\n");
    printf("2: DFS\n");
    printf("3: DFS with h1b (Warnsdorff's Rule)\n");
    printf("4: DFS with h2b (Enhanced Heuristic)\n");
    printf("Enter method number: ");
    scanf("%d", &method);

    printf("Enter time limit (seconds): ");
    scanf("%d", &timeLimit);
    if (timeLimit <= 0) {
        printf("Invalid time limit. Please enter a positive number.\n");
        return 1;
    }

    // Initialize board and variables
    memset(board, -1, sizeof(board));
    nodesExpanded = 0;
    startTime = clock();

    int result = 0;
    switch (method) {
        case 1:
            result = bfs(n);
            break;
        case 2:
            result = dfs(n, 1, (Position){0, 0});
            break;
        case 3:
            result = dfsWithH1b(n, 1, (Position){0, 0});
            break;
        case 4:
            result = dfsWithH2(n, 1, (Position){0, 0});
            break;
        default:
            printf("Invalid method selected.\n");
            return 1;
    }

    // Calculate elapsed time
    double elapsed = (double)(clock() - startTime) / CLOCKS_PER_SEC;

    // Output results
    if (result) {
        printf("Solution found!\n");
        printBoard(n);
    } else if (hasTimedOut()) {
        printf("Timeout! No solution found within the given time limit.\n");
    } else {
        printf("No solution exists.\n");
    }

    printf("Time elapsed: %.2f seconds\n", elapsed);
    printf("Nodes expanded: %d\n", nodesExpanded);

    return 0;
}
