#include <omp.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <time.h>

char* generateString(int n) {
    char *str = (char *)malloc((n + 1) * sizeof(char));
    for (int i = 0; i < n; i++) {
        // random number between 0 and 9
        str[i] = '0' + (rand() % 10);
    }
    // end string
    str[n] = '\0';
    return str;
}

// 0 = OK, 1 = left state, 2 = right state
int main(int argc, char *argv[]) {
    if (argc != 3 || atoi(argv[1]) < 0 || atoi(argv[1]) >= atoi(argv[2])) {
        printf("Usage: <t> <n>, n > t >= 0\n");
        return 1;
    }

    // set vars, t = deterministic + optimistic threads
    int t = atoi(argv[1]) + 1;
    int n = atoi(argv[2]);
    bool acceptState = false;

    // transition table to store the state transitions of threads
    int (*transitions)[3] = malloc(t * sizeof(*transitions));

    char *inputString = generateString(n);
    printf("Input string: %s\n", inputString);

    // convert input string to int array
    int *inputs = (int *)malloc(n * sizeof(int));

    for (int i = 0; i < n; i++) {
        inputs[i] = inputString[i] - '0';
    }

    // set number of threads
    omp_set_dynamic(0);
    omp_set_num_threads(t);

    // time parallel section, based on https://people.cs.rutgers.edu/~pxk/416/notes/c-tutorials/gettime.html
    struct timespec startTime, endTime;
    clock_gettime(CLOCK_MONOTONIC, &startTime);

#pragma omp parallel for
    // section of input string for each thread
    for (int x = 0; x < t; x++) {
        // iterate through the input array, optimistic approach with storing transitions of all 3 states
        for (int i = 0; i < 3; i++) {
            // determine start and end indexes based on section of the input string determined by x
            int currentState = i;
            int start = (n / t) * x;
            int end = (x == t - 1) ? n : start + (n/t);

            for (int j = start; j < end; j++) {
                int previousState = currentState;

                // transition based on dfa
                // OK state
                if (previousState == 0) {
                    if (inputs[j] == 0 || inputs[j] == 9) {
                        currentState = 0;
                    } else if (inputs[j] == 1 || inputs[j] == 3 || inputs[j] == 4 || inputs[j] == 7) {
                        currentState = 2;
                    } else {
                        currentState = 1;
                    }
                }

                // Left state
                if (previousState == 1) {
                    if (inputs[j] == 4 || inputs[j] == 8) {
                        currentState = 1;
                    } else if (inputs[j] == 1 || inputs[j] == 3 || inputs[j] == 7) {
                        currentState = 0;
                    } else {
                        currentState = 2;
                    }
                }

                // Right state
                if (previousState == 2) {
                    if (inputs[j] == 0 || inputs[j] == 3 || inputs[j] == 9) {
                        currentState = 2;
                    } else if (inputs[j] == 1 || inputs[j] == 4 || inputs[j] == 8) {
                        currentState = 1;
                    } else {
                        currentState = 0;
                    }
                }
            }
            // store the final state of the thread for each initial state based on section of the input string
            transitions[x][i] = currentState;
        }
    }

    // determine final DFA state
    int finalState = 0;
    for (int i = 0; i < t; i++) {
        finalState = transitions[i][finalState];
    }

    //printf("Final state: %d\n", finalState);
    if (finalState == 0) {
        acceptState = true;
    }

    // end time for parallel section
    clock_gettime(CLOCK_MONOTONIC, &endTime);
    float totalTime = (endTime.tv_sec - startTime.tv_sec) * 1000.0 + (endTime.tv_nsec - startTime.tv_nsec) / 1e6; // convert to milliseconds

    // print output
    printf("%s\n", acceptState ? "true" : "false");
    printf("Time taken: %f ms\n", totalTime);

    // free memory
    free(transitions);
    free(inputs);
    free(inputString);
    return 0;
       
}



