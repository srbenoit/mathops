package dev.mathops.app.scheduling;

/**
 * A student who wants to enroll in classes.  Given the list of classes available in a semester, each student will have
 * some target number of credits, and some preference level for each class.  We can then simulate their registration
 * choices to optimize their preferences along with the convenience of their daily schedule or commute, gaps for lunch,
 * etc.
 *
 * <p>
 * Many students in a simulation could share the same class preferences, so we manage those preferences as a separate
 * object.
 *
 * @param id          the student ID
 * @param minCredits  the minimum number of credits the student wants
 * @param maxCredits  the maximum number of credits the student wants
 * @param preferences the student's class preferences
 */
record EnrollingStudent(int id, int minCredits, int maxCredits, ClassPreferences preferences) {
}
