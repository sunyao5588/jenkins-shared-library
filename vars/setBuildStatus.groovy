// def getRepoURL() {
//   sh "git config --get remote.origin.url > .git/remote-url"
//   return readFile(".git/remote-url").trim()
// }

def getCommitSha() {
  sh "git rev-parse HEAD > .git/current-commit"
  return readFile(".git/current-commit").trim()
}

void call(String repoUrl, String gitcontext, String message, String state) {
  //repoUrl = getRepoURL()
  commitSha = getCommitSha()
  step([
      $class: "GitHubCommitStatusSetter",
      reposSource: [$class: "ManuallyEnteredRepositorySource", url: repoUrl],
      commitShaSource: [$class: "ManuallyEnteredShaSource", sha: commitSha],
      contextSource: [$class: "ManuallyEnteredCommitContextSource", context: gitcontext],
      errorHandlers: [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
      statusResultSource: [ $class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]] ]
  ]);
}