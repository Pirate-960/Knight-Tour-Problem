#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <time.h>
#include <string.h>
#include <limits.h>

// Knight's moves
const int dx[8] = {2, 2, -2, -2, 1, -1, 1, -1};
const int dy[8] = {1, -1, 1, -1, 2, 2, -2, -2};

// Position structure
typedef struct {
    int x, y;
} Position;

// TreeNode structure
typedef struct TreeNode {
    Position pos;
    struct TreeNode* parent;
    struct TreeNode* children[8];
    int depth;
} TreeNode;

// Queue structure for BFS
typedef struct QueueNode {
    TreeNode* treeNode;
    struct QueueNode* next;
} QueueNode;

typedef struct {
    QueueNode* front;
    QueueNode* rear;
} Queue;

// Function prototypes
bool isValidMove(int x, int y, int n, bool visited[n][n]);
void printPath(TreeNode* node, int n);
TreeNode* createTreeNode(Position pos, TreeNode* parent, int depth);
void freeTree(TreeNode* root);
bool bfs(TreeNode* root, int n, bool visited[n][n], long long int* nodesExpanded, time_t startTime, int timeLimit);
bool dfs(TreeNode* root, int n, bool visited[n][n], long long int* nodesExpanded, time_t startTime, int timeLimit, int (*heuristic)(int, int, int, bool[n][n]));
int heuristicH1b(int x, int y, int n, bool visited[n][n]);
int heuristicH2(int x, int y, int n, bool visited[n][n]);
Queue* createQueue();
void enqueue(Queue* queue, TreeNode* treeNode);
TreeNode* dequeue(Queue* queue);
bool isQueueEmpty(Queue* queue);

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

    bool* visitedMem = malloc(n * n * sizeof(bool));
    bool (*visited)[n] = (bool (*)[n])visitedMem;

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            visited[i][j] = false;
        }
    }

    Position start = {0, 0};
    visited[start.x][start.y] = true;

    TreeNode* root = createTreeNode(start, NULL, 1);
    time_t startTime = time(NULL);
    long long int nodesExpanded = 0;

    bool solved = false;
    switch (method) {
        case 1:
            solved = bfs(root, n, visited, &nodesExpanded, startTime, timeLimit);
            break;
        case 2:
            solved = dfs(root, n, visited, &nodesExpanded, startTime, timeLimit, NULL);
            break;
        case 3:
            solved = dfs(root, n, visited, &nodesExpanded, startTime, timeLimit, heuristicH1b);
            break;
        case 4:
            solved = dfs(root, n, visited, &nodesExpanded, startTime, timeLimit, heuristicH2);
            break;
        default:
            printf("Invalid method.\n");
            freeTree(root);
            free(visitedMem);
            return 1;
    }

    if (solved) {
        printf("A solution found.\n");
        printf("Nodes expanded: %lld\n", nodesExpanded);
        printPath(root, n * n);
    } else if (difftime(time(NULL), startTime) >= timeLimit) {
        printf("Timeout.\n");
    } else {
        printf("No solution exists.\n");
    }
    printf("Nodes expanded: %lld\n", nodesExpanded);

    freeTree(root);
    free(visitedMem);
    return 0;
}

TreeNode* createTreeNode(Position pos, TreeNode* parent, int depth) {
    TreeNode* node = malloc(sizeof(TreeNode));
    node->pos = pos;
    node->parent = parent;
    node->depth = depth;
    for (int i = 0; i < 8; i++) {
        node->children[i] = NULL;
    }
    return node;
}

void freeTree(TreeNode* root) {
    if (!root) return;
    for (int i = 0; i < 8; i++) {
        freeTree(root->children[i]);
    }
    free(root);
}

bool isValidMove(int x, int y, int n, bool visited[n][n]) {
    return x >= 0 && x < n && y >= 0 && y < n && !visited[x][y];
}

void printPath(TreeNode* node, int n) {
    if (!node) return;
    printPath(node->parent, n);
    printf("(%d, %d) -> ", node->pos.x, node->pos.y);
}

bool bfs(TreeNode* root, int n, bool visited[n][n], long long int* nodesExpanded, time_t startTime, int timeLimit) {
    Queue* queue = createQueue();
    enqueue(queue, root);

    while (!isQueueEmpty(queue)) {
        if (difftime(time(NULL), startTime) >= timeLimit) {
            free(queue);
            return false;
        }

        TreeNode* current = dequeue(queue);
        (*nodesExpanded)++;

        if (current->depth == n * n) {
            free(queue);
            return true;
        }

        for (int i = 0; i < 8; i++) {
            int nx = current->pos.x + dx[i];
            int ny = current->pos.y + dy[i];
            if (isValidMove(nx, ny, n, visited)) {
                TreeNode* child = createTreeNode((Position){nx, ny}, current, current->depth + 1);
                enqueue(queue, child);
                visited[nx][ny] = true;
            }
        }
    }

    free(queue);
    return false;
}

bool dfs(TreeNode* root, int n, bool visited[n][n], long long int* nodesExpanded, time_t startTime, int timeLimit, int (*heuristic)(int, int, int, bool[n][n])) {
    if (difftime(time(NULL), startTime) >= timeLimit) return false;
    (*nodesExpanded)++;

    if (root->depth == n * n) return true;

    Position moves[8];
    int scores[8], moveCount = 0;

    for (int i = 0; i < 8; i++) {
        int nx = root->pos.x + dx[i];
        int ny = root->pos.y + dy[i];
        if (isValidMove(nx, ny, n, visited)) {
            moves[moveCount] = (Position){nx, ny};
            scores[moveCount] = heuristic ? heuristic(nx, ny, n, visited) : 0;
            moveCount++;
        }
    }

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
        Position nextPos = moves[i];
        TreeNode* child = createTreeNode(nextPos, root, root->depth + 1);
        root->children[i] = child;

        visited[nextPos.x][nextPos.y] = true;
        if (dfs(child, n, visited, nodesExpanded, startTime, timeLimit, heuristic)) {
            return true;
        }
        visited[nextPos.x][nextPos.y] = false;
    }
    return false;
}

int heuristicH1b(int x, int y, int n, bool visited[n][n]) {
    int count = 0;
    for (int i = 0; i < 8; i++) {
        int nx = x + dx[i], ny = y + dy[i];
        if (isValidMove(nx, ny, n, visited)) count++;
    }
    return count;
}

int heuristicH2(int x, int y, int n, bool visited[n][n]) {
    return heuristicH1b(x, y, n, visited) * 10 + abs(x - n / 2) + abs(y - n / 2);
}

Queue* createQueue() {
    Queue* queue = malloc(sizeof(Queue));
    queue->front = queue->rear = NULL;
    return queue;
}

void enqueue(Queue* queue, TreeNode* treeNode) {
    QueueNode* newNode = malloc(sizeof(QueueNode));
    newNode->treeNode = treeNode;
    newNode->next = NULL;

    if (queue->rear == NULL) {
        queue->front = queue->rear = newNode;
        return;
    }

    queue->rear->next = newNode;
    queue->rear = newNode;
}

TreeNode* dequeue(Queue* queue) {
    if (queue->front == NULL) return NULL;

    QueueNode* temp = queue->front;
    TreeNode* treeNode = temp->treeNode;
    queue->front = queue->front->next;

    if (queue->front == NULL) queue->rear = NULL;

    free(temp);
    return treeNode;
}

bool isQueueEmpty(Queue* queue) {
    return queue->front == NULL;
}
