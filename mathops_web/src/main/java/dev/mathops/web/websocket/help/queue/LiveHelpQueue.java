package dev.mathops.web.websocket.help.queue;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.EmptyElement;
import dev.mathops.commons.parser.xml.IElement;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.parser.xml.NonemptyElement;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.web.websocket.help.StudentKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A queue of active live help request entries.
 *
 * <p>
 * Request entries are created by students requesting help from some context, and can be deleted three ways:
 *
 * <ul>
 * <li>They can be canceled by the student (either actively, or by closing their browser).
 * <li>They are deleted when the live help hours of operation end.
 * <li>They are deleted when they are converted into an active help session at the time a tutor
 * accepts the queued request.
 * </ul>
 *
 * <p>
 * The help queue should survive a server bounce - the queue should be persisted on shutdown of the
 * server and restored on startup.
 *
 * <p>
 * There can be at most one entry in the queue (active or canceled) for a particular student ID, so
 * when a new entry is queued, any existing entries with the same user ID are automatically deleted
 * (or replaced by the new request, so the student does not lose their place in the queue).
 */
public final class LiveHelpQueue {

    /** Timeout (in ms) before an active request is moved to CANCELED status - 15 seconds. */
    // private static final long ACTIVE_TIMEOUT_MS = 15 * 1000;
    private static final long ACTIVE_TIMEOUT_MS = (long) (1500 * 1000);

    /** Timeout (in ms) before a canceled request is deleted - 5 minutes. */
    private static final long CANCELED_TIMEOUT_MS = (long) (5 * 60 * 1000);

    /** Hours the live help system is operational. */
    final LiveHelpHours hours;

    /** The active list of queued requests. */
    private final SortedMap<Long, LiveHelpQueueEntry> active;

    /** Recently inactive requests that could be reactivated. */
    private final Map<Long, LiveHelpQueueEntry> inactive;

    /** The list of tutors who are online. */
    private final List<LiveHelpOnlineTutor> tutors;

    /** The list of administrators who are online. */
    private final List<LiveHelpOnlineAdministrator> administrators;

    /** The average wait time - a rolling average of the last 10 waits, initialized to 5 min. */
    private long avgWaitTime = (long) (5 * 60 * 1000);

    /**
     * Constructs a new {@code LiveHelpQueue}.
     */
    public LiveHelpQueue() {

        this.hours = new LiveHelpHours();
        this.active = new TreeMap<>();
        this.inactive = new HashMap<>(20);
        this.tutors = new ArrayList<>(5);
        this.administrators = new ArrayList<>(2);

        // BEGIN TEST DATA: Add some test data to check sending of the queue to the tutor site

        // final long t1 = System.currentTimeMillis() - 152000;
        // this.inactive.put(Long.valueOf(t1),
        // LiveHelpQueueEntry.forCourse(
        // new StudentKey("899123456", "Leonard", "Yechiel", "Lenny Yechiel"),
        // RawRecordConstants.M117, 3, 1,
        // t1, null));
        //
        // final long t2 = System.currentTimeMillis() - 185000;
        // enqueue(LiveHelpQueueEntry.forCourse(
        // new StudentKey("899123457", "Yash", "Agnieszka", "Yash Agnieszka"),
        // RawRecordConstants.M118, 1, 4, t2,
        // null));
        //
        // final long t3 = System.currentTimeMillis() - 141000;
        // enqueue(LiveHelpQueueEntry.forCourse(
        // new StudentKey("899123458", "Georg", "Dip", "Georg Dip"), RawRecordConstants.M124, 1, 4,
        // t3, null));
        //
        // final long t4 = System.currentTimeMillis() - 123000;
        // enqueue(LiveHelpQueueEntry.forCourse(
        // new StudentKey("899123459", "Karter", "Juhani", "Karter Juhani"),
        // RawRecordConstants.M125, 3, -1, t4,
        // null));
        //
        // final long t5 = System.currentTimeMillis() - 94000;
        // enqueue(LiveHelpQueueEntry.forCourse(
        // new StudentKey("899123460", "Gianmaria", "Tagwanibisan", "Gia Tagwanibisan"),
        // RawRecordConstants.M126,
        // -1, -1, t5, null));
        //
        // final long t6 = System.currentTimeMillis() - 74000;
        // enqueue(LiveHelpQueueEntry.forHomework(
        // new StudentKey("899123460", "Bjarni", "Ingrida", "Bjarni Ingrida"), "123456", t6,
        // null));
        //
        // final long t7 = System.currentTimeMillis() - 55000;
        // enqueue(LiveHelpQueueEntry.forPastExam(
        // new StudentKey("899123461", "Ljilja", "Marwa", "Lilly Marwa"), "987654", t7, null));
        //
        // final long t8 = System.currentTimeMillis() - 30000;
        // enqueue(LiveHelpQueueEntry.forMedia(
        // new StudentKey("899123462", "Kenton", "Jonatan", "Kenton Jonatan"), "919191", t8,
        // null));

        // this.tutors.add(
        // new LiveHelpOnlineTutor(new StudentKey("899654321", "Nima", "Nasser", "Nima Nasser")));
        // this.tutors.add(new LiveHelpOnlineTutor(
        // new StudentKey("899654322", "Emilija", "Lisanne", "Emilija Lisanne")));
        // this.tutors.add(new LiveHelpOnlineTutor(
        // new StudentKey("899654323", "Ellington", "Crocifissa", "Ellington Crocifissa")));

        // END TEST DATA
    }

