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
package org.apache.chemistry.opencmis.inmemory.server;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InMemoryService extends AbstractCmisService {

    private static final Log LOG = LogFactory.getLog(InMemoryService.class.getName());

    private StoreManager storeManager; // singleton root of everything
    private CallContext context;

    private InMemoryRepositoryServiceImpl fRepSvc;
    private InMemoryObjectServiceImpl fObjSvc;
    private InMemoryNavigationServiceImpl fNavSvc;
    private InMemoryVersioningServiceImpl fVerSvc;
    private InMemoryDiscoveryServiceImpl fDisSvc;
    private InMemoryMultiFilingServiceImpl fMultiSvc;

    public StoreManager getStoreManager() {
        return storeManager;
    }

    public InMemoryService(Map<String, String> parameters, StoreManager sm) {

        storeManager = sm;
        fRepSvc = new InMemoryRepositoryServiceImpl(storeManager);
        fNavSvc = new InMemoryNavigationServiceImpl(storeManager);
        fObjSvc = new InMemoryObjectServiceImpl(storeManager);
        fVerSvc = new InMemoryVersioningServiceImpl(storeManager, fObjSvc);
        fDisSvc = new InMemoryDiscoveryServiceImpl(storeManager, fRepSvc, fNavSvc);
        fMultiSvc = new InMemoryMultiFilingServiceImpl(storeManager);
    }

    public CallContext getCallContext() {
        return context;
    }

    public void setCallContext(CallContext context) {
        this.context = context;
    }

    // --- repository service ---

    @Override
    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        return fRepSvc.getRepositoryInfos(getCallContext(), extension);
    }

    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
        return fRepSvc.getRepositoryInfo(getCallContext(), repositoryId, extension);
    }

    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return fRepSvc.getTypeChildren(getCallContext(), repositoryId, typeId, includePropertyDefinitions, maxItems,
                skipCount, extension);
    }

    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        return fRepSvc.getTypeDefinition(getCallContext(), repositoryId, typeId, extension);
    }

    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        return fRepSvc.getTypeDescendants(getCallContext(), repositoryId, typeId, depth, includePropertyDefinitions,
                extension);
    }

    // --- navigation service ---

    public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return fNavSvc.getCheckedOutDocs(getCallContext(), repositoryId, folderId, filter, orderBy,
                includeAllowableActions, includeRelationships, renditionFilter, maxItems, skipCount, extension, this);
    }

    public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return fNavSvc.getChildren(getCallContext(), repositoryId, folderId, filter, orderBy, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegment, maxItems, skipCount, extension, this);
    }

    public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        return fNavSvc.getDescendants(getCallContext(), repositoryId, folderId, depth, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegment, extension, this);
    }

    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
        return fNavSvc.getFolderParent(getCallContext(), repositoryId, folderId, filter, extension, this);
    }

    public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        return fNavSvc.getFolderTree(getCallContext(), repositoryId, folderId, depth, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegment, extension, this);
    }

    public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, ExtensionsData extension) {
        return fNavSvc.getObjectParents(getCallContext(), repositoryId, objectId, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includeRelativePathSegment, extension, this);
    }

    // --- object service ---

    public String create(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
            VersioningState versioningState, List<String> policies, ExtensionsData extension) {
        String id = fObjSvc.create(getCallContext(), repositoryId, properties, folderId, contentStream,
                versioningState, policies, extension, this);
        return id;

    }

    public String createDocument(String repositoryId, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        return fObjSvc.createDocument(getCallContext(), repositoryId, properties, folderId, contentStream,
                versioningState, policies, addAces, removeAces, extension);
    }

    public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
            String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        return fObjSvc.createDocumentFromSource(getCallContext(), repositoryId, sourceId, properties, folderId,
                versioningState, policies, addAces, removeAces, extension);
    }

    public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        return fObjSvc.createFolder(getCallContext(), repositoryId, properties, folderId, policies, addAces,
                removeAces, extension);
    }

    public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        return fObjSvc.createPolicy(getCallContext(), repositoryId, properties, folderId, policies, addAces,
                removeAces, extension);
    }

    public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        return fObjSvc.createRelationship(getCallContext(), repositoryId, properties, policies, addAces, removeAces,
                extension);
    }

    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ExtensionsData extension) {
        fObjSvc.deleteContentStream(getCallContext(), repositoryId, objectId, changeToken, extension);
    }

    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {
        fObjSvc.deleteObjectOrCancelCheckOut(getCallContext(), repositoryId, objectId, allVersions, extension);
    }

    public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
            ExtensionsData extension) {
        fObjSvc.deleteObjectOrCancelCheckOut(getCallContext(), repositoryId, objectId, allVersions, extension);
    }

    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
        return fObjSvc.deleteTree(getCallContext(), repositoryId, folderId, allVersions, unfileObjects,
                continueOnFailure, extension);
    }

    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
        return fObjSvc.getAllowableActions(getCallContext(), repositoryId, objectId, extension);
    }

    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
            BigInteger length, ExtensionsData extension) {
        return fObjSvc.getContentStream(getCallContext(), repositoryId, objectId, streamId, offset, length, extension);
    }

    public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        return fObjSvc.getObject(getCallContext(), repositoryId, objectId, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension, this);
    }

    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        return fObjSvc.getObjectByPath(getCallContext(), repositoryId, path, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePolicyIds, includeAcl, extension, this);
    }

    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
        return fObjSvc.getProperties(getCallContext(), repositoryId, objectId, filter, extension);
    }

    public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return fObjSvc.getRenditions(getCallContext(), repositoryId, objectId, renditionFilter, maxItems, skipCount,
                extension);
    }

    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension) {
        fObjSvc.moveObject(getCallContext(), repositoryId, objectId, targetFolderId, sourceFolderId, extension, this);
    }

    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
        fObjSvc.setContentStream(getCallContext(), repositoryId, objectId, overwriteFlag, changeToken, contentStream,
                extension);
    }

    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            Properties properties, ExtensionsData extension) {
        fObjSvc.updateProperties(getCallContext(), repositoryId, objectId, changeToken, properties, null, extension,
                this);
    }

    // --- versioning service ---

    public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
        fVerSvc.cancelCheckOut(getCallContext(), repositoryId, objectId, extension);
    }

    public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties,
            ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        fVerSvc.checkIn(getCallContext(), repositoryId, objectId, major, properties, contentStream, checkinComment,
                policies, addAces, removeAces, extension, this);
    }

    public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
            Holder<Boolean> contentCopied) {
        fVerSvc.checkOut(getCallContext(), repositoryId, objectId, extension, contentCopied, this);
    }

    public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
        return fVerSvc.getObjectOfLatestVersion(getCallContext(), repositoryId, versionSeriesId, major, filter,
                includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl,
                extension, this);
    }

    public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, ExtensionsData extension) {
        return fVerSvc.getPropertiesOfLatestVersion(getCallContext(), repositoryId, versionSeriesId, major, filter,
                extension);
    }

    public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, ExtensionsData extension) {
        return fVerSvc.getAllVersions(getCallContext(), repositoryId, versionSeriesId==null ? objectId : versionSeriesId, 
                filter, includeAllowableActions, extension, this);
    }

    // --- discovery service ---

    public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties,
            String filter, Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension) {
        return fDisSvc.getContentChanges(getCallContext(), repositoryId, changeLogToken, includeProperties, filter, includePolicyIds,
                includeAcl, maxItems, extension, this);
    }

    public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return fDisSvc.query(getCallContext(), repositoryId, statement, searchAllVersions, includeAllowableActions,
                includeRelationships, renditionFilter, maxItems, skipCount, extension);
    }

    // --- multi filing service ---

    public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions,
            ExtensionsData extension) {
        fMultiSvc.addObjectToFolder(getCallContext(), repositoryId, objectId, folderId, allVersions, extension, this);
    }

    public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, ExtensionsData extension) {
        fMultiSvc.removeObjectFromFolder(getCallContext(), repositoryId, objectId, folderId, extension, this);
    }

    // --- relationship service ---

    public ObjectList getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes,
            RelationshipDirection relationshipDirection, String typeId, String filter, Boolean includeAllowableActions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return super.getObjectRelationships(repositoryId, objectId, includeSubRelationshipTypes, relationshipDirection,
                typeId, filter, includeAllowableActions, maxItems, skipCount, extension);
    }

    // --- ACL service ---

    public Acl applyAcl(String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {
        return super.applyAcl(repositoryId, objectId, aces, aclPropagation);
    }

    public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces,
            AclPropagation aclPropagation, ExtensionsData extension) {
        return super.applyAcl(repositoryId, objectId, addAces, removeAces, aclPropagation, extension);
    }

    public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
        return super.getAcl(repositoryId, objectId, onlyBasicPermissions, extension);
    }

    // --- policy service ---

    public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        super.applyPolicy(repositoryId, policyId, objectId, extension);
    }

    public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter,
            ExtensionsData extension) {
        return super.getAppliedPolicies(repositoryId, objectId, filter, extension);
    }

    public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        super.removePolicy(repositoryId, policyId, objectId, extension);
    }

    // //////////////
    //	

}
