plugins {
    id("publishing-conventions")
    id("com.gradle.plugin-publish")
}

ext[com.gradle.publish.PublishTask.GRADLE_PUBLISH_KEY] = System.getenv("GRADLE_PORTAL_PUBLISH_KEY")
ext[com.gradle.publish.PublishTask.GRADLE_PUBLISH_SECRET] = System.getenv("GRADLE_PORTAL_PUBLISH_SECRET")

gradlePlugin {
    website = ProjectInfo.url
    vcsUrl = ProjectInfo.url
}
