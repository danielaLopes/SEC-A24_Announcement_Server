openssl pkcs12 -export -name alias -in client/src/main/resources/crypto/client1.crt -inkey client/src/main/resources/crypto/client1.key -out client/src/main/resources/crypto/keystore1.p12
keytool -importkeystore -destkeystore client/src/main/resources/crypto/client1_keystore.jks -srckeystore client/src/main/resources/crypto/keystore1.p12 -srcstoretype pkcs12 -alias alias


openssl pkcs12 -export -name alias -in client/src/main/resources/crypto/client2.crt -inkey client/src/main/resources/crypto/client2.key -out client/src/main/resources/crypto/keystore2.p12
keytool -importkeystore -destkeystore client/src/main/resources/crypto/client2_keystore.jks -srckeystore client/src/main/resources/crypto/keystore2.p12 -srcstoretype pkcs12 -alias alias


openssl pkcs12 -export -name alias -in client/src/main/resources/crypto/client3.crt -inkey client/src/main/resources/crypto/client3.key -out client/src/main/resources/crypto/keystore3.p12
keytool -importkeystore -destkeystore client/src/main/resources/crypto/client3_keystore.jks -srckeystore client/src/main/resources/crypto/keystore3.p12 -srcstoretype pkcs12 -alias alias