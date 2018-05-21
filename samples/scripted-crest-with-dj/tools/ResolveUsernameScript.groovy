/*
 * Copyright 2014-2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */

import org.forgerock.json.resource.Connection
import org.forgerock.util.query.QueryFilter
import org.forgerock.json.resource.QueryRequest
import org.forgerock.json.resource.Requests
import org.forgerock.json.resource.ResourceResponse
import org.forgerock.services.context.RootContext
import org.forgerock.openicf.connectors.scriptedcrest.ScriptedCRESTConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.exceptions.UnknownUidException
import org.identityconnectors.framework.common.objects.Name
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.OperationOptions
import org.identityconnectors.framework.common.objects.Uid

def operation = operation as OperationType
def configuration = configuration as ScriptedCRESTConfiguration
def connection = connection as Connection
def username = username as String
def log = log as Log
def objectClass = objectClass as ObjectClass
def options = options as OperationOptions


def objectClassInfo = configuration.propertyBag[objectClass.objectClassValue];
if (objectClassInfo != null) {

    QueryRequest request = Requests.newQueryRequest(objectClassInfo.resourceContainer)
    request.setResourcePath("/api/" + request.getResourcePath())

    def attributeDefinition = objectClassInfo.attributes[name]
    if (null != attributeDefinition) {
        request.queryFilter = QueryFilter.equalTo(String.valueOf(attributeDefinition.jsonName), username)
    } else {
        request.queryFilter = QueryFilter.equalTo(Name.NAME, username)
    }
    request.addField("_id", "_rev")


    def results = []
    def queryResult = connection.query(new RootContext(), request, results)

    if (results.empty) {
        throw new UnknownUidException()
    } else if (results.size() > 1) {
        throw new ConnectException("Multiple results 'userName' is not unique!")
    } else {
        ResourceResponse r = results.get(0) as ResourceResponse;
        return new Uid(r.id, r.revision)
    }

} else {
    throw new UnsupportedOperationException(operation.name() + " operation of type:" +
            objectClass.objectClassValue + " is not supported.")
}
