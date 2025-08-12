package dev.mathops.web.cron;

import dev.mathops.dbjobs.batch.BulkUpdateMPLTestScores;
import dev.mathops.dbjobs.batch.daily.AuditBannerTestScores;
import dev.mathops.dbjobs.batch.daily.CheckStudentTerm;
import dev.mathops.dbjobs.batch.daily.CleanPending;
import dev.mathops.dbjobs.batch.daily.CloseIncompletes;
import dev.mathops.dbjobs.batch.daily.ImportBannerStudentRegistrations;
import dev.mathops.dbjobs.batch.daily.ImportOdsApplicants;
import dev.mathops.dbjobs.batch.daily.ImportOdsNewStus;
import dev.mathops.dbjobs.batch.daily.ImportOdsPastCourses;
import dev.mathops.dbjobs.batch.daily.ImportOdsTransferCredit;
import dev.mathops.dbjobs.batch.daily.PcCleanup;
import dev.mathops.dbjobs.batch.daily.SendQueuedBannerTestScores;
import dev.mathops.dbjobs.batch.daily.SetHolds;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * A cron job that runs various tasks.
 * <p>
 * NOTE: This job is conditionally installed in the 'cron' process based on a flag in the "context.xml" configuration
 * file in Tomcat, which allows it to be run only on the production server. This prevents batch jobs from running from
 * more than one location.
 */
public final class CronJobs implements ICronJob {

    /** A commonly used time. */
    private static final LocalTime ONE_AM = LocalTime.of(1, 0);

    /** A commonly used time. */
    private static final LocalTime THREE_AM = LocalTime.of(3, 0);

    /** A commonly used time. */
    private static final LocalTime ELEVEN_PM = LocalTime.of(23, 0);

    /** A commonly used time. */
    private static final LocalTime ELEVEN_FIFTY_PM = LocalTime.of(23, 50);

    /** The date/time the nightly reports are scheduled to next run. */
    private LocalDateTime reportsNextRun;

    /** The date/time the CLEAN_PENDING and PC_CLEANUP batches is scheduled to next run. */
    private LocalDateTime resetTestingNextRun;

    /** The date/time the end-of-day cleanup processes are scheduled to next run. */
    private LocalDateTime endOfDayNextRun;

    /** The date/time the early morning data gathering processes are scheduled to next run. */
    private LocalDateTime earlyMorning;

    /**
     * Constructs a new {@code CronJobs}.
     */
    public CronJobs() {

        final LocalDateTime now = LocalDateTime.now();
        final LocalDate today = now.toLocalDate();
        final LocalDate tomorrow = today.plusDays(1L);

        // Generate reports at 1 AM
        if (now.getHour() < 1) {
            this.reportsNextRun = LocalDateTime.of(today, ONE_AM);
        } else {
            this.reportsNextRun = LocalDateTime.of(tomorrow, ONE_AM);
        }

        // Reset testing at 11 PM
        if (now.getHour() < 23) {
            this.resetTestingNextRun = LocalDateTime.of(today, ELEVEN_PM);
        } else {
            this.resetTestingNextRun = LocalDateTime.of(tomorrow, ELEVEN_PM);
        }

        if (now.getHour() < 23 || now.getMinute() < 50) {
            this.endOfDayNextRun = LocalDateTime.of(today, ELEVEN_FIFTY_PM);
        } else {
            this.endOfDayNextRun = LocalDateTime.of(tomorrow, ELEVEN_FIFTY_PM);
        }

        if (now.getHour() < 3) {
            this.earlyMorning = LocalDateTime.of(today, THREE_AM);
        } else {
            this.earlyMorning = LocalDateTime.of(tomorrow, THREE_AM);
        }
    }

    /**
     * Called every 10 seconds by the CRON task.
     */
    @Override
    public void exec() {

        final LocalDateTime now = LocalDateTime.now();
        final LocalDate tomorrow = now.toLocalDate().plusDays(1L);

        if (now.isAfter(this.reportsNextRun)) {

            // *** This report is used during Summer to tell Engineering how many of their students are completing
            // placement

            // new PlacementReport("engr_plc_results", "ENGRPLC").execute();

            // *** This report gets used during orientation - each day there is a list of students to load into
            // 'special_stus', then this report goes out to the advising group list

//            new PlacementReport("orient_plc_results", "ORIENTN").execute();

            // *** This report runs during the semester - athletics sends an email near the start of the term to
            // request it, along with the list of people that the numan cron job should email it to.

//            new PrecalcProgressReport("athletes_summary", RawSpecialStus.ATHLETE,
//                    "PRECALCULUS PROGRESS REPORT FOR REGISTERED STUDENT ATHLETES").execute();

            // *** This report runs during the semester - engineering sends an email near the start of the term to
            // request it, along with the list of people that the numan cron job should email it to.

//            new PrecalcProCULUS PROGRESS ProgressReport("engineering_summary", RawSpecialStus.ENGRSTU,
//                    "PRECALCULUS PROGRESS REPORT FOR REGISTERED ENGINEERING STUDENTS").execute();

            // TODO: Add one for CSU Online using "DCE" students.

            this.reportsNextRun = LocalDateTime.of(tomorrow, ONE_AM);
        }

        if (now.isAfter(this.resetTestingNextRun)) {
            new CleanPending().execute();
            new PcCleanup().execute();
            this.resetTestingNextRun = LocalDateTime.of(tomorrow, ELEVEN_PM);
        }

        if (now.isAfter(this.endOfDayNextRun)) {
            CloseIncompletes.execute();
//            DeleteTestUserData.execute();
            SetHolds.execute();
            this.endOfDayNextRun = LocalDateTime.of(tomorrow, ELEVEN_FIFTY_PM);
        }

        if (now.isAfter(this.earlyMorning)) {
            new BulkUpdateMPLTestScores().execute();
//            new BulkUpdateStudentInformation().execute();
            new ImportOdsApplicants().execute();
            new ImportOdsTransferCredit().execute();
            new ImportOdsPastCourses().execute();
            new ImportOdsNewStus().execute();
            new ImportBannerStudentRegistrations().execute();
            new CheckStudentTerm().execute();
            new SendQueuedBannerTestScores().execute();
            new AuditBannerTestScores().execute();
            this.earlyMorning = LocalDateTime.of(tomorrow, THREE_AM);
        }
    }
}
