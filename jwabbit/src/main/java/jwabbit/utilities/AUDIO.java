package jwabbit.utilities;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.ICpuCallback;
import jwabbit.core.TimerContext;

/**
 * WABBITEMU SOURCE: utilities/sound.h, "AUDIO_t" struct.
 */
public final class AUDIO {

    /** WABBITEMU SOURCE: utilities/sound.h, "SAMPLE_RATE" macro. */
    public static final int SAMPLE_RATE = 48000;

    /** WABBITEMU SOURCE: utilities/sound.h, "SAMPLE_LENGTH" macro. */
    public static final float SAMPLE_LENGTH = 1.0f / (float) SAMPLE_RATE;

    /** WABBITEMU SOURCE: utilities/sound.h, "BUFFER_SAMPLES" macro. */
    public static final int BUFFER_SAMPLES = SAMPLE_RATE;

    /** Init. */
    private boolean init;

    /** Enabled. */
    private boolean enabled;

    /** The buffer. */
    private final SAMPLE[] buffer;

    /** The current pnt. */
    private int curPnt;

    /** The play pnt. */
    private int playPnt;

    /** The play time. */
    private double playTime;

    /** The last sample. */
    private double lastSample;

    /** Left on. */
    private int leftOn;

    /** The last flip left. */
    private double lastFlipLeft;

    /** The high length left. */
    private double highLengLeft;

    /** Right on. */
    private int rightOn;

    /** The last flip right. */
    private double lastFlipRight;

    /** The high length right. */
    private double highLengRight;

    /** The volume. */
    private double volume;

    /**
     * Constructs a new {@code AUDIO}.
     */
    public AUDIO() {

        super();

        this.buffer = new SAMPLE[BUFFER_SAMPLES];
        for (int i = 0; i < BUFFER_SAMPLES; ++i) {
            this.buffer[i] = new SAMPLE();
        }
    }

    /**
     * Sets the initialized flag.
     *
     * @param isInit true if initialized
     */
    public void setInit(final boolean isInit) {

        this.init = isInit;
    }

    /**
     * Tests whether audio has been initialized.
     *
     * @return true if initialized
     */
    public boolean isInit() {

        return this.init;
    }

    /**
     * Sets the enabled flag.
     *
     * @param isEnabled true if enabled
     */
    public void setEnabled(final boolean isEnabled) {

        this.enabled = isEnabled;
    }

    /**
     * Tests whether audio has been enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {

        return this.enabled;
    }

    /**
     * Gets the sample buffer.
     *
     * @return the buffer
     */
    public SAMPLE[] getBuffer() {

        return this.buffer;
    }

    /**
     * Sets the current pointer value.
     *
     * @param theCurPnt the value
     */
    public void setCurPnt(final int theCurPnt) {

        this.curPnt = theCurPnt;
    }

    /**
     * Gets the current pointer value.
     *
     * @return the value
     */
    public int getCurPnt() {

        return this.curPnt;
    }

    /**
     * Sets the last sample time.
     *
     * @param theLastSample the value
     */
    public void setLastSample(final double theLastSample) {

        this.lastSample = theLastSample;
    }

    /**
     * Gets the last sample time.
     *
     * @return the value
     */
    public double getLastSample() {

        return this.lastSample;
    }

    /**
     * Sets the left on value.
     *
     * @param theLeftOn the value
     */
    public void setLeftOn(final int theLeftOn) {

        this.leftOn = theLeftOn;
    }

    /**
     * Gets the left on value.
     *
     * @return the value
     */
    public int getLeftOn() {

        return this.leftOn;
    }

    /**
     * Sets the last flip left value.
     *
     * @param theLastFlipLeft the value
     */
    public void setLastFlipLeft(final double theLastFlipLeft) {

        this.lastFlipLeft = theLastFlipLeft;
    }

    /**
     * Gets the last flip left value.
     *
     * @return the value
     */
    public double getLastFlipLeft() {

        return this.lastFlipLeft;
    }

    /**
     * Sets the high length left value.
     *
     * @param theHighLengLeft the value
     */
    public void setHighLengLeft(final double theHighLengLeft) {

        this.highLengLeft = theHighLengLeft;
    }

    /**
     * Gets the high length left value.
     *
     * @return the value
     */
    public double getHighLengLeft() {

        return this.highLengLeft;
    }

    /**
     * Sets the right on value.
     *
     * @param theRightOn the value
     */
    public void setRightOn(final int theRightOn) {

        this.rightOn = theRightOn;
    }

    /**
     * Gets the right on value.
     *
     * @return the value
     */
    public int getRightOn() {

        return this.rightOn;
    }

    /**
     * Sets the last flip right value.
     *
     * @param theLastFlipRight the value
     */
    public void setLastFlipRight(final double theLastFlipRight) {

        this.lastFlipRight = theLastFlipRight;
    }

    /**
     * Gets the last flip right value.
     *
     * @return the value
     */
    public double getLastFlipRight() {

        return this.lastFlipRight;
    }

    /**
     * Sets the high length right value.
     *
     * @param theHighLengRight the value
     */
    public void setHighLengRight(final double theHighLengRight) {

        this.highLengRight = theHighLengRight;
    }

    /**
     * Gets the high length right value.
     *
     * @return the value
     */
    public double getHighLengRight() {

        return this.highLengRight;
    }

    /**
     * Sets the volume.
     *
     * @param theVolume the volume
     */
    public void setVolume(final double theVolume) {

        this.volume = theVolume;
    }

    /**
     * Gets the volume.
     *
     * @return the volume
     */
    public double getVolume() {

        return this.volume;
    }

    /**
     * Sets the CPU.
     *
     * @param theCpu the CPU
     */
    public void setCpu(final CPU theCpu) {

        // No action
    }

    /**
     * Sets the timer context.
     *
     * @param theTimer the timer context
     */
    public void setTimerContext(final TimerContext theTimer) {

        // No action
    }

    /**
     * Sets the audio frame callback.
     *
     * @param theCallback the audio frame callback
     */
    public void setAudioFrameCallback(final ICpuCallback theCallback) {

        // No action
    }

    /**
     * WABBITEMU SOURCE: utilities/sound.c, "KillSound" function.
     */
    public void killSound() {

        if (this.init) {
            this.enabled = false;
            this.init = false;
        }
    }
}