    /**
     * Adds an entry to the queue.
     *
     * <p>
     * If there is an existing entry in the queue with a matching student ID, that old entry is first deleted. The
     * student does not retain their place in line.
     *
     * @param entry the entry to add
     */
    void enqueue(final LiveHelpQueueEntry entry) {

        // If the policy of moving new student requests to the end causes student complaints, it
        // could be adjusted so that the new entry replaces the old entry, taking its place in the
        // queue - we would need to adjust the ID of the new entry to be that of the old so the
        // sorted queue keeps it in the correct location.

        final String studentId = entry.student.studentId;

        synchronized (this) {
            this.inactive.remove(entry.id);

            Long oldId = null;

            // Remove canceled requests with matching student ID
            final Iterator<Map.Entry<Long, LiveHelpQueueEntry>> canceledIter = this.inactive.entrySet().iterator();

            while (canceledIter.hasNext()) {
                final Map.Entry<Long, LiveHelpQueueEntry> next = canceledIter.next();
                if (studentId.equals(next.getValue().student.studentId)) {
                    oldId = next.getKey();
                    canceledIter.remove();
                }
            }

            // Remove active requests with matching student ID
            final Iterator<Map.Entry<Long, LiveHelpQueueEntry>> activeIter =
                    this.active.entrySet().iterator();

            while (activeIter.hasNext()) {
                final Map.Entry<Long, LiveHelpQueueEntry> next = activeIter.next();
                if (studentId.equals(next.getValue().student.studentId)) {
                    oldId = next.getKey();
                    activeIter.remove();
                }
            }

            // Log each time a student's new request bumps them out of an earlier position in the
            // queue, so we can see how often this is occurring.
            if (oldId != null) {
                Log.warning("Student ", studentId, " submitted new help queue request and was bumped from ID ",
                        oldId, " to ID ", entry.id);
            }

            this.active.put(entry.id, entry);

            for (final LiveHelpOnlineTutor tut : this.tutors) {
                final HelpQueueWebSocket sock = tut.getWebSocket();
                if (sock != null) {
                    sock.notifyOfQueueChange();
                }
            }
        }
    }

