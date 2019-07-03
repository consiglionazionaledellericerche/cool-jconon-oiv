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
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@Primary
public class CacheFPRepository extends CacheRepository {
    private static final String JCONON_ATTACHMENT_CALL_FP_ABSTRACT = "D:jconon_attachment:call_fp_abstract";
    private static final String FLOWS_ENABLE = "flows.enable";

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheFPRepository.class);

    @Autowired
    private CMISService cmisService;
    @Autowired
    private CacheFPEsitoRepository cacheFPEsitoRepository;

    @Value("${flows.enable}")
    private Boolean flowsEnable;

    @Cacheable(JSONLIST_CALL_ATTACHMENTS)
    public List<ObjectTypeCache> getCallAttachments() {
        try {
            List<ObjectTypeCache> list = new ArrayList<ObjectTypeCache>();
            populate(list, cmisService.createAdminSession().
                    getTypeChildren(JCONON_ATTACHMENT_CALL_FP_ABSTRACT, false), null, false);
            return list;
        } catch (CmisObjectNotFoundException _ex) {
            LOGGER.warn("Cannot find Model in repository parentTypes: {}",
                    JCONON_ATTACHMENT_CALL_FP_ABSTRACT, _ex);
            return null;
        }
    }

    @Override
    public Map<? extends String, ? extends Object> getExtraModel() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put(CacheFPEsitoRepository.JSONLIST_CALL_ESITO_ATTACHMENTS, cacheFPEsitoRepository.getCallEsitoAttachments());
        result.put(CacheFPEsitoRepository.JSONLIST_CALL_ESITO_PARTECIPANTI_ATTACHMENTS, cacheFPEsitoRepository.getCallEsitoPartecipantiAttachments());
        result.put(FLOWS_ENABLE, flowsEnable);
        return result;
    }


}