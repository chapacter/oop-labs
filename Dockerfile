FROM eclipse-temurin:17-jdk-alpine as builder

# Установка переменных окружения
ENV PORT=8081
ENV DB_HOST=localhost
ENV DB_PORT=5432
ENV DB_NAME=avokado-bd
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres

# Установка необходимых пакетов
RUN apk add --no-cache maven curl tar

# Копируем файлы проекта
COPY . /workspace
WORKDIR /workspace

# Сборка проекта
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine

ENV PORT=8081
ENV DB_HOST=db
ENV DB_PORT=5432
ENV DB_NAME=avokado-bd
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres

RUN apk add --no-cache curl tar

RUN curl -SL https://archive.apache.org/dist/tomcat/tomcat-10/v10.1.15/bin/apache-tomcat-10.1.15.tar.gz | tar xz -C /opt && \
    mv /opt/apache-tomcat-10.1.15 /opt/tomcat && \
    rm -rf /opt/tomcat/webapps/*

COPY --from=builder /workspace/target/oop-labs.war /opt/tomcat/webapps/ROOT.war

# Установка прав доступа
RUN chmod -R 755 /opt/tomcat

# Открытие порта
EXPOSE 8081

# Запуск Tomcat
CMD ["/opt/tomcat/bin/catalina.sh", "run"]