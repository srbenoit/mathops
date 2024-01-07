package dev.mathops.app.ops.snapin.messaging;

/**
 * Messages that are sent to students based on their status in courses, each with a type code and description.
 *
 * <p>
 * The ordering of entries in this enumeration is significant - reports are printed with records sorted on message code,
 * using the natural ordering provided by this enumeration.
 */
public enum EMsg {

    /** EPF message. */
    EPF("EPF"),

    //
    // Welcome messages
    //

    /** Welcome message. */
    WELCpr00("Welcome: Student does not have prereq met for first course"),

    /** Welcome message. */
    WELCst00("Welcome: Student has not yet started, now <= RE1"),

    /** Welcome message. */
    WELCst01("Welcome: Student has not yet started, RE1 < now"),

    /** Welcome message. */
    WELCus00("Welcome: Student has started, not tried User's, now <= RE1"),

    /** Welcome message. */
    WELCus01("Welcome: Student has started, not tried User's, RE1 < now"),

    /** Welcome message. */
    WELCus02("Welcome: Student has started, has tried User's < 4, now <= RE1"),

    /** Welcome message. */
    WELCus03("Welcome: Student has started, has tried User's < 4, RE1 < now"),

    /** Welcome message. */
    WELCus04("Welcome: Student has started, has tried User's >= 4, now <= RE1"),

    /** Welcome message. */
    WELCus05("Welcome: Student has started, has tried User's >= 4, RE1 < now"),

    /** Welcome message. */
    WELCsr00("Welcome: User's passed, not tried SR, now <= RE1"),

    /** Welcome message. */
    WELCsr01("Welcome: User's passed, not tried SR, RE1 < now"),

    /** Welcome message. */
    WELCsr02("Welcome: User's passed, has tried SR < 4, now <= RE1"),

    /** Welcome message. */
    WELCsr03("Welcome: User's passed, has tried SR < 4, RE1 < now"),

    /** Welcome message. */
    WELCsr04("Welcome: User's passed, has tried SR >= 4, now <= RE1"),

    /** Welcome message. */
    WELCsr05("Welcome: User's passed, has tried SR >= 4, RE1 < now"),

    /** Welcome message. */
    WELCok00("Welcome: User's and SR passed"),

    //
    // On-time messages
    //

    //

    /** On-time message. */
    RE1Rok00("Skills Review completed, congratulations"),

    /** On-time message. */
    RE1Rok01("Unit 1 Review Exam due soon"),

    /** On-time message. */
    UE1Rok00("Unit 1 next - Unit 2 review not due for a while"),

    /** On-time message. */
    UE1Rok01("Unit 1 next - Unit 2 review due soon"),

    /** On-time message. */
    RE2Rok00("Unit 1 Exam completed, congratulations"),

    /** On-time message. */
    RE2Rok01("Unit 2 Review Exam due soon"),

    /** On-time message. */
    UE2Rok00("Unit 2 next - Unit 3 review due soon"),

    /** On-time message. */
    RE3Rok00("Unit 2 Exam completed, congratulations"),

    /** On-time message. */
    RE3Rok01("Unit 3 Review Exam due soon"),

    /** On-time message. */
    UE3Rok00("Unit 3 next - Unit 4 review due soon"),

    /** On-time message. */
    RE4Rok00("Unit 3 Exam completed, congratulations"),

    /** On-time message. */
    RE4Rok01("Unit 4 Review Exam due soon"),

    /** On-time message. */
    UE4Rok00("Unit 4 next - final exam due soon"),

    /** On-time message. */
    FINRok00("Unit 4 Exam completed, congratulations"),

    /** On-time message. */
    FINRok01("Final Exam due soon"),

    /** On-time message. */
    GRDCok00("Final Exam passed, current grade is C, can earn A/B"),

    /** On-time message. */
    GRDCok01("Final Exam passed, current grade is C, can earn B"),

    /** On-time message. */
    GRDCok02("Final Exam passed, final grade is C"),

    /** On-time message. */
    GRDBok00("Final Exam passed, current grade is B, can earn A"),

    /** On-time message. */
    GRDBok01("Final Exam passed, final grade is B"),

    /** On-time message. */
    GRDAok00("Final Exam passed, final grade is A"),

    //
    // Messages when prerequisites are not satisfied.
    //

    /** Late message. */
    PREQpr00("Prereq not satisfied, has 2 placement tries left, message 1"),

    /** Late message. */
    PREQpr01("Prereq not satisfied, has 2 placement tries left, message 2"),

    /** Late message. */
    PREQpr02("Prereq not satisfied, has 2 placement tries left, message 3"),

    /** Late message. */
    PREQpr03("Prereq not satisfied, has 1 placement try left, message 1"),

    /** Late message. */
    PREQpr04("Prereq not satisfied, has 1 placement try left, message 2"),

    /** Late message. */
    PREQpr05("Prereq not satisfied, has 1 placement try left, message 3"),

    /** Late message. */
    PREQpr06("Prereq not satisfied, has no placement tries left, message 1"),

    /** Late message. */
    PREQpr07("Prereq not satisfied, has no placement tries left, message 2"),

    /** Late message. */
    PREQpr08("Prereq not satisfied, has no placement tries left, message 3"),

    /** Late message. */
    PREQpr09("Prereq not satisfied, message 4"),

    /** Late message. */
    PREQpr10("Prereq not satisfied, message 5"),

    /** Late message. */
    PREQpr11("Prereq not satisfied, message 6"),

    //

