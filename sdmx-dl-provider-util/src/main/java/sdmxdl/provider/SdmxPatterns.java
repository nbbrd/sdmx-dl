package sdmxdl.provider;

import java.util.regex.Pattern;

@lombok.experimental.UtilityClass
public class SdmxPatterns {

    private static final String ID_TYPE = "[A-Za-z0-9_@$-]+";
    private static final String NC_NAME_ID_TYPE = "[A-Za-z][A-Za-z0-9_-]*";
    private static final String VERSION_NUMBER = "[0-9]+(\\.[0-9]+)*";

    public static final Pattern VERSION_PATTERN = Pattern.compile("(all|latest|" + VERSION_NUMBER + ")");
    public static final Pattern RESOURCE_ID_PATTERN = Pattern.compile(ID_TYPE);
    public static final Pattern AGENCY_ID_PATTERN = Pattern.compile(NC_NAME_ID_TYPE);

    public static final Pattern FLOW_REF_PATTERN = Pattern.compile("(" + ID_TYPE + ")|(" + NC_NAME_ID_TYPE + "," + ID_TYPE + ",(latest|" + VERSION_NUMBER + "))");
}
