#! /bin/bash

case $1 in
"start"){
                echo " --------启动 $i 远程提交器-------"
                nohup java -jar spark-rest-submitter-0.0.3-SNAPSHOT.jar -conf ./application.properties >submitter.log 2>&1 &
};;
"stop"){
                echo " --------停止 $i 远程提交器-------"
                ps -ef | grep rest-submitter| grep -v grep |awk  '{print $2}' | xargs -n1 kill -9 

};;
esac

