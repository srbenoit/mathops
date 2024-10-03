/**
 * Classes to manage semester schedules, including the number of weeks in a semester, the days of week and hours each
 * day campus is available, and the type of daily blocking to use.
 *
 * <p>
 * Each 1-credit unit requires 800 contact minutes (counting final exam week).  This requires 50 minutes per week for a
 * 16-week term, 70 per week for a 12-week term, 90 per week for a 9-week term, 100 per week for an 8-week term, 135 per
 * week for a 6-week term, 160 per week for a 5-week term, and 200 per week for a 4-week term (the shortest we
 * contemplate). Most courses are 3-credits, so daily schedules should be designed to accommodate 3-credit courses.
 *
 * <p>
 * In a 16-week semester, a 3-credit course (needing 150 minutes per week of contact) could meet for three days at 50
 * minutes per day or two days at 75 minutes per day, so we might choose to have Monday, Wednesday, and Friday operate
 * on 1-hour blocks with 10-minute passing time, and Tuesday, Thursday, and Saturday/Sunday, if they are included,
 * operate with 75-minute periods with 15-minute passing time.
 */
package dev.mathops.app.sim.schedule;