    /** Late message. */
    STRTst00("First course not started, message 1"),

    /** Late message. */
    STRTst01("First course not started, message 2"),

    /** Late message. */
    STRTst02("First course not started, message 3"),

    /** Late message. */
    STRTst03("First course not started, message 4"),

    //

    /** Late message. */
    USRRus00("User's exam not attempted, message 1"),

    /** Late message. */
    USRRus01("User's exam not attempted, message 2"),

    /** Late message. */
    USRRus02("User's exam not attempted, message 3"),

    /** Late message. */
    USRRus03("User's exam not attempted, message 4"),

    /** Late message. */
    USRRus04("User's exam attempted < 4 times, message 1"),

    /** Late message. */
    USRRus05("User's exam attempted < 4 times, message 2"),

    /** Late message. */
    USRRus06("User's exam attempted < 4 times, message 3"),

    /** Late message. */
    USRRus07("User's exam attempted < 4 times, message 4"),

    /** Late message. */
    USRXus00("User's exam attempted >= 4 times, message 1"),

    /** Late message. */
    USRXus01("User's exam attempted >= 4 times, message 2"),

    /** Late message. */
    USRXus02("User's exam attempted >= 4 times, message 3"),

    /** Late message. */
    USRXus03("User's exam attempted >= 4 times, message 4"),

    //

    /** Late message. */
    SKLRsr00("Skills Review exam not attempted, message 1"),

    /** Late message. */
    SKLRsr01("Skills Review exam not attempted, message 2"),

    /** Late message. */
    SKLRsr02("Skills Review exam not attempted, message 3"),

    /** Late message. */
    SKLRsr03("Skills Review exam not attempted, message 4"),

    /** Late message. */
    SKLRsr04("Skills Review exam attempted < 4 times, message 1"),

    /** Late message. */
    SKLRsr05("Skills Review exam attempted < 4 times, message 2"),

    /** Late message. */
    SKLRsr06("Skills Review exam attempted < 4 times, message 3"),

    /** Late message. */
    SKLRsr07("Skills Review exam attempted < 4 times, message 4"),

    /** Late message. */
    SKLRsr99("Stuck on Skills Review"),

    /** Late message. */
    SKLXsr00("Skills Review exam attempted >= 4 times, message 1"),

    /** Late message. */
    SKLXsr01("Skills Review exam attempted >= 4 times, message 2"),

    /** Late message. */
    SKLXsr02("Skills Review exam attempted >= 4 times, message 3"),

