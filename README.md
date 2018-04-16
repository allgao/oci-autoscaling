# Compute Auto-Scaling on Oracle Cloud Infrastructure 

## Functionality
---
This program is a simple tool which makes you able to manage Oracle Cloud Infrastructure instances with web-UI.
- Browse running and backup servers in specific oracle cloud infrastructure compartment.
- Scale in/out specific group of servers.
- Create auxiliary servers for selected group.

## Provisioning
---
### Prerequisites
1. Oracle Cloud Infrastructure account
1. Configure user properly for SDK authentication. 
1. Create virtual network/subnet/loadbalancer and everything necessary for running your application within this environment.
1. Create a Compute instance and tag it with 4 specific tags:
    - group -represent which group does the instance belongs to
    - category -represent the server is a regular server or auxiliary server
    - loadbalancer -represent which loadbalancer should the server join in
    - backendset -represent which backendset should the server join in
### Configure Steps
1. Download Oracle Cloud Infrastructure Java SDK and unzip it.
1. Add oci-java-sdk into your Maven local repository
1. Import this project into your IDE.
1. Import third-party dependencies(except validation-api-1.1.0.Final.jar) into project build path. (SDK-LOCATION\oci-java-sdk\third-party\lib)
1. Modify application.properties according to your environment

## Running
---
1. Compile and build your project.
1. Find artifact oci-autoscaling-1.0-SNAPSHOT.jar at PROJECT_PATH/target/ 
1. java -jar oci-autoscaling-1.0-SNAPSHOT.jar
1. Open your browser and access: http://localhost:8080     
1. Login with user credential: admin/admin