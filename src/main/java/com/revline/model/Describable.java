package com.revline.model;

/**
 * Marker interface: any entity that can describe itself in a human-friendly way.
 */
public interface Describable {

    /** Returns a human-readable description of this entity. */
    String describe();
}