    /**
     * Adds an online tutor. This feeds the display of all online tutors that each tutor can see, and provides a list of
     * tutor web sockets that should be notified when changes to the queue occur.
     *
     * @param theTutor the tutor
     */
    void addTutor(final LiveHelpOnlineTutor theTutor) {

        synchronized (this) {
            this.tutors.remove(theTutor);
            this.tutors.add(theTutor);

            for (final LiveHelpOnlineAdministrator admin : this.administrators) {
                final HelpQueueWebSocket sock = admin.getWebSocket();
                if (sock != null) {
                    sock.notifyAdminOfAddedTutor(theTutor);
                }
            }
        }
    }

    /**
     * Removes any online tutor with a specified student ID.
     *
     * @param studentId the student ID
     */
    void removeTutor(final String studentId) {

        synchronized (this) {
            final Iterator<LiveHelpOnlineTutor> iter = this.tutors.iterator();
            LiveHelpOnlineTutor found = null;
            while (iter.hasNext()) {
                final LiveHelpOnlineTutor tut = iter.next();
                if (tut.student.studentId.equals(studentId)) {
                    iter.remove();
                    found = tut;
                }
            }

            if (found != null) {
                for (final LiveHelpOnlineAdministrator admin : this.administrators) {
                    final HelpQueueWebSocket sock = admin.getWebSocket();
                    if (sock != null) {
                        sock.notifyAdminOfRemovedTutor(found);
                    }
                }
            }
        }
    }

    /**
     * Adds an online administrator. This provides a list of administrator web sockets that should be notified when
     * changes to the queue occur.
     *
     * @param theAdministrator the administrator
     */
    public void addAdministrator(final LiveHelpOnlineAdministrator theAdministrator) {

        synchronized (this) {
            this.administrators.remove(theAdministrator);
            this.administrators.add(theAdministrator);

            for (final LiveHelpOnlineAdministrator admin : this.administrators) {
                final HelpQueueWebSocket sock = admin.getWebSocket();
                if (sock != null) {
                    sock.notifyAdminOfAddedAdministrator(theAdministrator);
                }
            }
        }
    }

    /**
     * Removes any online administrator with a specified student key.
     *
     * @param student the student key
     */
    public void removeAdministrator(final StudentKey student) {

        synchronized (this) {
            final Iterator<LiveHelpOnlineAdministrator> iter = this.administrators.iterator();
            LiveHelpOnlineAdministrator found = null;
            while (iter.hasNext()) {
                final LiveHelpOnlineAdministrator adm = iter.next();
                if (adm.student.equals(student)) {
                    iter.remove();
                    found = adm;
                }
            }

            if (found != null) {
                for (final LiveHelpOnlineAdministrator admin : this.administrators) {
                    final HelpQueueWebSocket sock = admin.getWebSocket();
                    if (sock != null) {
                        sock.notifyAdminOfRemovedAdministrator(found);
                    }
                }
            }
        }
    }

    /**
     * Cancels a queue request entry, which moves it from the queue to a canceled request cache. A canceled request will
     * not be answered by a tutor, but could be restored to the queue (in position based on its request time) until it
     * expires.
     *
     * <p>
     * This is used when a request is not "touched" for ACTIVE_TIMEOUT_MS, and often means the student closed the
     * browser tab or the network connection was lost. For example, if the student closed their laptop to move from
     * building to building, the request should move to the canceled state. When they re-open, the request could be
     * re-established, but it should not be answered while the laptop is closed, and it should not delay answering other
     * students' request.
     *
     * @param entryId the ID of the entry to cancel
     * @return true if a matching entry was found and canceled
     */
    public boolean cancel(final Long entryId) {

        boolean found = false;

        synchronized (this) {
            final LiveHelpQueueEntry entry = this.active.remove(entryId);
            if (entry != null) {
                found = true;
                this.inactive.put(entryId, entry);

                for (final LiveHelpOnlineAdministrator admin : this.administrators) {
                    final HelpQueueWebSocket sock = admin.getWebSocket();
                    if (sock != null) {
                        sock.notifyAdminOfDeactivatedRequest(entry);
                    }
                }
            }
        }

        return found;
    }

