# BOTSWANA EMR BACKEND

[![Build Status](https://travis-ci.org/codecentric/springboot-sample-app.svg?branch=master)](https://travis-ci.org/codecentric/springboot-sample-app)
[![Coverage Status](https://coveralls.io/repos/github/codecentric/springboot-sample-app/badge.svg?branch=master)](https://coveralls.io/github/codecentric/springboot-sample-app?branch=master)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

Botswana [Spring Boot](http://projects.spring.io/spring-boot/).

## Requirements

For building and running the application you need:

- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven 3](https://maven.apache.org)
- [Postgresql](https://https://www.postgresql.org/)
- [Docker](https://hub.docker.com/)
- [Keycloak](https://www.keycloak.org/)

## Deployments
- [Docker Deployment](https://drive.google.com/drive/folders/1D0jE6qPA8ykX8xJHY4RN9pifrVd4wO6p?usp=sharing)
- [K8s Deployment](https://drive.google.com/drive/folders/1D0jE6qPA8ykX8xJHY4RN9pifrVd4wO6p?usp=sharing)


## Running the application locally

```shell
mvn spring-boot:run
```
## Accessing Swagger

You can access the swagger UI platform from:

```shell
http://{{host_url}}:8085/swagger-ui.html
```

## Keycloak Configuration

Use the following

```shell
Using Docker

docker run -p 8999:8080 -e KEYCLOAK_ADMIN=intellisoft -e KEYCLOAK_ADMIN_PASSWORD=intellisoft quay.io/keycloak/keycloak:18.0.1 start-dev -d
```
```shell
One can also create Kecloack instance as a service
```

```shell
1. Add a realm
2. Create and configure a client.
    - Create client and add a client id eg botswana-emr
    - Configure it as follows: 
      ** Client authentication Enabled: ON 
      ** Authorization Enabled: OFF
      ** Authentication flow
        ** Standard Flow Enabled: ON
        ** Direct Access Grants Enabled: ON
        ** Service Accounts Enabled: ON
        ** Implicit Flow Enabled: ON
          
3. Create Roles for client and realm
4. Get credential secret from the credential secret
5. Disable Require SSL under the Realm Setting Login Tab
6. Change Token lifespan: https://botswanaemrauth.intellisoftkenya.com/admin/master/console/#/botswana-emr-realm/realm-settings/tokens

```

## Allowing ssl using .p12

```shell

openssl pkcs12 -export -out certificate.p12 -inkey icl.key  -in star_intellisoftkenya_com.pem

security.require-ssl=true
server.ssl.enabled=true
server.ssl.key-store=classpath:server/certificate.p12
server.ssl.key-store-password=****
server.ssl.key-store-type=JKS
server.ssl.keyStoreType=PKCS12

````

## Microservice Name
```shell
    Ambulance Service: http://botswanaambulance:7002
    Appointment Service: http://botswanaappointments:8082
    Authentication Service: http://botswanaauth:8081
    Facility Service: http://botswanafacility:7000
    FileStorage Service: file-storage-service
    Notifications Service: http://botswananotification:7001
```

## Copyright

Released under the Apache License 2.0. See the [LICENSE](https://github.com/codecentric/springboot-sample-app/blob/master/LICENSE) file.
