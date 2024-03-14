#!/bin/bash

# Required env variable: SPARK_VERSION

# Function to get all the references for a given webpage
get_all_refs() {
	curl -s "$1" | grep -o '<a href=['"'"'"][^"'"'"']*['"'"'"]' | sed -e 's/^<a href=["'"'"']//' -e 's/["'"'"']$//' -e 's/\///'
}

# Function to get stable versions of Spark
get_spark_stable_versions() {
	all_refs=$(get_all_refs "https://dlcdn.apache.org/spark/")
	echo -n "${all_refs}" | grep -o 'spark-[0-9]\+\.[0-9]\+\.[0-9]\+' | sed -e 's/spark-//' | sort -r
}

get_last_minors() {
	refs=$(get_spark_stable_versions)
	versions=()
	minors=$(echo -n "${refs}" | grep -o '[0-9]\+\.[0-9]\+' | uniq | head -n 2)
	# shellcheck disable=SC2068
	for minor in ${minors[@]}; do
		versions+=("$(echo -n "${refs}" | grep -o "${minor}\.[0-9]\+" | head -n 1)")
	done
	echo -n "${versions[@]}"
}

# Function to get the download URL for a given Spark version
spark_url() {
	stable_versions=$(get_spark_stable_versions)
	if echo -n "${stable_versions}" | grep -q "$1"; then
		echo "https://dlcdn.apache.org/spark"
	else
		echo "https://archive.apache.org/dist/spark"
	fi
}

# Main
spark_url "$1"