    /**
     * Deletes a queue request entry (which can be in either active or inactive status).
     *
     * <p>
     * This is used when the student actively cancels a request.
     *
     * @param entryId the ID of the entry to delete
     * @return true if a matching entry was found and deleted
     */
    public boolean delete(final Long entryId) {

        final boolean found;

        synchronized (this) {
            LiveHelpQueueEntry entry = this.active.remove(entryId);
            if (entry == null) {
                entry = this.inactive.remove(entryId);
            } else {
                this.inactive.remove(entryId);
            }

            if (entry == null) {
                found = false;
            } else {
                found = true;

                for (final LiveHelpOnlineAdministrator admin : this.administrators) {
                    final HelpQueueWebSocket sock = admin.getWebSocket();
                    if (sock != null) {
                        sock.notifyAdminOfDeletedRequest(entry);
                    }
                }
            }
        }

        return found;
    }

    /**
     * Scans for active entries that have not been touched in ACTIVE_TIMEOUT_MS milliseconds, and moves those from
     * Active to Canceled status, and checks any canceled entries that have not been touched in CANCELED_TIMEOUT_MS,
     * deleting those from the queue.
     */
    public void checkTimeouts() {

        final long now = System.currentTimeMillis();
        final long activeLimit = now - ACTIVE_TIMEOUT_MS;
        final long canceledLimit = now - CANCELED_TIMEOUT_MS;

        synchronized (this) {
            final Iterator<Map.Entry<Long, LiveHelpQueueEntry>> canceledIter =
                    this.inactive.entrySet().iterator();

            while (canceledIter.hasNext()) {
                final Map.Entry<Long, LiveHelpQueueEntry> next = canceledIter.next();
                if (next.getKey().longValue() < canceledLimit) {
                    canceledIter.remove();

                    for (final LiveHelpOnlineAdministrator admin : this.administrators) {
                        final HelpQueueWebSocket socket = admin.getWebSocket();
                        if (socket != null) {
                            socket.notifyAdminOfTimedOutInactiveRequest(next.getValue());
                        }
                    }
                }
            }

            final Iterator<Map.Entry<Long, LiveHelpQueueEntry>> activeIter = this.active.entrySet().iterator();

            while (activeIter.hasNext()) {
                final Map.Entry<Long, LiveHelpQueueEntry> next = activeIter.next();
                if (next.getKey().longValue() < activeLimit) {
                    this.inactive.put(next.getKey(), next.getValue());
                    activeIter.remove();

                    for (final LiveHelpOnlineAdministrator admin : this.administrators) {
                        final HelpQueueWebSocket socket = admin.getWebSocket();
                        if (socket != null) {
                            socket.notifyAdminOfTimedOutActiveRequest(next.getValue());
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds a queue entry and "touches" it to prevent it from timing out. This action can move a canceled entry back
     * into the active queue. This also updates the last-touched field in the entry.
     *
     * <p>
     * Called when the server receives a notification from the client browser that the user is still waiting.
     *
     * @param entryId the ID of the entry to touch
     * @return true if a matching entry was found and touched; false if not
     */
    public boolean touch(final Long entryId) {

        boolean found = false;

        synchronized (this) {
            for (final Map.Entry<Long, LiveHelpQueueEntry> next : this.active.entrySet()) {
                if (next.getKey().equals(entryId)) {
                    next.getValue().touch();
                    found = true;

                    for (final LiveHelpOnlineAdministrator admin : this.administrators) {
                        final HelpQueueWebSocket socket = admin.getWebSocket();
                        if (socket != null) {
                            socket.notifyAdminOfTouchOnActive(next.getValue());
                        }
                    }
                    break;
                }
            }

            if (!found) {
                final Iterator<Map.Entry<Long, LiveHelpQueueEntry>> canceledIter = this.inactive.entrySet().iterator();

                while (canceledIter.hasNext()) {
                    final Map.Entry<Long, LiveHelpQueueEntry> next = canceledIter.next();
                    if (next.getKey().equals(entryId)) {
                        next.getValue().touch();
                        canceledIter.remove();
                        this.active.put(next.getKey(), next.getValue());
                        found = true;

                        for (final LiveHelpOnlineAdministrator admin : this.administrators) {
                            final HelpQueueWebSocket socket = admin.getWebSocket();
                            if (socket != null) {
                                socket.notifyAdminOfTouchOnInactive(next.getValue());
                            }
                        }
                        break;
                    }
                }
            }
        }

        return found;
    }

    /**
     * Retrieves the oldest active queue entry (and removes it from the queue). Called when a tutor accepts a request.
     *
     * @return the request; {@code null} if the queue is empty
     */
    public LiveHelpQueueEntry pop() {

        LiveHelpQueueEntry result = null;

        synchronized (this) {
            if (!this.active.isEmpty()) {
                final Iterator<Map.Entry<Long, LiveHelpQueueEntry>> activeIter = this.active.entrySet().iterator();
                result = activeIter.next().getValue();
                activeIter.remove();
            }

            if (result != null) {
                // Update average wait time
                final long wait = result.whenQueued - System.currentTimeMillis();
                final long total = 9L * this.avgWaitTime + wait;
                this.avgWaitTime = (total + 5L) / 10L;

                for (final LiveHelpOnlineAdministrator admin : this.administrators) {
                    final HelpQueueWebSocket sock = admin.getWebSocket();
                    if (sock != null) {
                        sock.notifyAdminOfRequstAccepted(result, this.avgWaitTime);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Generates the serialized JSON representation of the queue, used to save its state on server shutdown so the queue
     * can be restored (using the {@code parse} factory method) after a restart.
     *
     * <pre>
     * { "avgWaitTime": "12:34",
     *   "tutors": [
     *    { "stu": "123456789", "name": "Joe Tutor"},
     *     ... additional tutors ...
     *   ],
     *   "active": [
     *    { "id": "123",
     *      "stu": "123456789",
     *      "screen": "Joe Tutor",
     *      "course": "M 117",
     *      "unit": 3,
     *      "objective": 2,
     *      "hw": "ABC132",
     *      "past": "DEF456",
     *      "queued": 43264634253253245,
     *      "wait": "4:27"},
     *     ... additional queue entries ...
     *   ],
     *   "canceled": [
     *     ... queue entries as above ...
     *   ]
     * }}
     * </pre>
     *
     * @return the JSON serialized representation
     */
    String toJSON() {

        synchronized (this) {
            final HtmlBuilder htm = new HtmlBuilder(100 + 100 * (this.active.size() + this.inactive.size()));

            final int sec = (int) ((this.avgWaitTime + 500L) / 1000L);
            final int mm = sec / 60;
            final int s1 = sec % 60 / 10;
            final int s2 = sec % 10;

            try {
                htm.addln("{ \"avgWaitTime\": \"", Integer.toString(mm), CoreConstants.COLON, Integer.toString(s1),
                        Integer.toString(s2), "\",");

                htm.addln("  \"tutors\": [");
                int len = this.tutors.size();
                for (int i = 0; i < len; ++i) {
                    htm.add("  ");
                    this.tutors.get(i).toJSON(htm);
                    if (i + 1 < len) {
                        htm.add(CoreConstants.COMMA_CHAR);
                    }
                    htm.addln();
                }
                htm.addln("  ],");

                final List<LiveHelpQueueEntry> list = new ArrayList<>(this.active.values());
                len = list.size();

                htm.addln("  \"active\": [");

                for (int i = 0; i < len; ++i) {
                    htm.add("  ");
                    list.get(i).toJSON(htm);
                    if (i + 1 < len) {
                        htm.add(CoreConstants.COMMA_CHAR);
                    }
                    htm.addln();
                }
                htm.addln("  ],");

                list.clear();
                list.addAll(this.inactive.values());
                len = list.size();

                htm.addln("  \"canceled\": [");
                for (int i = 0; i < len; ++i) {
                    htm.add("  ");
                    list.get(i).toJSON(htm);
                    if (i + 1 < len) {
                        htm.add(CoreConstants.COMMA_CHAR);
                    }
                    htm.addln();
                }
                htm.addln("  ]}");

            } catch (final Exception ex) {
                Log.warning(ex);
            }

            return htm.toString();
        }
    }

    /**
     * Generates a serialized XML representation of the queue, used to save its state on server shutdown so the queue
     * can be restored (using the {@code parse} factory method) after a restart.
     *
     * @return the serialized representation
     */
    public String toXML() {

        synchronized (this) {
            final HtmlBuilder htm =
                    new HtmlBuilder(100 + 100 * (this.active.size() + this.inactive.size()));

            htm.startNonempty(0, "help-queue", true);
            htm.startNonempty(1, "active", true);

            for (final LiveHelpQueueEntry entry : this.active.values()) {
                htm.add("  ");
                entry.toXML(htm);
                htm.addln();
            }

            htm.endNonempty(1, "active", true);
            htm.startNonempty(1, "canceled", true);

            for (final LiveHelpQueueEntry entry : this.inactive.values()) {
                htm.add("  ");
                entry.toXML(htm);
                htm.addln();
            }

            htm.endNonempty(1, "canceled", true);
            htm.endNonempty(0, "help-queue", true);

            return htm.toString();
        }
    }

    /**
     * Parsed an XML representation and rebuilds the queue.
     *
     * @param ser the serialization
     * @return the reconstructed {@code LiveHelpQueue}
     * @throws ParsingException if the XML could not be parsed
     */
    public static LiveHelpQueue parseXML(final String ser) throws ParsingException {

        final LiveHelpQueue result = new LiveHelpQueue();

        final XmlContent xml = new XmlContent(ser, true, false);

        final IElement top = xml.getToplevel();
        if (top == null) {
            throw new ParsingException(0, 0, "Expected 'help-queue' top-level element");
        } else if (!"help-queue".equals(top.getTagName())) {
            throw new ParsingException(top, "Expected 'help-queue' top-level element");
        }

        if (top instanceof final NonemptyElement netop) {

            for (final INode node : netop.getChildrenAsList()) {
                if (node instanceof final IElement elem) {
                    final String tag = elem.getTagName();

                    if ("active".equals(tag)) {
                        parseActive(elem, result);
                    } else if ("canceled".equals(tag)) {
                        parseCanceled(elem, result);
                    } else {
                        Log.warning("Unexpected <", tag, "> child of 'help-queue'");
                    }
                } else {
                    Log.warning("Unexpected ", node.getClass().getName(), " child of 'help-queue'");
                }
            }
        }

        return result;
    }

    /**
     * Parses the {@code <active>} sub-element.
     *
     * @param elem  the element
     * @param queue the queue to which to add parsed entries
     * @throws ParsingException if the XML could not be parsed
     */
    private static void parseActive(final IElement elem, final LiveHelpQueue queue)
            throws ParsingException {

        if (elem instanceof final NonemptyElement neelem) {

            for (final INode node : neelem.getChildrenAsList()) {
                if (node instanceof final EmptyElement child) {

                    if ("entry".equals(child.getTagName())) {
                        final LiveHelpQueueEntry entry = LiveHelpQueueEntry.parseXML(child);
                        queue.active.put(entry.id, entry);
                    }
                }
            }
        }
    }

    /**
     * Parses the {@code <canceled>} sub-element.
     *
     * @param elem  the element
     * @param queue the queue to which to add parsed entries
     * @throws ParsingException if the XML could not be parsed
     */
    private static void parseCanceled(final IElement elem, final LiveHelpQueue queue)
            throws ParsingException {

        if (elem instanceof final NonemptyElement neelem) {

            for (final INode node : neelem.getChildrenAsList()) {

                if (node instanceof final EmptyElement child) {

                    if ("entry".equals(child.getTagName())) {
                        final LiveHelpQueueEntry entry = LiveHelpQueueEntry.parseXML(child);
                        queue.inactive.put(entry.id, entry);
                    }
                }
            }
        }
    }
}
