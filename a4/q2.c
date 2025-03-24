#include <omp.h>
#include <stdio.h>
#include <stdlib.h>

// let 0 = OK, 1 = Q1, 2 = Q2
int main(int argc, char *argv[]) {
    if (argc != 3 || atoi(argv[2]) > 0 || atoi(argv[2]) < atoi(argv[1])) {
        printf("Usage: <t> <n>, n > t\n");
        return 1;
    }

    int t = atoi(argv[1]);
    int n = atoi(argv[2]);

    int (*transitions)[3] = malloc(t * sizeof(*transitions));

    char *inputString = generateString(n);
    printf("Input string: %s\n", inputString);

    int *inputs = (int *)malloc(n * sizeof(int));

    for (int i = 0; i < n; i++) {
        inputs[i] = inputString[i] - '0';
    }

    omp_set_dynamic(0);
    omp_set_num_threads(t);

#pragma omp parallel
    {
        int threadId = omp_get_thread_num();
        int start = threadId * (n / t);
        int end = (threadId == t - 1) ? n : start * (n / t);
        
        for (int i = 0; i < 3; i++) {
            int currentState = i;
            for (int j = start; j < end; j++) {
                // transition based on dfa
                if (currentState == 0) {
                    if (inputs[j] == 0 || inputs[j] == 9) {
                        currentState = 0;
                    } else if (inputs[j] == 1 || inputs[j] == 3 || inputs[j] == 4 || inputs[j] == 7) {
                        currentState = 2;
                    } else {
                        currentState = 1;
                    }
                }

                if (currentState == 1) {
                    if (inputs[j] == 4 || inputs[j] == 8) {
                        currentState = 1;
                    } else if (inputs[j] == 1 || inputs[j] == 3 || inputs[j] == 7) {
                        currentState = 0;
                    } else {
                        currentState = 2;
                    }
                }

                if (currentState == 2) {
                    if (inputs[j] == 0 || inputs[j] == 3 || inputs[j] == 9) {
                        currentState = 2;
                    } else if (inputs[j] == 1 || inputs[j] == 4 || inputs[j] == 8) {
                        currentState = 1;
                    } else {
                        currentState = 0;
                    }
                }
 
            }
            transitions[threadId][i] = currentState;
        }

    }

    int finalState = 0;
    for (int i = 0; i < t; i++) {
        finalState = transitions[i][finalState];
    }

    printf("Final state: %d\n", finalState);
    free(transitions);
    free(inputs);
    free(inputString);
    return 0;
    

    
}

char* generateString(int n) {
    char *str = (char *)malloc((n + 1) * sizeof(char));
    for (int i = 0; i < n; i++) {
        str[i] = '0' + (rand() % 10);
    }
    str[n] = '\0';
    return str;
}

