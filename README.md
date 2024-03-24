# spring-cloud-gateway-mvc-demo

## Pre-requisites
Install Java 21

Install Docker Desktop

## Wiremock server (on port 8888)

Start Mock server docker on port 8888
```
docker run -it --rm --name wm -p 8888:8080 -v "<project location>\spring-cloud-gateway-mvc-demo\tools\wiremock:/home/wiremock" rodolpheche/wiremock  --verbose

For example,
docker run -it --rm --name wm -p 8888:8080 -v "C:\Users\<user>\dev\Git_work\github\spring-cloud-gateway-mvc-demo\tools\wiremock:/home/wiremock" rodolpheche/wiremock  --verbose
```

## Generated requests file
Look at the file generated-requests.http under tools directory in order to execute api requests from the IDE(Intellij)

