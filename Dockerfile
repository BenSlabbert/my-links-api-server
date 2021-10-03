ARG ALPINE_IMAGE="alpine:3.14"

FROM $ALPINE_IMAGE as packager

ARG ZULU_JDK="zulu17.28.13-ca-jdk17.0.0-linux_musl_x64"

RUN apk add curl ca-certificates binutils --no-cache

RUN curl https://cdn.azul.com/zulu/bin/$ZULU_JDK.tar.gz -o $ZULU_JDK.tar.gz

RUN gunzip $ZULU_JDK.tar.gz

RUN tar -xvf $ZULU_JDK.tar

ENV PATH "$PATH:/$ZULU_JDK/bin"

RUN { \
        java --version ; \
        echo "jlink version:" && \
        jlink --version ; \
    }

ENV JAVA_MINIMAL=/opt/jre

# build modules distribution
RUN jlink \
    --verbose \
    --add-modules \
        java.base,java.sql,java.naming,java.desktop,java.management,java.security.jgss,java.instrument \
#         java.naming - javax/naming/NamingException
#         java.desktop - java/beans/PropertyEditorSupport
#         java.management - javax/management/MBeanServer
#         java.security.jgss - org/ietf/jgss/GSSException
#         java.instrument - java/lang/instrument/IllegalClassFormatException
    --compress 2 \
    --strip-debug \
    --no-header-files \
    --no-man-pages \
    --output "$JAVA_MINIMAL"

FROM $ALPINE_IMAGE

ENV JAVA_MINIMAL=/opt/jre
ENV PATH="$PATH:$JAVA_MINIMAL/bin"

COPY "target/lib" "/lib"

COPY --from=packager "$JAVA_MINIMAL" "$JAVA_MINIMAL"
COPY "target/api-server.jar" "/app.jar"

EXPOSE 8443
ENV JAVA_OPTS="-XX:+UseZGC -Xlog:gc -Xmx64m -Xms64m"
CMD [ "-jar", "/app.jar", "$JAVA_OPTS" ]
ENTRYPOINT [ "java", "-cp", "/lib" ]
