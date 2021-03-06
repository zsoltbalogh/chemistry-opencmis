/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.chemistry.opencmis.client.bindings.spi.atompub;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomAcl;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.spi.AclService;

/**
 * ACL Service AtomPub client.
 */
public class AclServiceImpl extends AbstractAtomPubService implements AclService {

    /**
     * Constructor.
     */
    public AclServiceImpl(BindingSession session) {
        setSession(session);
    }

    public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces,
            AclPropagation aclPropagation, ExtensionsData extension) {

        // fetch the current ACL
        Acl originalAces = getAcl(repositoryId, objectId, false, null);

        // if no changes required, just return the ACL
        if (!isAclMergeRequired(addAces, removeAces)) {
            return originalAces;
        }

        // merge ACLs
        Acl newACL = mergeAcls(originalAces, addAces, removeAces);

        // update ACL
        AtomAcl acl = updateAcl(repositoryId, objectId, newACL, aclPropagation);
        Acl result = acl.getACL();

        return result;
    }

    public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {

        // find the link
        String link = loadLink(repositoryId, objectId, Constants.REL_ACL, Constants.MEDIATYPE_ACL);

        if (link == null) {
            throwLinkException(repositoryId, objectId, Constants.REL_ACL, Constants.MEDIATYPE_ACL);
        }

        UrlBuilder url = new UrlBuilder(link);
        url.addParameter(Constants.PARAM_ONLY_BASIC_PERMISSIONS, onlyBasicPermissions);

        // read and parse
        HttpUtils.Response resp = read(url);
        AtomAcl acl = parse(resp.getStream(), AtomAcl.class);

        return acl.getACL();
    }

}
