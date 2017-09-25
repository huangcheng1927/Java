
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.xxxx.PolicyUtil;

@Controller
@RequestMapping("/xxx/xxx")
public class PolicyController{

	/**
	 * 获取凭证
	 * @param request
	 * @param fname
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("/getpolicy")
	public void getPolicy(HttpServletRequest request,HttpServletResponse response) throws Exception{
        	Map<String,String> respMap = PolicyUtil.getPolicy();
            JSONObject json = JSONObject.fromObject(respMap);
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST");
            response(request, response, json.toString());
	 }
	
	/**
	 * 
	 * 删除云端文件
	 * @param request
	 * @param fname 文件名
	 * @return
	 */
	@RequestMapping("/delByKey")
	@ResponseBody
	public Object delete(HttpServletRequest request, @RequestParam(value="fname",required=true)String fname){
		PolicyUtil.deleteOssByKey(fname);
		return "ok";
	}
	
	/**
	 * 
	 * @param request
	 * @param response
	 * @param results
	 * @throws IOException
	 */
	private void response(HttpServletRequest request, HttpServletResponse response, String results) throws IOException {
		String callbackFunName = request.getParameter("callback");
		if (callbackFunName==null || callbackFunName.equalsIgnoreCase(""))
			response.getWriter().println(results);
		else
			response.getWriter().println(callbackFunName + "( "+results+" )");
		response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();
	}
	
}
