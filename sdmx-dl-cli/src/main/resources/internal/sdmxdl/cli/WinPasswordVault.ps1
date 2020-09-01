
[void][Windows.Security.Credentials.PasswordVault,Windows.Security.Credentials,ContentType=WindowsRuntime]

function ConvertTo-PasswordCredential( [String] $resource, [System.Management.Automation.PSCredential] $pscred ) {
	return (New-Object Windows.Security.Credentials.PasswordCredential($resource, $pscred.UserName, $pscred.GetNetworkCredential().Password))
}

function GetCredential( [string] $resource ) {
	try {
		$cred = (New-Object Windows.Security.Credentials.PasswordVault).FindAllByResource($resource) | select -First 1
		$cred.retrievePassword()
		return $cred
	} catch {
		if ( $_.Exception.message -match "element not found" ) {
			return $null
		}
		throw $_.exception
	}
}

function AddCredential( [Windows.Security.Credentials.PasswordCredential] $cred ) {
	(New-Object Windows.Security.Credentials.PasswordVault).Add($cred)
}

function RemoveCredential( [Windows.Security.Credentials.PasswordCredential] $cred ) {
	(New-Object Windows.Security.Credentials.PasswordVault).Remove($cred)
}

function PromptCredential( [String] $resource, [String] $message ) {
	$result = $host.ui.PromptForCredential($resource, $message, "", "")
	if ($result -eq $null) {
		return $null
	}
	return ConvertTo-PasswordCredential -resource $resource -pscred $result
}

function GetOrPromptCredential( [String] $resource, [String] $message, [Bool] $force ) {
    if ($force -eq $false) {
    	$result = GetCredential -resource $resource
    }
	if ($result -eq $null) {
		$result = PromptCredential -resource $resource -message $message
		if ($result -ne $null) {
			AddCredential -cred $result
		}
	}
	return $result
}

function InvalidateCredential( [String] $resource ) {
    $cred = GetCredential -resource $resource
    if ($cred -ne $null) {
        RemoveCredential -cred $cred
    }
}