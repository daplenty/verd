package scripts

import java.util.regex.Pattern

node("${env.labelName}") {

    def projectName = "${env.projectName}"
    def targetBranch = "${env.ghprbTargetBranch}"

    def verifyStepLog = 'verify.txt'
    def slowTestSlackUsername = '#slow-test-alerts'
    def contextsRaisedToSendWarningMessage = 2
    def contextsRaisedToSendDangerMessage = 5

    def repositoryUrl = "https://github.com/wkda/$projectName"

    def getPullRequestInfo = { ->
        def sha1 = params.sha1

        if (sha1?.trim()) {
            def prNumber = sh (returnStdout: true, script: "echo $sha1 | sed 's/[^0-9]*//g'")
            echo "prNumber is $prNumber"
            if (prNumber?.trim()) {
                def prUrl = "$repositoryUrl/pull/$prNumber"
                return  "Find PR here: $prUrl"
            }
        }

        return ""
    }

  
    stage('Checkout') {
        checkout([
                $class                           : 'GitSCM',
                branches                         : [[name: "${params.sha1}"]],
                browser                          : [$class: 'GithubWeb', repoUrl: repositoryUrl],
                doGenerateSubmoduleConfigurations: false,
                userRemoteConfigs                : [[
                                                            credentialsId: 'github-ssh-rw-key',
                                                            refspec      : '+refs/pull/*:refs/remotes/origin/pr/*',
                                                            url          : 'git@github.com:wkda/${projectName}.git'
                                                    ]]
        ])
    }

    

    stage('Builds') {
        
           
        }
    }
}

@NonCPS
def static isInvalidCommitMessage(String message) {
    Pattern pattern = Pattern.compile('^([a-zA-Z][a-zA-Z]+-[1-9][0-9]*)|^SPIKE|^Revert')
    return !pattern.matcher(message).find()
}

