#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <time.h>
#include <math.h>

#define MAX_MOVES 8

// Node structure
typedef struct Node {
    int x, y, depth, heuristic;
    struct Node* parent;
} Node;

// Problem structure
typedef struct Problem {
    int n;
    int moves[MAX_MOVES][2];
} Problem;

// Function prototypes
Node* createNode(int x, int y, int depth, Node* parent);
void calculateH1b(Node* node, Problem* problem);
void calculateH2(Node* node, Problem* problem);
int distanceToNearestCorner(int x, int y, int n);
bool isVisited(Node* node, int x, int y);
bool isGoal(Node* node, Problem* problem);
Node** expand(Node* node, Problem* problem, const char* heuristicType, int* size);
bool isValid(Node* node, int x, int y, Problem* problem);
Node* treeSearch(Problem* problem, const char* strategy, long timeConstraint);
Node* pollWithHeuristic(Node** frontier, int* frontierSize);
void printBoard(Node* node, int n);
void printPath(Node* node);
void freeNodes(Node* node);

Node* createNode(int x, int y, int depth, Node* parent) {
    Node* node = (Node*)malloc(sizeof(Node));
    if (!node) {
        fprintf(stderr, "Error: Memory allocation failed for node.\n");
        exit(EXIT_FAILURE);
    }
    node->x = x;
    node->y = y;
    node->depth = depth;
    node->heuristic = 0;
    node->parent = parent;
    return node;
}

void calculateH1b(Node* node, Problem* problem) {
    int validMoves = 0;
    for (int i = 0; i < MAX_MOVES; i++) {
        int newX = node->x + problem->moves[i][0];
        int newY = node->y + problem->moves[i][1];
        if (isValid(node, newX, newY, problem)) {
            validMoves++;
        }
    }
    node->heuristic = validMoves;
}

void calculateH2(Node* node, Problem* problem) {
    int h1b = 0;
    for (int i = 0; i < MAX_MOVES; i++) {
        int newX = node->x + problem->moves[i][0];
        int newY = node->y + problem->moves[i][1];
        if (isValid(node, newX, newY, problem)) {
            h1b++;
        }
    }
    int distanceToCorner = distanceToNearestCorner(node->x, node->y, problem->n);
    node->heuristic = h1b * 1000 + distanceToCorner;
}

int distanceToNearestCorner(int x, int y, int n) {
    int topLeft = x + y;
    int topRight = x + (n - 1 - y);
    int bottomLeft = (n - 1 - x) + y;
    int bottomRight = (n - 1 - x) + (n - 1 - y);
    return fmin(fmin(topLeft, topRight), fmin(bottomLeft, bottomRight));
}

bool isVisited(Node* node, int x, int y) {
    while (node != NULL) {
        if (node->x == x && node->y == y) {
            return true;
        }
        node = node->parent;
    }
    return false;
}

bool isGoal(Node* node, Problem* problem) {
    return node->depth == problem->n * problem->n;
}

Node** expand(Node* node, Problem* problem, const char* heuristicType, int* size) {
    Node** children = (Node**)malloc(MAX_MOVES * sizeof(Node*));
    if (!children) {
        fprintf(stderr, "Error: Memory allocation failed for children.\n");
        exit(EXIT_FAILURE);
    }
    *size = 0;

    for (int i = 0; i < MAX_MOVES; i++) {
        int newX = node->x + problem->moves[i][0];
        int newY = node->y + problem->moves[i][1];
        if (isValid(node, newX, newY, problem)) {
            Node* child = createNode(newX, newY, node->depth + 1, node);
            if (strcmp(heuristicType, "h1b") == 0) {
                calculateH1b(child, problem);
            } else if (strcmp(heuristicType, "h2") == 0) {
                calculateH2(child, problem);
            }
            children[(*size)++] = child;
        }
    }

    return children;
}

bool isValid(Node* node, int x, int y, Problem* problem) {
    return x >= 0 && y >= 0 && x < problem->n && y < problem->n && !isVisited(node, x, y);
}

