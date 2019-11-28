def call(String branchName, String server, String imagename) {
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
                - ${server}
      containers:
      - name: build
        image: alpine
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
        image: ${imagename}
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