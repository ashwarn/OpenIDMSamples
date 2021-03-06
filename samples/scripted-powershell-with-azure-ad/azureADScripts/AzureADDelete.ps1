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
    This is a sample Delete script for Azure Active Directory
	
.DESCRIPTION
	The script uses the Remove-Msolser and Remove-MsolGroup cmdlets to delete objects
	
.INPUT VARIABLES
	The connector sends us the following:
	- <prefix>.Configuration : handler to the connector's configuration object
	- <prefix>.Options: a handler to the Operation Options
	- <prefix>.Operation: String correponding to the operation ("AUTHENTICATE" here)
	- <prefix>.ObjectClass: the Object class object (__ACCOUNT__ / __GROUP__ / other)
	- <prefix>.Uid: the Uid (__UID__) object that specifies the object to delete
	
.RETURNS 
	Nothing
	
.NOTES  
    File Name      : AzureADDelete.ps1  
    Author         : Gael Allioux (gael.allioux@forgerock.com)
    Prerequisite   : PowerShell V2 and later
    Copyright      : 2015-2016 - ForgeRock AS    

.LINK  
    Script posted over:  
    http://openicf.forgerock.org
		
	Azure Active Directory Module for Windows PowerShell
	https://msdn.microsoft.com/en-us/library/azure/jj151815.aspx
#>

# See https://technet.microsoft.com/en-us/library/hh847796.aspx
$ErrorActionPreference = "Stop"
$VerbosePreference = "Continue"

try
{
if ($Connector.Operation -eq "DELETE")
{
	if (!$Env:OpenICF_AAD) {
		$msolcred = New-object System.Management.Automation.PSCredential $Connector.Configuration.Login, $Connector.Configuration.Password.ToSecureString()
		connect-msolservice -credential $msolcred
		$Env:OpenICF_AAD = $true
		Write-Verbose "New session created"
	}

	switch ($Connector.ObjectClass.Type)
	{
		"__ACCOUNT__"  
		{ 
			Remove-MsolUser -ObjectId $Connector.Uid.GetUidValue() -Force 
		}
		"__GROUP__"    
		{ 
		Remove-MsolGroup -ObjectId $Connector.Uid.GetUidValue() -Force 
		}
		default 
		{
			throw New-Object Org.IdentityConnectors.Framework.Common.Exceptions.ConnectorException("Unsupported type: $($Connector.ObjectClass.Type)")
		}
	}
}
else
{
	throw New-Object Org.IdentityConnectors.Framework.Common.Exceptions.ConnectorException("DeleteScript can not handle operation: $($Connector.Operation)")
}
}
catch #Re-throw the original exception message within a connector exception
{
	($cause,$op) = $_.FullyQualifiedErrorId -split ","
	if ($cause.EndsWith("NotFoundException"))
	{
		throw New-Object Org.IdentityConnectors.Framework.Common.Exceptions.UnknownUidException($_.Exception.Message)
	}

	# It is safe to remove the session flag
	if ($Env:OpenICF_AAD) 
	{
		Remove-Item Env:\OpenICF_AAD
		Write-Verbose "Removed session flag"
	}

	throw New-Object Org.IdentityConnectors.Framework.Common.Exceptions.ConnectorException($_.Exception.Message)
}
