def call(String typestr){
    if (type == "test") {
        pipeline{
            agent {
                kubernetes {
                defaultContainer 'jnlp'
                yamlFile 'KubernetesPod-Smoke.yaml'
                }
            }
            triggers {       
                cron(env.BRANCH_NAME == 'master' ? '10 20 * * *' : '') // daily at 20:10 pm  --- QA  
            }
            environment {
                harboraccount = credentials('harboraccount')
                harborpasswd = credentials('harborpasswd')
                AES_SALT = credentials('cidr-aes-salt')
                AES_PASSWORD = credentials('cidr-aes-passwd')
                githubaccount = credentials('githubaccount')
                githubtoken = credentials('githubtoken')
            }
            options {
                //skipDefaultCheckout(true)
                //Specifying a global execution timeout of one hour, after which Jenkins will abort the Pipeline
                timeout(time: 40, unit: 'MINUTES')
                parallelsAlwaysFailFast()
                ansiColor('xterm')
            }
            parameters {
                string(name: 'FOLDERNAME', defaultValue: '/app/repo/cidr-src', description: 'Source code folder name')
                string(name: 'WORKSPACE', defaultValue: '/app/repo/cidr-src/ds-cidr', description: 'Source code workspace')
            }
            stages{
                stage('Python Unit Test'){
                    when {
                        not {
                            branch 'master'
                        }
                    }
                    steps{
                        echo "python test"
                        /*
                        container('build'){
                            sh(label: 'Python Unit Test Starts', script: """
                                echo -e "\033[32m Python Unit Test starting... \033[0m"
                            """)

                            /* Seperate test steps so easy to analyse test time distribution - Jason */
                            sh(label: 'OCR Processor Unit Test', script: """
                                export OMP_THREAD_LIMIT=1 && export TESSDATA_PREFIX="/models/tesseract/ver_fast" && pytest --durations=1 processor/creditreview/ocr_processor/unit_test 
                            """)
                            sh(label: 'Extraction Processor Unit Test', script: """
                                export OMP_THREAD_LIMIT=1 && export NLP_MODEL_PATH="/models/nlp_model" && pytest --durations=1 processor/creditreview/extraction_processor/unit_test 
                            """)
                            sh(label: 'Document Maker Processor Unit Test', script: """
                                export OMP_THREAD_LIMIT=1 && export NLP_MODEL_PATH="/models/nlp_model" && pytest --durations=1 processor/creditreview/document_maker_processor/unit_test
                            """)
                            sh(label: 'PDF Maker Processor Unit Test', script: """
                                export OMP_THREAD_LIMIT=1 && pytest --durations=1 processor/creditreview/pdf_maker_processor/unit_test
                            """)
                            sh(label: 'Page Maker Processor Unit Test', script: """
                                export OMP_THREAD_LIMIT=1 && pytest --durations=1 processor/creditreview/page_maker_processor/unit_test
                            """)
                            sh(label: 'Keyword Search Processor Unit Test', script: """
                                export OMP_THREAD_LIMIT=1 && pytest --durations=1 processor/creditreview/keyword_search_processor/unit_test
                            """)
                        }
                      
                    }
                }
                stage('Smoke Test'){
                    when {
                        not {
                            branch 'master'
                        }
                    }
                    steps{
                        echo "smoke test"
                        
                        container('build'){
                            sh(label: 'Core Smoke Test', script: """
                                cd engine && export DoraJobs=2 && export OMP_THREAD_LIMIT=1 && TESSDATA_PREFIX="/models/tesseract/ver_fast"  NLP_MODEL_PATH="/models/nlp_model" sbt clean core/test
                            """)
                            sh(label: 'Web Smoke Test', script: """                      
                                cd engine && export DoraJobs=2 && export OMP_THREAD_LIMIT=1 && TESSDATA_PREFIX="/models/tesseract/ver_fast"  NLP_MODEL_PATH="/models/nlp_model" sbt web/test
                            """)
                        }
                        
                    }  
                }       
            }
            
            post {
                always {           
                    echo 'One way or another, I have finished'      
                }
                failure {
                    echo 'Test Failed'  
                    setBuildStatus("https://github.com/pwccnhk/ds-cidr", "smoke-test/jenkins/build-status", "Build failed", "FAILURE");            
                }
                success {
                    echo 'Test Succeed'               
                    setBuildStatus("https://github.com/pwccnhk/ds-cidr", "smoke-test/jenkins/build-status", "Build succeeded", "SUCCESS");                   
                }
            } 
        }
    }
    else if (type == "CI") {
        sh 'echo "this for CI"'
    }
    else {
        sh 'echo nothing here'
    }
}
