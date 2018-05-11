package it.cnr.si.cool.jconon.service;

import it.cnr.si.cool.jconon.model.IPAAmministrazione;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.management.timer.Timer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class IPAService {

    public static final String AMMINISTRAZIONI_IPA = "amministrazioni-ipa";

    @Value("${ipa.url.amministrazioni}")
    private String iapURLAmministrazioni;

    @Cacheable(value = AMMINISTRAZIONI_IPA)
    public Map<String, IPAAmministrazione> amministrazioni() throws IOException {
        InputStream is = new URL(iapURLAmministrazioni)
                .openConnection().getInputStream();
        Predicate<String> filterFirstLine =
                line -> !(
                        "cod_amm".equals(line.split("\t", -1)[0])
                                && "des_amm".equals(line.split("\t", -1)[1])
                );
        return new BufferedReader(new InputStreamReader(is))
                .lines()
                .filter(filterFirstLine)
                .map(s -> s.split("\t", -1))
                .map(strings ->
                        new IPAAmministrazione()
                                .setCod_amm(strings[0])
                                .setDes_amm(strings[1])
                                .setComune(strings[2])
                                .setNome_resp(strings[3])
                                .setCognome_resp(strings[4])
                                .setCap(strings[5])
                                .setProvincia(strings[6])
                                .setSito_istituzionale(strings[8])
                                .setIndirizzo(strings[9])
                                .setTipologia_amm(strings[12])
                                .setAcronimo(strings[13])
                                .setMail1(strings[16])
                )
                .sorted((ipaAmministrazione, t1) -> ipaAmministrazione.getDes_amm().compareTo(t1.getDes_amm()))
                .collect(Collectors.toMap(amministrazione -> amministrazione.getCod_amm(), amministrazione -> amministrazione));
    }

    @Scheduled(fixedRate = Timer.ONE_WEEK)
    @CacheEvict(value = AMMINISTRAZIONI_IPA)
    public void clearCache() {
    }
}
