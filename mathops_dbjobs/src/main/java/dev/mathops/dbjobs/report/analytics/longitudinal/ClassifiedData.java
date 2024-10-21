package dev.mathops.dbjobs.report.analytics.longitudinal;

import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Student course records divided by classification.  Lists of second course records are  organized by integer term code
 * (like 202490), then by first course classification (local course with A, B, or lower grade, transfer credit with A,
 * B, or lower grade, or AP/IB/CLEP credit.
 */
public final class ClassifiedData {

    /** The initial size for lists. */
    private static final int INIT_SIZE = 20;

    /** Map from term to the list of second-course rows for students who passed first course locally with an A. */
    private final Map<Integer, List<EnrollmentRec>> localA;

    /** Map from term to the list of second-course rows for students who passed first course locally with a B. */
    private final Map<Integer, List<EnrollmentRec>> localB;

    /** Map from term to the list of second-course rows for students who passed first course locally with a C or D. */
    private final Map<Integer, List<EnrollmentRec>> localCD;

    /** Map from term to the list of second-course rows for students who transferred first course with an A. */
    private final Map<Integer, List<EnrollmentRec>> transferA;

    /** Map from term to the list of second-course rows for students who transferred first course with a B. */
    private final Map<Integer, List<EnrollmentRec>> transferB;

    /** Map from term to the list of second-course rows for students who transferred first course with a C or D. */
    private final Map<Integer, List<EnrollmentRec>> transferCD;

    /** Map from term to the list of second-course rows for students who passed first course via AP/IB/CLEP. */
    private final Map<Integer, List<EnrollmentRec>> ap;

    /**
     * Constructs a new {@code ClassifiedData}.
     */
    ClassifiedData() {

        this.localA = new HashMap<>(20);
        this.localB = new HashMap<>(20);
        this.localCD = new HashMap<>(20);
        this.transferA = new HashMap<>(20);
        this.transferB = new HashMap<>(20);
        this.transferCD = new HashMap<>(20);
        this.ap = new HashMap<>(20);
    }

    /**
     * Clears all stored data.
     */
    void clear() {

        this.localA.clear();
        this.localB.clear();
        this.localCD.clear();
        this.transferA.clear();
        this.transferB.clear();
        this.transferCD.clear();
        this.ap.clear();
    }

    /**
     * Creates lists to store data for a new term key.
     *
     * @param key the key
     */
    void createKey(final Integer  key) {

        this.localA.put(key, new ArrayList<>(INIT_SIZE));
        this.localB.put(key, new ArrayList<>(INIT_SIZE));
        this.localCD.put(key, new ArrayList<>(INIT_SIZE));
        this.transferA.put(key, new ArrayList<>(INIT_SIZE));
        this.transferB.put(key, new ArrayList<>(INIT_SIZE));
        this.transferCD.put(key, new ArrayList<>(INIT_SIZE));
        this.ap.put(key, new ArrayList<>(INIT_SIZE));
    }


    /** Map from term to the list of second-course rows for students who passed first course locally with an A. */
    Map<Integer, List<EnrollmentRec>> localA() {

        return this.localA;
    }

    /** Map from term to the list of second-course rows for students who passed first course locally with a B. */
    Map<Integer, List<EnrollmentRec>> localB() {

        return this.localB;
    }

    /** Map from term to the list of second-course rows for students who passed first course locally with a C or D. */
    Map<Integer, List<EnrollmentRec>> localCD() {

        return this.localCD;
    }

    /** Map from term to the list of second-course rows for students who transferred first course with an A. */
    Map<Integer, List<EnrollmentRec>> transferA() {

        return this.transferA;
    }

    /** Map from term to the list of second-course rows for students who transferred first course with a B. */
    Map<Integer, List<EnrollmentRec>> transferB() {

        return this.transferB;
    }

    /** Map from term to the list of second-course rows for students who transferred first course with a C or D. */
    Map<Integer, List<EnrollmentRec>> transferCD() {

        return this.transferCD;
    }

    /** Map from term to the list of second-course rows for students who passed first course via AP/IB/CLEP. */
    Map<Integer, List<EnrollmentRec>> ap() {

        return this.ap;
    }
}
