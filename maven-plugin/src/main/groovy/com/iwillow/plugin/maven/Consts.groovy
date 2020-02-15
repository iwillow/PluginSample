package com.iwillow.plugin.maven

class Consts {
    public static final String MAVEN_DEFAULT_GROUP = 'com.iwillow.plugin'

    public static final String MAVEN_REMOTE_URL_PREFIX = 'http://maven.iwillow.respository/nexus/content/repositories'

    public static final String MAVEN_REMOTE_URL_RELEASE = "${MAVEN_REMOTE_URL_PREFIX}/releases/"
    public static final String MAVEN_REMOTE_URL_SNAPSHOT = "${MAVEN_REMOTE_URL_PREFIX}/snapshots/"

    public static final String MAVEN_SNAPSHOT_USER = 'iwillow'
    public static final String MAVEN_SNAPSHOT_PASSWORD = 'iwillow'

    public static final String MAVEN_RELEASE_USER = 'iwillow'
    public static final String MAVEN_RELEASE_PASSWORD = 'iwillow'
}
