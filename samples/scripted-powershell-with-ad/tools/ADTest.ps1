#
# Copyright 2014-2017 ForgeRock AS. All Rights Reserved
#
# Use of this code requires a commercial software license with ForgeRock AS.
# or with one of its affiliates. All use shall be exclusively subject
# to such license between the licensee and ForgeRock AS.
#
#REQUIRES -Version 2.0

<#  
.SYNOPSIS  
    This is a sample Test script for Active Directory
	
.DESCRIPTION
	The script tries to fetch the list of Domains of the Forest the service
	is logged to

.INPUT
	The connector sends us the following:
	- <prefix>.Configuration : handler to the connector's configuration object
	- <prefix>.Operation: String correponding to the action ("TEST" here)
	
.RETURNS
	Nothing. Should throw an exception if test failed
	
.NOTES  
    File Name      : ADTest.ps1  
    Author         : Gael Allioux (gael.allioux@forgerock.com)
    Prerequisite   : PowerShell V2 - AD module loaded by the connector
    Copyright 2014 - ForgeRock AS    
	
.LINK  
    Script posted over:  
    http://openicf.forgerock.org
	
	Active Directory Administration with Windows PowerShell
	http://technet.microsoft.com/en-us/library/dd378937(v=ws.10).aspx
#>

try
{
 if ($Connector.Operation -eq "TEST")
 {
 	(Get-ADForest).domains
 }
 else
 {
 	throw New-Object Org.IdentityConnectors.Framework.Common.Exceptions.ConnectorException("TestScript can not handle operation: $($Connector.Operation)")
 }
}
catch #Rethrow the original exception
{
	throw
}
