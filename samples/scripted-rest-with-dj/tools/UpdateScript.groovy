/*
 * Copyright 2014-2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */

import groovy.json.JsonBuilder
import groovyx.net.http.RESTClient
import org.apache.http.client.HttpClient
import org.forgerock.openicf.connectors.scriptedrest.ScriptedRESTConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.objects.Attribute
import org.identityconnectors.framework.common.objects.AttributesAccessor
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.OperationOptions
import org.identityconnectors.framework.common.objects.Uid

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.PUT

def operation = operation as OperationType
def updateAttributes = new AttributesAccessor(attributes as Set<Attribute>)
def configuration = configuration as ScriptedRESTConfiguration
def httpClient = connection as HttpClient
def connection = customizedConnection as RESTClient
def name = id as String
def log = log as Log
def objectClass = objectClass as ObjectClass
def options = options as OperationOptions
def uid = uid as Uid

log.info("Entering " + operation + " Script");

switch (objectClass) {
    case ObjectClass.ACCOUNT:
        def builder = new JsonBuilder()
        builder {
            contactInformation {
                telephoneNumber(updateAttributes.hasAttribute("telephoneNumber") ? updateAttributes.findString("telephoneNumber") : "")
                emailAddress(updateAttributes.hasAttribute("emailAddress") ? updateAttributes.findString("emailAddress") : "")
            }
            delegate.name({
                familyName(updateAttributes.hasAttribute("familyName") ? updateAttributes.findString("familyName") : "")
                givenName(updateAttributes.hasAttribute("givenName") ? updateAttributes.findString("givenName") : "")
            })
            displayName(updateAttributes.hasAttribute("displayName") ? updateAttributes.findString("displayName") : "")
        }

        return connection.request(PUT, JSON) { req ->
            uri.path = "/api/users/${uid.uidValue}"
            body = builder.toString()
            headers.'If-Match' =  "*"

            response.success = { resp, json ->
                new Uid(json._id, json._rev)
            }
        }
    case ObjectClass.GROUP:
        if (updateAttributes.hasAttribute("members")) {
            def builder = new JsonBuilder()
            builder {
                members(updateAttributes.findList("members"))
            }
            return connection.request(PUT, JSON) { req ->
                uri.path = "/api/groups/${uid.uidValue}"
                body = builder.toString()
                headers.'If-Match' =  "*"

                response.success = { resp, json ->
                    new Uid(json._id, json._rev)
                }
            }           
        }
}
return uid
