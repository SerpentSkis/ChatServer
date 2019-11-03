FROM openjdk:12
WORKDIR /
ADD build/libs/ChatServer-0.0.1.jar ChatServer-0.0.1.jar
ADD resources/ resources/
EXPOSE 8080
CMD java -jar ChatServer-0.0.1.jar