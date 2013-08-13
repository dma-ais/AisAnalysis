/*
 * JQuery plugin for making elements expandable
 */
(function($){

	//override html function for expandable elements
//	var _oldHtml = $.fn.html;
//	$.fn.html = function(options){
////		this.filter(".expandable").each(function(){
////			console.log(this);
////			var container = $(this).children('.panelContainer');
////            return _oldHtml.call(container, options);
////        });
//		return _oldHtml.call($(this), options);
//        
////        this.each(function(){
////        	var element = $(this);
////            if ($.data(this, 'expandable')) {  
////                var container = element.children('.panelContainer');
////                return _oldHtml.apply(container, options);
////            }else{
//////            	console.log("no expandable");
////            	return _oldHtml.apply(element, options);
////            }
////        });
//    };
    
	//override slide up function for expandable elements
	var _oldSlideUp = $.fn.slideUp;
	$.fn.slideUp = function(options){
        this.each(function(){
        	var element = $(this);
            if ($.data(this, 'expandable')) {
                var container = element.children('.panelContainer');
                element.removeClass( "arrowUp" );
                element.addClass( "arrowDown" );
                
                var defaults = {
                    complete:function(){
                    	element.children('hr').remove();
        			}
                }
                var options =  $.extend(defaults, options);   

                _oldSlideUp.call(container, options);
            }else{
            	_oldSlideUp.call(element, options);
            }
        });
    };
    
  //override slide down function for expandable elements
	var _oldSlideDown = $.fn.slideDown;
	$.fn.slideDown = function(options){
        this.each(function(){
        	var element = $(this);
            if ($.data(this, 'expandable')) {
            	var container = element.children('.panelContainer');
            	if(container.html() == ""){
            		return;
            	}
            	if(container.css('display') != 'none'){
            		return;
            	}
            	element.addClass( "arrowUp" );
            	element.removeClass( "arrowDown" );
            	container.before(" <hr>");
            	_oldSlideDown.call(container,options);
            }else{
            	_oldSlideDown.call(element,options);
            }
        });
    };
    
    $.fn.extend({ 
        
    	
    	
        //pass the options variable to the function
        expandable: function(options) {

            //Set the default values, use comma to separate the settings, example:
            var defaults = {
            	header : "Default Header",
            	maxHeight: "250px"
            }
                 
            var options =  $.extend(defaults, options);
 
            //iterate over each matched element
            return this.each(function() {
            	$.data(this, 'expandable', true);
                var o = options;
                var panel = $(this);
                var defaultHTML = panel.html();
                panel.html("");
//                _oldHtml.call(panel,"");

                //add header and container
        		panel.addClass( "arrowDown" );
        		panel.append('<div class="panelHeader">'+o.header+'</div>');
        		panel.append('<div class="panelContainer" style="max-height: '+o.maxHeight+'">'+defaultHTML+'</div>');
        		var container = panel.children('.panelContainer');
        		var header = panel.children('.panelHeader');
        		container.css('display', 'none');
        		
        		//adding click listener to panel header
        		panel.children('.panelHeader').click(function(e) {
        			if(container.css('display') == 'none'){
        				panel.slideDown();
        			}else{
        				panel.slideUp();
        			}
        			return false;
        		});
            });
        }
    });
     
})(jQuery);
