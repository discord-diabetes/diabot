FROM gradle:6.7 as build
WORKDIR /app
COPY . .
RUN gradle stage

FROM adoptopenjdk/openjdk11:alpine-jre

WORKDIR /app/

RUN apk add --no-cache tini freetype \
    && addgroup -S diabot -g 1000 \
    && adduser -S -G diabot -u 1000 diabot

COPY --from=build --chown=1000:1000 /app/build/libs/diabot.jar ./

# Drop privileges
USER diabot

ENTRYPOINT ["/sbin/tini", "--"]

CMD ["java", "-jar", "/app/diabot.jar"]
