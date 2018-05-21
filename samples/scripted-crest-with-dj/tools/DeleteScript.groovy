/*
 * Copyright 2014-2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */

import org.forgerock.json.resource.Connection
import org.forgerock.json.resource.DeleteRequest
import org.forgerock.json.resource.Requests
import org.forgerock.services.context.RootContext
import org.forgerock.openicf.connectors.scriptedcrest.ScriptedCRESTConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.OperationOptions
import org.identityconnectors.framework.common.objects.Uid

def operation = operation as OperationType
def configuration = configuration as ScriptedCRESTConfiguration
def connection = connection as Connection
def log = log as Log
def objectClass = objectClass as ObjectClass
def options = options as OperationOptions
def uid = uid as Uid

def objectClassInfo = configuration.propertyBag[objectClass.objectClassValue];
if (objectClassInfo != null) {
    DeleteRequest request = Requests.newDeleteRequest(objectClassInfo.resourceContainer, uid.uidValue)
    request.setResourcePath("/api/" + request.getResourcePath())
    request.setRevision(uid.revision)
    connection.delete(new RootContext(), request)
} else {
    throw new UnsupportedOperationException(operation.name() + " operation of type:" +
            objectClass.objectClassValue + " is not supported.")
}
