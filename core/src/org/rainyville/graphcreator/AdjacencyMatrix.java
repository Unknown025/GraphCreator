package org.rainyville.graphcreator;

import java.util.HashSet;

/**
 * PACKAGE: org.rainyville.graphcreator
 * DATE: 4/28/2024
 * TIME: 8:36 PM
 * PROJECT: GraphCreator
 */
public class AdjacencyMatrix {
    // row -> col
    private int[][] matrix;
    private int vertices;
    private int edges;

    public AdjacencyMatrix() {
        matrix = new int[vertices][edges];
    }

    public void addVertex() {
        ++vertices;
        int[][] old = matrix.clone();
        matrix = new int[vertices][edges];
        for (int i = 0; i < old.length; i++) {
            System.arraycopy(old[i], 0, matrix[i], 0, old[i].length);
        }
    }

    public void removeVertex(int vertex) {
        HashSet<Integer> deleteList = new HashSet<>();
        for (int edge = 0; edge < matrix[vertex].length; edge++) {
            if (matrix[vertex][edge] == 1) {
                deleteList.add(edge);
            }
        }

        edges -= deleteList.size();
        --vertices;

        int[][] old = matrix.clone();
        matrix = new int[vertices][edges];
        int a = 0, b = 0;
        for (int i = 0; i < old.length; i++) {
            if (i == vertex) continue;

            for (int j = 0; j < old[i].length; j++) {
                if (deleteList.contains(j)) continue;

                matrix[a][b++] = old[i][j];
            }
            ++a;
            b = 0;
        }
    }

    public void addEdge(int from, int to) {
        ++edges;
        int[][] old = matrix.clone();
        matrix = new int[vertices][edges];
        for (int i = 0; i < old.length; i++) {
            System.arraycopy(old[i], 0, matrix[i], 0, old[i].length);
        }
        matrix[from][edges - 1] = 1;
        matrix[to][edges - 1] = 1;
    }

    public void removeEdge(int edge) {
        --edges;
        int[][] old = matrix.clone();
        matrix = new int[vertices][edges];
        for (int i = 0; i < matrix.length; i++) {
            if (edge >= 0) System.arraycopy(old[i], 0, matrix[i], 0, edge);
            if (old[i].length - (edge + 1) >= 0)
                System.arraycopy(old[i], edge + 1, matrix[i], edge + 1 - 1, old[i].length - (edge + 1));
        }
    }

    public void removeEdge(int from, int to) {
        int column = -1;

        for (int e = 0; e < matrix[from].length; e++) {
            if (matrix[from][e] == 1) {
                for (int v = 0; v < matrix.length; v++) {
                    if (matrix[v][e] == 1 && v == to) {
                        column = e;
                        break;
                    }
                }
            }
        }
        // Couldn't find a column to delete
        if (column == -1) return;

        --edges;
        int[][] old = matrix.clone();
        matrix = new int[vertices][edges];
        int a = 0, b = 0;
        for (int v = 0; v < matrix.length; v++) {
            for (int e = 0; e < matrix[v].length; e++) {
                if (e == column) continue;

                matrix[a][b++] = old[v][e];
            }
            ++a;
            b = 0;
        }
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public int getVertices() {
        return vertices;
    }

    public int getEdges() {
        return edges;
    }
}
