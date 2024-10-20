package dev.mathops.dbjobs.report.analytics.longitudinal;

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

        totalEnrollments = 0;
        numA = 0;
        numB = 0;
        numC = 0;
        numD = 0;
        numF = 0;
        numW = 0;
        numWithGrade = 0;
        totalGradeValue = 0.0;
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
        ++totalEnrollments;
    }

    void addWithdrawal() {
        ++numW;
    }

    void addWithGrade(final double gradeValue) {
        ++numWithGrade;
        this.totalGradeValue += gradeValue;

        if (gradeValue > 3.5) {
            ++numA;
        } else if (gradeValue > 2.5) {
            ++numB;
        } else if (gradeValue > 1.5) {
            ++numC;
        } else if (gradeValue > 0.5) {
            ++numD;
        } else {
            ++numF;
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

        return totalEnrollments == 0 ? 0.0 : 100.0 * (double) numW / (double) totalEnrollments;
    }

    double getPercentCompleting() {

        return totalEnrollments == 0 ? 0.0 : 100.0 * (double) numWithGrade / (double) totalEnrollments;
    }

    double getPercentA() {

        return numWithGrade == 0 ? 0.0 : 100.0 * (double) numA / (double) numWithGrade;
    }

    double getPercentB() {

        return numWithGrade == 0 ? 0.0 : 100.0 * (double) numB / (double) numWithGrade;
    }

    double getPercentC() {

        return numWithGrade == 0 ? 0.0 : 100.0 * (double) numC / (double) numWithGrade;
    }

    double getPercentD() {

        return numWithGrade == 0 ? 0.0 : 100.0 * (double) numD / (double) numWithGrade;
    }

    double getPercentF() {

        return numWithGrade == 0 ? 0.0 : 100.0 * (double) numF / (double) numWithGrade;
    }

    double getDfw() {

        return totalEnrollments == 0 ? 0.0 : 100.0 * (double) (numD + numF + numW) / (double) totalEnrollments;
    }

    double getDfwWithGrade() {

        return numWithGrade == 0 ? 0.0 : 100.0 * (double) (numD + numF) / (double) numWithGrade;
    }

    double getAverageGpa() {

        return numWithGrade == 0 ? 0.0 : totalGradeValue / (double) numWithGrade;
    }

    @Override
    public int compareTo(PopulationPerformance o) {

        return -Integer.compare(this.totalEnrollments, o.totalEnrollments);
    }
}
