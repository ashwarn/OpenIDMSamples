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
    This is a sample Search script with Active Directory as a target
	
.DESCRIPTION
	This script leverages both Get-ADUser and Get-ADGroup to search for users and groups.
	It also demo how to use PowerShell pipes to handles the results back to the connector framework.
	
.INPUT VARIABLES
	The connector injects the following variables to the script:
	- <prefix>.Configuration : handler to the connector's configuration object
	- <prefix>.Options: a handler to the Operation Options
	- <prefix>.Operation: String correponding to the operation ("AUTHENTICATE" here)
	- <prefix>.ObjectClass: the Object class object (__ACCOUNT__ / __GROUP__ / other)	
	- <prefix>.Query: a handler to the Query.
	
.RETURNS
	The entries found
	
.NOTES  
    File Name      : ADSearch.ps1  
    Author         : Gael Allioux (gael.allioux@forgerock.com)
    Prerequisite   : PowerShell V2 - AD module loaded by the connector
    Copyright 2014 - ForgeRock AS    
.LINK  
    Script posted over:  
    http://openicf.forgerock.org
		
	Active Directory Administration with Windows PowerShell
	http://technet.microsoft.com/en-us/library/dd378937(v=ws.10).aspx
#>
# We need this global Boolean to handle the case where the search handler returns false 
# and we don't want to break the pipe because of https://bugster.forgerock.org/jira/browse/OPENIDM-2650
$proceed = $TRUE

# We define a filter to process results through a pipe and feed the result handler
filter Process-Results {
	if ($proceed)
	{
		$result = @{"__UID__" = $_.ObjectGUID; "__NAME__" = $_.DistinguishedName}

		foreach($attrName in $Connector.Options.AttributesToGet)
		{
			if ($_.Contains($attrName))
			{
			
				if ($_.$attrName -eq $null)
				{
					$result.Add($attrName, $null)
				}
				elseif ($_.$attrName.GetType().Name -eq "ADPropertyValueCollection")
				{
					$values = @();
					foreach($val in $_.$attrName) 
					{
						$values += $val.ToString()
					}
					$result.Add($attrName, $values)
				}
				else
				{
					$result.Add($attrName, $_.$attrName.ToString())
				}
			}
		}
		$proceed = $Connector.Result.Process($result)
	}
}

# Always put code in try/catch statement and make sure exceptions are re-thrown to connector
try
{
	$searchBase = $Connector.Configuration.PropertyBag.baseContext
	$attrsToGet = "*"
	$filter = "*"

	if ( $Connector.Query ) {$filter = $Connector.Query}

	switch ($Connector.ObjectClass.Type)
	{
		"__ACCOUNT__"
		{
			Get-ADUser -Filter $filter -SearchBase $searchBase -Properties $attrsToGet | Process-Results
		}
		"__GROUP__"
		{
			Get-ADGroup -Filter $filter -SearchBase $searchBase -Properties $attrsToGet | Process-Results
		}
		default
		{
			throw "Unsupported type: $($Connector.ObjectClass.Type)"	
		}
	}	
}
catch #Re-throw the original exception
{
	throw
}
