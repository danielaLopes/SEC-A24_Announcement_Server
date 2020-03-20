# SEC-A24_Announcement_Server

## Developing Environment
* Ubuntu 18.04 LTS
* Java version - openjdk 1.8.0_122
* Mysql server 5.7.128

## To install the mySQL dependencies in order to run the project:
```console
$ sudo apt-get install libmysql-java
```

## Setup MySQL
1 - Install these 2 dependencies
```console
$ sudo apt-get install mysql-server
$ sudo apt-get install libmysql-java

sudo pkill mysql
sudo pkill mysqld
sudo service mysql restart

sudo mysql
```
## Configuring mySQL user and database:
```sql
CREATE USER 'sec'@'localhost' IDENTIFIED BY '1234';
GRANT ALL PRIVILEGES ON * . * TO 'sec'@'localhost';
FLUSH PRIVILEGES;
create database announcement;
```

## Using MySQL in bash
```bash
mysql -u sec -p 1234
use announcement;
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
mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.server.Application"
```
2. Run client:
``` 
cd client/
mvn exec:java -Dexec.mainClass="pt.ulisboa.tecnico.sec.client.Application"
```

## Interacting with the Client UI

### Posting an announcement:
* When posting an announcement, the announcements referenced must be in the format: id1,id2,id3,...
