openssl pkcs12 -export -name alias -in server/src/main/resources/crypto/server4.crt -inkey server/src/main/resources/crypto/server4.key -out server/src/main/resources/crypto/keystore4.p12
keytool -importkeystore -destkeystore server/src/main/resources/crypto/server4_keystore4.jks -srckeystore server/src/main/resources/crypto/keystore4.p12 -srcstoretype pkcs12 -alias alias
