FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM tomcat:10-jdk21
ENV CATALINA_HOME=/usr/local/tomcat
RUN rm -rf $CATALINA_HOME/webapps/ROOT
COPY --from=builder /build/target/G04PromotionalProduction-0.0.1-SNAPSHOT.war $CATALINA_HOME/webapps/G04PromotionalProduction.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
