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

package it.cnr.si.cool.jconon.service;

import it.cnr.cool.security.service.UserService;
import it.cnr.cool.security.service.impl.alfresco.CMISUser;
import it.cnr.cool.security.service.impl.alfresco.UserServiceImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@Primary
public class UserOIVService extends UserServiceImpl implements UserService {

    private static final String MANAGE_APPLICATION = "/manage-application";
    private static final String HOME = "/home";
    private static final String AVVISI_PUBBLICI_DI_SELEZIONE_COMPARATIVA = "/avvisi-pubblici-di-selezione-comparativa";
    private static final String MY_APPLICATIONS = "/my-applications";
    private static final List<String> GROUPS_GESTORI_PROCEDURE_COMPARATIVE = Arrays.asList("GROUP_GESTORI_PROCEDURE_COMPARATIVE", "GROUP_procedureComparativeGroup");
    private List<URI> mappedURIs = Arrays.asList(URI.create(HOME), URI.create(MY_APPLICATIONS), URI.create(MANAGE_APPLICATION));

    @Override
    public URI getRedirect(CMISUser cmisUser, URI uri) {
        if (Optional.ofNullable(uri).isPresent() && mappedURIs.stream().anyMatch(x -> uri.getPath().startsWith(x.getPath()))) {
            if (Optional.ofNullable(cmisUser)
                    .map(user -> user.getGroupsArray())
                    .orElse(Collections.emptyList())
                    .stream()
                    .anyMatch(group -> GROUPS_GESTORI_PROCEDURE_COMPARATIVE.contains(group))) {
                return URI.create(AVVISI_PUBBLICI_DI_SELEZIONE_COMPARATIVA);
            }
        }
        return super.getRedirect(cmisUser, uri);
    }
}
