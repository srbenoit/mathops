/**
 * Classes that support generation of HTML examples from XML source.  Examples are accessible HTML documents that can be
 * inserted into other pages (they are block-level elements) that include a statement of the learning objective(s), the
 * example prompt, and optional video walk-through, scaffolded hints, and full solution, as well as links to relevant
 * lessons.
 *
 * <p>
 * Emitted content has a top-level heading to denote the example, and subordinate headings to represent the prompt,
 * video walkthrough, solution, etc.  When the example is emitted, the caller can specify the heading level to use as
 * the "top-level" heading, so it flows properly into HTML heading hierarchies.
 *
 * <pre>
 *   &lt;example id="...">
 *     &lt;title>The title, emitted as a top-level heading (level from 1 to 5).&lt;/title>
 *     &lt;objective>The learning objective. (there may be more than one of these).&lt;/objective>
 *     &lt;prompt>
 *       &lt;heading>Prompt heading, emitted as heading level below top-level heading.&lt;/heading>
 *       &lt;content>
 *         ... Document content ...
 *       &lt;/content>
 *     &lt;/prompt>
 *     &lt;hint>  <!--  (there may be multiple hints, user can "unhide" them in turn using the hint button...) -->
 *       &lt;button>The button text used to show the hint (default is "Hint #")&lt;/button>
 *       &lt;content>
 *         ... Document content ...
 *       &lt;/content>
 *     &lt;/hint>
 *     &lt;walkthrough>
 *       &lt;heading>Walkthrough heading, emitted as heading level below top-level heading.&lt;/heading>
 *       &lt;content>
 *         ... Document content - typically a video with associated VTT file and a text-transcript ...
 *       &lt;/content>
 *     &lt;/walkthrough>
 *     &lt;solution>
 *       &lt;heading>Solution heading, emitted as heading level below top-level heading.&lt;/heading>
 *       &lt;content>
 *         ... Document content ...
 *       &lt;/content>
 *     &lt;/solution>
 *   &lt;/example>
 * </pre>
 */
package dev.mathops.assessment.example;
