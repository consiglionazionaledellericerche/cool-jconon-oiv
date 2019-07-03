/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.si.cool.jconon.repository;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.si.cool.jconon.repository.dto.ObjectTypeCache;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CacheFPEsitoRepository extends CacheRepository {
    public static final String JSONLIST_CALL_ESITO_ATTACHMENTS = "jsonlistCallEsitoAttachments";
    public static final String JSONLIST_CALL_ESITO_PARTECIPANTI_ATTACHMENTS = "jsonlistCallEsitoPartecipantiAttachments";
    private static final String JCONON_ATTACHMENT_CALL_FP_ESITO_ABSTRACT = "D:jconon_attachment:call_fp_esito_abstract";
    private static final String JCONON_ATTACHMENT_CALL_FP_ESITO_PARTECIPANTI = "D:jconon_attachment:call_fp_esito_partecipanti";
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheFPEsitoRepository.class);

    @Autowired
    private CMISService cmisService;

    @Cacheable(JSONLIST_CALL_ESITO_ATTACHMENTS)
    public List<ObjectTypeCache> getCallEsitoAttachments() {
        try {
            List<ObjectTypeCache> list = new ArrayList<ObjectTypeCache>();
            populate(list, cmisService.createAdminSession().
                    getTypeChildren(JCONON_ATTACHMENT_CALL_FP_ESITO_ABSTRACT, false), null, false);
            return list;
        } catch (CmisObjectNotFoundException _ex) {
            LOGGER.warn("Cannot find Model in repository parentTypes: {}",
                    JCONON_ATTACHMENT_CALL_FP_ESITO_ABSTRACT, _ex);
            return null;
        }
    }

    @Cacheable(JSONLIST_CALL_ESITO_PARTECIPANTI_ATTACHMENTS)
    public List<ObjectTypeCache> getCallEsitoPartecipantiAttachments() {
        try {
            Session session = cmisService.createAdminSession();
            List<ObjectTypeCache> list = new ArrayList<ObjectTypeCache>();
            ObjectType objectType = session.getTypeDefinition(JCONON_ATTACHMENT_CALL_FP_ESITO_PARTECIPANTI);
            ObjectTypeCache objectTypeCache = new ObjectTypeCache().
                    key(objectType.getId()).
                    label(objectType.getId()).
                    description(objectType.getDescription()).
                    defaultLabel(objectType.getDisplayName());
            list.add(objectTypeCache);
            populate(list, session.
                    getTypeChildren(JCONON_ATTACHMENT_CALL_FP_ESITO_PARTECIPANTI, false), null, false);
            return list;
        } catch (CmisObjectNotFoundException _ex) {
            LOGGER.warn("Cannot find Model in repository parentTypes: {}",
                    JCONON_ATTACHMENT_CALL_FP_ESITO_PARTECIPANTI, _ex);
            return null;
        }
    }
}