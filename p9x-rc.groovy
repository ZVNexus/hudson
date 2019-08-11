node('master') {
  // In this array we'll place the jobs that we wish to run.
  def branches = [:];
  def devices = [];
  stage('Preparation') { // For display purposes.
    def url = "https://raw.githubusercontent.com/zvnexus/hudson/master/du-jenkins-devices".toURL();
    println url;
    devices = url.readLines();
  }
  stage('Starting Builds') {
    telegramSend 'Starting builds'
    for (int i = 0; i < devices.size(); i++) {
      println devices[i];
      def device = devices[i].split();
      println device;
      if (device.size() == 4 && !device[0].startsWith( '#' )) {
        def codename = device[0];
        def buildtype = device[1];
        def du_buildtype = device[2];
        def jobname = device[3];
        branches["${codename}"] = {
            build job: "${jobname}", parameters: [
                string(name:'DEVICE', value:"${codename}"),
                string(name:'BUILDTYPE', value: "${buildtype}"),
                string(name:'DU_BUILD_TYPE', value: "${du_buildtype}"),
                string(name:'REPOPICK', value: "")]
        }
      }
    }
  }
  stage('Building') {
    parallel branches
  }
}
