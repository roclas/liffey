#!/usr/bin/env bash

#echo "basedir=$(pwd)"
echo "basedir=/home/carlos/Liferay/bundles/liferay7/liferay-portal-7.0-ce-a4/tomcat-7.0.62"
for i in $(ls functions)
do
funcname=$(echo $i | sed s/.gosh$//)
echo "$funcname = { gosh file://$(pwd)/functions/$i \$args}"
done
