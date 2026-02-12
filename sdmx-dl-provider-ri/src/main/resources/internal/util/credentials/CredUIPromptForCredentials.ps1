# Load the required assembly
Add-Type @"
using System;
using System.Runtime.InteropServices;
using System.Text;

public class CredUIPrompt {
    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Auto)]
    public struct CREDUI_INFO {
        public int cbSize;
        public IntPtr hwndParent;
        [MarshalAs(UnmanagedType.LPTStr)]
        public string pszMessageText;
        [MarshalAs(UnmanagedType.LPTStr)]
        public string pszCaptionText;
        public int cgCred;
    }

    [DllImport("credui.dll", SetLastError = true, CharSet = CharSet.Auto)]
    public static extern int CredUIPromptForCredentials(
        ref CREDUI_INFO creditInfo,
        string targetName,
        IntPtr reserved,
        int error,
        StringBuilder userName,
        int maxUserName,
        StringBuilder password,
        int maxPassword,
        ref bool save,
        int flags
    );
}
"@

# Prepare variables for the prompt
$credInfo = New-Object CredUIPrompt+CREDUI_INFO
$credInfo.cbSize = [Runtime.InteropServices.Marshal]::SizeOf($credInfo)
$credInfo.pszMessageText = $Env:MESSAGE
$credInfo.pszCaptionText = $Env:CAPTION
$credInfo.cgCred = 0

# Set the target name for the credentials
$target = "MySecureResource"
$userName = New-Object Text.StringBuilder(100)
$password = New-Object Text.StringBuilder(100)
$save = $false

# Call the function to display the prompt
$result = [CredUIPrompt]::CredUIPromptForCredentials([ref]$credInfo, $target, [IntPtr]::Zero, 0, $userName, $userName.Capacity, $password, $password.Capacity, [ref]$save, 0)

# Check the result and output the username and password if successful
if ($result -eq 0) {
    $user = $userName.ToString()
    if ($user.StartsWith($target + "\")) {
        $user = $user.Substring($target.Length + 1)
    }
    echo "$($user):$($password.ToString())"
}
