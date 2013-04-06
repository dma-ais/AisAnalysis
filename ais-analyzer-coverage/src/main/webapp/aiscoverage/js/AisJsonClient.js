function AisJsonClient (serverurl) {
	var self = this;
    this.serverurl = serverurl || "http://localhost:8090";
    this.sources = {};
    this.loading = false;
    this.jsoncoveragerequest = null;
    
    this.addListener = function(listener){
    	alert(listener);
    }

    this.getSources = function(callback){
    	$.post('/json/coverage', { action: "getSources"}, function(data) {
    		this.sources = data;
    		$.each(this.sources, function(key, val) {
    			val.enabled=true;
    			val.selected=false;
    		});
    		callback(this.sources);
    	});
    }
    
    this.getCoverage = function(dataToBeSent, screenarea, multifactor, callback){
    	//aborting any previous requests
    	if(self.jsoncoveragerequest != null){
    		self.jsoncoveragerequest.abort();
    	}
    	self.jsoncoveragerequest = $.post('/json/coverage', { action: "getCoverage", sources: dataToBeSent, area: screenarea, multiplicationFactor: multifactor }, function(data) {
    		callback(data);
    	}); 
    }
 
}