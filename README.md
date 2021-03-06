# TDLIoT [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
This repository provides utils to manage a number of topics (stored as Topic Description Language documents) in the context of IoT.
The project is subdivided into the following subprojects:
* topic-catalogue
* TDLWeb
* TDLClient
## Topic Catalog
In order to manage a lot of different topics, the topic catalog provides the topics as Topic Description Language (TDL)- documents. The catalog provides different operations, such as add, delete, update and search. Using a REST interface, all operations can be executed. 
The REST interface is documented using swagger and is available under [http://yourHost:8080/swagger-ui.html](http://yourHost:8080/swagger-ui.html)
The tdl-catalogue is also accessible as docker image and hosted on [Docker Hub](https://hub.docker.com/r/ipvs/tdl-catalogue/)
### Installation 
([docker](https://www.docker.com/) and [docker-compose](https://docs.docker.com/compose/) is required)
1. Switch into the directory :
```cd tdl-catalogue```
2. Build the docker image:
```docker build -t ipvs/tdl-catalogue .```
3. Run the docker compose file (The docker-compose file starts the tdl-catalogue and a mongoDB instance):
```docker-compose up```
## TDLWeb
TDLWeb provides a web interface to execute the provided operations of the topic catalog. Topic descriptions can be added, removed and edited.
![picture alt](https://raw.githubusercontent.com/IPVS-AS/TDLIoT/master/TDLWeb/screenshot.png)
Before using the web interface, please replace the link to the catalogue in the ```script.js``` file.
## TDLClient
The TDLClient is a Java library for using the topic catalog in your project. 
The TDLCLient contains two parts which can be used independently: 


The main *TDLClient* class provides functionality to publish and subscribe to topics with the MQTT protocol. 
Instead of extracting the address or topic manually out of the topic description, the client wraps all this functionality. 
The class provides an interface for subscribing to a single topic, multiple topics or all topics returned by a search. 
For publishing, the topic of the MQTT broker can be added in the same manner and returns the human readable topic name.
Internally it creates connections to the brokers and manages sending and receiving messages using the [eclipse paho](https://www.eclipse.org/paho/) MQTT client.


It relies on the *TDLUtil* class for accessing the topic catalog. 
It provides all REST interface possibilities in Java. 
Below, the interactions between the user and the classes are shown with an example:
The process of subscribing to a topic is shown with the internal resolution to the address of the broker. 
![TDLClient example usage](https://raw.githubusercontent.com/IPVS-AS/TDLIoT/master/TDLClient-example.png)

## Haftungsausschluss

Dies ist ein Forschungsprototyp.
Die Haftung für entgangenen Gewinn, Produktionsausfall, Betriebsunterbrechung, entgangene Nutzungen, Verlust von Daten und Informationen, Finanzierungsaufwendungen sowie sonstige Vermögens- und Folgeschäden ist, außer in Fällen von grober Fahrlässigkeit, Vorsatz und Personenschäden, ausgeschlossen.

## Disclaimer of Warranty

Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE.
You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.
