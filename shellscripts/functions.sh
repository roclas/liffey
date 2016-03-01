#!/usr/bin/env bash

#API guide: http://localhost:8080/api/jsonws/

liferayuser="test"
liferayemail="test@liferay.com"
liferaypass="test"
hostname="localhost"
port=8080
#companyId=20154
#companyId=$(liffey-get-company | get-fields-from-json companyId)
baseurl="http://$hostname:$port/api/jsonws"

function get-fields-from-json {
	a=$(echo "$@" | awk '{for(i=0;i++<NF;){printf("%s ","%s")}}'| sed s/.$//) 
	b=$(echo "$@" | awk '{for(i=0;i++<NF;){printf("obj[\"%s\"],",$i)}}'| sed s/.$//) 
	python -c 'import json,sys;obj=json.load(sys.stdin);print "'$a'"%('$b')'
}

function get-fields-from-json-array {
	a=$(echo "$@" | awk '{for(i=0;i++<NF;){printf("%s ","%s")}}'| sed s/.$//) 
	b=$(echo "$@" | awk '{for(i=0;i++<NF;){printf("obj[i][\"%s\"],",$i)}}'| sed s/.$//) 
	python -c 'import json,sys;obj=json.load(sys.stdin) 
for i in range(len(obj)): print "'$a'"%('$b')' 
}


function put-in-one-line { echo $@ } 

function liffey-get-company-by-virtual-host {
curl $baseurl/company/get-company-by-virtual-host \
  -u $liferayemail:$liferaypass \
  -d virtualHost="$1"
}

function liffey-get-company {
  liffey-get-company-by-virtual-host $hostname
}

companyId=$(liffey-get-company | get-fields-from-json companyId)

function liffey-get-user-by-email-address {
curl $baseurl/user/get-user-by-email-address \
  -u $liferayemail:$liferaypass \
  -d companyId=$companyId -d emailAddress="$1"
}

function liffey-add-user {
curl $baseurl/user/add-user \
  -u $liferayemail:$liferaypass \
  -d companyId=$companyId \
  -d autoPassword=false \
  -d password1='test' \
  -d password2='test' \
  -d autoScreenName=false \
  -d screenName="$1" \
  -d emailAddress="$2" \
  -d facebookId=1 \
  -d openId='' \
  -d locale=en \
  -d firstName="$1" \
  -d middleName='automatic' \
  -d lastName='automatic' \
  -d prefixId=0 \
  -d suffixId=0 \
  -d male=false \
  -d birthdayMonth=1 \
  -d birthdayDay=15 \
  -d birthdayYear=1999 \
  -d jobTitle='rock star' \
  -d groupIds= \
  -d organizationIds= \
  -d roleIds= \
  -d userGroupIds= \
  -d sendEmail=false
}

function liffey-get-organization-id {
 curl $baseurl/organization/get-organization-id \
  -u test@liferay.com:test \
  -d companyId=$companyId\
  -d name=$1
}

function liffey-get-organizations {
curl $baseurl/organization/get-organizations \
  -u $liferayemail:$liferaypass \
  -d companyId=$companyId \
  -d parentOrganizationId="$1" \
  -d start=0 \
  -d end=1000000000
}

function liffey-get-organization-tree-ids {
 parent=$1
 ${1+"false"} && parent="0"
 for i in $(liffey-get-organizations $parent| python -c 'import json,sys;obj=json.load(sys.stdin) 
for i in range(len(obj)): print obj[i]["organizationId"]');do
	echo $i
	liffey-get-organization-tree-ids $i 2>/dev/null
 done
}




function liffey-add-organization {
parent=$2
${2+"false"} && parent="0"
echo "creating organization $1 (parent=$parent)"
curl $baseurl/organization/add-organization \
  -u $liferayemail:$liferaypass \
  -d parentOrganizationId=$parent \
  -d name="$1" \
  -d type='regular-organization' \
  -d recursable=false \
  -d regionId=0 \
  -d countryId=0 \
  -d statusId=12017 \
  -d comments='' \
  -d site=false 2>/dev/null
}

function liffey-get-organization-id-from-name {
	liffey-get-organizations $2| 
	python -c 'import json,sys;obj=json.load(sys.stdin)
for i in range(len(obj)): print "%s %s"%(obj[i]["organizationId"],obj[i]["name"])' | grep "\<$1\>" | awk '{print $1}'
}


##given an input of "organization1/organization2/organization3"
##it creates the whole structure
function liffey-add-organization-simple-tree {
	first=$(echo $1 | sed 's@/.*@@')
	liffey-add-organization $first 0 2>/dev/null
	parentOrganizationId=$(liffey-get-organization-id-from-name $first 0)
	echo $1 | awk -F\/ '{for(i=1;i++<NF;){printf("%s %s\n",$i,$(i-1))}}' |
	while read i; do 
		currentName=$(echo $i| awk '{print $1}')
		liffey-add-organization $currentName $parentOrganizationId 2>/dev/null
		previous=$parentOrganizationId
		parentOrganizationId=$(liffey-get-organization-id-from-name $currentName $previous )
		echo "add organization $i" 
	done
	
}

#usage example:
#
#liffey-add-organization-tree-with-unique-names orgDelete/org{1..5}/org{1..5}
function liffey-add-organization-tree-with-unique-names {
	for i in $@;do
		mypath=$(echo "$i" | awk -F\/ '{for(i=0;i++<NF;){t=t"_"$i;printf("%s/",t)}}')
		liffey-add-organization-simple-tree  $mypath
	done
}

function liffey-get-organization-user-ids {
	uids=$(curl $baseurl/user/get-organization-user-ids -u $liferayemail:$liferaypass -d organizationId=$1)
	echo $uids | python -c 'import json,sys;obj=json.load(sys.stdin)
for i in range(len(obj)): print obj[i]'
}

function liffey-unset-organization-users {
 uids=$(echo $(liffey-get-organization-user-ids $1)|sed 's/ /,/g' )
 if [ -z $uids ]; then
  echo "no users to unset"
 else
  echo "org $1 unsetting users |$uids|"
  curl $baseurl/user/unset-organization-users -u $liferayemail:$liferaypass -d organizationId=$1 -d userIds=$uids
 fi
}

function liffey-delete-organization {
 echo "deleting organization $1"
 curl $baseurl/organization/delete-organization -u $liferayemail:$liferaypass -d organizationId=$1 
}

#
#You cannot delete organizations that have suborganizations or users. 
#this function deletes an organization in cascade, unsetting users from its organizations
#
function liffey-delete-organization-tree-by-id {
 echo "organization $1: $(liffey-unset-organization-users $1)"
 for o in $( liffey-get-organization-tree-ids $1 2>/dev/null);do
	liffey-delete-organization-tree-by-id $o
 done
 liffey-delete-organization $1
}

function liffey-delete-organization-tree-by-name {
 echo "deleting organization $1:"
 liffey-delete-organization-tree-by-id $(liffey-get-organization-id $1 2>/dev/null)
}

function liffey-list-local-dl-folders {
	parent=$1
	${1+"false"} && parent="0"
	idName=$(curl $baseurl/dlapp/get-folders -u $liferayemail:$liferaypass -d repositoryId=20181 -d parentFolderId=$parent -d includeMountFolders=true | get-fields-from-json-array folderId name 2>/dev/null)
	echo $idName | while read i; do 
		id=$(echo $i|awk '{print $1}')
		name=$(echo $i|awk '{print "'$2'"$2}')
 		if [ -z "$id" ]; then
			return
		else
			echo "$2 $name"
			liffey-list-local-dl-folders $id "$2\t" 2>/dev/null
		fi
	done
}
