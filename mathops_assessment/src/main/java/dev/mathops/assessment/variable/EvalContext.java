package dev.mathops.assessment.variable;

import dev.mathops.assessment.AbstractXmlObject;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.template.AbstractDocContainer;
import dev.mathops.assessment.document.template.AbstractDocInput;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.formula.ErrorValue;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.IntegerVectorValue;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * A collection of parameters. This class has the ability to generate values for the parameters, resolving ordering
 * issues to the extent possible. If there is a circular reference, or a reference to an unknown parameter in a
 * parameter's formula, an appropriate error message is generated.
 */
public final class EvalContext extends AbstractXmlObject {

    /** The seed for secure random number generation. */
    private static long seed;

    /** The set of parameters. */
    private final HashMap<String, AbstractVariable> vars;

    /** Random number generator for building parameters. */
    private final Random rand;

    /** Flag indicating derived value out of range, need to randomize again. */
    private boolean retry;

    /** Flag to indicate exam or problem being prepared for print output. */
    private boolean printTarget;

    /**
     * Constructs a new {@code ParameterSet}.
     */
    public EvalContext() {

        super();

        // Preserve insertion order
        this.vars = new LinkedHashMap<>(10);

        if (seed == 0L) {
            seed = System.currentTimeMillis();
        }

        this.rand = new Random();
        this.rand.setSeed(seed);
        seed = this.rand.nextLong();

        this.printTarget = false;
    }

    /**
     * Sets the flag that indicates exam or problem is being prepared for print output.
     *
     * @param isPrintTarget true if print output, false if interactive or web output
     */
    public void setPrintTarget(final boolean isPrintTarget) {

        this.printTarget = isPrintTarget;
    }

    /**
     * Tests whether problem is being prepared for print output.
     *
     * @return true if print output, false if interactive or web output
     */
    public boolean isPrintTarget() {

        return this.printTarget;
    }

    /**
     * Creates a copy of the parameter set. This is a deep copy that creates distinct copies of each contained
     * {@code Parameter}. The randomizer in the copy will NOT inherit the seed value of the original (so they will NOT
     * generate the same parameter values the next time each is generated).
     *
     * @return a copy of the original object
     */
    public EvalContext deepCopy() {

        final EvalContext copy = new EvalContext();

        for (final Map.Entry<String, AbstractVariable> entry : this.vars.entrySet()) {
            copy.vars.put(entry.getKey(), entry.getValue().deepCopy());
        }

        return copy;
    }

    /**
     * Gets the set of variable names in the context.
     *
     * @return a list of variable names (in declaration order)
     */
    public List<String> getVariableNames() {

        final List<String> list;

        synchronized (this.vars) {
            list = new ArrayList<>(this.vars.keySet());
        }

        return list;
    }

    /**
     * Gets the number of variables in the context.
     *
     * @return the number of variables
     */
    public int numVariables() {

        return this.vars.size();
    }

    /**
     * Gets the list of variables in the context.
     *
     * @return a collection of {@code Parameter} objects
     */
    public Collection<AbstractVariable> getVariables() {

        return this.vars.values();
    }

    /**
     * Retrieves a particular variable based on its name.
     *
     * @param name the name of the variable to retrieve
     * @return the requested variable, or null if the name was not found
     */
    public AbstractVariable getVariable(final String name) {

        return this.vars.get(name);
    }

    /**
     * Adds a variable to the variable set.
     *
     * @param var the variable to add
     */
    public void addVariable(final AbstractVariable var) {

        synchronized (this.vars) {
            this.vars.put(var.name, var);
        }
    }

    /**
     * Removes a parameter to the parameter set.
     *
     * @param name the name of the parameter to remove
     */
    public void removeVariable(final String name) {

        synchronized (this.vars) {
            this.vars.remove(name);
        }
    }

