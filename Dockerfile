FROM gcr.io/distroless/java25-debian13:nonroot
WORKDIR /app
ENV TZ="Europe/Oslo"
EXPOSE 8080

COPY build/libs/hm-finnhjelpemiddel-bff-all.jar ./app.jar

CMD ["-jar", "app.jar"]