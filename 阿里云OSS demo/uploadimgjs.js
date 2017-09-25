function uploadfiles(b){
	var fileboxLength = $("#"+b).parent().parent().find(".imgbox").length;
	if(fileboxLength>0){
		toastr.remove();
		toastr.error('最多只能上传1个附件！');
		return;
	}else {
		$('#'+b).click();
	}
}
function imgprogressTxt(a){
	setTimeout(function(){
		$("#progress"+a).hide();
	},300)
}

function deleteFile(attachId){
	if(attachId!=null&&attachId!=""){
		$.post("/fileManager/loadData/delete?id="+attachId).done(function(data) { 
    		if(typeof(callback)!="undefined"){
//    			callback(attachId);
    		}
    	});
	}
}

function fileDelete(a,d){
	$(".filesDelete"+a).click(function(){
		var fileAttachments = $("#"+d).val();
		var fileId = $(this).attr("fileId");
    	if(fileId!=null&&fileId!=""){
    		var fileIds1 = fileAttachments.split(",");
        	$("#"+d).val("");
        	fileAttachments = $("#"+d).val();
        	for(var i=0;i<fileIds1.length;i++){
        		if(fileIds1[i]!=fileId){
        			if(fileAttachments!=""){
        				fileAttachments+=","+fileIds1[i];
    	    		  	$("#"+d).val(fileAttachments);
    	    		 }else{
    	    			 fileAttachments = fileIds1[i];
    	    			$("#"+d).val(fileAttachments);
    	    		 }
        		}
        	}
        	deleteFile(fileId);
    	}
		
		$(this).parent().remove();
		/*spanHeight();*/
    	
	})
}
function imgDelete(a,d){
	$(".deletImg"+a).click(function(){
		var imageAttachments = $("#"+d).val();
		var imgId = $(this).next("img").attr("imgId");
    	if(imgId!=null&&imgId!=""){
    		var imgIds1 = imageAttachments.split(",");
    		$("#"+d).val("");
        	imageAttachments = $("#"+d).val();
        	for(var i=0;i<imgIds1.length;i++){
        		if(imgIds1[i]!=imgId){
        			if(imageAttachments!=""){
        				imageAttachments+=","+imgIds1[i];
        				$("#"+d).val(imageAttachments);
    	    		 }else{
    	    			imageAttachments = imgIds1[i];
    	    			$("#"+d).val(imageAttachments);
    	    		 }
        		}
        	}
        	deleteFile(imgId);
    	}
    	$(this).parent().parent().find("div.uploadimg-button").show();
		$(this).parent().remove();
		/*var imgboxLength = $(this).parent().parent().find(".imgbox").length;
    	if(imgboxLength==0){
    		$(this).parent().parent().find("div.uploadimg-button").show();
    	}else {
    		$(this).parent().parent().find("div.uploadimg-button").hide();
    	}*/
	})
}
function filesUpload(a,b,c,d){
	var input = document.getElementById(a);
    var result,div,acunt;
    var fileArr=[];
    var pecent = 50, loop = null, ReT = null;
	if(c=="save"){
		acunt=0;
	}else if(c=="update") {
		var filesLength = $(".filebox").length;
		acunt=filesLength+1;
	}
    if(typeof FileReader==='undefined'){
    	toastr.remove();
	   toastr.error('<font style="color:#fff">抱歉，你的浏览器不支持 FileReader</font>');
      /*result.innerHTML = "抱歉，你的浏览器不支持 FileReader";*/
      input.setAttribute('disabled','disabled');
    }else{
      input.addEventListener('change',readFiles,false);
    }　　　　　
    function readFiles(){
    	for(var i=0;i<this.files.length;i++){
	        if (input['value'].match(/.doc|.docx|.ppt|.pptx|.xlsx|.xls|.pdf/i)){
	        	
	    		var filename = this.files[0].name;
	    		var fileSize = 0;
	    		var isIE = /msie/i.test(navigator.userAgent) && !window.opera;            
    		    if (isIE && !this.files) {          
    		         var filePath = this.value;            
    		         var fileSystem = new ActiveXObject("Scripting.FileSystemObject");   
    		         var file = fileSystem.GetFile (filePath);               
    		         fileSize = file.Size;         
    		    }else {  
    		         fileSize = this.files[0].size;     
    		    } 
    		    fileSize=Math.round(fileSize/1024*100)/100; //单位为KB
    		    if(fileSize>=2048){
    		    	toastr.remove();
 		    		toastr.error("单个附件已超出2M，请重新上传!");
    		        return;
    		    }
    		    
	    		if(fileArr.length>0){
	    			var x=false;
	    			for(var j=0;j<fileArr.length;j++){
	    				if(fileArr[j]==filename){
	    					toastr.remove();
	    					toastr.error('此附件已上传<br />请重新选择!');
	    					x=true;
	    					break;
	    				}
	    			}
	    			if(!x){
	    				fileArr.push(filename);
	    			}else{
	    				return;
	    			}
	    		}else {
	    			fileArr.push(filename);
	    		}
	    		var reader = new FileReader();
	    		reader.readAsDataURL(this.files[i]);
	    		reader.onload = function(e){
	    			result = '<a class="fileslist" href="'+this.result+'">'+filename+'</a><a class="filesDelete filesDelete'+a+'" href="javascript:;" id="file'+a+acunt+'" fileId="">删除</a><div id="fileprogress'+a+acunt+'" class="progress-bar"></div>';
	    			div = document.createElement('div');
	    			div.className="dis_in_b filebox";
	    			div.innerHTML = result;
	    			$("#"+b+" .uploadfile-button").before(div);
	    			var fileboxLength = $("#"+b+" .filebox").length;
	    			if(fileboxLength>=8){
	    				$("#"+b+" .filetypeTip").hide();
	    			}else {
	    				$("#"+b+" .filetypeTip").show();
	    			}
	    			var fileSer = $("#file"+a+acunt);
	    			/*删除附件*/
	    			fileDelete(a,d);
	    			/*高度*/
	    			/*spanHeight();*/
	    			
	    			/* 文件上传 */
	    			var submitData={
	    					image:this.result,
	    					name:filename,
	    					fileLength:this.result.length
	    			}; 
	    			/*上传*/
	    			$.ajax({
	    				type: "POST",
	    				url:"/wechat/upload/uploadFile",//"/uploadFile",
	    				data: submitData,
	    				dataType:"json",
	    				beforeSend: function(XMLHttpRequest){
//		      	    	  demo_report(submitData.name, submitData.image, submitData.fileLength * 0.6,count);
	    				},
	    				success: function(data){
	    					/* 文件 */
	    					var fileAttachment = $("#"+d).val();
	    					
	    					if(fileAttachment!=""){
	    						fileAttachment+=","+data["fileId"];
	    						$("#"+d).val(fileAttachment);
	    					}else{
	    						$("#"+d).val(data["fileId"]);
	    					}
	    					fileSer.attr("fileId",data["fileId"]);
	    					/*删除附件*/
	    	    			fileDelete(a,d);
	    				}, 
	    				complete :function(XMLHttpRequest, textStatus){
	    					
	    					if (XMLHttpRequest.readyState === 4 && XMLHttpRequest.status === 200) {
	    						var text = "100%";
	    						//当收到该消息时上传完毕
	    						mockProgress(text,acunt,a);
	    					}
	    					//数据后50%用模拟进度
	    					function mockProgress(a,b,c){
	    						/*if(loop){
								$("#fileprogress"+b).remove();
							};*/
	    						loop = setInterval(function() {
	    							pecent++;
	    							$("#fileprogress"+c+b).css('width', pecent + "%");
	    							if (pecent >= 99) {
	    								$("#fileprogress"+c+b).animate({'width': "100%"}, pecent < 95 ? 200 : 0, function() {
	    									if(a=="100%"){
	    										ReT = setTimeout(function(){
	    											$("#fileprogress"+c+b).remove();
	    										},1000)
	    									}
	    								});
	    								clearInterval(loop);
	    							}
	    						}, 100)
	    					}
	    				},
	    				error:function(XMLHttpRequest, textStatus, errorThrown){ //上传失败 
	    					$("#fileprogress"+a+acunt).animate({'width':"100%"}).css({"background":"#ff3b30"});
	    				}
	    			});
	    		}
	        }else {
	        	toastr.remove();
	    		toastr.error('上传的文件格式不正确<br />请重新选择!');
	    		return;
	        }
	        
    	}
    	acunt++;
    }
}

