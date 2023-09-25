plugins {
    `kotlin-conventions`
    `library-publishing-conventions`
    alias(libs.plugins.protokt)
}

dependencies {
    protoktExtensions(libs.protokt.extensions)
}
