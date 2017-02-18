package it.cnr.si.cool.jconon.service;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.security.service.impl.alfresco.UserServiceImpl;

@Component
@Primary
public class UserOIVService extends UserServiceImpl implements UserService {
	
	private static final String MANAGE_APPLICATION = "/manage-application";
	private static final String HOME = "/home";
	private static final String AVVISI_PUBBLICI_DI_SELEZIONE_COMPARATIVA = "/avvisi-pubblici-di-selezione-comparativa";
	private static final String AMMINISTRAZIONE = "amministrazione";
	private static final String MY_APPLICATIONS = "/my-applications";
	private List<URI> mappedURIs = Arrays.asList(URI.create(HOME), URI.create(MY_APPLICATIONS), URI.create(MANAGE_APPLICATION)); 
	
	@Override
	public URI getRedirect(CMISUser cmisUser, URI uri) {		
		if (Optional.ofNullable(uri).isPresent() && mappedURIs.stream().anyMatch(x -> uri.getPath().startsWith(x.getPath()))) {
			return Optional.ofNullable(cmisUser)
				.map(user ->  Optional.ofNullable(user.getOther().get(AMMINISTRAZIONE)).map(x -> {
								return URI.create(AVVISI_PUBBLICI_DI_SELEZIONE_COMPARATIVA);
							}).orElse(URI.create(MY_APPLICATIONS)))
				.orElse(super.getRedirect(cmisUser, uri));			
		}
		return super.getRedirect(cmisUser, uri);
	}
}
