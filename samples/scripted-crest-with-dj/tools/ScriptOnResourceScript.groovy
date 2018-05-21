/*
 * Copyright 2014-2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */


import org.forgerock.json.JsonValue
import org.forgerock.json.resource.ActionRequest
import org.forgerock.json.resource.Connection
import org.forgerock.json.resource.Requests
import org.forgerock.services.context.RootContext
import org.forgerock.openicf.connectors.scriptedcrest.ScriptedCRESTConfiguration
import org.forgerock.openicf.misc.scriptedcommon.OperationType
import org.identityconnectors.common.logging.Log
import org.identityconnectors.framework.common.objects.OperationOptions

def operation = operation as OperationType
def configuration = configuration as ScriptedCRESTConfiguration
def connection = connection as Connection
def log = log as Log
def options = options as OperationOptions
def scriptArguments = scriptArguments as Map
def scriptLanguage = scriptLanguage as String
def scriptText = scriptText as String

if ("crest".equalsIgnoreCase(scriptLanguage)) {
    ActionRequest request = Requests.newActionRequest("users", scriptText)
    request.setContent(new JsonValue(scriptArguments))
    def response = connection.action(new RootContext(), request)
    return response.getObject()
} else {

    ActionRequest request = Requests.newActionRequest("system/ldap/account", "script")
    request.setAdditionalParameter("_scriptId", "")
    if ("resource".equalsIgnoreCase(scriptLanguage)) {
        request.setAdditionalParameter("_scriptExecuteMode", "resource")
    }
    if (null != options.options.variablePrefix) {
        request.setAdditionalParameter("_scriptVariablePrefix", options.options.variablePrefix)
    }


    scriptArguments.each { String key, Object value ->
        if (!key.startsWith('_')) {
            if (value instanceof String) {
                request.setAdditionalParameter(key, value as String)
            } else {
                log.warn("Argument parameter ${key} is ignored because its type ${value?.class} is not supported.")
            }
        }
    }

    def result = connection.action(new RootContext(), request)
    def returnValue = []


    result.actions.each { key, value ->
        if ("error".equals(key)) {
            throw new ConnectException(value as String)
        } else {
            returnValue.add(value)
        }
    }

    return returnValue as Object[]
}
