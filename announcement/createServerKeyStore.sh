openssl pkcs12 -export -name alias -in server/src/main/resources/crypto/server.crt -inkey server/src/main/resources/crypto/server.key -out server/src/main/resources/crypto/keystore.p12
keytool -importkeystore -destkeystore server/src/main/resources/crypto/server_keystore.jks -srckeystore server/src/main/resources/crypto/keystore.p12 -srcstoretype pkcs12 -alias alias
