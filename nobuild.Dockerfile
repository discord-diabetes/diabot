# This Dockerfile copies an already-built JAR from `./build/libs/diabot.jar` rather than building it

FROM eclipse-temurin:21-alpine

WORKDIR /app/

# `freetype fontconfig ttf-dejavu` are needed for graph generation
RUN apk add --no-cache tini freetype fontconfig ttf-dejavu \
    && addgroup -S diabot -g 1000 \
    && adduser -S -G diabot -u 1000 diabot

COPY --chown=1000:1000 ./build/libs/diabot.jar ./

# Drop privileges
USER diabot

ENTRYPOINT ["/sbin/tini", "--"]

CMD ["java", "-Djava.awt.headless=true", "-jar", "/app/diabot.jar"]
