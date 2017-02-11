package it.cnr.si.cool.jcononrepository;

import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import it.cnr.cool.cmis.service.CMISService;
import it.cnr.si.cool.jconon.cmis.model.JCONONDocumentType;
import it.cnr.si.cool.jconon.repository.CacheRepository;
import it.cnr.si.cool.jconon.repository.dto.ObjectTypeCache;
@Repository
@Primary
public class CacheFPRepository extends CacheRepository {
	private static final String JCONON_ATTACHMENT_CALL_FP_ABSTRACT = "D:jconon_attachment:call_fp_abstract";

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheFPRepository.class);

	@Autowired
	private CMISService cmisService;
	
	@Cacheable(JSONLIST_CALL_ATTACHMENTS)
	public List<ObjectTypeCache> getCallAttachments() {
		try {
			List<ObjectTypeCache> list = new ArrayList<ObjectTypeCache>();
			populate(list, cmisService.createAdminSession().
					getTypeChildren(JCONON_ATTACHMENT_CALL_FP_ABSTRACT, false), null, false);			
			return list;		
		} catch(CmisObjectNotFoundException _ex) {
			LOGGER.warn("Cannot find Model in repository parentTypes: {}",
					JCONON_ATTACHMENT_CALL_FP_ABSTRACT, _ex);
			return null;
		}		
	}
}