    /** Late message. */
    SKLXsr03("Skills Review exam attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H11Rhw00("Homework 1.1 not attempted, message 1"),

    /** Late message. */
    H11Rhw01("Homework 1.1 not attempted, message 2"),

    /** Late message. */
    H11Rhw02("Homework 1.1 not attempted, message 3"),

    /** Late message. */
    H11Rhw03("Homework 1.1 not attempted, message 4"),

    /** Late message. */
    H11Rhw04("Homework 1.1 attempted < 4 times, message 1"),

    /** Late message. */
    H11Rhw05("Homework 1.1 attempted < 4 times, message 2"),

    /** Late message. */
    H11Rhw06("Homework 1.1 attempted < 4 times, message 3"),

    /** Late message. */
    H11Rhw07("Homework 1.1 attempted < 4 times, message 4"),

    /** Late message. */
    H11Rhw99("Stuck on Homework 1.1"),

    /** Late message. */
    H11Xhw00("Homework 1.1 attempted >= 4 times, message 1"),

    /** Late message. */
    H11Xhw01("Homework 1.1 attempted >= 4 times, message 2"),

    /** Late message. */
    H11Xhw02("Homework 1.1 attempted >= 4 times, message 3"),

    /** Late message. */
    H11Xhw03("Homework 1.1 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H12Rhw00("Homework 1.2 not attempted, message 1"),

    /** Late message. */
    H12Rhw01("Homework 1.2 not attempted, message 2"),

    /** Late message. */
    H12Rhw02("Homework 1.2 not attempted, message 3"),

    /** Late message. */
    H12Rhw03("Homework 1.2 not attempted, message 4"),

    /** Late message. */
    H12Rhw04("Homework 1.2 attempted < 4 times, message 1"),

    /** Late message. */
    H12Rhw05("Homework 1.2 attempted < 4 times, message 2"),

    /** Late message. */
    H12Rhw06("Homework 1.2 attempted < 4 times, message 3"),

    /** Late message. */
    H12Rhw07("Homework 1.2 attempted < 4 times, message 4"),

    /** Late message. */
    H12Rhw99("Stuck on Homework 1.2"),

    /** Late message. */
    H12Xhw00("Homework 1.2 attempted >= 4 times, message 1"),

    /** Late message. */
    H12Xhw01("Homework 1.2 attempted >= 4 times, message 2"),

    /** Late message. */
    H12Xhw02("Homework 1.2 attempted >= 4 times, message 3"),

    /** Late message. */
    H12Xhw03("Homework 1.2 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H13Rhw00("Homework 1.3 not attempted, message 1"),

    /** Late message. */
    H13Rhw01("Homework 1.3 not attempted, message 2"),

    /** Late message. */
    H13Rhw02("Homework 1.3 not attempted, message 3"),

    /** Late message. */
    H13Rhw03("Homework 1.3 not attempted, message 4"),

    /** Late message. */
    H13Rhw04("Homework 1.3 attempted < 4 times, message 1"),

    /** Late message. */
    H13Rhw05("Homework 1.3 attempted < 4 times, message 2"),

    /** Late message. */
    H13Rhw06("Homework 1.3 attempted < 4 times, message 3"),

    /** Late message. */
    H13Rhw07("Homework 1.3 attempted < 4 times, message 4"),

    /** Late message. */
    H13Rhw99("Stuck on Homework 1.3"),

    /** Late message. */
    H13Xhw00("Homework 1.3 attempted >= 4 times, message 1"),

    /** Late message. */
    H13Xhw01("Homework 1.3 attempted >= 4 times, message 2"),

    /** Late message. */
    H13Xhw02("Homework 1.3 attempted >= 4 times, message 3"),

    /** Late message. */
    H13Xhw03("Homework 1.3 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H14Rhw00("Homework 1.4 not attempted, message 1"),

    /** Late message. */
    H14Rhw01("Homework 1.4 not attempted, message 2"),

    /** Late message. */
    H14Rhw02("Homework 1.4 not attempted, message 3"),

    /** Late message. */
    H14Rhw03("Homework 1.4 not attempted, message 4"),

    /** Late message. */
    H14Rhw04("Homework 1.4 attempted < 4 times, message 1"),

    /** Late message. */
    H14Rhw05("Homework 1.4 attempted < 4 times, message 2"),

    /** Late message. */
    H14Rhw06("Homework 1.4 attempted < 4 times, message 3"),

    /** Late message. */
    H14Rhw07("Homework 1.4 attempted < 4 times, message 4"),

    /** Late message. */
    H14Rhw99("Stuck on Homework 1.4"),

    /** Late message. */
    H14Xhw00("Homework 1.4 attempted >= 4 times, message 1"),

    /** Late message. */
    H14Xhw01("Homework 1.4 attempted >= 4 times, message 2"),

    /** Late message. */
    H14Xhw02("Homework 1.4 attempted >= 4 times, message 3"),

    /** Late message. */
    H14Xhw03("Homework 1.4 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H15Rhw00("Homework 1.5 not attempted, message 1"),

    /** Late message. */
    H15Rhw01("Homework 1.5 not attempted, message 2"),

    /** Late message. */
    H15Rhw02("Homework 1.5 not attempted, message 3"),

    /** Late message. */
    H15Rhw03("Homework 1.5 not attempted, message 4"),

    /** Late message. */
    H15Rhw04("Homework 1.5 attempted < 4 times, message 1"),

    /** Late message. */
    H15Rhw05("Homework 1.5 attempted < 4 times, message 2"),

    /** Late message. */
    H15Rhw06("Homework 1.5 attempted < 4 times, message 3"),

    /** Late message. */
    H15Rhw07("Homework 1.5 attempted < 4 times, message 4"),

    /** Late message. */
    H15Rhw99("Stuck on Homework 1.5"),

    /** Late message. */
    H15Xhw00("Homework 1.5 attempted >= 4 times, message 1"),

    /** Late message. */
    H15Xhw01("Homework 1.5 attempted >= 4 times, message 2"),

    /** Late message. */
    H15Xhw02("Homework 1.5 attempted >= 4 times, message 3"),

    /** Late message. */
    H15Xhw03("Homework 1.5 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    RE1Rre00("Review Exam 1 not attempted, message 1"),

    /** Late message. */
    RE1Rre01("Review Exam 1 not attempted, message 2"),

    /** Late message. */
    RE1Rre02("Review Exam 1 not attempted, message 3"),

    /** Late message. */
    RE1Rre03("Review Exam 1 not attempted, message 4"),

    /** Late message. */
    RE1Rre04("Review Exam 1 attempted < 4 times, message 1"),

    /** Late message. */
    RE1Rre05("Review Exam 1 attempted < 4 times, message 2"),

    /** Late message. */
    RE1Rre06("Review Exam 1 attempted < 4 times, message 3"),

    /** Late message. */
    RE1Rre07("Review Exam 1 attempted < 4 times, message 4"),

    /** Late message. */
    RE1Rre99("Stuck on Review Exam 1"),

    /** Late message. */
    RE1Xre00("Review Exam 1 attempted >= 4 times, message 1"),

    /** Late message. */
    RE1Xre01("Review Exam 1 attempted >= 4 times, message 2"),

    /** Late message. */
    RE1Xre02("Review Exam 1 attempted >= 4 times, message 3"),

    /** Late message. */
    RE1Xre03("Review Exam 1 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    UE1Rue00("Unit Exam 1 not attempted, message 1"),

    /** Late message. */
    UE1Rue01("Unit Exam 1 not attempted, message 2"),

    /** Late message. */
    UE1Rue02("Unit Exam 1 not attempted, message 3"),

    /** Late message. */
    UE1Rue03("Unit Exam 1 not attempted, message 4"),

    /** Late message. */
    UE1Rue04("Unit Exam 1 attempted < 4 times, message 1"),

    /** Late message. */
    UE1Rue05("Unit Exam 1 attempted < 4 times, message 2"),

    /** Late message. */
    UE1Rue06("Unit Exam 1 attempted < 4 times, message 3"),

    /** Late message. */
    UE1Rue07("Unit Exam 1 attempted < 4 times, message 4"),

    /** Late message. */
    UE1Rue99("Stuck on Unit Exam 1"),

    /** Late message. */
    UE1Xue00("Unit Exam 1 attempted >= 4 times, message 1"),

    /** Late message. */
    UE1Xue01("Unit Exam 1 attempted >= 4 times, message 2"),

    /** Late message. */
    UE1Xue02("Unit Exam 1 attempted >= 4 times, message 3"),

    /** Late message. */
    UE1Xue03("Unit Exam 1 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H21Rhw00("Homework 2.1 not attempted, message 1"),

    /** Late message. */
    H21Rhw01("Homework 2.1 not attempted, message 2"),

    /** Late message. */
    H21Rhw02("Homework 2.1 not attempted, message 3"),

    /** Late message. */
    H21Rhw03("Homework 2.1 not attempted, message 4"),

    /** Late message. */
    H21Rhw04("Homework 2.1 attempted < 4 times, message 1"),

    /** Late message. */
    H21Rhw05("Homework 2.1 attempted < 4 times, message 2"),

    /** Late message. */
    H21Rhw06("Homework 2.1 attempted < 4 times, message 3"),

    /** Late message. */
    H21Rhw07("Homework 2.1 attempted < 4 times, message 4"),

    /** Late message. */
    H21Rhw99("Stuck on Homework 2.1"),

    /** Late message. */
    H21Xhw00("Homework 2.1 attempted >= 4 times, message 1"),

    /** Late message. */
    H21Xhw01("Homework 2.1 attempted >= 4 times, message 2"),

    /** Late message. */
    H21Xhw02("Homework 2.1 attempted >= 4 times, message 3"),

    /** Late message. */
    H21Xhw03("Homework 2.1 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H22Rhw00("Homework 2.2 not attempted, message 1"),

    /** Late message. */
    H22Rhw01("Homework 2.2 not attempted, message 2"),

    /** Late message. */
    H22Rhw02("Homework 2.2 not attempted, message 3"),

    /** Late message. */
    H22Rhw03("Homework 2.2 not attempted, message 4"),

    /** Late message. */
    H22Rhw04("Homework 2.2 attempted < 4 times, message 1"),

    /** Late message. */
    H22Rhw05("Homework 2.2 attempted < 4 times, message 2"),

    /** Late message. */
    H22Rhw06("Homework 2.2 attempted < 4 times, message 3"),

    /** Late message. */
    H22Rhw07("Homework 2.2 attempted < 4 times, message 4"),

    /** Late message. */
    H22Rhw99("Stuck on Homework 2.2"),

    /** Late message. */
    H22Xhw00("Homework 2.2 attempted >= 4 times, message 1"),

    /** Late message. */
    H22Xhw01("Homework 2.2 attempted >= 4 times, message 2"),

    /** Late message. */
    H22Xhw02("Homework 2.2 attempted >= 4 times, message 3"),

    /** Late message. */
    H22Xhw03("Homework 2.2 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H23Rhw00("Homework 2.3 not attempted, message 1"),

    /** Late message. */
    H23Rhw01("Homework 2.3 not attempted, message 2"),

    /** Late message. */
    H23Rhw02("Homework 2.3 not attempted, message 3"),

    /** Late message. */
    H23Rhw03("Homework 2.3 not attempted, message 4"),

    /** Late message. */
    H23Rhw04("Homework 2.3 attempted < 4 times, message 1"),

    /** Late message. */
    H23Rhw05("Homework 2.3 attempted < 4 times, message 2"),

    /** Late message. */
    H23Rhw06("Homework 2.3 attempted < 4 times, message 3"),

    /** Late message. */
    H23Rhw07("Homework 2.3 attempted < 4 times, message 4"),

    /** Late message. */
    H23Rhw99("Stuck on Homework 2.3"),

    /** Late message. */
    H23Xhw00("Homework 2.3 attempted >= 4 times, message 1"),

    /** Late message. */
    H23Xhw01("Homework 2.3 attempted >= 4 times, message 2"),

    /** Late message. */
    H23Xhw02("Homework 2.3 attempted >= 4 times, message 3"),

    /** Late message. */
    H23Xhw03("Homework 2.3 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H24Rhw00("Homework 2.4 not attempted, message 1"),

    /** Late message. */
    H24Rhw01("Homework 2.4 not attempted, message 2"),

    /** Late message. */
    H24Rhw02("Homework 2.4 not attempted, message 3"),

    /** Late message. */
    H24Rhw03("Homework 2.4 not attempted, message 4"),

    /** Late message. */
    H24Rhw04("Homework 2.4 attempted < 4 times, message 1"),

    /** Late message. */
    H24Rhw05("Homework 2.4 attempted < 4 times, message 2"),

    /** Late message. */
    H24Rhw06("Homework 2.4 attempted < 4 times, message 3"),

    /** Late message. */
    H24Rhw07("Homework 2.4 attempted < 4 times, message 4"),

    /** Late message. */
    H24Rhw99("Stuck on Homework 2.4"),

    /** Late message. */
    H24Xhw00("Homework 2.4 attempted >= 4 times, message 1"),

    /** Late message. */
    H24Xhw01("Homework 2.4 attempted >= 4 times, message 2"),

    /** Late message. */
    H24Xhw02("Homework 2.4 attempted >= 4 times, message 3"),

    /** Late message. */
    H24Xhw03("Homework 2.4 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H25Rhw00("Homework 2.5 not attempted, message 1"),

    /** Late message. */
    H25Rhw01("Homework 2.5 not attempted, message 2"),

    /** Late message. */
    H25Rhw02("Homework 2.5 not attempted, message 3"),

    /** Late message. */
    H25Rhw03("Homework 2.5 not attempted, message 4"),

    /** Late message. */
    H25Rhw04("Homework 2.5 attempted < 4 times, message 1"),

    /** Late message. */
    H25Rhw05("Homework 2.5 attempted < 4 times, message 2"),

    /** Late message. */
    H25Rhw06("Homework 2.5 attempted < 4 times, message 3"),

    /** Late message. */
    H25Rhw07("Homework 2.5 attempted < 4 times, message 4"),

    /** Late message. */
    H25Rhw99("Stuck on Homework 2.5"),

    /** Late message. */
    H25Xhw00("Homework 2.5 attempted >= 4 times, message 1"),

    /** Late message. */
    H25Xhw01("Homework 2.5 attempted >= 4 times, message 2"),

    /** Late message. */
    H25Xhw02("Homework 2.5 attempted >= 4 times, message 3"),

    /** Late message. */
    H25Xhw03("Homework 2.5 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    RE2Rre00("Review Exam 2 not attempted, message 1"),

    /** Late message. */
    RE2Rre01("Review Exam 2 not attempted, message 2"),

    /** Late message. */
    RE2Rre02("Review Exam 2 not attempted, message 3"),

    /** Late message. */
    RE2Rre03("Review Exam 2 not attempted, message 4"),

    /** Late message. */
    RE2Rre04("Review Exam 2 attempted < 4 times, message 1"),

    /** Late message. */
    RE2Rre05("Review Exam 2 attempted < 4 times, message 2"),

    /** Late message. */
    RE2Rre06("Review Exam 2 attempted < 4 times, message 3"),

    /** Late message. */
    RE2Rre07("Review Exam 2 attempted < 4 times, message 4"),

    /** Late message. */
    RE2Rre99("Stuck on Review Exam 2"),

    /** Late message. */
    RE2Xre00("Review Exam 2 attempted >= 4 times, message 1"),

    /** Late message. */
    RE2Xre01("Review Exam 2 attempted >= 4 times, message 2"),

    /** Late message. */
    RE2Xre02("Review Exam 2 attempted >= 4 times, message 3"),

    /** Late message. */
    RE2Xre03("Review Exam 2 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    UE2Rue00("Unit Exam 2 not attempted, message 1"),

    /** Late message. */
    UE2Rue01("Unit Exam 2 not attempted, message 2"),

    /** Late message. */
    UE2Rue02("Unit Exam 2 not attempted, message 3"),

    /** Late message. */
    UE2Rue03("Unit Exam 2 not attempted, message 4"),

    /** Late message. */
    UE2Rue04("Unit Exam 2 attempted < 4 times, message 1"),

    /** Late message. */
    UE2Rue05("Unit Exam 2 attempted < 4 times, message 2"),

    /** Late message. */
    UE2Rue06("Unit Exam 2 attempted < 4 times, message 3"),

    /** Late message. */
    UE2Rue07("Unit Exam 2 attempted < 4 times, message 4"),

    /** Late message. */
    UE2Rue99("Stuck on Unit Exam 2"),

    /** Late message. */
    UE2Xue00("Unit Exam 2 attempted >= 4 times, message 1"),

    /** Late message. */
    UE2Xue01("Unit Exam 2 attempted >= 4 times, message 2"),

    /** Late message. */
    UE2Xue02("Unit Exam 2 attempted >= 4 times, message 3"),

    /** Late message. */
    UE2Xue03("Unit Exam 2 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H31Rhw00("Homework 3.1 not attempted, message 1"),

    /** Late message. */
    H31Rhw01("Homework 3.1 not attempted, message 2"),

    /** Late message. */
    H31Rhw02("Homework 3.1 not attempted, message 3"),

    /** Late message. */
    H31Rhw03("Homework 3.1 not attempted, message 4"),

    /** Late message. */
    H31Rhw04("Homework 3.1 attempted < 4 times, message 1"),

    /** Late message. */
    H31Rhw05("Homework 3.1 attempted < 4 times, message 2"),

    /** Late message. */
    H31Rhw06("Homework 3.1 attempted < 4 times, message 3"),

    /** Late message. */
    H31Rhw07("Homework 3.1 attempted < 4 times, message 4"),

    /** Late message. */
    H31Rhw99("Stuck on Homework 3.1"),

    /** Late message. */
    H31Xhw00("Homework 3.1 attempted >= 4 times, message 1"),

    /** Late message. */
    H31Xhw01("Homework 3.1 attempted >= 4 times, message 2"),

    /** Late message. */
    H31Xhw02("Homework 3.1 attempted >= 4 times, message 3"),

    /** Late message. */
    H31Xhw03("Homework 3.1 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H32Rhw00("Homework 3.2 not attempted, message 1"),

    /** Late message. */
    H32Rhw01("Homework 3.2 not attempted, message 2"),

    /** Late message. */
    H32Rhw02("Homework 3.2 not attempted, message 3"),

    /** Late message. */
    H32Rhw03("Homework 3.2 not attempted, message 4"),

    /** Late message. */
    H32Rhw04("Homework 3.2 attempted < 4 times, message 1"),

    /** Late message. */
    H32Rhw05("Homework 3.2 attempted < 4 times, message 2"),

    /** Late message. */
    H32Rhw06("Homework 3.2 attempted < 4 times, message 3"),

    /** Late message. */
    H32Rhw07("Homework 3.2 attempted < 4 times, message 4"),

    /** Late message. */
    H32Rhw99("Stuck on Homework 3.2"),

    /** Late message. */
    H32Xhw00("Homework 3.2 attempted >= 4 times, message 1"),

    /** Late message. */
    H32Xhw01("Homework 3.2 attempted >= 4 times, message 2"),

    /** Late message. */
    H32Xhw02("Homework 3.2 attempted >= 4 times, message 3"),

    /** Late message. */
    H32Xhw03("Homework 3.2 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H33Rhw00("Homework 3.3 not attempted, message 1"),

    /** Late message. */
    H33Rhw01("Homework 3.3 not attempted, message 2"),

    /** Late message. */
    H33Rhw02("Homework 3.3 not attempted, message 3"),

    /** Late message. */
    H33Rhw03("Homework 3.3 not attempted, message 4"),

    /** Late message. */
    H33Rhw04("Homework 3.3 attempted < 4 times, message 1"),

    /** Late message. */
    H33Rhw05("Homework 3.3 attempted < 4 times, message 2"),

    /** Late message. */
    H33Rhw06("Homework 3.3 attempted < 4 times, message 3"),

    /** Late message. */
    H33Rhw07("Homework 3.3 attempted < 4 times, message 4"),

    /** Late message. */
    H33Rhw99("Stuck on Homework 3.3"),

    /** Late message. */
    H33Xhw00("Homework 3.3 attempted >= 4 times, message 1"),

    /** Late message. */
    H33Xhw01("Homework 3.3 attempted >= 4 times, message 2"),

    /** Late message. */
    H33Xhw02("Homework 3.3 attempted >= 4 times, message 3"),

    /** Late message. */
    H33Xhw03("Homework 3.3 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H34Rhw00("Homework 3.4 not attempted, message 1"),

    /** Late message. */
    H34Rhw01("Homework 3.4 not attempted, message 2"),

    /** Late message. */
    H34Rhw02("Homework 3.4 not attempted, message 3"),

    /** Late message. */
    H34Rhw03("Homework 3.4 not attempted, message 4"),

    /** Late message. */
    H34Rhw04("Homework 3.4 attempted < 4 times, message 1"),

    /** Late message. */
    H34Rhw05("Homework 3.4 attempted < 4 times, message 2"),

    /** Late message. */
    H34Rhw06("Homework 3.4 attempted < 4 times, message 3"),

    /** Late message. */
    H34Rhw07("Homework 3.4 attempted < 4 times, message 4"),

    /** Late message. */
    H34Rhw99("Stuck on Homework 3.4"),

    /** Late message. */
    H34Xhw00("Homework 3.4 attempted >= 4 times, message 1"),

    /** Late message. */
    H34Xhw01("Homework 3.4 attempted >= 4 times, message 2"),

    /** Late message. */
    H34Xhw02("Homework 3.4 attempted >= 4 times, message 3"),

    /** Late message. */
    H34Xhw03("Homework 3.4 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H35Rhw00("Homework 3.5 not attempted, message 1"),

    /** Late message. */
    H35Rhw01("Homework 3.5 not attempted, message 2"),

    /** Late message. */
    H35Rhw02("Homework 3.5 not attempted, message 3"),

    /** Late message. */
    H35Rhw03("Homework 3.5 not attempted, message 4"),

    /** Late message. */
    H35Rhw04("Homework 3.5 attempted < 4 times, message 1"),

    /** Late message. */
    H35Rhw05("Homework 3.5 attempted < 4 times, message 2"),

    /** Late message. */
    H35Rhw06("Homework 3.5 attempted < 4 times, message 3"),

    /** Late message. */
    H35Rhw07("Homework 3.5 attempted < 4 times, message 4"),

    /** Late message. */
    H35Rhw99("Stuck on Homework 3.5"),

    /** Late message. */
    H35Xhw00("Homework 3.5 attempted >= 4 times, message 1"),

    /** Late message. */
    H35Xhw01("Homework 3.5 attempted >= 4 times, message 2"),

    /** Late message. */
    H35Xhw02("Homework 3.5 attempted >= 4 times, message 3"),

    /** Late message. */
    H35Xhw03("Homework 3.5 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    RE3Rre00("Review Exam 3 not attempted, message 1"),

    /** Late message. */
    RE3Rre01("Review Exam 3 not attempted, message 2"),

    /** Late message. */
    RE3Rre02("Review Exam 3 not attempted, message 3"),

    /** Late message. */
    RE3Rre03("Review Exam 3 not attempted, message 4"),

    /** Late message. */
    RE3Rre04("Review Exam 3 attempted < 4 times, message 1"),

    /** Late message. */
    RE3Rre05("Review Exam 3 attempted < 4 times, message 2"),

    /** Late message. */
    RE3Rre06("Review Exam 3 attempted < 4 times, message 3"),

    /** Late message. */
    RE3Rre07("Review Exam 3 attempted < 4 times, message 4"),

    /** Late message. */
    RE3Rre99("Stuck on Review exam 3"),

    /** Late message. */
    RE3Xre00("Review Exam 3 attempted >= 4 times, message 1"),

    /** Late message. */
    RE3Xre01("Review Exam 3 attempted >= 4 times, message 2"),

    /** Late message. */
    RE3Xre02("Review Exam 3 attempted >= 4 times, message 3"),

    /** Late message. */
    RE3Xre03("Review Exam 3 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    UE3Rue00("Unit Exam 3 not attempted, message 1"),

    /** Late message. */
    UE3Rue01("Unit Exam 3 not attempted, message 2"),

    /** Late message. */
    UE3Rue02("Unit Exam 3 not attempted, message 3"),

    /** Late message. */
    UE3Rue03("Unit Exam 3 not attempted, message 4"),

    /** Late message. */
    UE3Rue04("Unit Exam 3 attempted < 4 times, message 1"),

    /** Late message. */
    UE3Rue05("Unit Exam 3 attempted < 4 times, message 2"),

    /** Late message. */
    UE3Rue06("Unit Exam 3 attempted < 4 times, message 3"),

    /** Late message. */
    UE3Rue07("Unit Exam 3 attempted < 4 times, message 4"),

    /** Late message. */
    UE3Rue99("Stuck on Unit exam 3"),

    /** Late message. */
    UE3Xue00("Unit Exam 3 attempted >= 4 times, message 1"),

    /** Late message. */
    UE3Xue01("Unit Exam 3 attempted >= 4 times, message 2"),

    /** Late message. */
    UE3Xue02("Unit Exam 3 attempted >= 4 times, message 3"),

    /** Late message. */
    UE3Xue03("Unit Exam 3 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H41Rhw00("Homework 4.1 not attempted, message 1"),

    /** Late message. */
    H41Rhw01("Homework 4.1 not attempted, message 2"),

    /** Late message. */
    H41Rhw02("Homework 4.1 not attempted, message 3"),

    /** Late message. */
    H41Rhw03("Homework 4.1 not attempted, message 4"),

    /** Late message. */
    H41Rhw04("Homework 4.1 attempted < 4 times, message 1"),

    /** Late message. */
    H41Rhw05("Homework 4.1 attempted < 4 times, message 2"),

    /** Late message. */
    H41Rhw06("Homework 4.1 attempted < 4 times, message 3"),

    /** Late message. */
    H41Rhw07("Homework 4.1 attempted < 4 times, message 4"),

    /** Late message. */
    H41Rhw99("Stuck on Homework 4.1"),

    /** Late message. */
    H41Xhw00("Homework 4.1 attempted >= 4 times, message 1"),

    /** Late message. */
    H41Xhw01("Homework 4.1 attempted >= 4 times, message 2"),

    /** Late message. */
    H41Xhw02("Homework 4.1 attempted >= 4 times, message 3"),

    /** Late message. */
    H41Xhw03("Homework 4.1 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H42Rhw00("Homework 4.2 not attempted, message 1"),

    /** Late message. */
    H42Rhw01("Homework 4.2 not attempted, message 2"),

    /** Late message. */
    H42Rhw02("Homework 4.2 not attempted, message 3"),

    /** Late message. */
    H42Rhw03("Homework 4.2 not attempted, message 4"),

    /** Late message. */
    H42Rhw04("Homework 4.2 attempted < 4 times, message 1"),

    /** Late message. */
    H42Rhw05("Homework 4.2 attempted < 4 times, message 2"),

    /** Late message. */
    H42Rhw06("Homework 4.2 attempted < 4 times, message 3"),

    /** Late message. */
    H42Rhw07("Homework 4.2 attempted < 4 times, message 4"),

    /** Late message. */
    H42Rhw99("Stuck on Homework 4.2"),

    /** Late message. */
    H42Xhw00("Homework 4.2 attempted >= 4 times, message 1"),

    /** Late message. */
    H42Xhw01("Homework 4.2 attempted >= 4 times, message 2"),

    /** Late message. */
    H42Xhw02("Homework 4.2 attempted >= 4 times, message 3"),

    /** Late message. */
    H42Xhw03("Homework 4.2 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H43Rhw00("Homework 4.3 not attempted, message 1"),

    /** Late message. */
    H43Rhw01("Homework 4.3 not attempted, message 2"),

    /** Late message. */
    H43Rhw02("Homework 4.3 not attempted, message 3"),

    /** Late message. */
    H43Rhw03("Homework 4.3 not attempted, message 4"),

    /** Late message. */
    H43Rhw04("Homework 4.3 attempted < 4 times, message 1"),

    /** Late message. */
    H43Rhw05("Homework 4.3 attempted < 4 times, message 2"),

    /** Late message. */
    H43Rhw06("Homework 4.3 attempted < 4 times, message 3"),

    /** Late message. */
    H43Rhw07("Homework 4.3 attempted < 4 times, message 4"),

    /** Late message. */
    H43Rhw99("Stuck on Homework 4.3"),

    /** Late message. */
    H43Xhw00("Homework 4.3 attempted >= 4 times, message 1"),

    /** Late message. */
    H43Xhw01("Homework 4.3 attempted >= 4 times, message 2"),

    /** Late message. */
    H43Xhw02("Homework 4.3 attempted >= 4 times, message 3"),

    /** Late message. */
    H43Xhw03("Homework 4.3 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H44Rhw00("Homework 4.4 not attempted, message 1"),

    /** Late message. */
    H44Rhw01("Homework 4.4 not attempted, message 2"),

    /** Late message. */
    H44Rhw02("Homework 4.4 not attempted, message 3"),

    /** Late message. */
    H44Rhw03("Homework 4.4 not attempted, message 4"),

    /** Late message. */
    H44Rhw04("Homework 4.4 attempted < 4 times, message 1"),

    /** Late message. */
    H44Rhw05("Homework 4.4 attempted < 4 times, message 2"),

    /** Late message. */
    H44Rhw06("Homework 4.4 attempted < 4 times, message 3"),

    /** Late message. */
    H44Rhw07("Homework 4.4 attempted < 4 times, message 4"),

    /** Late message. */
    H44Rhw99("Stuck on Homework 4.4"),

    /** Late message. */
    H44Xhw00("Homework 4.4 attempted >= 4 times, message 1"),

    /** Late message. */
    H44Xhw01("Homework 4.4 attempted >= 4 times, message 2"),

    /** Late message. */
    H44Xhw02("Homework 4.4 attempted >= 4 times, message 3"),

    /** Late message. */
    H44Xhw03("Homework 4.4 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    H45Rhw00("Homework 4.5 not attempted, message 1"),

    /** Late message. */
    H45Rhw01("Homework 4.5 not attempted, message 2"),

    /** Late message. */
    H45Rhw02("Homework 4.5 not attempted, message 3"),

    /** Late message. */
    H45Rhw03("Homework 4.5 not attempted, message 4"),

    /** Late message. */
    H45Rhw04("Homework 4.5 attempted < 4 times, message 1"),

    /** Late message. */
    H45Rhw05("Homework 4.5 attempted < 4 times, message 2"),

    /** Late message. */
    H45Rhw06("Homework 4.5 attempted < 4 times, message 3"),

    /** Late message. */
    H45Rhw07("Homework 4.5 attempted < 4 times, message 4"),

    /** Late message. */
    H45Rhw99("Stuck on Homework 4.5"),

    /** Late message. */
    H45Xhw00("Homework 4.5 attempted >= 4 times, message 1"),

    /** Late message. */
    H45Xhw01("Homework 4.5 attempted >= 4 times, message 2"),

    /** Late message. */
    H45Xhw02("Homework 4.5 attempted >= 4 times, message 3"),

    /** Late message. */
    H45Xhw03("Homework 4.5 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    RE4Rre00("Review Exam 4 not attempted, message 1"),

    /** Late message. */
    RE4Rre01("Review Exam 4 not attempted, message 2"),

    /** Late message. */
    RE4Rre02("Review Exam 4 not attempted, message 3"),

    /** Late message. */
    RE4Rre03("Review Exam 4 not attempted, message 4"),

    /** Late message. */
    RE4Rre04("Review Exam 4 attempted < 4 times, message 1"),

    /** Late message. */
    RE4Rre05("Review Exam 4 attempted < 4 times, message 2"),

    /** Late message. */
    RE4Rre06("Review Exam 4 attempted < 4 times, message 3"),

    /** Late message. */
    RE4Rre07("Review Exam 4 attempted < 4 times, message 4"),

    /** Late message. */
    RE4Rre99("Stuck on Review Exam 4"),

    /** Late message. */
    RE4Xre00("Review Exam 4 attempted >= 4 times, message 1"),

    /** Late message. */
    RE4Xre01("Review Exam 4 attempted >= 4 times, message 2"),

    /** Late message. */
    RE4Xre02("Review Exam 4 attempted >= 4 times, message 3"),

    /** Late message. */
    RE4Xre03("Review Exam 4 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    UE4Rue00("Unit Exam 4 not attempted, message 1"),

    /** Late message. */
    UE4Rue01("Unit Exam 4 not attempted, message 2"),

    /** Late message. */
    UE4Rue02("Unit Exam 4 not attempted, message 3"),

    /** Late message. */
    UE4Rue03("Unit Exam 4 not attempted, message 4"),

    /** Late message. */
    UE4Rue04("Unit Exam 4 attempted < 4 times, message 1"),

    /** Late message. */
    UE4Rue05("Unit Exam 4 attempted < 4 times, message 2"),

    /** Late message. */
    UE4Rue06("Unit Exam 4 attempted < 4 times, message 3"),

    /** Late message. */
    UE4Rue07("Unit Exam 4 attempted < 4 times, message 4"),

    /** Late message. */
    UE4Rue99("Stuck on Unit Exam 4"),

    /** Late message. */
    UE4Xue00("Unit Exam 4 attempted >= 4 times, message 1"),

    /** Late message. */
    UE4Xue01("Unit Exam 4 attempted >= 4 times, message 2"),

    /** Late message. */
    UE4Xue02("Unit Exam 4 attempted >= 4 times, message 3"),

    /** Late message. */
    UE4Xue03("Unit Exam 4 attempted >= 4 times, message 4"),

    //

    /** Late message. */
    FINRfe00("Final Exam not attempted, message 1"),

    /** Late message. */
    FINRfe01("Final Exam not attempted, message 2"),

    /** Late message. */
    FINRfe02("Final Exam not attempted, message 3"),

    /** Late message. */
    FINRfe04("Final Exam attempted < 4 times, message 1"),

    /** Late message. */
    FINRfe05("Final Exam attempted < 4 times, message 2"),

    /** Late message. */
    FINRfe06("Final Exam attempted < 4 times, message 3"),

    /** Late message. */
    FINXfe00("Final Exam attempted >= 4 times, message 1"),

    /** Late message. */
    FINXfe01("Final Exam attempted >= 4 times, message 2"),

    /** Late message. */
    FINXfe02("Final Exam attempted >= 4 times, message 3"),

    //

    /** Late message. */
    LASTfe00("Final Exam not attempted, last try window"),

    /** Late message. */
    LASTfe01("Final Exam attempted, last try window"),

    /** Late message. */
    BLOKwd00("Blocked"),

    //

    /** Late message. */
    PNTSrt00("Final passed, needs to get to 54 points"),

    /** Late message. */
    PNTSrt99("Stuck with less than 54 points"),

    /** Late message. */
    GRDCrt00("Has grade C"),

    /** Late message. */
    GRDBrt00("Has grade B"),

    //

    ;

    /** A description of the message. */
    final String desc;

    /**
     * Constructs a new {@code EMsgCode}.
     *
     * @param theDesc the description
     */
    EMsg(final String theDesc) {

        this.desc = theDesc;
    }
}
