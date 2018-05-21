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
    This is a sample Delete script for Active Directory
	
.DESCRIPTION
	The script uses the Remove-ADUser and Remove-ADGroup cmdlets to delete objects
	
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
    File Name      : ADDelete.ps1  
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
if ($Connector.Operation -eq "DELETE")
{
	switch ($Connector.ObjectClass.Type)
	{
		"__ACCOUNT__"  {Remove-ADUser -Identity $Connector.Uid.GetUidValue() -Confirm:$false}
		"__GROUP__" {Remove-ADGroup -Identity $Connector.Uid.GetUidValue() -Confirm:$false}
		default {throw "Unsupported type: $($Connector.ObjectClass.Type)"}
	}
}
else
{
	throw New-Object Org.IdentityConnectors.Framework.Common.Exceptions.ConnectorException("DeleteScript can not handle operation: $($Connector.Operation)")
}
}
catch #Re-throw the original exception
{
	throw
}
