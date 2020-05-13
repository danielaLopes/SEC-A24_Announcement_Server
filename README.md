<<<<<<< HEAD
# SEC-A24_Announcement_Server

## Developing Environment
* Ubuntu 18.04 LTS
* Java version - openjdk 1.8.0_122
* Mysql server 5.7.128

## Configuring mySQL user and database:
```sql
CREATE USER 'sec'@'localhost' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON * . * TO 'sec'@'localhost';
FLUSH PRIVILEGES;
create database announcement;
```
## Instructions to Run Tests (by default keys are already generated for 3 clients and the server)
Inside project root directory (announcement/):  
``` $ mvn clean install -DskipTests```
``` $ mvn clean install -Dmaven.test.skip=true```

### Server Tests
Focus on the server application functionalities.  
1. Inside project server directory (announcement/server/):  
    - ``` $ mvn test```

### Client Tests 
Focus on the client application functionalities and UI. Require running a server in a different window.  
1. Inside project server directory (announcement/server/):  
    - Start the server: ``` $ mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.server.Application" -Dexec.args="password password alias src/main/resources/crypto/public.key src/main/resources/crypto/server_keystore.jks"```  
2. Inside project client directory (announcement/client/):  
    - ``` $ mvn test```

### Client-Server Communication Tests (Tampering, Integrity, Dropping, Replaying)
Focus on the possible attacks that can happen in the communication between a server and the client.
If the server was running, shut it down (to clean the internal data structures).  
1. Inside project server directory (announcement/server/):  
    - Start the server: ``` $ mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.server.Application" -Dexec.args="password password alias src/main/resources/crypto/public.key src/main/resources/crypto/server_keystore.jks"```  
2. Inside project client/server testing directory (announcement/client_server_testing/):  
    - ``` $ mvn test```

## Instructions (only needed if keys are not already generated)
Inside project root directory (announcement/):
1. Generate Client and Server keys and keystore.
    - For simple Client generation (will generate 3 key pairs):
        ```
        ./generateClientKeys.sh < generateClientKeysInput.txt
        ./createClientKeyStore.sh
        ```
    - For simple Server generation:
        ```
        ./generateServerKeys.sh < generateServerKeysInput.txt
        ./createServerKeyStore.sh
        ```

2. Build project:
    * Without running tests
    ```
    mvn clean install -DskipTests
    ```
    * Without compilling tests
    ```
    mvn clean install -Dmaven.test.skip=true 
    ```


3. Run server:
    - General:
        ```
        cd server/
        mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.server.Application" -Dexec.args="<port> <keyStorePassword> <entryPassword> <alias> <pubKeyPath> <keyStorePath>"
        ```
    - Example 3 servers:
        ```
        cd server/
        mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.server.Application" -Dexec.args="4 1 9001 password password alias src/main/resources/crypto/public1.key src/main/resources/crypto/server_keystore.jks"
        mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.server.Application" -Dexec.args="4 1 9002 password password alias src/main/resources/crypto/public2.key src/main/resources/crypto/server2_keystore2.jks"
        mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.server.Application" -Dexec.args="4 1 9003 password password alias src/main/resources/crypto/public3.key src/main/resources/crypto/server3_keystore3.jks"
        mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.server.Application" -Dexec.args="4 1 9004 password password alias src/main/resources/crypto/public4.key src/main/resources/crypto/server4_keystore4.jks"
        ```
4. Run client:
    - General:
        ```
        cd client/
        mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.client.Application" -Dexec.args="<pubKeyPath> <keyStorePath> <keyStorePassword> <entryPassword> <alias> <numberOfServers> <numberOfOtherClients> <otherClientsPubKeyPaths>*"
        ```
    - 3 clients:
        ```
        mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.client.Application" -Dexec.args="src/main/resources/crypto/public1.key src/main/resources/crypto/client1_keystore.jks password password alias 4 1 2 src/main/resources/crypto/public2.key src/main/resources/crypto/public3.key"
        mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.client.Application" -Dexec.args="src/main/resources/crypto/public2.key src/main/resources/crypto/client2_keystore.jks password password alias 4 1 2 src/main/resources/crypto/public1.key src/main/resources/crypto/public3.key"
        mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.client.Application" -Dexec.args="src/main/resources/crypto/public3.key src/main/resources/crypto/client3_keystore.jks password password alias 4 1 2 src/main/resources/crypto/public1.key src/main/resources/crypto/public2.key"
        ```

## Interacting with the Client UI

### Posting an announcement:
* When posting an announcement, the announcements referenced must be in the format: id1,id2,id3,...

## Tests Structure

### Byzantine Client
    ```
    cd client_server_communication/
    mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.client_server_testing.ByzantineClientApplication"
    ```
    
### Byzantine Server
    ```
    cd client_server_communication/
    mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.client_server_testing.ByzantineServerApplication"
    ```

### Requirements to run tests:
- server/src/main/resources/crypto/public.key
- server/src/main/resources/crypto/server_keystore.jks
- client/src/main/resources/crypto/public1.key
- client/src/main/resources/crypto/client1_keystore.jks
- client/src/main/resources/crypto/public2.key
- client/src/main/resources/crypto/client2_keystore.jks
- client/src/main/resources/crypto/public3.key
- client/src/main/resources/crypto/client3_keystore.jks

- All KeyStore and Entry password to be "password"
- All Entry alias to be "alias"