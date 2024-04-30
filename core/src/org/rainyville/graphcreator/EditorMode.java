package org.rainyville.graphcreator;

/**
 * PACKAGE: org.rainyville.graphcreator
 * DATE: 4/28/2024
 * TIME: 4:53 PM
 * PROJECT: GraphCreator
 */
public enum EditorMode {
    VERTEX(true),
    EDGE(true),
    COLOR,
    NONE;

    private static final EditorMode[] vals = values();
    private final boolean hasDelete;

    EditorMode() {
        hasDelete = false;
    }

    EditorMode(boolean hasDelete) {
        this.hasDelete = hasDelete;
    }

    public EditorMode next() {
        return vals[(this.ordinal() + 1) % vals.length];
    }

    public boolean hasDelete() {
        return hasDelete;
    }
}
