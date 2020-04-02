mkdir server/src/main/resources
mkdir server/src/main/resources/crypto

openssl genrsa -out server/src/main/resources/crypto/server.key

openssl pkcs8 -topk8 -inform PEM -outform DER -in server/src/main/resources/crypto/server.key  -nocrypt > server/src/main/resources/crypto/server_pkcs8.key

openssl rsa -in server/src/main/resources/crypto/server.key -out server/src/main/resources/crypto/public.key -pubout

openssl req -new -key server/src/main/resources/crypto/server.key -out server/src/main/resources/crypto/server.csr

openssl x509 -req -days 365 -in server/src/main/resources/crypto/server.csr -signkey server/src/main/resources/crypto/server.key -out server/src/main/resources/crypto/server.crt

echo 01 > server/src/main/resources/crypto/server.srl