package jwabbit.iface;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: interface/calc.h, "EVENT_TYPE" enum.
 */
public enum EnumEventType {

    /** NO_EVENT. */
    NO_EVENT,

    /** ROM_LOAD_EVENT. */
    ROM_LOAD_EVENT,

    /** LCD_ENQUEUE_EVENT. */
    LCD_ENQUEUE_EVENT,

    /** ROM_RUNNING_EVENT. */
    ROM_RUNNING_EVENT,

    /** BREAKPOINT_EVENT. */
    BREAKPOINT_EVENT,

    /** GIF_FRAME_EVENT. */
    GIF_FRAME_EVENT,

    /** AVI_VIDEO_FRAME_EVENT. */
    AVI_VIDEO_FRAME_EVENT,

    /** AVI_AUDIO_FRAME_EVENT. */
    AVI_AUDIO_FRAME_EVENT
}
