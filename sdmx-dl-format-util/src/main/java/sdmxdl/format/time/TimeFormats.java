package sdmxdl.format.time;

import nbbrd.design.MightBePromoted;

/**
 * Time formats as described in the <a href="https://sdmx.org/wp-content/uploads/SDMX_2-1_SECTION_6_TechnicalNotes_2020-07.pdf">SDMX21 technical notes</a>
 *
 * <pre>
 * 536 4.2 Time and Time Format
 * 537 4.2.1 Introduction
 * 538 First, it is important to recognize that most observation times are a period. SDMX
 * 539 specifies precisely how Time is handled.
 * 540
 * 541 The representation of time is broken into a hierarchical collection of representations.
 * 542 A data structure definition can use of any of the representations in the hierarchy as
 * 543 the representation of time. This allows for the time dimension of a particular data
 * 544 structure definition allow for only a subset of the default representation.
 * 545
 * 546 The hierarchy of time formats is as follows (bold indicates a category which is made
 * 547 up of multiple formats, italic indicates a distinct format):
 * 548
 * 549    Observational Time Period
 * 550     o Standard Time Period
 * 551        Basic Time Period
 * 552          Gregorian Time Period
 * 553          Date Time
 * 554        Reporting Time Period
 * 555     o Time Range
 * 556
 * 557 The details of these time period categories and of the distinct formats which make
 * 558 them up are detailed in the sections to follow.
 * </pre>
 */
abstract class TimeFormats {

    private TimeFormats() {
    }

    @MightBePromoted
    static int indexOf(CharSequence text, char c) {
        if (text instanceof String) {
            return ((String) text).indexOf(c);
        }
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    @MightBePromoted
    static int parseNumeric(CharSequence text, int start, int end) {
        int result = 0;
        for (int i = start; i < end; i++) {
            int c = text.charAt(i) - '0';
            if (c < 0 || c > 9) {
                return -1;
            }
            result = result * 10 + c;
        }
        return result;
    }

    @MightBePromoted
    static boolean isInRange(int value, int startInclusive, int endExclusive) {
        return startInclusive <= value && value < endExclusive;
    }
}
