package dev.mathops.app.sim.registration;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A room in which sections can be scheduled.
 */
final class Room implements Comparable<Room> {

    /** The unique room ID. */
    private final String id;

    /** The seating capacity. */
    private final int capacity;

    /** The number of 50-minute blocks each Monday, Wednesday, and Friday the room is available. */
    private final int blocksPerDayMWF;

    /** The number of 75-minute blocks each Tuesday and Thursday the room is available. */
    private final int blocksPerDayTR;

    /** The list of all sections that will use the room Monday, Wednesday, or Friday. */
    private final List<SectionMWF> sectionsMWF;

    /** The list of all sections that will use the room Tuesday or Thursday. */
    private final List<SectionTR> sectionsTR;

    /**
     * Constructs a new {@code Room}.
     *
     * @param theId           the unique room ID
     * @param theCapacity     the seating capacity
     * @param theBlocksPerMWF the number of 50-minute blocks each Monday, Wednesday, and Friday the room is available
     * @param theBlocksPerTR  the number of 75-minute blocks each Tuesday and Thursday the room is available
     */
    Room(final String theId, final int theCapacity, final int theBlocksPerMWF, final int theBlocksPerTR) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Room ID may not be null or blank");
        }
        if (theCapacity < 1) {
            throw new IllegalArgumentException("Room capacity may not be less than 1");
        }
        if (theBlocksPerMWF < 0 || theBlocksPerTR < 0) {
            throw new IllegalArgumentException("Number of blocks of availability may not be negative");
        }
        if (theBlocksPerMWF == 0 && theBlocksPerTR == 0) {
            throw new IllegalArgumentException("Number of blocks of availability may not be zero");
        }

        this.id = theId;
        this.capacity = theCapacity;
        this.blocksPerDayMWF = theBlocksPerMWF;
        this.blocksPerDayTR = theBlocksPerTR;

        this.sectionsMWF = new ArrayList<>(theBlocksPerMWF);
        this.sectionsTR = new ArrayList<>(theBlocksPerTR);
    }

    /**
     * Gets the unique room ID.
     *
     * @return the room ID
     */
    String getId() {

        return this.id;
    }

    /**
     * Gets the seating capacity.
     *
     * @return the seating capacity
     */
    int getCapacity() {

        return this.capacity;
    }

    /**
     * Gets a copy of the sections scheduled to meet in this room on Monday, Wednesday, and/or Friday.
     *
     * @return the Monday/Wednesday/Friday sections
     */
    List<SectionMWF> getSectionsMWF() {

        return new ArrayList<>(this.sectionsMWF);
    }

    /**
     * Gets the number of free blocks available on Monday.
     *
     * @return the number of free blocks on Monday
     */
    int getFreeBlocksM() {

        int free = this.blocksPerDayMWF;

        for (final SectionMWF sect : this.sectionsMWF) {
            final EMeetingDaysMWF meeting = sect.meetingDays();
            if (meeting.includesMonday()) {
                --free;
            }
        }

        return free;
    }

    /**
     * Gets the number of free blocks available on Tuesday.
     *
     * @return the number of free blocks on Tuesday
     */
    int getFreeBlocksT() {

        int free = this.blocksPerDayTR;

        for (final SectionTR sect : this.sectionsTR) {
            final EMeetingDaysTR meeting = sect.meetingDays();
            if (meeting.includesTuesday()) {
                --free;
            }
        }

        return free;
    }

    /**
     * Gets the number of free blocks available on Wednesday.
     *
     * @return the number of free blocks on Wednesday
     */
    int getFreeBlocksW() {

        int free = this.blocksPerDayMWF;

        for (final SectionMWF sect : this.sectionsMWF) {
            final EMeetingDaysMWF meeting = sect.meetingDays();
            if (meeting.includesWednesday()) {
                --free;
            }
        }

        return free;
    }

    /**
     * Gets the number of free blocks available on Thursday.
     *
     * @return the number of free blocks on Thursday
     */
    int getFreeBlocksR() {

        int free = this.blocksPerDayTR;

        for (final SectionTR sect : this.sectionsTR) {
            final EMeetingDaysTR meeting = sect.meetingDays();
            if (meeting.includesThursday()) {
                --free;
            }
        }

        return free;
    }

    /**
     * Gets the number of free blocks available on Friday.
     *
     * @return the number of free blocks on Friday
     */
    int getFreeBlocksF() {

        int free = this.blocksPerDayMWF;

        for (final SectionMWF sect : this.sectionsMWF) {
            final EMeetingDaysMWF meeting = sect.meetingDays();
            if (meeting.includesFriday()) {
                --free;
            }
        }

        return free;
    }

    /**
     * Gets the total number of free blocks.
     *
     * @return the total blocks free
     */
    int getTotalBlocksFree() {

        return getFreeBlocksM() + getFreeBlocksT() + getFreeBlocksW() + getFreeBlocksR() + getFreeBlocksF();
    }

    /**
     * Gets a copy of the sections scheduled to meet in this room on Tuesday and/or Thursday.
     *
     * @return the Tuesday/Thursday sections
     */
    List<SectionTR> getSectionsTR() {

        return new ArrayList<>(this.sectionsTR);
    }

    /**
     * Removes a section that meets Monday, Wednesday, and/or Friday from this room.
     *
     * @param section the section to remove
     */
    void removeSection(final SectionMWF section) {

        this.sectionsMWF.remove(section);
    }

    /**
     * Removes a section that meets Tuesday and/or Thursday from this room.
     *
     * @param section the section to remove
     */
    void removeSection(final SectionTR section) {

        this.sectionsTR.remove(section);
    }

    /**
     * Removes all sections.
     */
    void clearSections() {

        this.sectionsMWF.clear();
        this.sectionsTR.clear();
    }

    /**
     * Attempts to add a section that will meet Monday, Wednesday, and/or Friday.
     *
     * @param numBlockPerWeek the number of 50-minute blocks needed per week
     * @param type            the assignment type
     * @param course          the course being assigned
     * @param numSeats        the number of seats
     * @param usage           the room usage
     * @return an object with the room assignment if it was made, or without if there was insufficient available time to
     *         make the assignment (the assignment ID will be unique within the room)
     */
    Optional<SectionMWF> addSectionMWF(final int numBlockPerWeek, final EAssignmentType type, final Course course,
                                       final int numSeats, final ERoomUsage usage) {

        final int sectId = this.sectionsMWF.size() + this.sectionsTR.size() + 1;

        SectionMWF sect = null;

        if (type == EAssignmentType.BLOCKS_OF_50 || type == EAssignmentType.BLOCKS_OF_50_OR_75) {
            sect = addNormalSectionMWF(numBlockPerWeek, sectId, course, numSeats, usage);
        } else if (type == EAssignmentType.CONTIGUOUS) {
            sect = addContiguousSectionMWF(numBlockPerWeek, sectId, course, numSeats, usage);
        } else {
            Log.warning("Scheduling a course that wants 75-minute blocks for Mon-Wed-Fri is not supported");
        }

        return sect == null ? Optional.empty() : Optional.of(sect);
    }

    /**
     * Attempts to add a contiguous assignment on a Monday, Wednesday, or Friday (the day with the most free hours is
     * selected).
     *
     * @param numBlocks the number of 50-minute blocks needed
     * @param sectId    the assignment ID to use
     * @return the section if it was created; {@code null} if the section would exceed room available hours
     */
    private SectionMWF addContiguousSectionMWF(final int numBlocks, final int sectId, final Course course,
                                               final int numSeats, final ERoomUsage usage) {

        final int blocksFreeM = getFreeBlocksM();
        final int blocksFreeW = getFreeBlocksW();
        final int blocksFreeF = getFreeBlocksF();

        final int maxMW = Math.max(blocksFreeM, blocksFreeW);
        final int maxMWF = Math.max(maxMW, blocksFreeF);

        SectionMWF sect = null;

        if (blocksFreeM == maxMWF) {
            if (blocksFreeM >= numBlocks) {
                sect = createSectionMWF(EMeetingDaysMWF.M, numBlocks, sectId, course, numSeats, usage);
            }
        } else if (blocksFreeW == maxMWF) {
            if (blocksFreeW >= numBlocks) {
                sect = createSectionMWF(EMeetingDaysMWF.W, numBlocks, sectId, course, numSeats, usage);
            }
        } else if (blocksFreeF >= numBlocks) {
            sect = createSectionMWF(EMeetingDaysMWF.F, numBlocks, sectId, course, numSeats, usage);
        }

        return sect;
    }

    /**
     * Attempts to add an assignment that is broken into 50-minute blocks spread over different days.
     *
     * @param numBlockPerWeek the number of 50-minute blocks needed per week
     * @param sectId          the assignment ID to use
     * @param course          the course being assigned
     * @param numSeats        the number of seats
     * @param usage           the room usage
     * @return the section if it was created; {@code null} if the section would exceed room available hours
     */
    private SectionMWF addNormalSectionMWF(final int numBlockPerWeek, final int sectId, final Course course,
                                           final int numSeats, final ERoomUsage usage) {

        SectionMWF sect = null;

        if (numBlockPerWeek == 1) {
            // A 1-day a week course can be considered "contiguous" and we already have logic to do that
            sect = addContiguousSectionMWF(1, sectId, course, numSeats, usage);
        } else if (numBlockPerWeek == 2) {
            sect = addNormalSectionTwoDayMWF(sectId, course, numSeats, usage);
        } else if (numBlockPerWeek == 3) {
            sect = addNormalSectionThreeDayMWF(sectId, course, numSeats, usage);
        } else {
            final String numBlocksStr = Integer.toString(numBlockPerWeek);
            Log.warning("ERROR: Classes needing ", numBlocksStr, " 50-minute blocks a week not yet supported");
        }

        return sect;
    }

    /**
     * Attempts to add a section that will meet two days a week, for one hour.
     *
     * @param sectId   the assignment ID to use
     * @param course   the course being assigned
     * @param numSeats the number of seats
     * @param usage    the room usage
     * @return the section if it was created; {@code null} if the section would exceed room available hours
     */
    private SectionMWF addNormalSectionTwoDayMWF(final int sectId, final Course course, final int numSeats,
                                                 final ERoomUsage usage) {

        final int blocksFreeM = getFreeBlocksM();
        final int blocksFreeW = getFreeBlocksW();
        final int blocksFreeF = getFreeBlocksF();

        final int minMW = Math.min(blocksFreeM, blocksFreeW);
        final int minMWF = Math.min(minMW, blocksFreeF);

        SectionMWF sect = null;

        if (blocksFreeM == minMWF) {
            if (blocksFreeW > 0 && blocksFreeF > 0) {
                sect = createSectionMWF(EMeetingDaysMWF.WF, 1, sectId, course, numSeats, usage);
            }
        } else if (blocksFreeW == minMWF) {
            if (blocksFreeM > 0 && blocksFreeF > 0) {
                sect = createSectionMWF(EMeetingDaysMWF.MF, 1, sectId, course, numSeats, usage);
            }
        } else {
            if (blocksFreeM > 0 && blocksFreeW > 0) {
                sect = createSectionMWF(EMeetingDaysMWF.MW, 1, sectId, course, numSeats, usage);
            }
        }

        return sect;
    }

    /**
     * Attempts to add a section that will meet three days a week, for one hour.
     *
     * @param sectId   the assignment ID to use
     * @param course   the course being assigned
     * @param numSeats the number of seats
     * @param usage    the room usage
     * @return the section if it was created; {@code null} if the section would exceed room available hours
     */
    private SectionMWF addNormalSectionThreeDayMWF(final int sectId, final Course course, final int numSeats,
                                                   final ERoomUsage usage) {

        final int blocksFreeM = getFreeBlocksM();
        final int blocksFreeW = getFreeBlocksW();
        final int blocksFreeF = getFreeBlocksF();

        SectionMWF sect = null;

        if (blocksFreeM > 0 && blocksFreeW > 0 && blocksFreeF > 0) {
            sect = createSectionMWF(EMeetingDaysMWF.MWF, 1, sectId, course, numSeats, usage);
        }

        return sect;
    }

    /**
     * If possible, creates a section that will meet Monday, Wednesday, and/or Friday and adds it to this room's
     * schedule.  If doing so would exceed the number of blocks for which the room is available; this operation will
     * fail.
     *
     * @param meetingDays  the days on which the section will meet
     * @param blocksPerDay the number of 50-minute blocks needed per day
     * @param sectId       the section ID to use for the new section
     * @param course       the course
     * @param numSeats     the number of seats needed
     * @param usage        the usage
     * @return the section if it was created; {@code null} if the section would exceed room available hours
     */
    private SectionMWF createSectionMWF(final EMeetingDaysMWF meetingDays, final int blocksPerDay, final int sectId,
                                        final Course course, final int numSeats, final ERoomUsage usage) {

        boolean ok = true;

        if (meetingDays.includesMonday()) {
            if (getFreeBlocksM() < blocksPerDay) {
                ok = false;
            }
        }
        if (meetingDays.includesWednesday()) {
            if (getFreeBlocksW() < blocksPerDay) {
                ok = false;
            }
        }
        if (meetingDays.includesFriday()) {
            if (getFreeBlocksF() < blocksPerDay) {
                ok = false;
            }
        }

        SectionMWF sect = null;

        if (ok) {
            final SectionMWF temp = new SectionMWF(sectId, meetingDays, this, course, numSeats, usage, blocksPerDay);

            this.sectionsMWF.add(temp);
            if (canPackSectionsMWF()) {
                sect = temp;
            } else {
                this.sectionsMWF.remove(temp);
            }
        }

        return sect;
    }

    /**
     * Attempts to pack the MWF sections so they all fit within the allowed hours of the day.
     *
     * @return true if the packing succeeded; false if not
     */
    private boolean canPackSectionsMWF() {

        for (final SectionMWF sect : this.sectionsMWF) {
            sect.clearBlockIndex();
        }

        // Pack all sections that meet all three days into the first indexes
        int currentIndex = 0;
        for (final SectionMWF sect : this.sectionsMWF) {
            if (sect.meetingDays() == EMeetingDaysMWF.MWF) {
                sect.setBlockIndex(currentIndex);
                currentIndex += sect.blocksPerDay();
            }
        }

        int maxM = currentIndex;
        int maxW = currentIndex;
        int maxF = currentIndex;

        // Now pack all the "MW" sections, and slot in as many "F" sections as we can fit.
        {
            for (final SectionMWF sect : this.sectionsMWF) {
                if (sect.meetingDays() == EMeetingDaysMWF.MW) {
                    sect.setBlockIndex(currentIndex);
                    currentIndex += sect.blocksPerDay();
                    maxM = currentIndex;
                    maxW = currentIndex;
                }
            }
            int freeFHours = currentIndex - maxF;
            if (freeFHours > 0) {
                for (final SectionMWF sect : this.sectionsMWF) {
                    if (sect.meetingDays() == EMeetingDaysMWF.F) {
                        final int blocks = sect.blocksPerDay();
                        if (blocks <= freeFHours) {
                            sect.setBlockIndex(maxF);
                            maxF += blocks;
                            freeFHours -= blocks;
                            if (freeFHours == 0) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Move "maxF" up to match the rest of the row (may leave a gap, but nothing will fit in that gap)
        maxF = currentIndex;

        // Next, pack all the "MF" sections, and slot in as many "W" sections as we can fit.
        {
            for (final SectionMWF sect : this.sectionsMWF) {
                if (sect.meetingDays() == EMeetingDaysMWF.MF) {
                    sect.setBlockIndex(currentIndex);
                    currentIndex += sect.blocksPerDay();
                    maxM = currentIndex;
                    maxF = currentIndex;
                }
            }
            int freeWHours = currentIndex - maxW;
            if (freeWHours > 0) {
                for (final SectionMWF sect : this.sectionsMWF) {
                    if (sect.meetingDays() == EMeetingDaysMWF.W) {
                        final int blocks = sect.blocksPerDay();
                        if (blocks <= freeWHours) {
                            sect.setBlockIndex(maxW);
                            maxW += blocks;
                            freeWHours -= blocks;
                            if (freeWHours == 0) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Move "maxW" up to match the rest of the row (may leave a gap, but nothing will fit in that gap)
        maxW = currentIndex;

        // pack all the "WF" sections, and slot in as many "M" sections as we can fit.
        {
            for (final SectionMWF sect : this.sectionsMWF) {
                if (sect.meetingDays() == EMeetingDaysMWF.WF) {
                    sect.setBlockIndex(currentIndex);
                    currentIndex += sect.blocksPerDay();
                    maxW = currentIndex;
                    maxF = currentIndex;
                }
            }
            int freeMHours = currentIndex - maxM;
            if (freeMHours > 0) {
                for (final SectionMWF sect : this.sectionsMWF) {
                    if (sect.meetingDays() == EMeetingDaysMWF.M) {
                        final int blocks = sect.blocksPerDay();
                        if (blocks <= freeMHours) {
                            sect.setBlockIndex(maxM);
                            maxM += blocks;
                            freeMHours -= blocks;
                            if (freeMHours == 0) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        // NOTE: We do not move "maxM" up since we're going to be adding one-day sections, and we could add a Monday
        // section here.

        // TODO: A future enhancement might check whether we had unused hours in Wednesday or Friday as we did the
        //  2-day assignments, and if that day's one-day assignments end up too large, we could shift that set of
        //  2-day courses to the top of the stack (rather than leaving Monday on top as we have here), and shift that
        //  day's 1-day indexes down to "Tetris" into the gap.

        // Add the 1-day sections
        for (final SectionMWF sect : this.sectionsMWF) {
            if (sect.getBlocksIndex() == -1) {
                if (sect.meetingDays() == EMeetingDaysMWF.M) {
                    sect.setBlockIndex(maxM);
                    maxM += sect.blocksPerDay();
                } else if (sect.meetingDays() == EMeetingDaysMWF.W) {
                    sect.setBlockIndex(maxW);
                    maxW += sect.blocksPerDay();
                } else if (sect.meetingDays() == EMeetingDaysMWF.F) {
                    sect.setBlockIndex(maxF);
                    maxF += sect.blocksPerDay();
                }
            }
        }

        return maxM <= this.blocksPerDayMWF && maxW <= this.blocksPerDayMWF && maxF <= this.blocksPerDayMWF;
    }

    /**
     * Attempts to add a section that will meet Tuesday and/or Thursday.
     *
     * @param numBlockPerWeek the number of 75-minute blocks needed per week
     * @param type            the assignment type
     * @param course          the course being assigned
     * @param numSeats        the number of seats
     * @param usage           the room usage
     * @return an object with the room assignment if it was made, or without if there was insufficient available time to
     *         make the assignment (the assignment ID will be unique within the room)
     */
    Optional<SectionTR> addSectionTR(final int numBlockPerWeek, final EAssignmentType type, final Course course,
                                     final int numSeats, final ERoomUsage usage) {

        final int sectId = this.sectionsTR.size() + this.sectionsTR.size() + 1;

        final SectionTR sect;

        if (type == EAssignmentType.BLOCKS_OF_75 || type == EAssignmentType.BLOCKS_OF_50_OR_75) {
            sect = addNormalSectionTR(numBlockPerWeek, sectId, course, numSeats, usage);
        } else if (type == EAssignmentType.CONTIGUOUS) {
            sect = addContiguousSectionTR(numBlockPerWeek, sectId, course, numSeats, usage);
        } else {
            // Course wants blocks of 50, but we can make that work within 75-minute blocks
            if (numBlockPerWeek == 1) {
                sect = addContiguousSectionTR(1, sectId, course, numSeats, usage);
            } else if (numBlockPerWeek == 2) {
                sect = addNormalSectionTR(2, sectId, course, numSeats, usage);
            } else {
                Log.warning("Scheduling a course that wants more than 2 50-minute blocks for Tue/Thr is not supported");
                sect = null;
            }
        }

        return sect == null ? Optional.empty() : Optional.of(sect);
    }

    /**
     * Attempts to add a contiguous assignment on a Tuesday or Thursday (the day with the most free hours is selected).
     *
     * @param numBlocks the number of 75-minute blocks needed
     * @param sectId    the assignment ID to use
     * @return the section if it was created; {@code null} if the section would exceed room available hours
     */
    private SectionTR addContiguousSectionTR(final int numBlocks, final int sectId, final Course course,
                                             final int numSeats, final ERoomUsage usage) {

        final int blocksFreeT = getFreeBlocksT();
        final int blocksFreeR = getFreeBlocksR();

        final int maxTR = Math.max(blocksFreeT, blocksFreeR);

        SectionTR sect = null;

        if (blocksFreeT == maxTR) {
            if (blocksFreeT >= numBlocks) {
                sect = createSectionTR(EMeetingDaysTR.T, numBlocks, sectId, course, numSeats, usage);
            }
        } else if (blocksFreeR >= numBlocks) {
            sect = createSectionTR(EMeetingDaysTR.R, numBlocks, sectId, course, numSeats, usage);
        }

        return sect;
    }

    /**
     * Attempts to add an assignment that is broken into 75-minute blocks spread over different days.
     *
     * @param numBlockPerWeek the number of 75-minute blocks needed per week
     * @param sectId          the assignment ID to use
     * @param course          the course being assigned
     * @param numSeats        the number of seats
     * @param usage           the room usage
     * @return the section if it was created; {@code null} if the section would exceed room available hours
     */
    private SectionTR addNormalSectionTR(final int numBlockPerWeek, final int sectId, final Course course,
                                         final int numSeats, final ERoomUsage usage) {

        SectionTR sect = null;

        if (numBlockPerWeek == 1) {
            // A 1-day a week course can be considered "contiguous" and we already have logic to do that
            sect = addContiguousSectionTR(1, sectId, course, numSeats, usage);
        } else if (numBlockPerWeek == 2) {
            sect = addNormalSectionTwoDayTR(sectId, course, numSeats, usage);
        } else {
            final String numBlocksStr = Integer.toString(numBlockPerWeek);
            Log.warning("ERROR: Classes needing ", numBlocksStr, " 75-minute blocks a week not yet supported");
        }

        return sect;
    }

    /**
     * Attempts to add a section that will meet three days a week, for one hour.
     *
     * @param sectId   the assignment ID to use
     * @param course   the course being assigned
     * @param numSeats the number of seats
     * @param usage    the room usage
     * @return the section if it was created; {@code null} if the section would exceed room available hours
     */
    private SectionTR addNormalSectionTwoDayTR(final int sectId, final Course course, final int numSeats,
                                               final ERoomUsage usage) {

        final int blocksFreeT = getFreeBlocksT();
        final int blocksFreeR = getFreeBlocksR();

        SectionTR sect = null;

        if (blocksFreeT > 0 && blocksFreeR > 0) {
            sect = createSectionTR(EMeetingDaysTR.TR, 1, sectId, course, numSeats, usage);
        }

        return sect;
    }

    /**
     * If possible, creates a section that will meet Tuesday and/or Thursday and adds it to this room's schedule.  If
     * doing so would exceed the number of blocks for which the room is available; this operation will fail.
     *
     * @param meetingDays  the days on which the section will meet
     * @param blocksPerDay the number of 50-minute blocks needed per day
     * @param sectId       the section ID to use for the new section
     * @param course       the course
     * @param numSeats     the number of seats needed
     * @param usage        the usage
     * @return the section if it was created; {@code null} if the section would exceed room available hours
     */
    private SectionTR createSectionTR(final EMeetingDaysTR meetingDays, final int blocksPerDay, final int sectId,
                                      final Course course, final int numSeats, final ERoomUsage usage) {

        boolean ok = true;

        if (meetingDays.includesTuesday()) {
            if (getFreeBlocksT() < blocksPerDay) {
                ok = false;
            }
        }
        if (meetingDays.includesThursday()) {
            if (getFreeBlocksR() < blocksPerDay) {
                ok = false;
            }
        }

        final SectionTR sect;

        if (ok) {
            final SectionTR temp = new SectionTR(sectId, meetingDays, this, course, numSeats, usage, blocksPerDay);

            this.sectionsTR.add(temp);
            if (canPackSectionsTR()) {
                sect = temp;
            } else {
                this.sectionsTR.remove(temp);
                sect = null;
            }
        } else {
            sect = null;
        }

        return sect;
    }

    /**
     * Attempts to pack the TR sections so they all fit within the allowed hours of the day.
     *
     * @return true if the packing succeeded; false if not
     */
    private boolean canPackSectionsTR() {

        for (final SectionTR sect : this.sectionsTR) {
            sect.clearBlockIndex();
        }

        // Pack all sections that meet both days into the first indexes
        int currentIndex = 0;
        for (final SectionTR sect : this.sectionsTR) {
            if (sect.meetingDays() == EMeetingDaysTR.TR) {
                sect.setBlockIndex(currentIndex);
                currentIndex += sect.blocksPerDay();
            }
        }

        int maxT = currentIndex;
        int maxR = currentIndex;

        // Add the 1-day sections
        for (final SectionTR sect : this.sectionsTR) {
            if (sect.getBlocksIndex() == -1) {
                if (sect.meetingDays() == EMeetingDaysTR.T) {
                    sect.setBlockIndex(maxT);
                    maxT += sect.blocksPerDay();
                } else if (sect.meetingDays() == EMeetingDaysTR.R) {
                    sect.setBlockIndex(maxR);
                    maxR += sect.blocksPerDay();
                }
            }
        }

        return maxT <= this.blocksPerDayTR && maxR <= this.blocksPerDayTR;
    }

    /**
     * Computes a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.id.hashCode();
    }

    /**
     * Tests whether this object is equal to another.  Equality of this class is tested only on equality of the unique
     * course ID.
     *
     * @param obj the other object
     * @return true if this object is equal
     */
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final Room room) {
            final String objId = room.getId();
            equal = this.id.equals(objId);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares this object to another for order.  Ordering is based on room capacity.
     *
     * @param o the object to be compared
     * @return the value 0 if this object's capacity equals the other object's capacity a value less than 0 if this
     *         object's capacity is less than that of the other; and a value greater than 0 if this object's capacity is
     *         greater than that of the other
     */
    @Override
    public int compareTo(final Room o) {

        final int cap = o.getCapacity();

        return Integer.compare(this.capacity, cap);
    }

    /**
     * Generates a string representation of the list of rooms.
     *
     * @return the string representation
     */
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        builder.add(this.id);
        builder.add(" (cap=");
        builder.add(this.capacity);
        builder.add(", blocks/day MWF=");
        builder.add(this.blocksPerDayMWF);
        builder.add(", blocks/day TR=");
        builder.add(this.blocksPerDayTR);
        builder.add(')');

        return builder.toString();
    }
}
