rootProject.name = "gmscore-patches"

buildCache {
    local {
        isEnabled = "CI" !in System.getenv()
    }
}
