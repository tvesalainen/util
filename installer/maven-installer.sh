#! /bin/sh
REPO=/mnt/m2/repository
JAR=/usr/local/lib
EXE=/usr/local/bin
echo >$EXE/maven-installer
echo "#! /bin/sh" >>$EXE/maven-installer
echo "# This is script for java client applications managed by maven_installer" >>$EXE/maven-installer
echo "" >>$EXE/maven-installer
echo "# Read configuration file if it is present" >>$EXE/maven-installer
echo "[ -r /etc/default/maven_installer ] && . /etc/default/maven_installer" >>$EXE/maven-installer
echo "" >>$EXE/maven-installer
echo "ARGS=\$@" >>$EXE/maven-installer
echo "" >>$EXE/maven-installer
echo "##### DON'T EDIT LINES BELOW" >>$EXE/maven-installer
echo "" >>$EXE/maven-installer
echo "CLASSPATH=$JAR/org/vesalainen/maven-installer/1.8.1/maven-installer-1.8.1.jar:$JAR/org/vesalainen/util/1.8.1/util-1.8.1.jar:$JAR/org/vesalainen/test/1.0.0/test-1.0.0.jar:$JAR/org/vesalainen/dev/1.0.1/dev-1.0.1.jar:$JAR/org/apache/maven/maven-model-builder/3.1.1/maven-model-builder-3.1.1.jar:$JAR/commons-net/commons-net/3.3/commons-net-3.3.jar:$JAR/com/googlecode/efficient-java-matrix-library/equation/0.26/equation-0.26.jar:$JAR/log4j/log4j/1.2.17/log4j-1.2.17.jar:$JAR/org/vesalainen/lpg/javalpg/1.8.1/javalpg-1.8.1.jar:$JAR/org/codehaus/plexus/plexus-utils/3.0.15/plexus-utils-3.0.15.jar:$JAR/org/codehaus/plexus/plexus-interpolation/1.19/plexus-interpolation-1.19.jar:$JAR/org/codehaus/plexus/plexus-component-annotations/1.5.5/plexus-component-annotations-1.5.5.jar:$JAR/org/apache/maven/maven-model/3.1.1/maven-model-3.1.1.jar:$JAR/com/googlecode/efficient-java-matrix-library/core/0.26/core-0.26.jar:$JAR/org/apache/ant/ant/1.9.4/ant-1.9.4.jar:$JAR/org/vesalainen/lpg/lpg/1.8.1/lpg-1.8.1.jar:$JAR/org/apache/ant/ant-launcher/1.9.4/ant-launcher-1.9.4.jar:$JAR/org/vesalainen/bcc/bcc/1.1.5/bcc-1.1.5.jar" >>$EXE/maven-installer
echo "MAIN=org.vesalainen.installer.Installer" >>$EXE/maven-installer
echo "" >>$EXE/maven-installer
echo "##### DON'T EDIT LINES ABOVE" >>$EXE/maven-installer
echo "" >>$EXE/maven-installer
echo "\$JAVA -classpath \$CLASSPATH \$MAIN \$ARGS" >>$EXE/maven-installer
chmod 744 $EXE/maven-installer
chown root $EXE/maven-installer
mkdir -p $JAR/org/vesalainen/maven-installer/1.8.1
cp $REPO/org/vesalainen/maven-installer/1.8.1/maven-installer-1.8.1.jar $JAR/org/vesalainen/maven-installer/1.8.1/maven-installer-1.8.1.jar
chmod 644 $JAR/org/vesalainen/maven-installer/1.8.1/maven-installer-1.8.1.jar
chown root $JAR/org/vesalainen/maven-installer/1.8.1/maven-installer-1.8.1.jar
mkdir -p $JAR/org/vesalainen/util/1.8.1
cp $REPO/org/vesalainen/util/1.8.1/util-1.8.1.jar $JAR/org/vesalainen/util/1.8.1/util-1.8.1.jar
chmod 644 $JAR/org/vesalainen/util/1.8.1/util-1.8.1.jar
chown root $JAR/org/vesalainen/util/1.8.1/util-1.8.1.jar
mkdir -p $JAR/org/vesalainen/test/1.0.0
cp $REPO/org/vesalainen/test/1.0.0/test-1.0.0.jar $JAR/org/vesalainen/test/1.0.0/test-1.0.0.jar
chmod 644 $JAR/org/vesalainen/test/1.0.0/test-1.0.0.jar
chown root $JAR/org/vesalainen/test/1.0.0/test-1.0.0.jar
mkdir -p $JAR/org/vesalainen/dev/1.0.1
cp $REPO/org/vesalainen/dev/1.0.1/dev-1.0.1.jar $JAR/org/vesalainen/dev/1.0.1/dev-1.0.1.jar
chmod 644 $JAR/org/vesalainen/dev/1.0.1/dev-1.0.1.jar
chown root $JAR/org/vesalainen/dev/1.0.1/dev-1.0.1.jar
mkdir -p $JAR/org/apache/maven/maven-model-builder/3.1.1
cp $REPO/org/apache/maven/maven-model-builder/3.1.1/maven-model-builder-3.1.1.jar $JAR/org/apache/maven/maven-model-builder/3.1.1/maven-model-builder-3.1.1.jar
chmod 644 $JAR/org/apache/maven/maven-model-builder/3.1.1/maven-model-builder-3.1.1.jar
chown root $JAR/org/apache/maven/maven-model-builder/3.1.1/maven-model-builder-3.1.1.jar
mkdir -p $JAR/commons-net/commons-net/3.3
cp $REPO/commons-net/commons-net/3.3/commons-net-3.3.jar $JAR/commons-net/commons-net/3.3/commons-net-3.3.jar
chmod 644 $JAR/commons-net/commons-net/3.3/commons-net-3.3.jar
chown root $JAR/commons-net/commons-net/3.3/commons-net-3.3.jar
mkdir -p $JAR/com/googlecode/efficient-java-matrix-library/equation/0.26
cp $REPO/com/googlecode/efficient-java-matrix-library/equation/0.26/equation-0.26.jar $JAR/com/googlecode/efficient-java-matrix-library/equation/0.26/equation-0.26.jar
chmod 644 $JAR/com/googlecode/efficient-java-matrix-library/equation/0.26/equation-0.26.jar
chown root $JAR/com/googlecode/efficient-java-matrix-library/equation/0.26/equation-0.26.jar
mkdir -p $JAR/log4j/log4j/1.2.17
cp $REPO/log4j/log4j/1.2.17/log4j-1.2.17.jar $JAR/log4j/log4j/1.2.17/log4j-1.2.17.jar
chmod 644 $JAR/log4j/log4j/1.2.17/log4j-1.2.17.jar
chown root $JAR/log4j/log4j/1.2.17/log4j-1.2.17.jar
mkdir -p $JAR/org/vesalainen/lpg/javalpg/1.8.1
cp $REPO/org/vesalainen/lpg/javalpg/1.8.1/javalpg-1.8.1.jar $JAR/org/vesalainen/lpg/javalpg/1.8.1/javalpg-1.8.1.jar
chmod 644 $JAR/org/vesalainen/lpg/javalpg/1.8.1/javalpg-1.8.1.jar
chown root $JAR/org/vesalainen/lpg/javalpg/1.8.1/javalpg-1.8.1.jar
mkdir -p $JAR/org/codehaus/plexus/plexus-utils/3.0.15
cp $REPO/org/codehaus/plexus/plexus-utils/3.0.15/plexus-utils-3.0.15.jar $JAR/org/codehaus/plexus/plexus-utils/3.0.15/plexus-utils-3.0.15.jar
chmod 644 $JAR/org/codehaus/plexus/plexus-utils/3.0.15/plexus-utils-3.0.15.jar
chown root $JAR/org/codehaus/plexus/plexus-utils/3.0.15/plexus-utils-3.0.15.jar
mkdir -p $JAR/org/codehaus/plexus/plexus-interpolation/1.19
cp $REPO/org/codehaus/plexus/plexus-interpolation/1.19/plexus-interpolation-1.19.jar $JAR/org/codehaus/plexus/plexus-interpolation/1.19/plexus-interpolation-1.19.jar
chmod 644 $JAR/org/codehaus/plexus/plexus-interpolation/1.19/plexus-interpolation-1.19.jar
chown root $JAR/org/codehaus/plexus/plexus-interpolation/1.19/plexus-interpolation-1.19.jar
mkdir -p $JAR/org/codehaus/plexus/plexus-component-annotations/1.5.5
cp $REPO/org/codehaus/plexus/plexus-component-annotations/1.5.5/plexus-component-annotations-1.5.5.jar $JAR/org/codehaus/plexus/plexus-component-annotations/1.5.5/plexus-component-annotations-1.5.5.jar
chmod 644 $JAR/org/codehaus/plexus/plexus-component-annotations/1.5.5/plexus-component-annotations-1.5.5.jar
chown root $JAR/org/codehaus/plexus/plexus-component-annotations/1.5.5/plexus-component-annotations-1.5.5.jar
mkdir -p $JAR/org/apache/maven/maven-model/3.1.1
cp $REPO/org/apache/maven/maven-model/3.1.1/maven-model-3.1.1.jar $JAR/org/apache/maven/maven-model/3.1.1/maven-model-3.1.1.jar
chmod 644 $JAR/org/apache/maven/maven-model/3.1.1/maven-model-3.1.1.jar
chown root $JAR/org/apache/maven/maven-model/3.1.1/maven-model-3.1.1.jar
mkdir -p $JAR/com/googlecode/efficient-java-matrix-library/core/0.26
cp $REPO/com/googlecode/efficient-java-matrix-library/core/0.26/core-0.26.jar $JAR/com/googlecode/efficient-java-matrix-library/core/0.26/core-0.26.jar
chmod 644 $JAR/com/googlecode/efficient-java-matrix-library/core/0.26/core-0.26.jar
chown root $JAR/com/googlecode/efficient-java-matrix-library/core/0.26/core-0.26.jar
mkdir -p $JAR/org/apache/ant/ant/1.9.4
cp $REPO/org/apache/ant/ant/1.9.4/ant-1.9.4.jar $JAR/org/apache/ant/ant/1.9.4/ant-1.9.4.jar
chmod 644 $JAR/org/apache/ant/ant/1.9.4/ant-1.9.4.jar
chown root $JAR/org/apache/ant/ant/1.9.4/ant-1.9.4.jar
mkdir -p $JAR/org/vesalainen/lpg/lpg/1.8.1
cp $REPO/org/vesalainen/lpg/lpg/1.8.1/lpg-1.8.1.jar $JAR/org/vesalainen/lpg/lpg/1.8.1/lpg-1.8.1.jar
chmod 644 $JAR/org/vesalainen/lpg/lpg/1.8.1/lpg-1.8.1.jar
chown root $JAR/org/vesalainen/lpg/lpg/1.8.1/lpg-1.8.1.jar
mkdir -p $JAR/org/apache/ant/ant-launcher/1.9.4
cp $REPO/org/apache/ant/ant-launcher/1.9.4/ant-launcher-1.9.4.jar $JAR/org/apache/ant/ant-launcher/1.9.4/ant-launcher-1.9.4.jar
chmod 644 $JAR/org/apache/ant/ant-launcher/1.9.4/ant-launcher-1.9.4.jar
chown root $JAR/org/apache/ant/ant-launcher/1.9.4/ant-launcher-1.9.4.jar
mkdir -p $JAR/org/vesalainen/bcc/bcc/1.1.5
cp $REPO/org/vesalainen/bcc/bcc/1.1.5/bcc-1.1.5.jar $JAR/org/vesalainen/bcc/bcc/1.1.5/bcc-1.1.5.jar
chmod 644 $JAR/org/vesalainen/bcc/bcc/1.1.5/bcc-1.1.5.jar
chown root $JAR/org/vesalainen/bcc/bcc/1.1.5/bcc-1.1.5.jar
