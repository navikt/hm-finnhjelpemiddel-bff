FROM ghcr.io/navikt/baseimages/temurin:17
USER apprunner
COPY build/libs/hm-finnhjelpemiddel-bff-all.jar ./app.jar