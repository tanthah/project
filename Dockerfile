# Sử dụng image Tomcat 10.1.28 với JDK 21
FROM tomcat:10.1.28-jdk21

# Xóa các ứng dụng mặc định của Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Sao chép file WAR từ thư mục dist vào thư mục webapps
COPY dist/el1-1.0-SNAPSHOT.war /usr/local/tomcat/webapps/ROOT.war

# Mở cổng 8080
EXPOSE 8080

# Khởi động Tomcat
CMD ["catalina.sh", "run"]