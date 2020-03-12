# SEC-A24_Announcement_Server

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
