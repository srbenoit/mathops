/**
 * Panels relating to the testing center.
 *
 * <p>
 * From "pendex.4gl":
 *
 * <pre>
 * pending_ex()  - presents the items under the "Exams" menu:
 *                 Add  Delete  Check_ans  make-Up  Exams  Homework  Pick  lock  Quit
 *
 * add_online()  - issue an exam (must have student picked, clear_type < 4)
 *
 * del_online()  - delete an exam (must have clear_type < 4)
 *
 * check_ans()   - check/update answers for an exam (must have clear_type < 4)
 *                 this function is complex - after an exam's answers are updated, if the passing
 *                 status has changed, the course needs to be updated.  If the exam was one that
 *                 generates a placement result (a test score in Banner), that test score may have
 *                 to be rolled back, or a new test score sent.
 *
 * makeup()      - issue calculator or make-up exam (must have clear_type < 4)
 *
 * view_stexam() - View all unit and review exams on record (any clear_type)
 *
 * view_sthomework() - View all homework on record (any clear_type)
 *
 * mod_pending() - OBSOLETE (used to change serial # on exam in progress)
 *
 * get_course()  - prompt for a course and unit and validate
 *
 * del_pending() - deletes a selected pending exam row (after prompting for confirm)  If the exam is
 *                 M 100P, deletes the mpe_log row as well.  It does not appear to clean a client_pc
 *                 row if one had been assigned.
 *
 * NOTE: it would be nice to have a function to "force-submit" an exam (or all exams in the room,
 *       a few minutes after closing) rather than just deleting it.
 *
 * NOTE: it would be nice to have a "add hand graded exam" that does all post-exam updates.
 *
 * NOTE: it would be nice to be able to "pause all exams" if the testing center needs to be
 *       evacuated (say, for a fire alarm), so students could re-enter and resume afterward.
 *       Ideally, screens should show student name/ID on screen with staff checkin to restart exam
 *       once student is verified to be back in their seat.  It might also be nice to be able to
 *       pause an exam while a student is drawn aside to discuss a possible cheating incident, and
 *       if it turns out to be nothing, the exam could be resumed without loss of time.
 * </pre>
 */
package dev.mathops.app.adm.testing;
