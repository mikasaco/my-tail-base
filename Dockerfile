#基础镜像
FROM openjdk:8-jre-alpine

#将jdk8包放入/usr/local/src并自动解压，jdk8.tar.gz 需要到oracle官方下载，注意解压后的java版本号

COPY target/my-tail-based-0.0.1-SNAPSHOT.jar /usr/local/src/
WORKDIR /usr/local/src
COPY start.sh /usr/local/src/
RUN chmod +x /usr/local/src/start.sh
ENTRYPOINT ["/bin/sh", "/usr/local/src/start.sh"]
