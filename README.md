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

## Using MySQL in bash (password is 1234)
```bash
mysql -u sec -p announcement
show tables;
```


## Instructions
Inside project root directory (announcement/):
1. Build project:
```
mvn clean install
```
2. Run server:
```
cd server/
mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.server.Application" -Dexec.args="<keyStorePassword> <entryPassword> <alias>"
```
2. Run client:
```
cd client/
mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.client.Application" -Dexec.args="<pubKeyPath> <keyStorePath> <keyStorePassword> <entryPassword> <alias> <serverPubKeyPath> <numberOfOtherClients> <otherClientsPubKeyPaths>*"
mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.client.Application" -Dexec.args="src/main/resources/crypto/public1.key src/main/resources/crypto/client1_keystore.jks password password ola ../server/src/main/resources/crypto/public.key 2 src/main/resources/crypto/public2.key src/main/resources/crypto/public3.key"
```

## Interacting with the Client UI

### Posting an announcement:
* When posting an announcement, the announcements referenced must be in the format: id1,id2,id3,...

## Generating keypairs and certificates (required for each server)
1. Private key:
```
openssl genrsa -out server/src/main/resources/crypto/server.key
```
2. Generate a pkcs8 private key from the private key for compatibility with Java

2.a) (if required):
```
set OPENSSL_CONF=C:\Program Files\OpenSSL-Win64\bin\openssl.cfg
```
```
openssl pkcs8 -topk8 -inform PEM -outform DER -in server/src/main/resources/crypto/server.key  -nocrypt > server/src/main/resources/crypto/server_pkcs8.key
```
3. Public key:
    - Ubuntu:
        ```
        openssl rsa -in server/src/main/resources/crypto/server.key -out server/src/main/resources/crypto/public.key -pubout
        ```
    - Windows:
        ```
        openssl rsa -in server/src/main/resources/crypto/server.key –pubout > server/src/main/resources/crypto/public.key
        ```
4. Self-signed certificate:
    - Certificate signing request:
    ```
    openssl req -new -key server/src/main/resources/crypto/server.key -out server/src/main/resources/crypto/server.csr
    ```
    - Self-sign:
    ```
    openssl x509 -req -days 365 -in server/src/main/resources/crypto/server.csr -signkey server/src/main/resources/crypto/server.key -out server/src/main/resources/crypto/server.crt
    ```

    - In order for our certificate to sign other certificates, OpenSSL requires that a database exists (a .srl file):
    ```
    echo 01 > server/src/main/resources/crypto/server.srl
    ```

## Generating keypairs and certificates (required for each client)
1. Private key:
```
openssl genrsa -out client/src/main/resources/crypto/client.key
```
2. Public key:
    - Ubuntu:
        ```
        openssl rsa -in client/src/main/resources/crypto/client.key -out client/src/main/resources/crypto/public.key -pubout
        ```
    - Windows:
        ```
        openssl rsa -in client/src/main/resources/crypto/client.key –pubout > client/src/main/resources/crypto/public.key
        ```
        
3. Signing the client certificate:
    - Certificate signing request:
    ```
    openssl req -new -key client/src/main/resources/crypto/client.key -out client/src/main/resources/crypto/client.csr
    ```
    - Sign:
    ```
    openssl x509 -req -days 365 -in client/src/main/resources/crypto/client.csr -CA server/src/main/resources/crypto/server.crt -CAkey server/src/main/resources/crypto/server.key -out client/src/main/resources/crypto/client.crt
    ```

## Create new keystore
```
cd crypto_lib/
mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.crypto_lib.CreateKeyStorage" -Dexec.args="keyStorePassword keystore_entity_name"
```
Server example:
```
mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.crypto_lib.CreateKeyStorage" -Dexec.args="password ../server/src/main/resources/crypto/server1_keystore.jks"
```

## Store private keys in keystore (NOT WORKING)
```
cd crypto_lib/
mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.crypto_lib.StoreKeys" -Dexec.args="keyStorePassword entryPassword alias path_to_keystore.jks path_to_private.key path_cert.crt"
```
Server example:
```
mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.crypto_lib.StoreKeys" -Dexec.args="password1 password2 ../server/src/main/resources/crypto/server1_keystore.jks ../server/src/main/resources/crypto/server_pkcs8.key ../server/src/main/resources/crypto/server.crt"
rm ../server/src/main/resources/crypto/server_pkcs8.key
rm ../server/src/main/resources/crypto/server.key
```

## KeyStore commands
- Import private key (in Windows, requires Administrator)
    1. Create PKCS12 keystore from private key and public certificate.
    ```
    openssl pkcs12 -export -name ola -in server/src/main/resources/crypto/server.crt -inkey server/src/main/resources/crypto/server.key -out server/src/main/resources/crypto/keystore.p12
    ```
    2. Convert PKCS12 keystore into a JKS keystore
    ```
    keytool -importkeystore -destkeystore server/src/main/resources/crypto/server1_keystore.jks -srckeystore server/src/main/resources/crypto/keystore.p12 -srcstoretype pkcs12 -alias ola
    ```
    3. Remove keys
    ```
    rm ../server/src/main/resources/crypto/server_pkcs8.key
    rm ../server/src/main/resources/crypto/server.key
    ```
- List entries
```
keytool -list -v -keystore [enter keystore name] -storepass [password]
```
Server example:
```
keytool -list -v -keystore server/src/main/resources/crypto/server_keystore.jks -storepass password
```
