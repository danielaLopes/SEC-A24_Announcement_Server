openssl genrsa -out server/src/main/resources/crypto/server4.key

openssl pkcs8 -topk8 -inform PEM -outform DER -in server/src/main/resources/crypto/server4.key  -nocrypt > server/src/main/resources/crypto/server_pkcs8.key

openssl rsa -in server/src/main/resources/crypto/server4.key -out server/src/main/resources/crypto/public4.key -pubout

openssl req -new -key server/src/main/resources/crypto/server4.key -out server/src/main/resources/crypto/server4.csr

openssl x509 -req -days 365 -in server/src/main/resources/crypto/server4.csr -signkey server/src/main/resources/crypto/server4.key -out server/src/main/resources/crypto/server4.crt

echo 01 > server/src/main/resources/crypto/server4.srl