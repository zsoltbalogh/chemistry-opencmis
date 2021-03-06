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
package org.apache.chemistry.opencmis.client.bindings.spi.browser;

import java.io.OutputStream;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.spi.AclService;

/**
 * ACL Service Browser Binding client.
 */
public class AclServiceImpl extends AbstractBrowserBindingService implements AclService {

    /**
     * Constructor.
     */
    public AclServiceImpl(BindingSession session) {
        setSession(session);
    }

    public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
        // build URL
        UrlBuilder url = getObjectUrl(repositoryId, objectId, Constants.SELECTOR_ACL);
        url.addParameter(Constants.PARAM_ONLY_BASIC_PERMISSIONS, onlyBasicPermissions);

        // read and parse
        HttpUtils.Response resp = read(url);
        Map<String, Object> json = parseObject(resp.getStream(), resp.getCharset());

        return JSONConverter.convertAcl(json, null);
    }

    public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces,
            AclPropagation aclPropagation, ExtensionsData extension) {
        // build URL
        UrlBuilder url = getObjectUrl(repositoryId, objectId);

        // prepare form data
        final FormDataWriter formData = new FormDataWriter(Constants.CMISACTION_APPLY_ACL);
        formData.addAddAcesParameters(addAces);
        formData.addRemoveAcesParameters(removeAces);
        formData.addParameter(Constants.PARAM_ACL_PROPAGATION, aclPropagation);

        // send and parse
        HttpUtils.Response resp = post(url, formData.getContentType(), new HttpUtils.Output() {
            public void write(OutputStream out) throws Exception {
                formData.write(out);
            }
        });
        Map<String, Object> json = parseObject(resp.getStream(), resp.getCharset());

        return JSONConverter.convertAcl(json, null);
    }
}
