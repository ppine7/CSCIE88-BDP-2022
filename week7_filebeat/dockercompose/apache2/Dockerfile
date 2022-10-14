FROM httpd:latest
RUN apt-get update && apt-get install -y curl
HEALTHCHECK --interval=3s --retries=5 CMD curl -f http://localhost
COPY ./httpd.conf /usr/local/apache2/conf/httpd.conf
