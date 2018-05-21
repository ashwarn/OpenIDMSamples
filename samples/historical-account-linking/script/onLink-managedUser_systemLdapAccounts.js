/*
 * Copyright 2015-2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */

/**
 * When a managed/user is linked to a target resource, this adds a relationship to the managed/user's historicalAccounts
 * field which also includes the linked date property and will set the historical account as being active.
 * 
 * The newly added relationship is of the form:
 * 
 * {
 *     "_ref" : "system/ldap/accounts/uid=jdoe,ou=People,dc=example,dc=com"
 *     "_refProperties" : {
 *         "_id": "b6580bf0-7ece-4856-b716-64f16f8cb6a7",
 *         "_rev": "2",
 *         "linkedDate" : "Sun Oct 04 2015 19:18:31 GMT-0700 (PDT)",
 *         "active" : true
 *     }
 * }
 */

var targetRef = mappingConfig.target + "/" + target._id,
    sourcePath = mappingConfig.source + "/" + source._id,
    state = target.disabled ? "disabled" : "enabled",
    historicalAccount = {
		"_ref" : targetRef,
        "_refProperties" : {
        	"active" : true,
        	"linkDate" : (new Date()).toString(),
            "state" : state,
            "stateLastChanged" : (new Date()).toString()
        }
    },
    result;

logger.debug("Adding historical account " + targetRef + " to managed user " + source._id);

result = openidm.create(sourcePath + "/historicalAccounts", null, historicalAccount);

logger.debug("Created historical account: " + result);
