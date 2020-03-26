openssl genrsa -out client/src/main/resources/crypto/client1.key

openssl rsa -in client/src/main/resources/crypto/client1.key -out client/src/main/resources/crypto/public1.key -pubout

openssl req -new -key client/src/main/resources/crypto/client1.key -out client/src/main/resources/crypto/client1.csr

openssl x509 -req -days 365 -in client/src/main/resources/crypto/client1.csr -CA server/src/main/resources/crypto/server.crt -CAkey server/src/main/resources/crypto/server.key -out client/src/main/resources/crypto/client1.crt


openssl genrsa -out client/src/main/resources/crypto/client2.key

openssl rsa -in client/src/main/resources/crypto/client2.key -out client/src/main/resources/crypto/public2.key -pubout

openssl req -new -key client/src/main/resources/crypto/client2.key -out client/src/main/resources/crypto/client2.csr

openssl x509 -req -days 365 -in client/src/main/resources/crypto/client2.csr -CA server/src/main/resources/crypto/server.crt -CAkey server/src/main/resources/crypto/server.key -out client/src/main/resources/crypto/client2.crt


openssl genrsa -out client/src/main/resources/crypto/client3.key

openssl rsa -in client/src/main/resources/crypto/client3.key -out client/src/main/resources/crypto/public3.key -pubout

openssl req -new -key client/src/main/resources/crypto/client3.key -out client/src/main/resources/crypto/client3.csr

openssl x509 -req -days 365 -in client/src/main/resources/crypto/client3.csr -CA server/src/main/resources/crypto/server.crt -CAkey server/src/main/resources/crypto/server.key -out client/src/main/resources/crypto/client3.crt