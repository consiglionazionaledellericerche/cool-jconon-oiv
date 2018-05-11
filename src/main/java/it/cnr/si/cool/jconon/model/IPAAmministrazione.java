package it.cnr.si.cool.jconon.model;

import java.io.Serializable;
import java.util.Objects;

public class IPAAmministrazione implements Serializable {
    private String cod_amm;
    private String des_amm;
    private String comune;
    private String cap;
    private String provincia;
    private String nome_resp;
    private String cognome_resp;
    private String sito_istituzionale;
    private String indirizzo;
    private String tipologia_amm;
    private String acronimo;
    private String mail1;

    public IPAAmministrazione() {
    }

    public String getCod_amm() {
        return cod_amm;
    }

    public IPAAmministrazione setCod_amm(String cod_amm) {
        this.cod_amm = cod_amm;
        return this;
    }

    public String getDes_amm() {
        return des_amm;
    }

    public IPAAmministrazione setDes_amm(String des_amm) {
        this.des_amm = des_amm;
        return this;
    }

    public String getNome_resp() {
        return nome_resp;
    }

    public IPAAmministrazione setNome_resp(String nome_resp) {
        this.nome_resp = nome_resp;
        return this;
    }

    public String getSito_istituzionale() {
        return sito_istituzionale;
    }

    public IPAAmministrazione setSito_istituzionale(String sito_istituzionale) {
        this.sito_istituzionale = sito_istituzionale;
        return this;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public IPAAmministrazione setIndirizzo(String indirizzo) {
        this.indirizzo = indirizzo;
        return this;
    }

    public String getTipologia_amm() {
        return tipologia_amm;
    }

    public IPAAmministrazione setTipologia_amm(String tipologia_amm) {
        this.tipologia_amm = tipologia_amm;
        return this;
    }

    public String getAcronimo() {
        return acronimo;
    }

    public IPAAmministrazione setAcronimo(String acronimo) {
        this.acronimo = acronimo;
        return this;
    }

    public String getMail1() {
        return mail1;
    }

    public IPAAmministrazione setMail1(String mail1) {
        this.mail1 = mail1;
        return this;
    }

    public String getCognome_resp() {
        return cognome_resp;
    }

    public IPAAmministrazione setCognome_resp(String cognome_resp) {
        this.cognome_resp = cognome_resp;
        return this;
    }

    public String getComune() {
        return comune;
    }

    public IPAAmministrazione setComune(String comune) {
        this.comune = comune;
        return this;
    }

    public String getCap() {
        return cap;
    }

    public IPAAmministrazione setCap(String cap) {
        this.cap = cap;
        return this;
    }

    public String getProvincia() {
        return provincia;
    }

    public IPAAmministrazione setProvincia(String provincia) {
        this.provincia = provincia;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IPAAmministrazione that = (IPAAmministrazione) o;
        return Objects.equals(cod_amm, that.cod_amm) &&
                Objects.equals(des_amm, that.des_amm) &&
                Objects.equals(comune, that.comune) &&
                Objects.equals(cap, that.cap) &&
                Objects.equals(provincia, that.provincia) &&
                Objects.equals(nome_resp, that.nome_resp) &&
                Objects.equals(cognome_resp, that.cognome_resp) &&
                Objects.equals(sito_istituzionale, that.sito_istituzionale) &&
                Objects.equals(indirizzo, that.indirizzo) &&
                Objects.equals(tipologia_amm, that.tipologia_amm) &&
                Objects.equals(acronimo, that.acronimo) &&
                Objects.equals(mail1, that.mail1);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cod_amm, des_amm, comune, cap, provincia, nome_resp, cognome_resp, sito_istituzionale, indirizzo, tipologia_amm, acronimo, mail1);
    }

    @Override
    public String toString() {
        return "IPAAmministrazione{" +
                "cod_amm='" + cod_amm + '\'' +
                ", des_amm='" + des_amm + '\'' +
                ", comune='" + comune + '\'' +
                ", cap='" + cap + '\'' +
                ", provincia='" + provincia + '\'' +
                ", nome_resp='" + nome_resp + '\'' +
                ", cognome_resp='" + cognome_resp + '\'' +
                ", sito_istituzionale='" + sito_istituzionale + '\'' +
                ", indirizzo='" + indirizzo + '\'' +
                ", tipologia_amm='" + tipologia_amm + '\'' +
                ", acronimo='" + acronimo + '\'' +
                ", mail1='" + mail1 + '\'' +
                '}';
    }

}
