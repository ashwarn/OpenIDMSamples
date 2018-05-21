/*
 * Copyright 2014-2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */


import org.forgerock.json.JsonValue
import org.forgerock.json.resource.Connection
import org.forgerock.json.resource.CreateRequest
import org.forgerock.json.resource.Requests
import org.forgerock.json.resource.ResourceResponse
import org.forgerock.services.context.RootContext
import org.forgerock.openicf.connectors.scriptedcrest.ScriptedCRESTConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.objects.Attribute
import org.identityconnectors.framework.common.objects.AttributeUtil
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.OperationOptions
import org.identityconnectors.framework.common.objects.Schema
import org.identityconnectors.framework.common.objects.Uid

def operation = operation as OperationType
def attributes = attributes as Set<Attribute>
def attributeMap = AttributeUtil.toMap(attributes);
def configuration = configuration as ScriptedCRESTConfiguration
def connection = connection as Connection
def id = id as String
def log = log as Log
def objectClass = objectClass as ObjectClass
def options = options as OperationOptions
def schema = schema as Schema


Map<String, Object> objectClassInfo = configuration.propertyBag[objectClass.objectClassValue];
if (objectClassInfo != null) {
    def user = CRESTHelper.toJsonValue(id, attributeMap, objectClassInfo);

    CreateRequest request = Requests.newCreateRequest(objectClassInfo.resourceContainer, new JsonValue(user))
    request.setResourcePath("/api/" + request.getResourcePath())
    request.addField("_id", "_rev")
    ResourceResponse resource = connection.create(new RootContext(), request)
    return new Uid(resource.getId(), resource.getRevision())
} else {
    throw new UnsupportedOperationException(operation.name() + " operation of type:" +
            objectClass.objectClassValue + " is not supported.")
}
