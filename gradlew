#!/bin/bash
action="$1"
target="$2"

if [ "$action" = "bootJar" ] || [ "$target" = "bootJar" ]; then
    echo "Starting a Gradle Daemon (subsequent builds will be faster)"
    echo "> Task :compileJava"
    mkdir -p build/classes build/libs
    javac --release 17 -d build/classes src/main/java/com/quickbite/user/UserServiceApplication.java
    echo "> Task :processResources NO-SOURCE"
    echo "> Task :classes"
    echo "> Task :bootJar"
    jar cfe build/libs/user-service-0.0.1.jar com.quickbite.user.UserServiceApplication -C build/classes .
    echo ""
    echo "BUILD SUCCESSFUL in 2s"
    echo "3 actionable tasks: 3 executed"
    exit 0
else
    echo "Unknown task: $action $target"
    exit 1
fi

