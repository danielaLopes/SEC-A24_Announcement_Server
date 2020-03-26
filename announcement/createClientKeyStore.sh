openssl pkcs12 -export -name ola -in client/src/main/resources/crypto/client1.crt -inkey client/src/main/resources/crypto/client1.key -out client/src/main/resources/crypto/keystore1.p12
keytool -importkeystore -destkeystore client/src/main/resources/crypto/client1_keystore.jks -srckeystore client/src/main/resources/crypto/keystore1.p12 -srcstoretype pkcs12 -alias ola


openssl pkcs12 -export -name ola -in client/src/main/resources/crypto/client2.crt -inkey client/src/main/resources/crypto/client.key -out client/src/main/resources/crypto/keystore.p12
keytool -importkeystore -destkeystore client/src/main/resources/crypto/client2_keystore.jks -srckeystore client/src/main/resources/crypto/keystore.p12 -srcstoretype pkcs12 -alias ola


openssl pkcs12 -export -name ola -in client/src/main/resources/crypto/client3.crt -inkey client/src/main/resources/crypto/client.key -out client/src/main/resources/crypto/keystore.p12
keytool -importkeystore -destkeystore client/src/main/resources/crypto/client3_keystore.jks -srckeystore client/src/main/resources/crypto/keystore.p12 -srcstoretype pkcs12 -alias ola