def ecsBuildConf = new groovy.json.JsonSlurper().parseText(streamFileFromWorkspace('EcsServices.json').getText())

ecsBuildConf.each { team , services ->
    def folderName = "java/${team}"

    
    folder(folderName) {
        displayName("${team} Jobs")
        description("folder for all ${team}s pipeline jobs")
    }


    services.each{ service ->

     def projectName =  service.service_name
     def jdk_type =  service.jdk_type
     def dockerfilePath =  service.dockerfilePath
     def labelName = service.node
     def jobName = "MasterPR_${projectName}_${jdk_type}_IMAGE"

        pipelineJob("${folderName}/${jobName}") {

            displayName(jobName)
            description("<h2>${jdk_type} Image Pipeline job for ${projectName} pull requests </h2>")

            parameters {
                stringParam('sha1', 'master', 'When starting build give the sha1 parameter commit id you want to build or refname (eg: origin/pr/9/head).')
            }

            logRotator(-1, 10, -1, 1)

            quietPeriod(1)

            jdk('(Default)')

            scm {
                git {
                    remote {
                        github("wkda/${projectName}", 'ssh')
                        refspec('+refs/pull/*:refs/remotes/origin/pr/*')
                        credentials('github-ssh-rw-key')
                        branch('${sha1}')
                    }
                    extensions {
                        wipeOutWorkspace()
                    }
                }
            }

            triggers {
                githubPullRequest {
                    triggerPhrase(".*test\\W+this\\W+please.*")
                    useGitHubHooks()
                    cron('')
                    permitAll()
                    extensions {
                        commitStatus {
                            context("${jobName}")
                            triggeredStatus("starting PR build of ${projectName}")
                            startedStatus('started PR build...')
                            completedStatus('SUCCESS', 'Click on Details to see changes')
                            completedStatus('FAILURE', "Something went wrong")
                            completedStatus('PENDING', 'Still in progress...')
                            completedStatus('ERROR', "Something went really wrong")
                        }
                    }
                }
            }


            environmentVariables {
                envs('projectName': projectName, 'labelName': labelName, 'dockerfilePath': dockerfilePath)
            }

            definition {
                cps {
                    sandbox()
                    script(readFileFromWorkspace('pr_pipeline.gvy'))
                }
            }

            publishers {
                githubCommitNotifier()
            }
        }

    }
}
