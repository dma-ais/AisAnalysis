/**
 * Tool object
 * 
 * @param panel
 * 		Name of the panel to open.
 * @param img
 *		Name of the image in the /img folder
 * @returns 
 * 		A tool object
 */
function Tool(panel, img) {
	this.panel = panel;
	this.img = "img/" + img + ".png";

	this.toHTML = function (){

		var panelName = '"' + panel + '"'; 

		var html = "<div class='tool' onClick='openPanel(" + panelName + ")'>" +
						"<img src=" + this.img + ">" +
					"</div>";

		return html;

	}

}
