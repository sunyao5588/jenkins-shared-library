def call(JOB_NAME, BUILD_NUMBER) {
    def jobName = JOB_NAME
    def buildNumber = BUILD_NUMBER.toInteger()
    /* Get job name */
    def currentJob = Jenkins.instance.getItemByFullName(jobName)

    /* Iterating over the builds for specific job */
    for ( build in currentJob.builds) {
        /* If there is a build that is currently running and it's not current build */
        if (build.isBuilding() && build.getNumber().toInteger() != buildNumber) {
            /* Than stopping it */
            build.doStop()
        }
    }
}
