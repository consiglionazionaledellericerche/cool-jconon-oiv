# Elenco Nazionale OIV
<p align="center">
  <a href="https://github.com/consiglionazionaledellericerche/cool-jconon-oiv/blob/master/LICENSE">
    <img src="https://img.shields.io/badge/License-AGPL%20v3-blue.svg" alt="Cool Jconon is released under the GNU AGPL v3 license." />
  </a>
  <a href="https://mvnrepository.com/artifact/it.cnr.si/cool-jconon-oiv">
    <img alt="Maven Central" src="https://img.shields.io/maven-central/v/it.cnr.si/cool-jconon-oiv.svg?style=flat" alt="Current version on maven central.">
  </a>
</p>


## MAVEN dependency
|Artifact| Version |
|---|---|
|[Apache Chemistry](https://chemistry.apache.org/java/opencmis.html)| ![Maven Central](https://img.shields.io/maven-central/v/org.apache.chemistry.opencmis/chemistry-opencmis-client-impl.svg)|
|[Spring Boot](https://spring.io/projects/spring-boot)| ![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/org.springframework.boot/spring-boot/2.1.6.RELEASE.svg) |
|[Spring.io](https://spring.io/)| ![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/org.springframework/spring-context/5.1.8.RELEASE.svg) |
|[Cool](https://github.com/consiglionazionaledellericerche/cool) | ![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/it.cnr.si.cool/cool-parent/3.1.65.svg)|
|[Jconon](https://github.com/consiglionazionaledellericerche/cool-jconon) | ![Maven Central with version prefix filter](https://img.shields.io/maven-central/v/it.cnr.si.cool.jconon/cool-jconon-parent/4.2.65.svg)|
|[OpenCMIS Criteria](https://mvnrepository.com/artifact/it.cnr.si/opencmis-criteria) | ![Maven Central](https://img.shields.io/maven-central/v/it.cnr.si/opencmis-criteria.svg)|


## Run

#### Prerequisites Docker and docker-compose
```
git clone https://github.com/consiglionazionaledellericerche/cool-jconon-oiv.git
cd cool-jconon-oiv/docker-compose
docker-compose up -d;docker-compose logs -f
```
### Normally after 120 seconds the application responds

* [OIV](http://localhost/)
* [Alfresco](http://localhost/alfresco)
* [Solr](http://localhost/solr4)

### Build local

```bash
mvn clean spring-boot:run -Dspring.profiles.active=fp -Drepository.base.url=http://localhost:9080/alfresco/
```

<http://localhost:8080/>