    /**
     * Generates values for the parameters, if possible.
     *
     * @param identifier an identifier to include in log messages on error
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean generate(final String identifier) {

        synchronized (this) {
            synchronized (this.vars) {

                // See if all parameters referenced in any formula are really in our set of parameters.
                // If not, there's no point trying to generate.
                if (!verifyParametersPresent()) {
                    return false;
                }

                // If random parameters result in derived values falling outside the permitted range,
                // we will re-attempt to generate new random parameters. We cap this number of attempts
                // at 10,000 tries.
                for (int attempts = 0; attempts < 10000; ++attempts) {
                    this.retry = false;

                    // Clear all random and derived parameter values
                    for (final AbstractVariable p : this.vars.values()) {
                        p.clearDerivedValues();
                    }

                    // Loop until every parameter that is not an input has a value, or until we pass
                    // through all parameters without making any progress, or until too many iterations
                    // have been attempted.
                    int inx;

                    for (inx = 0; inx < 1000; ++inx) {

                        // See if we're done yet (if all parameters have a value)
                        boolean done = true;

                        for (final AbstractVariable param : this.vars.values()) {
                            if (param.isInput()) {
                                continue;
                            }

                            if (!param.hasValue()) {
                                done = false;
                                break;
                            }
                        }

                        if (done) {
                            return true;
                        }

                        // Scan all parameters, trying to generate values for those that do not yet
                        // have one, and tracking whether we made any headway.
                        boolean change = false;

                        for (final AbstractVariable p : this.vars.values()) {
                            // Don't generate input parameters
                            if (p.isInput()) {
                                continue;
                            }

                            if (!p.hasValue() && generateValue(p)) {
                                change = true;
                            }
                        }

                        // If we derived some values, but they are forbidden, retry
                        if (this.retry) {
                            inx = 2000; // So we don't think it was an infinite loop
                            break;
                        }

                        // If we went through the list and were not able to derive any more values,
                        // break out to prevent infinite loop.
                        if (!change) {
                            final HtmlBuilder msg = new HtmlBuilder(100);
                            msg.addln(identifier, ": Unable to compute values for:");

                            for (final AbstractVariable param : this.vars.values()) {
                                if (param.isInput()) {
                                    continue;
                                }

                                if (!param.hasValue()) {
                                    msg.addln("   {", param.name, "} : ", param.toXmlString(0));
                                }
                            }

                            msg.add(" [other parameter values are ");

                            for (final AbstractVariable param : this.vars.values()) {

                                if (param instanceof VariableInteger || param instanceof VariableReal
                                        || param instanceof VariableBoolean || param instanceof VariableRandomInteger
                                        || param instanceof VariableRandomReal || param instanceof VariableRandomBoolean
                                        || param instanceof VariableRandomPermutation
                                        || param instanceof VariableRandomSimpleAngle) {
                                    msg.add(" {", param.name,
                                            "=", param.getValue(), "}");
                                } else if (param instanceof VariableSpan || param instanceof VariableRandomChoice
                                        || param instanceof VariableDerived) {
                                    final Object value = param.getValue();

                                    if (value instanceof final DocSimpleSpan span) {
                                        msg.add(" {", param.name, "=\"", span.toXml(0), "\"}");
                                    } else {
                                        msg.add(" {", param.name, "=", value, "}");
                                    }
                                } else if (param.isInput()) {
                                    final Object value = param.getValue();

                                    msg.add(" {", param.name, "=", value, "}");
                                }
                            }

                            msg.add(". Check for circular references.");

                            Log.warning(msg.toString());

                            return false;
                        }
                    }

                    if (inx == 1000) {
                        final HtmlBuilder msg = new HtmlBuilder(50);
                        msg.add(identifier, ": Unable to compute values for");

                        for (final AbstractVariable param : this.vars.values()) {
                            if (param.isInput()) {
                                continue;
                            }

                            if (!param.hasValue()) {
                                msg.add(" {", param.name, "}");
                            }
                        }

                        msg.add(". Infinite loop while trying.");

                        Log.warning(msg.toString());

                        return false;
                    }
                }
            }

            // If we get here, we tried 10,000 times to build a "good" set of parameters, but were
            // unable to.
            Log.warning(identifier, ": Unable to obey derived value constraints. Infinite loop.");

            return false;
        }
    }

//    /**
//     * Processes the fact that a parameter's value has changed by recomputing any derived parameter
//     * values. Only DERIVED and SPAN parameter types will be recomputed. None of the min/max,
//     * choose-from or excludes formulae will be reevaluated.
//     *
//     * @return {@code true} if successful, {@code false} otherwise
//     */
//     public synchronized boolean parameterChanged() {
//
//     // Don't process changed messages while we're processing a change.
//     if (this.changing) {
//     return true;
//     }
//
//     this.changing = true;
//
//     synchronized (this.vars) {
//
//     // Clear all derived parameter values, leave random ones
//     for (final AbstractVariable p : this.vars.values()) {
//
//     if (p instanceof VariableDerived) {
//     p.clearDerivedValues();
//     }
//     }
//
//     // Loop until every parameter has a value, or until we pass through all parameters
//     // without making any progress, or until too many iterations have been attempted.
//     for (int i = 0; i < 1000; ++i) {
//
//     // See if we're done yet (if all parameters have a value)
//     boolean done = true;
//
//     for (final AbstractVariable p : this.vars.values()) {
//
//     if (!(p instanceof VariableInputInt || p instanceof VariableInputReal)) {
//
//     if (!p.hasValue()) {
//     done = false;
//
//     break;
//     }
//     }
//     }
//
//     if (done) {
//
//     // Now that all parameters have a value, apply these values to any span
//     // objects acting as parameter values.
//     for (final AbstractVariable p : this.vars.values()) {
//     final Object value = p.getValue();
//
//     if (value instanceof DocSimpleSpan) {
//     ((DocSimpleSpan) value).realize(this);
//     }
//     }
//
//     // This is the only way we leave this method on success.
//     this.changing = false;
//
//     return true;
//     }
//
//     // Scan all parameters, trying to generate values for those that do not yet have
//     // one, and tracking whether we made any headway.
//     boolean change = false;
//
//     for (final AbstractVariable p : this.vars.values()) {
//
//     if (!p.hasValue()) {
//
//     if (generateValue(p)) {
//     change = true;
//     }
//     }
//     }
//
//     // If we went through the list and were not able to derive any more values, break
//     // out to prevent infinite loop.
//     if (!change) {
//     break;
//     }
//     }
//     }
//
//     // Got through 1000 iterations without completing.
//     this.changing = false;
//
//     return false;
//     }

