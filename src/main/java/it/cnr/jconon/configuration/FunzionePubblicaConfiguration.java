package it.cnr.jconon.configuration;

import it.cnr.cool.service.I18nServiceLocation;
import it.cnr.cool.service.PageService;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(FunzionePubblicaConfiguration.class);

    public static final String I18N_COOL_JCONON_FP = "i18n.cool-jconon-fp";

    private PageService pageService;

    public FunzionePubblicaConfiguration (PageService pageService) {
        this.pageService = pageService;
    }

    @PostConstruct
    public void overrideLang() {
        pageService.setOverrideLang(Locale.ITALIAN.getLanguage());
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
