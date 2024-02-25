/**
 * HTML-based single item.
 *
 * <p>
 * A single item can be embedded in a page of an e-text as a formative assessment or exercise.  Pages could be
 * configured not to show the portion of the page after the item until it has been attempted/completed.  Items can be
 * configured to show answers or full solutions after N attempts.
 *
 * <p>
 * An item could appear on multiple pages, and students could have an active session on each page, so these sessions
 * are managed by a GUID (globally unique ID) associated with the placement if the item in the page.
 *
 * <p>
 * Data associated with an item placement in a page, in XML format, includes:
 *
 * <ul>
 *     <li>GUID (String)</li>
 *     <li>Assessment item ID (String)</li>
 *     <li>Maximum total attempts allowed (integer, null for unlimited)</li>
 *     <li>Behavior after incorrect answer (enum: hide, show "incorrect", show answer, show solution)</li>
 *     <li>Behavior after correct answer (enum: hide, show "correct", show answer, show solution)</li>
 *     <li>Display hints (enum: always | after incorrect)</li>
 *     <li>Allow regenerate (boolean)</li>
 * </ul>
 *
 * A document may have blocks whose visibility are controlled by item status, each with these attributes:
 *
 * <ul>
 *     <li>Controlling GUID (String)</li>
 *     <li>Behavior (enum: hide until attempted, hide until correct, show until attempted, show until correct)</li>
 * </ul>
 *
 * <p>
 * The state of an item within a student's e-text is stored based on the student ID and GUID of the item placement.
 * This state includes:
 * <ul>
 *     <li>nbr_instances_generated (integer)</li>
 *     <li>current_instance (integer)</li>
 *     <li>first_attempt_date_time</li>
 *     <li>last_attempt_date_time</li>
 *     <li>total_attempts (integer)</li>
 *     <li>attempts_current_instance (integer)</li>
 *     <li>correct_overall (boolean)</li>
 *     <li>correct_current_instance (boolean)</li>
 *     <li>student_response (text, CSV list of field values)</li>
 * </ul>
 */
package dev.mathops.web.site.html.item;