    /**
     * Tests a set of parameters to see that it is internally consistent. That is, see that every parameter referenced
     * in any formula used in any parameter is one of those in the set of parameters.
     *
     * @return {@code true} if set is internally consistent; {@code false} otherwise
     */
    private boolean verifyParametersPresent() {

        synchronized (this.vars) {

            for (final AbstractVariable param : this.vars.values()) {

                final Collection<Formula> list = new ArrayList<>(10);

                if (param instanceof final IRangedVariable pRanged) {
                    if (pRanged.getMin() != null && pRanged.getMin().getFormula() != null) {
                        list.add(pRanged.getMin().getFormula());
                    }
                    if (pRanged.getMax() != null && pRanged.getMax().getFormula() != null) {
                        list.add(pRanged.getMax().getFormula());
                    }
                }

                if (param instanceof final VariableDerived der) {
                    if (der.getFormula() != null) {
                        list.add(der.getFormula());
                    }
                }

                for (final Formula formula : list) {
                    final String[] names = formula.parameterNames();

                    for (final String name : names) {
                        // Domain parameters can be missing
                        if ("x".equals(name)) {
                            continue;
                        }

                        String actualName = name;
                        if (!name.isEmpty() && name.charAt(name.length() - 1) == ']') {
                            final int end = name.indexOf('[');
                            actualName = name.substring(0, end);
                        }

                        if (!this.vars.containsKey(actualName)) {
                            Log.warning("No parameter '", actualName, "' defined.");
                            return false;
                        }
                    }
                }

                if (param instanceof IExcludableVariable) {
                    final Formula[] excludes = ((IExcludableVariable) param).getExcludes();

                    if (excludes != null) {

                        for (final Formula exclude : excludes) {

                            if (exclude != null) {
                                final String[] names = exclude.parameterNames();

                                for (final String name : names) {
                                    // Domain parameters can be missing
                                    if ("x".equals(name)) {
                                        continue;
                                    }

                                    if (!this.vars.containsKey(name)) {
                                        Log.warning("No parameter '", name, "' defined.");
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }

                if (param instanceof final VariableRandomChoice rcvar) {

                    final Formula[] choices = rcvar.getChooseFromList();

                    if (choices != null) {

                        for (final Formula choice : choices) {

                            if (choice != null) {
                                final String[] names = choice.parameterNames();

                                for (final String name : names) {
                                    // Domain parameters can be missing
                                    if ("x".equals(name)) {
                                        continue;
                                    }

                                    if (!this.vars.containsKey(name)) {
                                        Log.warning("No parameter '", name, "' defined.");
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }

                if (param.getValue() instanceof final DocSimpleSpan span) {
                    final Set<String> names = span.parameterNames();

                    for (final String name : names) {
                        // Domain parameters can be missing
                        if ("x".equals(name)) {
                            continue;
                        }

                        if (!this.vars.containsKey(name)) {
                            Log.warning("No parameter '", name, "' defined.", new Exception());
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Generate a value for a single parameter.
     *
     * @param var the parameter for which to generate a value
     * @return {@code true} if the parameter's value was set, {@code false} otherwise
     */
    private boolean generateValue(final AbstractVariable var) {

        final boolean result;

        if (var instanceof final VariableRandomChoice choiceVar) {
            result = generateRandomChoice(choiceVar);
        } else if (var instanceof VariableRandomInteger || var instanceof VariableRandomReal
                || var instanceof VariableRandomBoolean || var instanceof VariableRandomSimpleAngle) {
            result = generateRandomValue(var);
        } else if (var instanceof final VariableRandomPermutation permVar) {
            result = generateRandomPermutation(permVar);
        } else // It is an error for any other type not to have a value yet.
            if (var instanceof final VariableDerived derivedVar) {
                result = generateDerivedValue(derivedVar);
            } else {
                result = var instanceof VariableInputInteger || var instanceof VariableInputReal
                        || var instanceof VariableInputString;
            }

        return result;
    }

    /**
     * Generates a value for a single derived parameter using its formula. This is possible only if all parameters
     * referenced in the formula have values.
     *
     * @param derived the parameter for which to generate a value
     * @return {@code true} if the parameter's value was set, {@code false} otherwise
     */
    private boolean generateDerivedValue(final VariableDerived derived) {

        // If there is a min/max, compute it.
        final Number[] minMax = new Number[2];
        if (!getMinAndMax(derived, minMax) || !verifyExcludes(derived)) {
            return false;
        }

        final Formula formula = derived.getFormula();

        if (formula != null) {
            final Object obj = formula.evaluate(this);

            if (obj instanceof final Long lng) {
                final double val = lng.doubleValue();

                if ((minMax[0] != null && val < minMax[0].doubleValue())
                        || (minMax[1] != null && val > minMax[1].doubleValue())) {
                    // No action
                } else if (checkExcludes(lng.longValue(), derived)) {
                    derived.setValue(obj);
                    return true;
                }
                this.retry = true;
            } else if (obj instanceof final Number nbr) {
                final double val = nbr.doubleValue();

                if ((minMax[0] != null && val < minMax[0].doubleValue())
                        || (minMax[1] != null && val > minMax[1].doubleValue())) {
                    // No action
                } else {
                    // NOTE: Excludes are ignored for non-integer numeric values
                    derived.setValue(obj);
                    return true;
                }
                this.retry = true;
            } else if (obj instanceof final Boolean boo) {
                if (checkExcludes(boo.booleanValue(), derived)) {
                    derived.setValue(obj);
                    return true;
                }
                this.retry = true;
            } else if (obj instanceof String || obj instanceof DocSimpleSpan
                    || obj instanceof IntegerVectorValue) {
                derived.setValue(obj);
                return true;
            }
        }

        return false;
    }

    /**
     * Generates a value for a single random parameter. Note that the formulae for the min and max values must be
     * evaluated to do this, so this is possible only if all parameters referenced in those formulae have values.
     *
     * @param param the parameter for which to generate a value
     * @return {@code true} if the parameter's value was set, {@code false} otherwise
     */
    private boolean generateRandomChoice(final VariableRandomChoice param) {

        // See if the excludes list can be computed yet.
        // See if the choices list can be computed yet.
        if (!verifyExcludes(param) || !verifyChooseFrom(param)) {
            return false;
        }

        // See that there are some items to choose from
        final Formula[] choices = param.getChooseFromList();
        if (choices == null || choices.length == 0) {
            return false;
        }

        for (int loop = 0; loop < 100; ++loop) {
            // Now select from the choices
            final int which = this.rand.nextInt(choices.length);
            final Object value = choices[which].evaluate(this);

            if (value instanceof Long) {
                if (checkExcludes(((Long) value).longValue(), param)) {
                    param.setValue(value);
                    return true;
                }
            } else {
                // Choices that aren't longs don't need exclusion checking
                param.setValue(value);
                return true;
            }
        }

        this.retry = true;

        return false;
    }

    /**
     * Generates a value for a single random parameter. Note that the formulae for the min and max values must be
     * evaluated to do this, so this is possible only if all parameters referenced in those formulae have values.
     *
     * @param param the parameter for which to generate a value
     * @return {@code true} if the parameter's value was set, {@code false} otherwise
     */
    private boolean generateRandomValue(final AbstractVariable param) {

        // Boolean values are trivial, so take care of this case quickly
        if (param instanceof final VariableRandomBoolean prBool) {
            final boolean randBoolean = this.rand.nextBoolean();
            prBool.setValue(Boolean.valueOf(randBoolean));
            return true;
        }

        // Enforce requirements for min/max to be present
        final Number[] minMax = new Number[2];
        if (!getMinAndMax(param, minMax)) {
            return false;
        }

        // Now we have values for both min and max, and we can generate the
        // random value, checking against excluded values as needed
        if (param instanceof final VariableRandomInteger pRInt) {

            // See if the excludes list can be computed yet.
            if (!verifyExcludes(pRInt) || minMax[0] == null || minMax[1] == null) {
                return false;
            }

            final long minInt = minMax[0].doubleValue() < 0.0 ? (long) (minMax[0].doubleValue() - 0.1)
                    : (long) (minMax[0].doubleValue() + 0.1);

            final long maxInt = minMax[1].doubleValue() < 0.0 ? (long) (minMax[1].doubleValue() - 0.1)
                    : (long) (minMax[1].doubleValue() + 0.1);

            final long iRange = maxInt - minInt + 1L;

            if (iRange > 0L) {

                for (int loop = 0; loop < 100; ++loop) {
                    final long iVal = (long) this.rand.nextInt((int) iRange) + minInt;

                    if (checkExcludes(iVal, pRInt)) {
                        pRInt.setValue(Long.valueOf(iVal));
                        break;
                    }
                }

                return pRInt.hasValue();
            } else {
                return false;
            }
        } else if (param instanceof VariableRandomReal) {

            if (minMax[0] != null && minMax[1] != null) {
                final double rRange = minMax[1].doubleValue() - minMax[0].doubleValue();

                if (rRange > 0.0) {
                    final double rVal = Math.abs(this.rand.nextDouble()) * rRange + minMax[0].doubleValue();
                    param.setValue(Double.valueOf(rVal));
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else if (param instanceof final VariableRandomSimpleAngle pRAngle) {

            // See if the excludes list can be computed yet.
            if (!verifyExcludes(pRAngle)) {
                return false;
            }

            final int min = clamp(minMax[0] == null ? 0 : minMax[0].intValue(), 0, 359);
            final int max = clamp(minMax[1] == null ? 359 : minMax[1].intValue(), 0, 359);
            int maxDenom = 30;

            final NumberOrFormula maxD = pRAngle.getMaxDenom();
            if (maxD != null) {
                final Object obj = maxD.evaluate(this);
                if (obj instanceof final Long l) {
                    maxDenom = clamp(l.intValue(), 1, 180);
                } else {
                    return false;
                }
            }

            final int iRange = max - min + 1;
            for (int loop = 0; loop < 100; ++loop) {
                final int degrees = this.rand.nextInt(iRange) + min;
                final int denom = lookupDenom(degrees);
                if (denom <= maxDenom && checkExcludes((long) degrees, pRAngle)) {
                    pRAngle.setValue(Long.valueOf((long) degrees));
                    break;
                }
            }

            return pRAngle.hasValue();
        }

        return true;
    }

    /**
     * Clamps a value to a min/max range.
     *
     * @param value the value
     * @param min   the minimum
     * @param max   the maximum
     * @return the clamped result (no less than min, no greater than max)
     */
    private static int clamp(final int value, final int min, final int max) {

        final int result;

        if (value < min) {
            result = min;
        } else {
            result = Math.min(value, max);
        }

        return result;
    }

    /**
     * Given a degree measure, returns the denominator in reduced fraction coefficient on PI in the corresponding radian
     * measure. For example, 150 degrees corresponds to "5 PI / 6", so this method would return 6. A degree measure of 0
     * returns 1 (0 PI / 1).
     *
     * @param degree the degree measure (in the range 0 to 359)
     * @return the denominator in the radian measure
     */
    private static int lookupDenom(final int degree) {

        // Fraction is "d/180" - find GCD of d and 180, then divide 180 by this...

        final BigInteger biggcd = new BigInteger(Integer.toString(degree)).gcd(new BigInteger("180"));

        return 180 / Integer.parseInt(biggcd.toString());
    }

    /**
     * Generates a random permutation.
     *
     * @param param the parameter for which to generate a value
     * @return {@code true} if the parameter's value was set, {@code false} otherwise
     */
    private boolean generateRandomPermutation(final VariableRandomPermutation param) {

        if (param.getMin() == null || param.getMax() == null) {
            return false;
        }
        // Enforce requirements for min/max to be present
        final Number[] minMax = new Number[2];
        if (!getMinAndMax(param, minMax) || minMax[0] == null || minMax[1] == null) {
            return false;
        }

        final int min = (int) Math.ceil(minMax[0].doubleValue() - 0.0001);
        final int max = (int) Math.floor(minMax[1].doubleValue() + 0.0001);

        if (max < min || (max - min) > 10000) {
            return false;
        }

        final int numEntries = max - min + 1;

        final List<Long> ordered = new ArrayList<>(numEntries);
        final List<Long> permuted = new ArrayList<>(numEntries);

        for (int i = 0; i < numEntries; ++i) {
            ordered.add(Long.valueOf((long) (min + i)));
        }

        while (!ordered.isEmpty()) {
            final int index = this.rand.nextInt(ordered.size());
            permuted.add(ordered.remove(index));
        }

        param.setValue(new IntegerVectorValue(permuted));

        return true;
    }

    /**
     * Gets the minimum and maximum value allowed for a parameter.
     *
     * @param param  the {@code Parameter} from which to get the minimum
     * @param minMax a 2-double array which will be populated with the minimum (in [0]) and maximum (in [1]) values - if
     *               the parameter has no min or max, Double.MIN_VALUE or Double.MAX_VALUE will be used
     * @return {@code true} if successful; {@code false} if the min/max formulae are present but cannot be evaluated
     */
    private boolean getMinAndMax(final AbstractVariable param, final Number[] minMax) {

        boolean ok = true;

        if (param instanceof final IRangedVariable pRanged) {

            final NumberOrFormula min = pRanged.getMin();
            final NumberOrFormula max = pRanged.getMax();

            if (min != null) {
                final Object obj = min.evaluate(this);

                if (obj instanceof final Number nbr) {
                    minMax[0] = nbr;
                } else {
                    ok = false;
                }
            }

            if (max != null) {
                final Object obj = max.evaluate(this);

                if (obj instanceof final Number nbr) {
                    minMax[1] = nbr;
                } else {
                    ok = false;
                }
            }
        }

        return ok;
    }

    /**
     * Tests whether all the excludes list can be computed. Since the excludes list is a set of formulae, this tests
     * whether all the needed parameter values are known to compute all excluded values.
     *
     * @param param the {@code Parameter} to test
     * @return {@code true} if all excluded values can be computed; {@code false} otherwise
     */
    private boolean verifyExcludes(final IExcludableVariable param) {

        final Formula[] excludes = param.getExcludes();
        boolean result;

        if (excludes == null || excludes.length == 0) {

            // No excludes, so we're fine.
            result = true;
        } else {
            result = true;

            for (final Formula exclude : excludes) {
                final Object obj = exclude.evaluate(this);

                if (obj instanceof ErrorValue) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Tests whether all the choose-from list can be computed. Since the choose-from list is a set of formulae, this
     * tests whether all the needed parameter values are known to compute all choice values.
     *
     * @param param the {@code Parameter} to test
     * @return {@code true} if all choice values can be computed; {@code false} otherwise
     */
    private boolean verifyChooseFrom(final VariableRandomChoice param) {

        final Formula[] chooseFrom = param.getChooseFromList();
        boolean result;

        if (chooseFrom == null || chooseFrom.length == 0) {
            result = true;
        } else {
            result = true;
            for (final Formula formula : chooseFrom) {
                final Object obj = formula.evaluate(this);

                if (obj instanceof ErrorValue) {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Tests whether an integer value matches any of the excludes values.
     *
     * @param val   the value to test against excluded values
     * @param param the {@code Parameter} to check
     * @return {@code true} if the value is not excluded; {@code false} otherwise
     */
    private boolean checkExcludes(final long val, final IExcludableVariable param) {

        final Formula[] excludes = param.getExcludes();
        boolean result;

        if (excludes == null || excludes.length == 0) {
            result = true;
        } else {
            result = true;

            for (final Formula exclude : excludes) {
                final Object obj = exclude.evaluate(this);

                if (obj instanceof final Long lng) {
                    if (val == lng.longValue()) {
                        result = false;
                        break;
                    }
                } else if (obj instanceof final Double dbl) {
                    if (Double.valueOf((double) val).equals(dbl)) {
                        result = false;
                        break;
                    }
                } else {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Tests whether a boolean value matches any of the excludes values.
     *
     * @param val   the value to test against excluded values
     * @param param the {@code Parameter} to check
     * @return {@code true} if the value is not excluded; {@code false} otherwise
     */
    private boolean checkExcludes(final boolean val, final IExcludableVariable param) {

        final Formula[] excludes = param.getExcludes();
        boolean result;

        if (excludes == null || excludes.length == 0) {
            result = true;
        } else {
            result = true;

            for (final Formula exclude : excludes) {
                final Object obj = exclude.evaluate(this);

                if (obj instanceof final Boolean boolVal) {
                    if (val == boolVal.booleanValue()) {
                        result = false;
                        break;
                    }
                } else {
                    result = false;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Generates a diagnostic {@code String} representation of the parameter set, indicating which parameters have
     * values.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder buf = new HtmlBuilder(100);

        synchronized (this.vars) {
            boolean comma = false;

            for (final AbstractVariable param : this.vars.values()) {
                final Object value = param.getValue();
                buf.add(param.name);

                if (value != null) {
                    buf.add('=');
                    buf.add(value.toString());
                    comma = true;
                }

                if (comma) {
                    buf.add(CoreConstants.COMMA_CHAR);
                }
            }
        }

        return buf.toString();
    }

    /**
     * Appends the XML representation of the object to an {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void appendXml(final HtmlBuilder xml, final int indent) {

        synchronized (this.vars) {

            for (final AbstractVariable abstractVariable : this.vars.values()) {
                abstractVariable.appendXml(xml, indent);
            }
        }
    }

    /**
     * Generates a hash code based on the non-transient member variables.
     *
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public int hashCode() {

        return this.vars.hashCode();
    }

    /**
     * Tests non-transient member variables in this base class for equality with another instance.
     *
     * @param obj the other instance
     * @return {@code true} if all fields in this base class are equal in the two objects
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final EvalContext ctx) {
            equal = Objects.equals(this.vars, ctx.vars);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Gather all the DocInput objects contained in any span-valued variable in this context.
     *
     * @param inputs the list to which to add found inputs
     */
    public void accumulateInputs(final List<AbstractDocInput> inputs) {

        for (final AbstractVariable var : this.vars.values()) {

            if (var instanceof final VariableSpan spanVar) {
                final Object obj = spanVar.getValue();
                if (obj instanceof final AbstractDocContainer contain) {
                    contain.accumulateInputs(inputs);
                }
            }
        }
    }
}
