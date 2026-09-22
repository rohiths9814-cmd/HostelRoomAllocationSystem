# ============================================================
#  Dockerfile - tells Railway how to build and run the Java API
# ============================================================
#
#  WHY THIS FILE EXISTS
#  Railway knows how to build a Maven or Gradle project on its own.
#  This project deliberately uses neither - it is plain javac, which
#  is exactly what the syllabus asked for. So we describe the build
#  ourselves in six lines, and Railway follows them.
#
#  A Dockerfile is just a recipe:
#    FROM    which base system to start from
#    COPY    which files to bring in
#    RUN     what to execute while BUILDING the image
#    CMD     what to execute when the container STARTS
# ============================================================

# A ready-made Linux image with JDK 21 already installed.
# "jdk" (not "jre") because we compile inside the container.
FROM eclipse-temurin:21-jdk

# Everything below happens inside this folder in the container.
WORKDIR /app

# Bring in the MySQL driver and our source code.
# bin/ is NOT copied - it is rebuilt here, so a stale local build
# can never be deployed by accident.
COPY lib ./lib
COPY src ./src

# Compile every .java file, exactly like compile.bat does locally.
# Note the classpath separator is ":" here, not ";" - this is Linux.
RUN find src -name "*.java" > sources.txt \
    && javac -d bin -cp "lib/*" @sources.txt \
    && rm sources.txt

# Documentation only. Railway ignores this and injects its own PORT,
# which ApiServer reads from the environment.
EXPOSE 8080

# The command that runs when the container starts.
# ApiServer - NOT Main. The console app expects a keyboard, and a
# container has no keyboard, so it would exit immediately.
CMD ["sh", "-c", "java -cp 'bin:lib/*' com.hostel.server.ApiServer"]