function imgsUpload(a,c){
	var b = a + "show",d = a + "Attachment",
		input = document.getElementById(a),
		result,div,acunt,imgArr = [],
		pecent = 50, loop = null, ReT = null;
	if(c=="save"){
		acunt=0;
	}else if(c=="update") {
		var filesLength = $(".imgbox").length;
		acunt=filesLength+1;
	}
    if(typeof FileReader==='undefined'){
    	/*toastr.remove();
	   toastr.error('<font style="color:#fff">抱歉，你的浏览器不支持 FileReader</font>');*/
      /*result.innerHTML = "抱歉，你的浏览器不支持 FileReader";*/
      /*input.setAttribute('disabled','disabled');*/
    	input.addEventListener('change',ieTips,false);
    }else{
      input.addEventListener('change',readFiles,false);
    }　　　　　
    function ieTips(){
    	$("#"+a+"before").parent().find("p").remove();
    	$("#"+a+"before").after("<p style='color:#bd362f;line-height:45px;padding-left:10px;'>&nbsp;&nbsp;不支持在IE浏览器下上传图片，如需上传图片，建议您使用谷歌内核的浏览器！</p>");
    }
    function isIEupload(){
    	if (input['value'].match(/.jpg|.gif|.png|.bmp/i)){
    		var imgNameArr = this.value.split("\\");
        	var imgName = imgNameArr[(imgNameArr.length)-1];
        	var fileSize = 0;	
    		var isIE = /msie/i.test(navigator.userAgent) && !window.opera;            
		    if (isIE && !this.files) {          
		         var filePath = this.value;            
		         var fileSystem = new ActiveXObject("Scripting.FileSystemObject");   
		         var file = fileSystem.GetFile (filePath);               
		         fileSize = file.Size;    
		    }else {  
		         fileSize = this.files[0].size;     
		    } 
		    fileSize=Math.round(fileSize/1024*100)/100; //单位为KB
		    if(fileSize>=2048){
		    	toastr.remove();
		    		toastr.error("单个附件已超出2M，请重新上传!");
		        return;
		    }
		    
		    result = '<a href="javascript:;" class="deletImg deletImg'+a+'"></a>'+
		    '<img id="img'+a+acunt+'" src="/static/pc/img/forIE.gif" style="width:50px;height:50px;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + this.value + '\')" onerror="ifNotImg(this);" imgId="" alt=""/><span class="hidden">'+
		    '<img src="/static/pc/img/forIE.gif" style="width:500px;filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + this.value + '\');" onerror="ifNotImg(this);" /></span><div id="imgprogress'+a+acunt+'" class="progressText" style="display:block;">等待上传</div>';
        	div = document.createElement('div');
        	div.className="dis_in_b imgbox";
        	div.innerHTML = result;
        	$("#"+a+"before").before(div);
        	/*进度显示*/
        	imgprogressTxt(a+acunt);
        	/*高度*/
			/*删除图片*/
    		imgDelete(a,d);
        	/*查看图片大图*/
        	bigimgshow("img"+a+acunt);
        	var imgSer = $("#img"+a+acunt);
	        	
		    var submitData={
				image:this.value,
				name:imgName,
				fileLength:fileSize
			}; 
        	/*上传*/
        	$.ajax({
        	      type: "POST",
        	      url:"/wechat/upload/uploadFile",//"/uploadFile",
        	      data: submitData,
        	      dataType:"json",
        	      beforeSend: function(XMLHttpRequest){
// 	        	     	    	  demo_report(submitData.name, submitData.image, submitData.fileLength * 0.6,count);
        	    },
        	      success: function(data){
        	    	 /*图片*/
        	    	 var imageAttachment = $("#"+d).val();
        		    	imgSer.attr("imgId",data["fileId"]);
        			 if(imageAttachment!=""){
        				 imageAttachment+=","+data["fileId"];
        				$("#"+d).val(imageAttachment);
        			 }else{
        				$("#"+d).val(data["fileId"]);
        			 }
        			 /*删除图片*/
        			imgDelete(a,d);
        	         
        	      }, 
        	 	  complete :function(XMLHttpRequest, textStatus){

        	 		  	if (XMLHttpRequest.readyState === 4 && XMLHttpRequest.status === 200) {
        	 		  		var text = "100%";
        	 		  		//当收到该消息时上传完毕
        	 		  		mockProgress(text,acunt,a);
        	 		  	}
        	 		  	//数据后50%用模拟进度
        	 		  	function mockProgress(a,b,c){
        					loop = setInterval(function() {
        						pecent+=5;
        						$("#imgprogress"+c+b).html("上传中"+pecent + "%");
        						if (pecent >= 95) {
        							pecent = 50;
        							$("#imgprogress"+c+b).html("上传中"+a);
        							if(a=="100%"){
        								ReT = setTimeout(function(){
        									$("#imgprogress"+c+b).remove();
        	                        	},1000)
        	                    	}
        							clearInterval(loop);
        						}
        					}, 100)
        				}
        	 	    },
        	 	    error:function(XMLHttpRequest, textStatus, errorThrown){ //上传失败 
        	 	            $("#imgprogress"+a+acunt).html("上传失败");
        	 		    }
        	 	    });
		    
     	    /*if(imgArr.length>0){
     	    	var x=false;
     	    	for(var j=0;j<imgArr.length;j++){
     		    	 if(imgArr[j]==imgName){
     		    		toastr.remove();
     		    		toastr.error('此张图片已上传<br />请重新选择!');
     		    		x=true;
     		    		break;
     		 	    }
     		    }
     	    	if(!x){
     	    		imgArr.push(imgName);
     	    	}else{
     	    		return;
     	    	}
     	    }else {
     	    	imgArr.push(imgName);
     	    }*/
    	}else {
        	toastr.remove();
    		toastr.error('上传的图片格式不正确<br />请重新选择!');
    		return;
        }
    	
    }
    function readFiles(){
    	for(var i=0;i<this.files.length;i++){
	        if (input['value'].match(/.jpg|.gif|.png|.bmp/i)){
	        	
	        	var imgName = this.files[0].name;
	        	var fileSize = 0;
	    		var isIE = /msie/i.test(navigator.userAgent) && !window.opera;            
    		    if (isIE && !this.files) {          
    		         var filePath = this.value;            
    		         var fileSystem = new ActiveXObject("Scripting.FileSystemObject");   
    		         var file = fileSystem.GetFile (filePath);               
    		         fileSize = file.Size;         
    		    }else {  
    		         fileSize = this.files[0].size;     
    		    } 
    		    fileSize=Math.round(fileSize/1024*100)/100; //单位为KB
    		    if(fileSize>=2048){
    		    	toastr.remove();
 		    		toastr.error("单个附件已超出2M，请重新上传!");
    		        return;
    		    }
	     	    if(imgArr.length>0){
	     	    	var x=false;
	     	    	for(var j=0;j<imgArr.length;j++){
	     		    	 if(imgArr[j]==imgName){
	     		    		toastr.remove();
	     		    		toastr.error('此张图片已上传<br />请重新选择!');
	     		    		x=true;
	     		    		break;
	     		 	    }
	     		    }
	     	    	if(!x){
	     	    		imgArr.push(imgName);
	     	    	}else{
	     	    		return;
	     	    	}
	     	    }else {
	     	    	imgArr.push(imgName);
	     	    }
	             var reader = new FileReader();
	             reader.readAsDataURL(this.files[i]);
	             reader.onload = function(e){
     	        	result = '<a href="javascript:;" class="deletImg deletImg'+a+'"></a><img id="img'+a+acunt+'" src="'+this.result+'" imgId="" alt=""/><span class="hidden"><img src="'+this.result+'"/></span><div id="imgprogress'+a+acunt+'" class="progressText" style="display:block;">等待上传</div>';
     	        	div = document.createElement('div');
     	        	div.className="dis_in_b imgbox";
     	        	div.innerHTML = result;
     	        	$("#"+a+"before").before(div);
     	        	/*var imgboxLength = $("#"+b+" .imgbox").length;
     	        	if(imgboxLength>=1){
     	        		$("#"+b+" .filetypeTip")
     	        	}else {
     	        		$("#"+b+" .filetypeTip")
     	        	}*/
     	        	/*进度显示*/
     	        	imgprogressTxt(a+acunt);
     	        	/*高度*/
	    			/*spanHeight();*/
	    			 /*删除图片*/
 		    		imgDelete(a,d);
     	        	/*查看图片大图*/
     	        	bigimgshow("img"+a+acunt);
     	        	var imgSer = $("#img"+a+acunt);
     	        	var submitData={
     					image:this.result,
     					name:imgName,
     					fileLength:this.result.length
     				}; 
     	        	/*上传*/
     	        	$.ajax({
 	        	      type: "GET",
 	        	      url:"/virtual/system/counter1/getpolicy",//"/uploadFile",
 	        	      /*data: submitData,*/
 	        	      dataType:"json",
 	        	      beforeSend: function(XMLHttpRequest){
 	        	    	 //demo_report(submitData.name, submitData.image, submitData.fileLength * 0.6,count);
 	        	    },
 	        	      success: function(data){
 	        	    	  console.log(data);
 	        	    	var keyNameArr = submitData.name.split(".");
 	        	    	var keyName = "";
 	        	    	var time = new Date();
 	        	    	keyNameArr[0] += time.getTime();
 	        	    	keyName = keyNameArr.join(".");
 	        	    	console.log(keyName);
 	        	    	var new_multipart_params = {
 	        	   	         'key' : keyName,
 	        	   	         'policy': data.policy,
 	        	   	         'OSSAccessKeyId': data.accessid, 
 	        	   	         'success_action_status' : '200', //让服务端返回200,不然，默认会返回204
 	        	   	         /*'callback' : callbackbody,*/
 	        	   	         'signature': data.signature,
 	        	   	     };
 	        	    	$.ajax({  
 	        	           url: data.host,  
 	        	           dataType: 'json',  
 	        	           type: 'post', 
 	        	           data:new_multipart_params,
 	        	           beforeSend: function (xhr) {  
 	        	               xhr.setRequestHeader("Test", "testheadervalue");  
 	        	           },  
 	        	           cache: false,  
 	        	           //contentType: 'application/x-www-form-urlencoded',  
 	        	           success: function (sResponse) {  
 	        	           }  
 	        	       });  
 	        	    	/*$.post(data.host,new_multipart_params);*/
 	        	    	
 	        	    	 /*图片*/
 	        	    	 var imageAttachment = $("#"+d).val();
 	        		    	imgSer.attr("imgId",data["fileId"]);
 	        			 if(imageAttachment!=""){
 	        				 imageAttachment+=","+data["fileId"];
 	        				$("#"+d).val(imageAttachment);
 	        			 }else{
 	        				$("#"+d).val(data["fileId"]);
 	        			 }
 	        			 
 	        			 /*删除图片*/
 	        			imgDelete(a,d);
 	        	         
 	        	      }, 
 	        	 	  complete :function(XMLHttpRequest, textStatus){

 	        	 		  	if (XMLHttpRequest.readyState === 4 && XMLHttpRequest.status === 200) {
 	        	 		  		var text = "100%";
 	        	 		  		//当收到该消息时上传完毕
 	        	 		  		mockProgress(text,acunt,a);
 	        	 		  	}
 	        	 		  	//数据后50%用模拟进度
 	        	 		  	function mockProgress(a,b,c){
 	        					/*if(loop){
 	        						$("#imgprogress"+b).remove();
 	        					};*/
 	        					loop = setInterval(function() {
 	        						pecent+=5;
 	        						$("#imgprogress"+c+b).html("上传中"+pecent + "%");
 	        						if (pecent >= 95) {
 	        							pecent = 50;
 	        							$("#imgprogress"+c+b).html("上传中"+a);
 	        							if(a=="100%"){
 	        								ReT = setTimeout(function(){
 	        									$("#imgprogress"+c+b).remove();
 	        	                        	},1000)
 	        	                    	}
 	        							clearInterval(loop);
 	        						}
 	        					}, 100)
 	        				}
 	        	 	    },
 	        	 	    error:function(XMLHttpRequest, textStatus, errorThrown){ //上传失败 
 	        	 	            $("#imgprogress"+a+acunt).html("上传失败");
 	        	 		    }
 	        	 	    });
     	        	$("#"+a).parent().hide();
     			}
	        }else {
	        	toastr.remove();
	    		toastr.error('上传的图片格式不正确<br />请重新选择!');
	    		return;
	        }
	        
    	}
    	acunt++;
    }
}
function ifNotImg(elem){
	$(elem).attr("src",contextPath+"/static/pc/img/forIE.gif");
	elem.onerror = null;
}