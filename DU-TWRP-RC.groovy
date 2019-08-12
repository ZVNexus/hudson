node {
    currentBuild.displayName = "$DEVICE"
    currentBuild.description = "Build ID: $BUILD_NUMBER"
    def BUILD_TREE = "$BUILD_HOME/DU-TWRP"
      stage('Sync') {
          telegramSend("Syncing source...")
	    sh '''#!/bin/bash
		cd '''+BUILD_TREE+'''
		. venv/bin/activate
		repo init --depth=1 -u https://github.com/zvnexus/du_manifest.git -b p9x-twrp --no-tags
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
		mka recoveryimage bootimage
		'''
		telegramSend("Build complete!")
  }
}
