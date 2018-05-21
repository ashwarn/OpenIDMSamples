/*
 * Copyright 2014-2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */

import org.forgerock.json.resource.Connection
import org.forgerock.json.resource.NotFoundException
import org.forgerock.json.resource.PatchRequest
import org.forgerock.json.resource.ReadRequest
import org.forgerock.json.resource.Requests
import org.forgerock.json.resource.ResourceResponse
import org.forgerock.services.context.RootContext
import org.forgerock.json.resource.UpdateRequest
import org.forgerock.openicf.connectors.scriptedcrest.ScriptedCRESTConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.exceptions.ConnectorException
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException
import org.identityconnectors.framework.common.exceptions.UnknownUidException
import org.identityconnectors.framework.common.objects.Attribute
import org.identityconnectors.framework.common.objects.ObjectClass
import org.identityconnectors.framework.common.objects.OperationOptions
import org.identityconnectors.framework.common.objects.Schema
import org.identityconnectors.framework.common.objects.Uid

def operation = operation as OperationType
def updateAttributes = attributes as Set<Attribute>
def configuration = configuration as ScriptedCRESTConfiguration
def connection = connection as Connection
def id = id as String
def log = log as Log
def objectClass = objectClass as ObjectClass
def options = options as OperationOptions
def uid = uid as Uid
def schema = schema as Schema


switch (operation) {
    case OperationType.UPDATE:
        Map<String, Object> objectClassInfo = configuration.propertyBag[objectClass.objectClassValue];
        if (objectClassInfo != null) {

            if (true) {
                ReadRequest request = Requests.newReadRequest(objectClassInfo.resourceContainer, uid.uidValue)
                request.setResourcePath("/api/" + request.getResourcePath())
                request.addField("/")
                try {
                    ResourceResponse resource = connection.read(new RootContext(), request)

                    updateAttributes.each {
                        def info = objectClassInfo.attributes[it.name]
                        if (null != info && info.attributeInfo.isUpdateable()) {
                            if (it.value == null) {
                                if (null != resource.content.get(info.jsonName)) {
                                    resource.content.addPermissive(info.jsonName, null)
                                }
                            } else if (info.attributeInfo.isMultiValued()) {
                                resource.content.remove(info.jsonName)
                                if (it.value.isEmpty()) {
                                    resource.content.addPermissive(info.jsonName, it.value)
                                } else {
                                    resource.content.addPermissive(info.jsonName, it.value.each { a ->
                                        CRESTHelper.fromAttributeToJSON(info.attributeInfo, a)
                                    })
                                }
                            } else if (it.value.size() > 1) {
                                final StringBuilder msg =
                                        new StringBuilder("The ").append(it.name).append(
                                                " attribute is not single value attribute.");
                                throw new InvalidAttributeValueException(msg.toString());
                            } else {
                                if (it.value.isEmpty()) {
                                    if (null != resource.content.get(info.jsonName)) {
                                        resource.content.addPermissive(info.jsonName, null)
                                    }
                                } else {
                                    resource.content.remove(info.jsonName)
                                    resource.content.addPermissive(info.jsonName, CRESTHelper.fromAttributeToJSON(info.attributeInfo, it.value.get(0)))
                                }
                            }
                        }
                    }

                    resource.content.remove("_rev")
                    resource.content.remove("_meta")
                    Set<String> keys = new HashSet<String>();
                    keys.addAll(resource.content.keys())
                    keys.each {
                        def info = objectClassInfo.attributes[it]
                        if (null != info && !info.attributeInfo.isUpdateable()) {
                            resource.content.remove(it)
                        }
                    }

                    UpdateRequest updateRequest = Requests.newUpdateRequest(request.resourcePath, resource.content)
                    def r = connection.update(new RootContext(), updateRequest)
                    return new Uid(r.getId(), r.getRevision())
                } catch (NotFoundException e) {
                    throw new UnknownUidException(uid, objectClass).initCause(e)
                }
            } else {
                PatchRequest request = Requests.newPatchRequest(objectClassInfo.resourceContainer, uid.uidValue)

                ResourceResponse resource = connection.patch(new RootContext(), request)
                return new Uid(resource.getId(), resource.getRevision())
            }
        } else {
            throw new UnsupportedOperationException(operation.name() + " operation of type:" +
                    objectClass.objectClassValue + " is not supported.")
        }
        break
    case OperationType.ADD_ATTRIBUTE_VALUES:
        throw new UnsupportedOperationException(operation.name() + " operation of type:" +
                objectClass.objectClassValue + " is not supported.")
    case OperationType.REMOVE_ATTRIBUTE_VALUES:
        throw new UnsupportedOperationException(operation.name() + " operation of type:" +
                objectClass.objectClassValue + " is not supported.")
    default:
        throw new ConnectorException("UpdateScript can not handle operation:" + operation.name())
}
