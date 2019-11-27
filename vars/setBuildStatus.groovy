void call(String commitSha, String giturl, String gitcontext, String message, String state) {
  step([
      $class: "GitHubCommitStatusSetter",
      reposSource: [$class: "ManuallyEnteredRepositorySource", url: giturl],
      commitShaSource: [$class: "ManuallyEnteredShaSource", sha: commitSha],
      contextSource: [$class: "ManuallyEnteredCommitContextSource", context: gitcontext],
      errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
      statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]] ]
  ]);
}