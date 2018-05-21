/*
 * Copyright 2015-2017 ForgeRock AS. All Rights Reserved
 *
 * Use of this code requires a commercial software license with ForgeRock AS.
 * or with one of its affiliates. All use shall be exclusively subject
 * to such license between the licensee and ForgeRock AS.
 */

var pw,
    moContext = context.managedObject;

if (typeof moContext !== 'undefined' && moContext !== null) {
    pw = moContext.fields.ldapPassword;
    if (typeof pw !== 'undefined' && pw !== null) {
        target.userPassword = pw;
    }
}

target.dn = 'uid=' + source.userName + ',ou=People,dc=example,dc=com';
