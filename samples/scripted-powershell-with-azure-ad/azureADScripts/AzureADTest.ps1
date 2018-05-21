#
# Copyright 2016-2017 ForgeRock AS. All Rights Reserved
#
# Use of this code requires a commercial software license with ForgeRock AS.
# or with one of its affiliates. All use shall be exclusively subject
# to such license between the licensee and ForgeRock AS.
#
#REQUIRES -Version 2.0

<#  
.SYNOPSIS  
    This is a sample Test script for Azure Active Directory
	
.DESCRIPTION
	The Test script is used to check the configuration is good 
	and the target system is reachable.
	This script tries to fetch the company-level information.

.INPUT
	The connector injects the following:
	- <prefix>.Configuration : handler to the connector's configuration object
	- <prefix>.Operation: String correponding to the action ("TEST" here)
	
.RETURNS
	Nothing. An exception should be thrown if the test failed
	
.NOTES  
    File Name      : AzureADTest.ps1  
    Author         : Gael Allioux (gael.allioux@forgerock.com)
    Prerequisite   : PowerShell V2 and later
    Copyright      : 2015-2016 - ForgeRock AS
	
.LINK  
    OpenICF
    http://openicf.forgerock.org
	
	OpenICF Javadoc
	https://forgerock.org/openicf/doc/apidocs/

	Azure Active Directory Module for Windows PowerShell
	https://msdn.microsoft.com/en-us/library/azure/jj151815.aspx
#>

# Preferences variables can be set here.
# See https://technet.microsoft.com/en-us/library/hh847796.aspx
$ErrorActionPreference = "Stop"
$VerbosePreference = "Continue"

# The script code should always be enclosed within a try/catch block. 
# If any exception is thrown, it is good practice to catch the original exception 
# message and re-throw it within a connector exception
try
{
# Since one script can be used for multiple actions, it is safe to check the operation first.
 if ($Connector.Operation -eq "TEST")
 {
	if (!$Env:OpenICF_AAD) 
	{
		$msolcred = New-Object System.Management.Automation.PSCredential $Connector.Configuration.Login, $Connector.Configuration.Password.ToSecureString()
		connect-msolservice -credential $msolcred
		$Env:OpenICF_AAD = $true
		Write-Verbose "New session created"
	}
	
	$comp = Get-MsolCompanyInformation
	Write-Verbose "Company: $($comp.DisplayName)"
 }
 else
 {
 	throw New-Object Org.IdentityConnectors.Framework.Common.Exceptions.ConnectorException("TestScript can not handle operation: $($Connector.Operation)")
 }
}
catch #Re-throw the original exception message within a connector exception
{
	# It is safe to remove the session flag
	if ($Env:OpenICF_AAD) 
	{
		Remove-Item Env:\OpenICF_AAD
		Write-Verbose "Removed session flag"
	}

	throw New-Object Org.IdentityConnectors.Framework.Common.Exceptions.ConnectorException($_.Exception.Message)
}
