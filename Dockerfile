FROM gradle:8.14 as build
WORKDIR /app
COPY . .
RUN gradle stage

FROM eclipse-temurin:21-alpine

WORKDIR /app/

# `freetype fontconfig ttf-dejavu` are needed for graph generation
RUN apk add --no-cache tini freetype fontconfig ttf-dejavu \
    && addgroup -S diabot -g 1000 \
    && adduser -S -G diabot -u 1000 diabot

COPY --from=build --chown=1000:1000 /app/build/libs/diabot.jar ./

# Drop privileges
USER diabot

ENTRYPOINT ["/sbin/tini", "--"]

CMD ["java", "-Djava.awt.headless=true", "-jar", "/app/diabot.jar"]