Node* treeSearch(Problem* problem, const char* strategy, long timeConstraint) {
    Node** frontier = (Node**)malloc(problem->n * problem->n * sizeof(Node*));
    if (!frontier) {
        fprintf(stderr, "Error: Memory allocation failed for frontier.\n");
        exit(EXIT_FAILURE);
    }
    int frontierSize = 0;
    frontier[frontierSize++] = createNode(0, 0, 1, NULL);

    clock_t startTime = clock();
    int nodesExpanded = 0;

    while (frontierSize > 0) {
        clock_t currentTime = clock();
        if (((currentTime - startTime) * 1000 / CLOCKS_PER_SEC) > timeConstraint) {
            printf("Timeout.\n");
            for (int i = 0; i < frontierSize; i++) free(frontier[i]);
            free(frontier);
            return NULL;
        }

        Node* node;
        if (strcmp(strategy, "bfs") == 0) {
            node = frontier[0];
            memmove(&frontier[0], &frontier[1], (--frontierSize) * sizeof(Node*));
        } else if (strcmp(strategy, "dfs") == 0) {
            node = frontier[--frontierSize];
        } else if (strcmp(strategy, "dfs-h1b") == 0 || strcmp(strategy, "dfs-h2") == 0) {
            node = pollWithHeuristic(frontier, &frontierSize);
        } else {
            node = frontier[0];
            memmove(&frontier[0], &frontier[1], (--frontierSize) * sizeof(Node*));
        }

        if (isGoal(node, problem)) {
            printf("Nodes Expanded: %d\n", nodesExpanded);
            for (int i = 0; i < frontierSize; i++) free(frontier[i]);
            free(frontier);
            return node;
        }

        int childrenSize;
        Node** children = expand(node, problem, strstr(strategy, "h") ? strategy + 4 : "", &childrenSize);
        nodesExpanded++;
        for (int i = 0; i < childrenSize; i++) {
            frontier[frontierSize++] = children[i];
        }
        free(children);
    }

    printf("No solution exists.\n");
    printf("Nodes Expanded: %d\n", nodesExpanded);
    for (int i = 0; i < frontierSize; i++) free(frontier[i]);
    free(frontier);
    return NULL;
}

Node* pollWithHeuristic(Node** frontier, int* frontierSize) {
    int bestIndex = 0;
    for (int i = 1; i < *frontierSize; i++) {
        if (frontier[i]->heuristic < frontier[bestIndex]->heuristic) {
            bestIndex = i;
        }
    }
    Node* bestNode = frontier[bestIndex];
    memmove(&frontier[bestIndex], &frontier[bestIndex + 1], (--(*frontierSize) - bestIndex) * sizeof(Node*));
    return bestNode;
}

void printBoard(Node* node, int n) {
    int** board = (int**)malloc(n * sizeof(int*));
    if (!board) {
        fprintf(stderr, "Error: Memory allocation failed for board.\n");
        exit(EXIT_FAILURE);
    }
    for (int i = 0; i < n; i++) {
        board[i] = (int*)calloc(n, sizeof(int));
        if (!board[i]) {
            fprintf(stderr, "Error: Memory allocation failed for board row.\n");
            exit(EXIT_FAILURE);
        }
    }

    while (node != NULL) {
        board[node->x][node->y] = node->depth;
        node = node->parent;
    }

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            printf("%3d ", board[i][j]);
        }
        printf("\n");
        free(board[i]);
    }
    free(board);
}

void printPath(Node* node) {
    if (node == NULL) return;
    printPath(node->parent);
    printf("(%d, %d) -> ", node->x, node->y);
}

void freeNodes(Node* node) {
    if (node == NULL) return;
    freeNodes(node->parent);
    free(node);
}

int main() {
    int n;
    char strategy[10];
    int timeLimitInMinutes;

    printf("Enter board size (n): ");
    scanf("%d", &n);

    printf("Enter search method (bfs, dfs, dfs-h1b, dfs-h2): ");
    scanf("%s", strategy);

    printf("Enter time constraint in minutes: ");
    scanf("%d", &timeLimitInMinutes);

    Problem problem = {n, {{-2, -1}, {-1, -2}, {1, -2}, {2, -1}, {2, 1}, {1, 2}, {-1, 2}, {-2, 1}}};
    long timeConstraint = timeLimitInMinutes * 60 * 1000;

    Node* result = treeSearch(&problem, strategy, timeConstraint);

    if (result != NULL) {
        printf("A solution was found:\n");
        printPath(result);
        printf("END\n");
        printBoard(result, n);
        freeNodes(result);
    } else {
        printf("No solution found.\n");
    }

    return 0;
}
