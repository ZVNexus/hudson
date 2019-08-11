node {
    currentBuild.displayName = "$DEVICE"
    currentBuild.description = "Build ID: $BUILD_NUMBER"
    def BUILD_TREE = "$BUILD_HOME/DU"
      stage('Sync') {
          telegramSend("Syncing source...")
	    sh '''#!/bin/bash
		cd '''+BUILD_TREE+'''
		. venv/bin/activate
		repo init --depth=1 -u https://github.com/dirtyunicorns/android_manifest.git -b p9x --no-tags
		rm -rf .repo/local_manifests
	    repo sync -d -c --force-sync --no-tags --no-clone-bundle -j16
	    repo forall -vc "git reset --hard"
	    repo forall -vc "git checkout"
		'''
  }
  stage('Clean') {
		sh '''#!/bin/bash
		cd '''+BUILD_TREE+'''
		make clean
		make clobber
		'''
  }
  stage('Build') {
      telegramSend("Starting build for $DEVICE")
      telegramSend("Job url: $BUILD_URL")
		sh '''#!/bin/bash +e
		cd '''+BUILD_TREE+'''
		. venv/bin/activate
		. build/envsetup.sh
		ccache -M 100G
		export USE_CCACHE=1
		lunch du_$DEVICE-$BUILDTYPE
		set -e
		if [[ ! -z "${REPOPICK}" ]]; then repopick -f ${REPOPICK}; else echo "No Commits to pick!"; fi
		mka bacon
		'''
		telegramSend("Build complete!")
  }
  //  stage('Upload') {

  //              TODO: Set up file server.
  //              telegramSend("Uploading build of $DEVICE")
  //    	  sh '''#!/bin/bash
  //   	          set -e
  //		  echo "Deploying artifacts..."
  //		  rsync --progress -a --include "du_$DEVICE-*-$DU_BUILD_TYPE.zip" --exclude "*" $OUT_DIR_COMMON_BASE/DU/target/product/$DEVICE/ [USER]@[HOST]
  //		  '''
  //  		  telegramSend("Upload complete!")
  //  }
}
