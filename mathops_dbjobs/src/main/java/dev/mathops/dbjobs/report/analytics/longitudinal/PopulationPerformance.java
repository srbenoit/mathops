package dev.mathops.dbjobs.report.analytics.longitudinal;

import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;

/**
 * A container for the performance of a population.
 */
class PopulationPerformance implements Comparable<PopulationPerformance> {

    final String major;
    int totalEnrollments = 0;
    int numA = 0;
    int numB = 0;
    int numC = 0;
    int numD = 0;
    int numF = 0;
    int numW = 0;
    int numWithGrade = 0;
    double totalGradeValue = 0.0;

    PopulationPerformance(final String theMajor) {

        this.major = theMajor;
    }

    String getMajor() {

        return this.major;
    }

    void clear() {

        this.totalEnrollments = 0;
        this.numA = 0;
        this.numB = 0;
        this.numC = 0;
        this.numD = 0;
        this.numF = 0;
        this.numW = 0;
        this.numWithGrade = 0;
        this.totalGradeValue = 0.0;
    }

    void accumulate(final PopulationPerformance o) {
        this.totalEnrollments += o.totalEnrollments;
        this.numA += o.numA;
        this.numB += o.numB;
        this.numC += o.numC;
        this.numD += o.numD;
        this.numF += o.numF;
        this.numW += o.numW;
        this.numWithGrade += o.numWithGrade;
        this.totalGradeValue += o.totalGradeValue;
    }

    void recordEnrollment(final EnrollmentRec rec) {

        addEnrollment();

        if (rec.isWithdraw()) {
            addWithdrawal();
        } else {
            final Double gradeValueObj = rec.gradeValue();

            if (gradeValueObj != null) {
                final double gradeValue = gradeValueObj.doubleValue();
                addWithGrade(gradeValue);
            }
        }
    }

    void addEnrollment() {
        ++this.totalEnrollments;
    }

    void addWithdrawal() {
        ++this.numW;
    }

    void addWithGrade(final double gradeValue) {
        ++this.numWithGrade;
        this.totalGradeValue += gradeValue;

        if (gradeValue > 3.5) {
            ++this.numA;
        } else if (gradeValue > 2.5) {
            ++this.numB;
        } else if (gradeValue > 1.5) {
            ++this.numC;
        } else if (gradeValue > 0.5) {
            ++this.numD;
        } else {
            ++this.numF;
        }
    }

    int getTotalEnrollments() {

        return this.totalEnrollments;
    }

    int getNumW() {

        return this.numW;
    }

    int getNumWithGrade() {

        return this.numWithGrade;
    }

    double getPercentWithdrawal() {

        return this.totalEnrollments == 0 ? 0.0 : 100.0 * (double) this.numW / (double) this.totalEnrollments;
    }

    double getPercentCompleting() {

        return this.totalEnrollments == 0 ? 0.0 : 100.0 * (double) this.numWithGrade / (double) this.totalEnrollments;
    }

    double getPercentA() {

        return this.numWithGrade == 0 ? 0.0 : 100.0 * (double) this.numA / (double) this.numWithGrade;
    }

    double getPercentB() {

        return this.numWithGrade == 0 ? 0.0 : 100.0 * (double) this.numB / (double) this.numWithGrade;
    }

    double getPercentC() {

        return this.numWithGrade == 0 ? 0.0 : 100.0 * (double) this.numC / (double) this.numWithGrade;
    }

    double getPercentD() {

        return this.numWithGrade == 0 ? 0.0 : 100.0 * (double) this.numD / (double) this.numWithGrade;
    }

    double getPercentF() {

        return this.numWithGrade == 0 ? 0.0 : 100.0 * (double) this.numF / (double) this.numWithGrade;
    }

    double getDfw() {

        return this.totalEnrollments == 0 ? 0.0 :
                100.0 * (double) (this.numD + this.numF + this.numW) / (double) this.totalEnrollments;
    }

    double getDfwWithGrade() {

        return this.numWithGrade == 0 ? 0.0 : 100.0 * (double) (this.numD + this.numF) / (double) this.numWithGrade;
    }

    double getAverageGpa() {

        return this.numWithGrade == 0 ? 0.0 : this.totalGradeValue / (double) this.numWithGrade;
    }

    @Override
    public int compareTo(final PopulationPerformance o) {

        return -Integer.compare(this.totalEnrollments, o.totalEnrollments);
    }
}
