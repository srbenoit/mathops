/**
 * Classes to manage semester schedules, including the number of weeks in a semester, the days of week and hours each
 * day campus is available, and the type of daily blocking to use.
 *
 * <p>
 * Each 1-credit unit requires 800 contact minutes (counting final exam week).  This requires 50 minutes per week for a
 * 16-week term, 70 per week for a 12-week term, 100 per week for an 8-week term (the shortest we contemplate). Most
 * courses are 3-credits, so daily schedules should be designed to accommodate 3-credit courses.
 *
 * <p>
 * In a 16-week semester, a 3-credit course (needing 150 minutes per week of contact) could meet for three days at 50
 * minutes per day or two days at 75 minutes per day, so we might choose to have Monday, Wednesday, and Friday operate
 * on 1-hour periods with 10-minute passing time, and Tuesday, Thursday, and Saturday/Sunday, if they are included,
 * operate with 75-minute periods with 15-minute passing time.
 *
 * <p>
 * In a 12-week semester, a 3-credit course (needing 210 minutes per week of contact) could meet for three days at 70
 * minutes per day or two days at 105 minutes per day, so we might choose to have Monday, Wednesday, and Friday operate
 * on 90-minute periods with 20-minute passing time, and Tuesday, Thursday, and Saturday/Sunday, if they are included,
 * operate with 120-minute periods with 15-minute passing time.
 *
 * <p>
 * In an 8-week semester, a 3-credit course (needing 300 minutes per week of contact) could meet for three days at 100
 * minutes per day or two days at 150 minutes per day, so we might choose to have Monday, Wednesday, and Friday operate
 * on 50-minute periods with 10-minute passing time (2 periods per class), and Tuesday, Thursday, and Saturday/Sunday,
 * if they are included, operate with 75-minute periods with 15-minute passing time (2 periods per class).  This has the
 * advantage that one could run a "late-start" 8-week semester in tandem with a 16-week semester.
 */
package dev.mathops.app.sim.schedule;
