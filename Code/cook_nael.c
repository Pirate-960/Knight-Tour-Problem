#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <time.h>
#include <math.h>

// Knight's moves
const int dx[8] = {2, 2, -2, -2, 1, -1, 1, -1};
const int dy[8] = {1, -1, 1, -1, 2, 2, -2, -2};

// Global variables
static int timeLimit;
static long long nodesExpanded;

// Structure to store the knight's position
typedef struct {
    int x, y;
} Position;

// Structure for a node in the search tree
typedef struct Node {
    Position pos;
    struct Node* parent;
    int depth;
} Node;

// Check if a move is valid
bool isValidMove(int x, int y, int n, bool visited[][n]) {
    return x >= 0 && x < n && y >= 0 && y < n && !visited[x][y];
}

// Free memory for queue/stack
void freeQueue(Node** queue, int* front, int* rear) {
    while (*front < *rear) {
        free(queue[(*front)++]);
    }
    free(queue);
}

// Breadth-First Search Strategy
Node* bfsStrategy(Node** frontier, int* front, int* rear, Node* newNode) {
    if (newNode != NULL) {
        frontier[(*rear)++] = newNode; // Add to end of queue
        return NULL;
    } else {
        return frontier[(*front)++]; // Remove from front of queue
    }
}

// Depth-First Search Strategy
Node* dfsStrategy(Node** frontier, int* front, int* rear, Node* newNode) {
    if (newNode != NULL) {
        frontier[(*rear)++] = newNode; // Add to top of stack
        return NULL;
    } else {
        return frontier[--(*rear)]; // Remove from top of stack
    }
}

// Heuristics
int heuristicH1b(Position pos, int n, bool visited[][n]) {
    int count = 0;
    for (int i = 0; i < 8; i++) {
        int nx = pos.x + dx[i];
        int ny = pos.y + dy[i];
        if (isValidMove(nx, ny, n, visited)) {
            count++;
        }
    }
    return count;
}

int heuristicH2(Position pos, int n, bool visited[][n]) {
    int h1bValue = heuristicH1b(pos, n, visited);
    int cornerDistance = abs(pos.x - n / 2) + abs(pos.y - n / 2);
    return h1bValue * 10 + cornerDistance;
}

// General Search Algorithm (for BFS/DFS)
bool generalSearch(int n, Node* (*strategy)(Node**, int*, int*, Node*)) {
    Node** frontier = malloc(n * n * sizeof(Node*));
    int front = 0, rear = 0;

    // Initialize visited array
    bool visited[n][n];
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            visited[i][j] = false;
        }
    }

    // Initialize the start node
    Node* startNode = malloc(sizeof(Node));
    startNode->pos = (Position){0, 0};
    startNode->parent = NULL;
    startNode->depth = 1;

    frontier[rear++] = startNode;
    visited[startNode->pos.x][startNode->pos.y] = true;

    time_t startTime = time(NULL);

    while (front < rear) {
        if (difftime(time(NULL), startTime) >= timeLimit) {
            freeQueue(frontier, &front, &rear);
            return false; // Timeout
        }

        Node* current = strategy(frontier, &front, &rear, NULL);
        nodesExpanded++;

        if (current->depth == n * n) { // Goal state
            printf("A solution was found:\n");
            Node* temp = current;
            while (temp != NULL) {
                printf("(%d, %d) ", temp->pos.x, temp->pos.y);
                temp = temp->parent;
            }
            printf("\n");
            freeQueue(frontier, &front, &rear);
            return true;
        }

        // open the next 8 children
        for (int i = 0; i < 8; i++) {
            int nx = current->pos.x + dx[i];
            int ny = current->pos.y + dy[i];

            if (isValidMove(nx, ny, n, visited)) {
                Node* newNode = malloc(sizeof(Node));
                newNode->pos = (Position){nx, ny};
                newNode->parent = current;
                newNode->depth = current->depth + 1;
                strategy(frontier, &front, &rear, newNode);
                visited[nx][ny] = true;
            }
        }
    }

    freeQueue(frontier, &front, &rear);
    return false;
}

// Heuristic Search
bool heuristicSearch(int n, int (*heuristic)(Position, int, bool[][n])) {
    Node** frontier = malloc(n * n * sizeof(Node*));
    int front = 0, rear = 0;

    // Initialize visited array
    bool visited[n][n];
    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            visited[i][j] = false;
        }
    }

    // Initialize the start node
    Node* startNode = malloc(sizeof(Node));
    startNode->pos = (Position){0, 0};
    startNode->parent = NULL;
    startNode->depth = 1;

    frontier[rear++] = startNode;
    visited[startNode->pos.x][startNode->pos.y] = true;

    time_t startTime = time(NULL);

    while (front < rear) {
        if (difftime(time(NULL), startTime) >= timeLimit) {
            freeQueue(frontier, &front, &rear);
            return false; // Timeout
        }

        // Select node with the best heuristic
        int bestIndex = front;
        int bestScore = heuristic(frontier[bestIndex]->pos, n, visited);
        for (int i = front + 1; i < rear; i++) {
            int score = heuristic(frontier[i]->pos, n, visited);
            if (score < bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        Node* current = frontier[bestIndex];
        frontier[bestIndex] = frontier[--rear]; // Replace with last node
        nodesExpanded++;

        if (current->depth == n * n) { // Goal state
            printf("A solution was found:\n");
            Node* temp = current;
            while (temp != NULL) {
                printf("(%d, %d) ", temp->pos.x, temp->pos.y);
                temp = temp->parent;
            }
            printf("\n");
            freeQueue(frontier, &front, &rear);
            return true;
        }

        for (int i = 0; i < 8; i++) {
            int nx = current->pos.x + dx[i];
            int ny = current->pos.y + dy[i];

            if (isValidMove(nx, ny, n, visited)) {
                Node* newNode = malloc(sizeof(Node));
                newNode->pos = (Position){nx, ny};
                newNode->parent = current;
                newNode->depth = current->depth + 1;
                frontier[rear++] = newNode;
                visited[nx][ny] = true;
            }
        }
    }

    freeQueue(frontier, &front, &rear);
    return false;
}

// Main function
int main() {
    int n, method;
    printf("Enter the board size (n): ");
    scanf("%d", &n);

    printf("Choose method:\n");
    printf("1: Breadth-First Search (BFS)\n");
    printf("2: Depth-First Search (DFS)\n");
    printf("3: Informed Heuristic h1b (Warnsdorff's Rule)\n");
    printf("4: Informed Heuristic h2 (Enhanced Heuristic)\n");
    printf("Enter your choice: ");
    scanf("%d", &method);

    printf("Enter the time limit in seconds: ");
    scanf("%d", &timeLimit);

    nodesExpanded = 0;

    bool solved = false;
    switch (method) {
        case 1:
            solved = generalSearch(n, bfsStrategy);
            break;
        case 2:
            solved = generalSearch(n, dfsStrategy);
            break;
        case 3:
            solved = heuristicSearch(n, heuristicH1b);
            break;
        case 4:
            solved = heuristicSearch(n, heuristicH2);
            break;
        default:
            printf("Invalid method.\n");
            return 1;
    }

    if (!solved) {
        printf("No solution found or timeout.\n");
    }

    printf("Nodes Expanded: %lld\n", nodesExpanded);
    return 0;
}
