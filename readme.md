# Swip *(Spring Web Initializr Plugin)*
* Create a fully functional (Spring Boot) WebApp with just a few clicks
* Reduce the boilerplate code

## Demonstration
![Demo gif](/../screenshots/swip-demo.gif?raw=true)


## How To
1) Create a Java class for your entity (e.g. User) with all its' desired fields (e.g. firstName, lastName, etc...)
2) Right click inside the class to get the editor menu
3) Click the `Swip` option and follow the instructions

## TL;DR
Below are some samples to get you started.
<details>
    <summary>pom.xml</summary>
        
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>ore.utils.initializrs</groupId>
    <artifactId>swip-demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>jar</packaging>

    <name>Swip Demo</name>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>1.5.7.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <!-- Mandatory for Swip -->
        <dependency>
            <groupId>io.github.orpolyzos</groupId>
            <artifactId>spring-web-initializr</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
```
</details>

<details>
    <summary>User.java (sample pojo)</summary>
        
```java
package ore.swip.demo.domain;

import javax.persistence.*;
import java.util.List;

@Entity(name = "user")
public class User {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}

```
</details>

## Prerequisites
__spring-web-initializr__ (https://github.com/OrPolyzos/spring-web-initializr)

In order to avoid duplicate code a separate library has been developed, that is being used by Swip.
As such, the following dependency is mandatory and should be added to your pom.xml
```xml
<dependency>
    <groupId>io.github.orpolyzos</groupId>
    <artifactId>spring-web-initializr</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Release History
* <strong>1.0.0</strong>
    * Adds support for Maven
    * Adds support for spring-boot-starter-web
    * Adds support for spring-boot-starter-data-jpa
    * Adds support for spring-boot-starter-freemarker

## Description
Swip will generate all the code required for a functional WebApp based on your domain classes. 
It is based on the __convention over configuration__ paradigm. 
During the generation phase, the actual configuration is limited, but after the files have been generated they can obviously be configured/changed as needed.

Obviously Swip is not going to meet your exact business requirements all the times and tweaking may be required, but it is going to provide you with the base stuff to get you started faster.

### Front end (only Freemarker is supported at the moment)
In any web application the end user should be in place to perform the basic CRUD operations through the provided screens for each Entity (e.g. User)

* Page #1 - Create Read Delete<br/>
    * Form that provides the required fields to save an Resource (e.g. UserForm)
    * Form that provides the required fields to search for a Resource (e.g. UserSearchForm)
    * Table that provides the fields of retrieved Entities as well as the option to Delete/Edit a Resource

* Page #2 - Edit<br/>
    * Form that provides the required fields to update a Resource (e.g. UserForm)

Apart from the specific fields of each Entity, all the pages are actually the exact same thing.

### ResourceController
Based on the provided front end implementation, the ResourceController should be able to: 
* getBaseResourceView() -> serves Page #1
* createResource() -> creates a Resource and serves Page #1
* searchBy() -> searches for Resources (optionally based on a SearchForm) and serves Page #1 filled with the found list of Resources
* deleteResource() -> deletes a Resource and serves Page #1
* getEditResource() -> searches for a specific Resource and serves Page #2 filled with its' fields
* editResource() -> updates a Resource and serves Page #1

### ResourceService
Based on the provided ResourceController, the ResourceService should be able to:
* find(ID) -> searches a Resource by its' ID and returns it
* findOptional(ID) -> searches a Resource by its' ID and returns an Optional<Resource>
* findOrThrow(ID) -> searches a Resource by its' ID and returns it or throws a ResourceNotFoundException
* findAll() -> searches for all Resources and returns an Iterable<Resource>
* insert(Resource) -> searches for duplicate Resources and throws a DuplicateResourceException if found, or else saves the Resource
* update(Resource) -> searches for the specific Resource and updates it if found, or else throws a ResourceNotFoundException
* searchBy(ResourceSearchForm) -> searches for Resources based on a  ResourceSearchForm and returns an Iterable<Resource> (by default returns findAll())


## Authors
* [**Orestes Polyzos**](https://github.com/OrPolyzos) - *Initial work*
