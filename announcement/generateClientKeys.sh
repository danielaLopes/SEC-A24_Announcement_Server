openssl genrsa -out client/src/main/resources/crypto/client.key

openssl rsa -in client/src/main/resources/crypto/client.key -out client/src/main/resources/crypto/public.key -pubout

openssl req -new -key client/src/main/resources/crypto/client.key -out client/src/main/resources/crypto/client.csr

openssl x509 -req -days 365 -in client/src/main/resources/crypto/client.csr -CA server/src/main/resources/crypto/server.crt -CAkey server/src/main/resources/crypto/server.key -out client/src/main/resources/crypto/client.crt