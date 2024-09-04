/**
 * A registration simulation designed to determine, for a given set of classrooms and offered courses, and a specified
 * set of student preferences for courses, how many students can be accommodated.
 *
 * <p>
 * A registration simulation begins with a variable-size population of students, grouped by preferences regarding
 * classes they want to take (for example, 10% of students have preference set 1, 8% have preference set 2, etc.), and a
 * list of offered classes.  It first performs a registration cycle to determine the number of seats of each course that
 * should be offered to meet projected demand.
 *
 * <p>
 * The simulation next considers the set of classrooms and hour blocks available, and the number of classroom blocks
 * each class (or its lab) needed.  It determines possible section sizes and classroom assignments and tries to optimize
 * classroom capacity usage and minimize needed sections.  During this phase, the size of the population is increased
 * until classroom capacity is exceeded, giving an absolute upper bound on student population.  A "realistic" student
 * population size is then selected as some percentage of this absolute bound.
 *
 * <p>
 * Finally, it simulates the registration process some number of times to get lists of student schedules. Then for each
 * set of schedules, and each possible ordering of hour blocks within days, it computes the "best" time for instructors
 * to hold office hours (when the instructor is not teaching, and the maximum number of students in their courses are
 * on-campus but not in class, and a "desirability" score based on (1) how many days a week students need to travel to
 * campus, (2) how many hours they need to spend on campus each day to take classes, (3) how many days a week
 * instructors need to travel to campus, (4) how many hours instructors need to spend on campus each day, and (5) how
 * many instructors are needed.
 *
 * <p>
 * At the end of the process, the simulation emits the number of sections of each course needed, room assignments, and
 * the two or three "best" schedule layouts (those with the highest desirability scores).
 *
 * <p>
 * Each semester, the group preference matrices can be updated to refine estimations of demand, and could even include
 * statistical parameters like mean and standard deviation to support stochastic modeling of student registration
 * choices.
 */
package dev.mathops.app.sim.schedule;
