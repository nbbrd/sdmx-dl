package sdmxdl.format.time;

/**
 * <pre>
 * 562 4.2.3 Standard Time Period
 * 563 This is the superset of any predefined time period or a distinct point in time. A time
 * 564 period consists of a distinct start and end point. If the start and end of a period are
 * 565 expressed as date instead of a complete date time, then it is implied that the start of
 * 566 the period is the beginning of the start day (i.e. 00:00:00) and the end of the period is
 * 567 the end of the end day (i.e. 23:59:59).
 * </pre>
 */
public interface StandardTimePeriod extends ObservationalTimePeriod {
}
