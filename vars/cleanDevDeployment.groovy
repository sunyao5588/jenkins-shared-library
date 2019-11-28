def call(String branchName) {
    podTemplate(yaml: """
    apiVersion: v1
    kind: Pod
    metadata:
        labels:
        some-label: cleanup
    spec:
        affinity:
        nodeAffinity:
            requiredDuringSchedulingIgnoredDuringExecution: 
            nodeSelectorTerms:
            - matchExpressions:
                - key: kubernetes.io/hostname
                operator: In 
                values:
                - hkappdlv041
        containers:
        - name: build
        image: hkappdlv006.asia.pwcinternal.com:443/cidr/cidr-build:v6.0.0
        resources:
            requests:
            memory: "50Mi"
            cpu: "50m"
            limits:
            memory: "4000Mi"
            cpu: "2000m"
        command:
        - cat
        tty: true
        volumeMounts:
            - mountPath: /var/run/docker.sock
            name: docker-cli
        - name: deploy
        image: hkappdlv006.asia.pwcinternal.com:443/middleware/operate-tools:kubectl-cidr-dev
        resources:
            requests:
            memory: "50Mi"
            cpu: "50m"
            limits:
            memory: "1000Mi"
            cpu: "1000m"
        command:
        - cat
        tty: true
        volumeMounts:
            - mountPath: /var/run/docker.sock
            name: docker-cli
            - mountPath: /app/repo/cidr-src
            name: source-code-repo
        volumes:
        - hostPath:
            path: /var/run/docker.sock
            type: ""
            name: docker-cli
        - name: source-code-repo
            emptyDir: {}
    """
    ) {
        node(POD_LABEL) {
            container('build') {
                sh 'echo cleanup docker images we build'
                sh 'docker image prune -af'
            }
            container('deploy') {
                sh 'echo cleanup dev app and DB we deploy'
                sh """
                mysql-BRANCHNAME-pevc-svc
                kubectl delete deploy/app-${branchName}-cidr -n cidr-dev || true 
                kubectl delete deploy/mysql-${branchName}-cidr -n cidr-dev || true 
                kubectl delete deploy/app-${branchName}-pevc -n pevc-dev || true  
                kubectl delete deploy/mysql-${branchName}-pevc -n pevc-dev || true  
                kubectl delete svc/app-${branchName}-cidr-svc -n cidr-dev || true  
                kubectl delete svc/mysql-${branchName}-cidr-svc -n cidr-dev || true  
                kubectl delete svc/app-${branchName}-pevc-svc -n pevc-dev || true  
                kubectl delete svc/mysql-${branchName}-pevc-svc -n pevc-dev || true  
                """
            }
            
        }
    }
}