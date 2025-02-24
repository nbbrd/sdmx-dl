package sdmxdl;

public enum Confidentiality {

    PUBLIC,       // severity: none       // color: gray
    UNRESTRICTED, // severity: low        // color: blue
    RESTRICTED,   // severity: medium     // color: yellow
    CONFIDENTIAL, // severity: high       // color: orange
    SECRET;       // severity: very high  // color: red
}
