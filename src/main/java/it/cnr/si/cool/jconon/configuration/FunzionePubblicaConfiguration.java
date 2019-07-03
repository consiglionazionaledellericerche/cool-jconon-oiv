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

package it.cnr.si.cool.jconon.configuration;

import it.cnr.cool.service.I18nServiceLocation;
import it.cnr.cool.service.PageService;
import it.cnr.si.cool.jconon.rest.ApplicationOIV;
import it.cnr.si.cool.jconon.rest.CallOIV;
import it.cnr.si.cool.jconon.rest.IPA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Created by francesco on 14/11/16.
 */

@Configuration
public class FunzionePubblicaConfiguration {

    public static final String I18N_COOL_JCONON_FP = "i18n.cool-jconon-fp";
    private static final Logger LOGGER = LoggerFactory.getLogger(FunzionePubblicaConfiguration.class);
    private PageService pageService;

    private String LANGUAGE = Locale.ITALIAN.getLanguage();

    public FunzionePubblicaConfiguration(PageService pageService, JerseyConfig jerseyConfig) {
        this.pageService = pageService;
        jerseyConfig.register(ApplicationOIV.class);
        jerseyConfig.register(CallOIV.class);
        jerseyConfig.register(IPA.class);
    }

    @PostConstruct
    public void overrideLang() {
        LOGGER.warn("overriding language: {}", LANGUAGE);
        pageService.setOverrideLang(LANGUAGE);
    }


    @Bean(name = "jconon-fpI18nServiceLocation")
    public I18nServiceLocation fpI18nServiceLocation() {
        LOGGER.info("loading i18n {}", I18N_COOL_JCONON_FP);
        I18nServiceLocation fpI18nServiceLocation = new I18nServiceLocation();
        List<String> locations = Arrays.asList(I18N_COOL_JCONON_FP);
        fpI18nServiceLocation.setLocations(locations);
        return fpI18nServiceLocation;
    }


}
