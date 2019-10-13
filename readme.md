Swip *(Spring Web Initializr Plugin)*
==========
[![release-2.0.0][shield-release]](#)
[![code-coverage-100%][shield-coverage]](#)  
[![jdk-8][shield-jdk]](#)
[![swi-3.0.0][shield-spring]](#)
[![MIT licensed][shield-license]](#)

[Swip (Spring Web Initializr Plugin)](https://plugins.jetbrains.com/plugin/12239-swip-spring-web-initializr-) _(will be referenced __Swip__ from now on)_ is a plugin for IntelliJ IDEA.  
It will help you create fully functional Spring Boot WebApps with just a few clicks, based on your Entities.

Table of Contents
-----------------
  * [Demonstration](#Demonstration)
  * [Download](#Download)
  * [Prerequisites](#Prerequisites)
  * [Usage](#Usage)
  * [Description](#Description)
  * [Releases](#Releases)
  * [Contributing](#Contributing)
  * [License](#License)
  
  
Demonstration
-------------
![Demonstration Gif](/../screenshots/swip-demo.gif?raw=true)

Download
--------
Get it through IntelliJ IDEA by writing *Swip* (Spring Web Initializr Plugin) or else through the [Jetbrains Repo](https://plugins.jetbrains.com/plugin/12239-swip-spring-web-initializr)

Prerequisites
-------------
[__spring-web-initializr__](https://github.com/OrPolyzos/spring-web-initializr)

Spring Web Initializr is a separate library has been developed in order to support the Swip and avoid duplicate code, where that's possible.  
__As such, the following dependency is mandatory and should be added to your `pom.xml`.__
```xml
<dependency>
    <groupId>io.github.orpolyzos</groupId>
    <artifactId>spring-web-initializr</artifactId>
    <version>3.0.0</version>
</dependency>
```

[__freemarker__](https://freemarker.apache.org/)

Freemarker is a template engine provided by Apache. Swip by default will generate the frontend resources using the Freemarker syntax. _(In the future Thymeleaf will be supported as well)_  
__As such, the following dependency is mandatory and should be added to your `pom.xml`.__
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-freemarker</artifactId>
</dependency>
```

Usage
-----
1) Create a Java class for your Entity (e.g. User) with all its' desired fields (e.g. id, firstName, lastName, etc...)
2) Right click inside the class to get the editor menu
3) Click the `Swip` option and follow the instructions
4) Choose the field that will be used as the primary key for the Entity
5) Choose the packages for the generated Controller, Service, Repository classes

Description
-----------
Swip will generate all the code required for a functional WebApp based on your Entities. 
It is based on the __convention over configuration__ paradigm. 
During the generation phase, the actual configuration is limited, but after the files have been generated they can obviously be modified as needed.

Obviously Swip is not going to meet your exact business requirements all the times and tweaking may be required, but it is going to provide you with the base stuff to get you started faster.

__TL;DR__  
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
        <version>2.1.6.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <!-- Mandatory for Swip BEGIN-->
        <dependency>
            <groupId>io.github.orpolyzos</groupId>
            <artifactId>spring-web-initializr</artifactId>
            <version>1.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-freemarker</artifactId>
        </dependency>
        <!-- Mandatory for Swip END-->
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
    <summary>User.java (Getters/Setters omitted)</summary>
        
```java
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

    @Column(name = "email", nullable = false, unique = true)
    private String email;

}

```
</details>

_Frontend resources_  
The end user of any web application, should be in place to perform the basic CRUD operations through the provided screens for each Entity (e.g. User, Vehicle, etc..)

* ResourcePersistableBaseView - __Create Read Delete__  
  * Form that provides the required fields to save a ResourcePersistable (e.g. UserForm, VehicleForm, etc...)
  * Form that provides the required fields to search for a Resource (e.g. UserSearchForm, VehicleSearchForm, etc...)
  * Table that provides the fields of retrieved ResourcePersistables as well as the option to Delete/Edit one

* ResourcePersistableEditView - __Update__  
  * Form that provides the required fields to edit a ResourcePersistable (e.g. UserForm, VehicleForm, etc...)

_ResourcePersistableController_  
Based on the provided front end implementation, the ResourcePersistableController should be able to: 
* getResourcePersistableBaseView() -> serves Page #1
* createResourcePersistable() -> creates a ResourcePersistable and serves Page #1
* searchResourcePersistablesBy() -> searches for ResourcePersistables (optionally based on a ResourcePersistableSearchForm) and serves Page #1 filled with the found list of ResourcePersistables
* deleteResourcePersistable() -> deletes a ResourcePersistable and serves Page #1
* getResourcePersistableEditView() -> searches for a specific ResourcePersistable and serves Page #2 filled with its' fields
* editResourcePersistable() -> updates a ResourcePersistable and serves Page #1

_ResourcePersistableService_  
Based on the provided ResourcePersistableController, the ResourcePersistableService should be able to:
* find(ID) -> searches a ResourcePersistable by its' ID and returns it
* findOptional(ID) -> searches a ResourcePersistable by its' ID and returns an Optional<ResourcePersistable>
* findOrThrow(ID) -> searches a ResourcePersistable by its' ID and returns it or throws a RPRuntimeNotFoundException
* findAll() -> searches for all ResourcePersistables and returns a List<ResourcePersistable>
* insert(ResourcePersistable) -> searches for duplicate Resources and throws a RPRuntimeDuplicateException if found, or else saves the ResourcePersistable
* update(ResourcePersistable) -> searches for the specific ResourcePersistable and updates it if found, or else throws a RPRuntimeNotFoundException
* searchBy(ResourcePersistableSearchForm) -> searches for ResourcePersistables based on a ResourcePersistableSearchForm and returns a List<ResourcePersistable> (by default returns findAll())
  
Releases
---------------
* <strong>1.1.0</strong>
    * Adds support for spring-boot-starter-parent from version '1.5.20.RELEASE' up to LATEST
    * Adds support for older IntelliJ IDEA since build '171.4424.54'

* <strong>1.0.0</strong> - First Release
    * Adds support for Maven
    * Adds support for spring-boot-starter-web
    * Adds support for spring-boot-starter-data-jpa
    * Adds support for spring-boot-starter-freemarker
    
Contributing
------------
To contribute to Spring Web Initializr Plugin, follow the instructions in our [contributing guide](/contributing.md).

License
-------
Spring Web Initializr Plugin is licensed under the [MIT](/license.md) license.  
Copyright &copy; 2019, Orestes Polyzos

[shield-release]: https://img.shields.io/badge/release-2.0.0-brightgreen.svg
[shield-coverage]: https://img.shields.io/badge/coverage-0%25-red.svg
[shield-jdk]: https://img.shields.io/badge/jdk-8-blue.svg
[shield-spring]: https://img.shields.io/badge/swi-3.0.0-blue.svg
[shield-license]: https://img.shields.io/badge/license-MIT-blue.svg    